package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询对应的套餐id
     * @param id
     * @return
     */
    List<Long> getSetmealIdsByDishId(List<Long> dishIds);

    /**
     * 批量插入套餐包含的菜品
     * @param setmealDishList
     */
    void saveBatch(List<SetmealDish> setmealDishList);

    /**
     * 根据套餐id获取套餐对应的菜品
     * @param id
     * @return
     */
    @Select(" select * from setmeal_dish where setmeal_id = #{setmealId} ")
    List<SetmealDish> getBySetmealId(Long setmealId);
}
