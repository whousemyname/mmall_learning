package com.gogotao.service;

import com.github.pagehelper.PageInfo;
import com.gogotao.common.ServerResponse;
import com.gogotao.pojo.Shipping;

import java.util.Map;

public interface IShippingService {

    ServerResponse<Map<String, Integer>> add(Integer userId, Shipping shipping);

    ServerResponse delete(Integer userId, Integer shippingId);

    ServerResponse update(Integer userId, Shipping shipping);

    ServerResponse<Shipping> select(Integer userId, Integer shippingId);

    ServerResponse<PageInfo> list(Integer userId, Integer pageNum, Integer pageSize);
}
