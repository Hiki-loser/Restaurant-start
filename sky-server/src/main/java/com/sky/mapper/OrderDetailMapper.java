package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderDetailMapper {

    /**
     * 批量插入订单明细数据
     *
     * @param orderDetailList
     */
    void insertBatch(List<OrderDetail> orderDetailList);

    List<Map<String, Object>> selectByOrderId(Long orderId);
}
