package com.gogotao.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gogotao.common.ServerResponse;
import com.gogotao.dao.ShippingMapper;
import com.gogotao.pojo.Shipping;
import com.gogotao.service.IShippingService;
import com.sun.xml.internal.ws.server.ServerRtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    @Override
    public ServerResponse<Map<String, Integer>> add(Integer userId, Shipping shipping){
        shipping.setUserId(userId);
        if (shippingMapper.insert(shipping) == 0){
            return ServerResponse.createByErrorMessage("添加地址失败");
        }
        Map result = new HashMap<>();
        result.put("shippingId", shipping.getId());
        return ServerResponse.createBySuccessData("添加地址成功", result);
    }

    @Override
    public ServerResponse delete(Integer userId, Integer shippingId){
        if (shippingMapper.deleteByUserIdShippingId(userId, shippingId) == 0){
            return ServerResponse.createByErrorMessage("删除地址失败");
        }
        return ServerResponse.createBySuccessMessage("删除地址成功");
    }

    @Override
    public ServerResponse update(Integer userId, Shipping shipping){
        shipping.setUserId(userId);
        if (shippingMapper.updateByShipping(shipping) == 0){
            return ServerResponse.createByErrorMessage("更新地址失败");
        }
        return ServerResponse.createBySuccessMessage("更新地址成功");
    }

    @Override
    public ServerResponse<Shipping> select(Integer userId, Integer shippingId){
        Shipping shipping;
        if ((shipping = shippingMapper.selectByUserIdShippingId(userId, shippingId)) == null){
            return ServerResponse.createByErrorMessage("查询地址失败");
        }
        return ServerResponse.createBySuccessData("查询地址成功", shipping);
    }

    @Override
    public ServerResponse<PageInfo> list(Integer userId, Integer pageNum, Integer pageSize){
        PageHelper.startPage(pageNum, pageSize);
        List<Shipping> shippings =  shippingMapper.selectListByUserId(userId);
        PageInfo pageInfo = new PageInfo(shippings);
        return ServerResponse.createBySuccessData(pageInfo);
    }
}
