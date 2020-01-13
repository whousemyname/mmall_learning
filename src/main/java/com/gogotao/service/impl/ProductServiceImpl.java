package com.gogotao.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gogotao.common.Const;
import com.gogotao.common.ResponseCode;
import com.gogotao.common.ServerResponse;
import com.gogotao.dao.CategoryMapper;
import com.gogotao.dao.ProductMapper;
import com.gogotao.pojo.Category;
import com.gogotao.pojo.Product;
import com.gogotao.service.ICategoryService;
import com.gogotao.service.IProductService;
import com.gogotao.utils.DateTimeUtils;
import com.gogotao.utils.PropertiesUtil;
import com.gogotao.vo.ProductDetailVo;
import com.gogotao.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.nio.file.ProviderNotFoundException;
import java.security.cert.CertificateNotYetValidException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@Service("iProductService")
public class ProductServiceImpl implements IProductService {
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    @Qualifier("iCategoryService")
    private ICategoryService iCategoryService;

    @Override
    public ServerResponse saveOrUpdateProduct(Product product) {
        if (product == null){
            return ServerResponse.createByErrorCode(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        if (StringUtils.isNotBlank(product.getSubImages())){
            String[] subImageArray = product.getSubImages().split(",");
            if (subImageArray.length > 0){
                product.setMainImage(subImageArray[0]);
            }
        }
        if (product.getId() != null){ //update更新操作必须带有productId主键
            if (productMapper.updateByPrimaryKey(product) > 0){
                return ServerResponse.createBySuccessMessage("更新产品信息成功");
            }
            return ServerResponse.createByErrorMessage("更新产品信息失败");
        }else{ //insert
            if (productMapper.insert(product) > 0){
                return ServerResponse.createBySuccessMessage("新增产品成功");
            }
            return ServerResponse.createByErrorMessage("新增产品失败");
        }
    }

    @Override
    public ServerResponse setSaleStatus(Integer productId, Integer status){
        if (productId == null || status == null){
            return ServerResponse.createByErrorCode(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product updateProduct = new Product();
        updateProduct.setId(productId);
        updateProduct.setStatus(status);
        if (productMapper.updateByPrimaryKeySelective(updateProduct) > 0){
            return ServerResponse.createBySuccessMessage("更新商品status成功");
        }
        return ServerResponse.createByErrorMessage("更新商品status失败");
    }

    @Override
    public ServerResponse<ProductDetailVo> manageGetDetail(Integer productId) {
        if (productId == null){
            return ServerResponse.createByErrorCode(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null){
            return ServerResponse.createByErrorMessage("商品已下架或者删除");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccessData("获取商品详情成功", productDetailVo);
    }
    private ProductDetailVo assembleProductDetailVo(Product product){
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setCategoryId(product.getCategoryId());

        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if (category.getParentId() == null){
            productDetailVo.setParentCategoryId(0); //根节点
        }else {
            productDetailVo.setParentCategoryId(category.getParentId());
        }

        productDetailVo.setName(product.getName());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setDetail(productDetailVo.getDetail());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setSubtitle(product.getSubtitle());

        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://image.happymmall.com/"));
        productDetailVo.setCreateTime(DateTimeUtils.dateToString(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtils.dateToString(product.getUpdateTime()));
        return productDetailVo;
    }

    @Override
    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId) {
        if (productId == null){
            return ServerResponse.createByErrorCode(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null || product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()){
            return ServerResponse.createByErrorMessage("商品已下架或者删除");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccessData("获取商品详情成功", productDetailVo);
    }

    @Override
    public ServerResponse<PageInfo> manageGetProductList(Integer pageNum, Integer pageSize){
        PageHelper.startPage(pageNum, pageSize);
        List<Product> list = productMapper.selectList();
        List<ProductListVo> listVos = new ArrayList<>(list.size());
        for (Product productItem : list){
            listVos.add(assembleProductListVo(productItem));
        }
        PageInfo pageResult = new PageInfo(list);
        pageResult.setList(listVos);
        return ServerResponse.createBySuccessData("获取商品列表成功", pageResult);
    }
    @Override
    public ServerResponse<PageInfo> getProductList(String productName, Integer categoryId, Integer pageNum, Integer pageSize, String orderBy){
        if (StringUtils.isBlank(productName) && categoryId == null){
            return ServerResponse.createByErrorCode(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        List<Integer> categoryIdList = new ArrayList<>();
        if(categoryId != null){
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if (category == null && StringUtils.isBlank(productName) && categoryId != 0){
                PageHelper.startPage(pageNum, pageSize);
                List<ProductListVo> listVos = new ArrayList<>();
                PageInfo pageInfo = new PageInfo(listVos);
                return ServerResponse.createBySuccessData("返回产品结果集", pageInfo);
            }
            categoryIdList = iCategoryService.selectCategoryAndChildrenById(categoryId).getData();
        }
        if (StringUtils.isNotBlank(productName)){
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.isNotBlank(orderBy)){
            if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){
                String[] strs = orderBy.split("_");
                PageHelper.orderBy(strs[0] + " " + strs[1]);
            }
        }
        List<Product> productList = productMapper.selectByNameAndCategoryIds(StringUtils.isBlank(productName)? null : productName,
                                                                             categoryIdList.size()==0? null : categoryIdList);
        List<ProductListVo> listVos = new ArrayList<>(productList.size());
        for (Product productItem : productList){
            ProductListVo productListVo = assembleProductListVo(productItem);
            listVos.add(productListVo);
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(listVos);
        return ServerResponse.createBySuccessData("成功获取商品列表", pageInfo);
    }
    private ProductListVo assembleProductListVo(Product product){
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setName(product.getName());
        productListVo.setPrice(product.getPrice());
        productListVo.setStatus(product.getStatus());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setMainImage(product.getMainImage());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://image.happymmall.com/"));
        return productListVo;
    }

    @Override
    public ServerResponse<PageInfo> searchProduct(Integer productId, String productName, Integer pageNum, Integer pageSize){
        PageHelper.startPage(pageNum, pageSize);
        if (StringUtils.isNotBlank(productName)){
           productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
        List<Product> list = productMapper.selectByNameAndId(productId, productName);
        List<ProductListVo> listVos = new ArrayList<>(list.size());
        for (Product productItem : list){
            listVos.add(assembleProductListVo(productItem));
        }

        PageInfo pageResult = new PageInfo(list);
        pageResult.setList(listVos);

        return ServerResponse.createBySuccessData("获取商品列表成功", pageResult);
    }
}
