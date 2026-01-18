package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;

public interface DishService {

    void addDish(DishDTO dishDTO);

    PageResult pageDish(DishPageQueryDTO dishPageQueryDTO);

    void deleteDish(Long[] ids);

    DishDTO getById(Long id);

    void updateDish(DishDTO dishDTO);

    void updateDishStatus(Integer status, Long id);
}
