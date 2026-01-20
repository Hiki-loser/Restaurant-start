package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    @Scheduled(cron = "0 * * * * ?") //每分钟触发一次
    public void processTimeoutOrder() {
        LocalDateTime now = LocalDateTime.now();
        log.info("定时处理超时订单：{}", now);

        //处理15分钟前创建的未付款订单
        LocalDateTime time = now.minusMinutes(15);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, time);

        //修改订单状态、取消时间、取消原因并批量更新
        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelTime(now);
                orders.setCancelReason("订单超时，自动取消");
            }
            orderMapper.updateBatch(ordersList);
        }
    }

    @Scheduled(cron = "0 0 1 * * ?") //每天凌晨1点触发一次
    public void processDeliveryOrder() {
        LocalDateTime now = LocalDateTime.now();
        log.info("定时处理处于派送中的订单：{}", now);

        //处理前一天创建的处于派送中的订单
        LocalDateTime time = now.minusHours(1);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);

        //修改订单状态并批量更新
        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
            }
            orderMapper.updateBatch(ordersList);
        }
    }
}
