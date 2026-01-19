package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {


    void update(ShoppingCart shoppingCart);

    void insert(ShoppingCart shoppingCart);

    void deleteByUserId(Long currentId);

    List<ShoppingCart> list(ShoppingCart shoppingCart);
}
