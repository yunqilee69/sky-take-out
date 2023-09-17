package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.QiniuOssService;
import com.sky.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private QiniuOssService qiniuOssService;

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Override
    public void save(SetmealDTO setmealDTO) {
        // 保存套餐信息，并获取套餐id
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);

        // 为套餐包含的菜品设置套餐id，并保存
        List<SetmealDish> setmealDishList = setmealDTO.getSetmealDishes();
        setmealDishList.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmeal.getId());
        });
        setmealDishMapper.insertBatch(setmealDishList);
    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<Setmeal> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page);
    }

    /**
     * 根据id获取套餐
     * @param id
     * @return
     */
    @Override
    public SetmealDTO getByIdWithDish(Long id) {
        // 获取套餐信息
        Setmeal setmeal = setmealMapper.getById(id);

        // 获取套餐包含的菜品
        List<SetmealDish> setmealDishList = setmealDishMapper.getBySetmealId(id);

        SetmealDTO setmealDTO = new SetmealDTO();
        BeanUtils.copyProperties(setmeal, setmealDTO);
        setmealDTO.setSetmealDishes(setmealDishList);
        return setmealDTO;
    }

    /**
     * 修改套餐
     * @param setmealDTO
     */
    @Override
    public void updateSetmeal(SetmealDTO setmealDTO) {
        // 更新套餐基本信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        setmealMapper.update(setmeal);

        // 更新套餐包含的菜品信息，先删除之前的数据，在新增数据
        setmealDishMapper.deleteBatch(setmealDTO.getId());

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealDTO.getId());
        });
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 套餐启售、停售
     * @param setmeal
     */
    @Override
    public void updateSetmealStatus(Setmeal setmeal) {
        setmealMapper.update(setmeal);
    }

    /**
     * 批量删除套餐
     * @param ids
     * @return
     */
    @Override
    public void deleteSetmeals(List<Long> ids) {
        ids.forEach(id -> {
            // 获取套餐的图片并进行删除，包含的菜品图片不删除
            Setmeal setmeal = setmealMapper.getById(id);
            qiniuOssService.deleteImage(setmeal.getImage());

            // 删除套餐信息
            setmealMapper.delete(id);

            // 删除套餐包含的菜品信息
            setmealDishMapper.deleteBatch(id);

        });

    }
}
