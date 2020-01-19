package com.gogotao.dao;

import com.gogotao.pojo.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderItemMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(OrderItem record);

    int insertSelective(OrderItem record);

    OrderItem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OrderItem record);

    int updateByPrimaryKey(OrderItem record);

    List<OrderItem> selectOrderItemListByUserIdOrderNo(@Param("userId") Integer userId, @Param("orderNo") Long orderNo);

    List<OrderItem> selectOrderItemListByOrderNo(@Param("orderNo") Long orderNo);

    void batchInsert(List<OrderItem> orderItemList);
}