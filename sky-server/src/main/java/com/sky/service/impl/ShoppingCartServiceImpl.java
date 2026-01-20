package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


@Slf4j
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    ShoppingCartMapper shoppingCartMapper;

    @Autowired
    DishMapper dishMapper;

    @Autowired
    SetmealMapper setmealMapper;

    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);
        if(shoppingCart.getDishId()!=null){
            log.info("Adding dish to shopping cart: dishId={}, userId={}", shoppingCart.getDishId(), userId);
            if(shoppingCarts!=null && !shoppingCarts.isEmpty()&& Objects.equals(shoppingCarts.get(0).getDishId(), shoppingCart.getDishId())){
                ShoppingCart shoppingCartUpdate = shoppingCarts.get(0);
                shoppingCartUpdate.setNumber(shoppingCartUpdate.getNumber() + 1);
                shoppingCartMapper.update(shoppingCartUpdate);
            }else{
                Dish dish = dishMapper.getById(shoppingCartDTO.getDishId());
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setCreateTime(LocalDateTime.now());
                shoppingCart.setNumber(1);
                shoppingCartMapper.insert(shoppingCart);
            }
        }else{
            log.info("Adding setmeal to shopping cart: setmealId={}, userId={}", shoppingCart.getSetmealId(), userId);
            if(shoppingCarts!=null && !shoppingCarts.isEmpty()&& Objects.equals(shoppingCarts.get(0).getSetmealId(), shoppingCart.getSetmealId())){
                ShoppingCart shoppingCartUpdate = shoppingCarts.get(0);
                shoppingCartUpdate.setNumber(shoppingCartUpdate.getNumber() + 1);
                shoppingCartMapper.update(shoppingCartUpdate);
            }else{
                Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setCreateTime(LocalDateTime.now());
                shoppingCart.setNumber(1);
                shoppingCartMapper.insert(shoppingCart);
            }
        }
    }

    @Override
    public List<ShoppingCart> list() {
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(BaseContext.getCurrentId())
                .build();
        return shoppingCartMapper.list(shoppingCart);
    }

    @Override
    public void clean() {
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());
    }

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);
        if(shoppingCarts!=null && !shoppingCarts.isEmpty()) {
            ShoppingCart shoppingCartUpdate = shoppingCarts.get(0);
            Integer number = shoppingCartUpdate.getNumber();
            if (number > 1) {
                shoppingCartUpdate.setNumber(number - 1);
                shoppingCartMapper.update(shoppingCartUpdate);
            } else {
                shoppingCartMapper.deleteByUserId(userId);
            }
        }
    }
}
