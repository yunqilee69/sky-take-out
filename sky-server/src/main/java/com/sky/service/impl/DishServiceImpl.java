package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.service.QiniuOssService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private QiniuOssService qiniuOssService;

    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    @Transactional
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        // 向菜品表插入一条数据
        dishMapper.insert(dish);

        // 获取dishId
        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            // 为flavors设置dishId
            flavors.forEach((flavor) -> {
                flavor.setDishId(dishId);
            });

            // 向口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        // 开启分页查询
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除菜品
     * @param dishIds
     */
    @Override
    public void deleteBatch(List<Long> dishIds) {
        // 判断当前菜品能否删除  --- 启售中的商品不能删除
        for (Long id : dishIds) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                // 当前菜品处于启售中，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        // 被套餐关联的菜品不能删除
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishId(dishIds);
        if (setmealIds != null & setmealIds.size() > 0) {
            // 当前菜品被套餐关联了，不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        for (Long dishId : dishIds) {
            // 删除菜品相关的图片,先获取图片的地址，在调用工具类进行删除
            String image = dishMapper.getImageById(dishId);
            qiniuOssService.deleteImage(image);

            // 删除菜品表中的菜品数据
            dishMapper.deleteById(dishId);

            // 删除菜品关联的口味数据
            dishFlavorMapper.deleteByDishId(dishId);
        }

    }

    /**
     * 修改菜品售卖状态
     * @param status
     * @param id
     */
    @Override
    public void changeDishStatus(Integer status, Long id) {
        dishMapper.changeDishStatusById(status, id);
    }

    /**
     * 修改菜品数据
     * @param dishDTO
     */
    @Override
    public void updateDishWithFlavor(DishDTO dishDTO) {
        // 更新菜品信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        dishMapper.update(dish);

        // 更新菜品对应的口味信息
        // 先删除以前的数据，在进行添加当前的口味数据
        Long dishId = dish.getId();
        dishFlavorMapper.deleteByDishId(dishId);
        // 为口味数据设置dishId
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            // 为flavors设置dishId
            flavors.forEach((flavor) -> {
                flavor.setDishId(dishId);
            });

            // 向口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 根据菜品id获取菜品信息及其口味数据
     *
     * @param id
     * @return
     */
    @Override
    public DishVO getDishWithFalvorById(Long id) {
        DishVO dishVO = new DishVO();
        // 根据id获取菜品信息
        Dish dish = dishMapper.getById(id);
        // 根据菜品id获取对应口味信息
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);

        // 组装数据
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }
}
