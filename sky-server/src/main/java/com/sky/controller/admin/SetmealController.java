package com.sky.controller.admin;

import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @GetMapping("/page")
    public Result<PageResult> page(@RequestParam Integer page, @RequestParam Integer size) {
        log.info("套餐分页查询：page={},size={}", page, size);
        return Result.success(setmealService.pageQuery(page, size));
    }

    @GetMapping("/list")
    public Result<List<Setmeal>> list(@RequestParam Integer type) {
        log.info("根据分类id查询套餐：type={}", type);
        List<Setmeal> list = setmealService.list(type);
        return Result.success(list);
    }
}
