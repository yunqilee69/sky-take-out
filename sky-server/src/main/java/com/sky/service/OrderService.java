package com.sky.service;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {

    /**
     * 用户下单
     * @param submitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO submitDTO);

    /**
     * 分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult page(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 获取订单详情
     * @param id
     * @return
     */
    OrderVO getWithDetailById(Long id);

    /**
     * 取消订单
     * @param id
     */
    void userCancelOrder(Long id);

    /**
     * 再来一单
     * @param id
     */
    void repetition(Long id);
}
