package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import java.util.List;

public interface DishService {

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);

    void addDish(DishDTO dishDTO);

    PageResult pageDish(DishPageQueryDTO dishPageQueryDTO);

    void updateDishStatus(Integer status, Long id);

    void deleteDish(Long[] ids);

    DishDTO getById(Long id);

    void updateDish(DishDTO dishDTO);
}
