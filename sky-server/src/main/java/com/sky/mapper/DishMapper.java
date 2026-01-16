package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishMapper {

    void update();

    Page<Dish> pageQuery(DishPageQueryDTO dishPageQueryDTO);

}
