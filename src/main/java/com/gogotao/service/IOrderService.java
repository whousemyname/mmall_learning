package com.gogotao.service;


import com.gogotao.common.ServerResponse;

import java.util.Map;

public interface IOrderService {

    ServerResponse<Map<String, String>> pay(Integer userId, Long orderNo, String path);
    ServerResponse alipayCallback(Map<String, String> params);
    ServerResponse<Boolean> queryPaied(Integer  userId, Long orderId);
    ServerResponse create(Integer userId, Integer shippingId);
    ServerResponse cancle(Integer userId, Long orderId);
    ServerResponse getOrderProduct(Integer userId);
    ServerResponse detail(Integer userId, Long orderNo);
    ServerResponse list(Integer userId, Integer pageNum, Integer pageSize);
    ServerResponse manageDetail(Long orderNo);
    ServerResponse manageSearch(Long orderNo, Integer pageNum, Integer pageSize);
    ServerResponse manageSendGoods(Long orderNo);
}
