package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SetmealDishMapper {
    int insert(SetmealDish setmealDish);
    // 你也可以根据需要添加批量插入/删除方法，例如：
    // int insertBatch(@Param("list") List<SetmealDish> list);
}
