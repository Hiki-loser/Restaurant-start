package com.sky.controller.user;


import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Api(tags = "C端-购物车接口")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    @ApiOperation("添加购物车")
    @CacheEvict(
            value = "shoppingCartCache",
            key = "'user:' + T(com.sky.context.BaseContext).getCurrentId()"
    )
    public Result<String> add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("add to shoppingCartDTO:{}", shoppingCartDTO);
        shoppingCartService.add(shoppingCartDTO);
        return Result.success("Added to shopping cart successfully");
    }

    @GetMapping("/list")
    @ApiOperation("获取购物车列表")
    @Cacheable(
            value = "shoppingCartCache",
            key = "'user:' + T(com.sky.context.BaseContext).getCurrentId()"
    )
    public Result<List<ShoppingCart>> list(){
        log.info("get shopping cart list");
        List<ShoppingCart>list = shoppingCartService.list();
        return Result.success(list);
    }

    @PostMapping("/sub")
    @ApiOperation("减少购物车商品数量")
    @CacheEvict(
            value = "shoppingCartCache",
            key = "'user:' + T(com.sky.context.BaseContext).getCurrentId()"
    )
    public Result<String> sub(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("sub from shoppingCartDTO:{}", shoppingCartDTO);
        shoppingCartService.sub(shoppingCartDTO);
        return Result.success("Reduced item quantity in shopping cart successfully");
    }

    @DeleteMapping("/clean")
    @ApiOperation("清空购物车")
    @CacheEvict(
            value = "shoppingCartCache",
            key = "'user:' + T(com.sky.context.BaseContext).getCurrentId()"
    )
    public Result<String> clean(){
        log.info("clean shopping cart");
        shoppingCartService.clean();
        return Result.success("Shopping cart cleared successfully");
    }

}
