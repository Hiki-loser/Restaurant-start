package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DishMapper {

     void deleteBatch(Long[] ids);

    void update(Dish dish);

    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    void insert(Dish dish);

    Integer countByCategoryId(Long categoryId);

    @Select("SELECT * FROM dish WHERE id = #{id}")
    Dish getById(Long id);
}
