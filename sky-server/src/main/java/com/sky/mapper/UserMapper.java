package com.sky.mapper;

import com.sky.entity.Category;
import com.sky.entity.ShoppingCart;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    public User getByOpenid(String openid);

    void insert(User user);

    List<Category> getCategoryList();

    List<ShoppingCart> getShoppingCartList();
}
