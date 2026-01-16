package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.service.EmployeeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
@Api(value = "分类管理")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @ApiOperation("新增分类")
    @PostMapping
    public Result<T> addCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("新增分类");
        categoryService.addCategory(categoryDTO);
        return Result.success();
    }

    @ApiOperation("修改分类")
    @PutMapping
    public Result updateCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("修改分类");
        categoryService.updateCategory(categoryDTO);
        return Result.success();
    }

    @ApiOperation("删除分类")
    @DeleteMapping
    public Result deleteCategory(@RequestParam Long id) {
        log.info("删除分类");
        categoryService.deleteCategory(id);
        return Result.success();
    }

    @ApiOperation("分页查询分类")
    @GetMapping("/page")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO) {
        log.info("分页查询分类");
        PageResult pageResult = categoryService.pageQuery(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    @ApiOperation("启用禁用分类")
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, @RequestParam Long id) {
        log.info("启用禁用分类");
        categoryService.startOrStop(status, id);
        return Result.success();
    }


    /**
     *  todo 完善根据类型查询分类
     * @param type
     * @return
     */
    @ApiOperation("根据类型查询分类")
    @GetMapping("/list")
    public Result<List<Category>> list(@RequestParam Integer type) {
        log.info("根据类型查询分类");
        List<Category> list = categoryService.list(type);
        return Result.success(list);
    }

}
