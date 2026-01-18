package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Override
    public void addDish(DishDTO dishDTO) {
        Dish  dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);
        List<DishFlavor> flavors = dishDTO.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dish.getId());
            dishFlavorMapper.insert(flavor);
        }
    }

    @Override
    public PageResult pageDish(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> dishes = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(dishes.getTotal(), dishes.getResult());
    }

    @Override
    public void deleteDish(Long[] ids) {
        dishMapper.deleteBatch(ids);
        dishFlavorMapper.deleteBatchByDishIds(ids);
    }

    @Override
    public DishDTO getById(Long id) {
        Dish dish = dishMapper.getById(id);
        DishDTO dishDTO = new DishDTO();
        BeanUtils.copyProperties(dish,dishDTO);
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);
        dishDTO.setFlavors(flavors);
        return dishDTO;
    }

    @Override
    public void updateDish(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishFlavorMapper.deleteBatchByDishIds(new Long[]{dish.getId()});
        for(DishFlavor flavor : dishDTO.getFlavors()){
            flavor.setDishId(dish.getId());
            dishFlavorMapper.insert(flavor);
        }
        dishMapper.update(dish);
    }

    @Override
    public void updateDishStatus(Integer status, Long id) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);
        dishMapper.update(dish);
    }
}
