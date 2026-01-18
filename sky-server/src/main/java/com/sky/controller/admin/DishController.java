package com.sky.controller.admin;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;

import com.sky.entity.Dish;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishMapper dishMapper;

    @PostMapping
    public Result<String> adddish(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品:{}", dishDTO);
        dishService.addDish(dishDTO);
        return Result.success("新增菜品成功");
    }

    @GetMapping("/page")
    public Result<PageResult> pageDish(DishPageQueryDTO dishPageQueryDTO) {
        log.info("分页查询菜品:{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageDish(dishPageQueryDTO);
        return Result.success(pageResult);
    }
    @DeleteMapping
    public Result<String> deleteDish(@RequestParam("ids") Long[] ids) {
        log.info("删除菜品:{}", ids);
        dishService.deleteDish(ids);
        return Result.success("删除菜品成功");
    }

    @GetMapping("/{id}")
    public Result<DishDTO> getDishById(@PathVariable Long id) {
        log.info("根据id查询菜品:{}", id);
        return Result.success(dishService.getById(id));
    }

    @PutMapping
    public Result<String> updateDish(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品:{}", dishDTO);
        dishService.updateDish(dishDTO);
        return Result.success("修改菜品成功");
    }

    @PostMapping("/status/{status}")
    public Result<String> updateDishStatus(@PathVariable Integer status, @RequestParam("id") Long id) {
        log.info("修改菜品状态: {}, {}", status, id);
        dishService.updateDishStatus(status, id);
        return Result.success("修改菜品状态成功");
    }
}
