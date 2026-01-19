package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/shop")
@Slf4j
@Api(tags = "Admin Shop Management")
public class ShopController {

    @Autowired
    ShopService shopService;

    @GetMapping("/status")
    @ApiOperation("Get Shop Status")
    public Result<Integer> getShopStatus() {
        log.info("Fetching shop status");
        return Result.success(shopService.getShopStatus());
    }

    @PutMapping("/{status}")
    @ApiOperation("Update Shop Status")
    public Result<Integer> updateShopStatus(@PathVariable Integer status) {
        log.info("Updating shop status to: {}", status);
        shopService.updateShopStatus(status);
        return Result.success(status);
    }

}
