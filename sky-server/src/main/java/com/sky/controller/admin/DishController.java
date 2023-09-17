package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result<String> save(@RequestBody DishDTO dishDTO) {
        dishService.saveWithFlavor(dishDTO);

        // 清理缓存数据
        String key = "dish_" + dishDTO.getCategoryId();
        cleanCache(key);
        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询：{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 批量删除菜品
     * @param dishIds
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result delete(@RequestParam List<Long> ids){
        log.info("dishIds: {}", ids);
        dishService.deleteBatch(ids);

        // 清理所有的菜品缓存
        cleanCache("dish_");
        return Result.success();
    }

    /**
     * 菜品起售、停售
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售、停售")
    public Result changeDishStatus(@PathVariable Integer status, @RequestParam Long id){
        dishService.changeDishStatus(status, id);

        // 清理所有的菜品缓存
        cleanCache("dish_");
        return Result.success();
    }

    /**
     * 根据菜品id获取菜品信息
     * @param id
     * @return
     */
    @GetMapping("{id}")
    @ApiOperation("根据菜品id获取菜品信息")
    public Result<DishVO> getDishWithFlavorById(@PathVariable Long id) {
        DishVO dishVO = dishService.getDishWithFalvorById(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result updateDish(@RequestBody DishDTO dishDTO) {
        dishService.updateDishWithFlavor(dishDTO);

        // 清理所有的菜品缓存
        cleanCache("dish_");
        return Result.success();
    }

    /**
     * 根据分类id查询所有的菜品
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询所有的菜品")
    public Result<List<Dish>> getListByCategoryId(@RequestParam Long categoryId) {
        List<Dish> dishList = dishService.getListByCategoryId(categoryId);
        return Result.success(dishList);
    }

    /**
     * 清理缓存数据
     * @param pattern
     */
    private void cleanCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
