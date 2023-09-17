package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

/**
 * @author liyun
 */
public interface DishService {



    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    public void saveWithFlavor(DishDTO dishDTO);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 批量删除菜品
     * @param dishIds
     */
    void deleteBatch(List<Long> dishIds);

    /**
     * 修改菜品售卖状态
     * @param status
     * @param id
     */
    void changeDishStatus(Integer status, Long id);

    /**
     * 修改菜品数据
     * @param dishDTO
     */
    void updateDishWithFlavor(DishDTO dishDTO);

    /**
     * 根据菜品id获取菜品信息及其口味数据
     *
     * @param id
     * @return
     */
    DishVO getDishWithFalvorById(Long id);

    /**
     * 根据分类id获取该分类下的所有菜品
     * @param categoryId
     * @return
     */
    List<Dish> getListByCategoryId(Long categoryId);

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);
}
