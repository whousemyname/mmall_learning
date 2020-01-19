package com.gogotao.dao;

import com.gogotao.pojo.Shipping;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShippingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shipping record);

    int insertSelective(Shipping record);

    Shipping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shipping record);

    int updateByPrimaryKey(Shipping record);

    int deleteByUserIdShippingId(@Param("userId")Integer userId, @Param("shippingId")Integer shippingId);

    int updateByShipping(Shipping shipping);

    Shipping selectByUserIdShippingId(@Param("userId")Integer userId, @Param("shippingId")Integer shippingId);

    List<Shipping> selectListByUserId(Integer userId);
}