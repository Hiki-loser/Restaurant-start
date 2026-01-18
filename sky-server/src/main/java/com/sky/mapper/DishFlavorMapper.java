package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    void deleteBatchByDishIds(Long[] ids);

    void insert(DishFlavor dishFlavor);

    List<DishFlavor> getByDishId(Long id);
}
