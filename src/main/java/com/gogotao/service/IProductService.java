package com.gogotao.service;

import com.github.pagehelper.PageInfo;
import com.gogotao.common.ServerResponse;
import com.gogotao.pojo.Product;
import com.gogotao.vo.ProductDetailVo;
import org.springframework.stereotype.Service;

@Service
public interface IProductService {

    ServerResponse saveOrUpdateProduct(Product product);

    ServerResponse setSaleStatus(Integer productId, Integer status);

    ServerResponse<ProductDetailVo>manageGetDetail(Integer productId);
    ServerResponse<ProductDetailVo> getProductDetail(Integer productId);

    ServerResponse<PageInfo> manageGetProductList(Integer pageNum, Integer pageSize);
    ServerResponse<PageInfo> getProductList(String productName, Integer categoryId, Integer pageNum, Integer pageSize, String orderBy);

    ServerResponse<PageInfo> searchProduct(Integer productId, String productName, Integer pageNum, Integer pageSize);
}
