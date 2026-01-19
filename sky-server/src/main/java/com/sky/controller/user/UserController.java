package com.sky.controller.user;


import com.sky.dto.UserLoginDTO;
import com.sky.entity.Category;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@Slf4j
@Api(tags = "User User Management")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/user/login")
    @ApiOperation("User Login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) throws Exception {
        log.info("User login with data: {}", userLoginDTO);
        UserLoginVO userLoginVO = userService.login(userLoginDTO);
        return Result.success(userLoginVO);
    }

    @GetMapping("/shop/status")
    @ApiOperation("Get Shop Status")
    public Result<Integer> getShopStatus() {
        log.info("Fetching shop status");
        Integer status = userService.getShopStatus();
        return Result.success(status);
    }


}
