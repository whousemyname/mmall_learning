package com.gogotao.controller.portal;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.gogotao.common.ServerResponse;
import com.gogotao.service.IProductService;
import com.gogotao.vo.ProductDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/product/")
public class ProductController {
    @Autowired
    private IProductService iProductService;

    @RequestMapping(value = "detail.do")
    @ResponseBody
    public ServerResponse<ProductDetailVo> getProductDetail(@RequestParam(value = "productId")Integer productId){
        return iProductService.getProductDetail(productId);
    }

    @RequestMapping(value = "list.do")
    @ResponseBody
    public ServerResponse<PageInfo> getProductList(@RequestParam(value = "productName", required = false)String productName,
                                                   @RequestParam(value = "categoryId", required = false)Integer categoryId,
                                                   @RequestParam(value = "pageNum", defaultValue = "1")Integer pageNum,
                                                   @RequestParam(value = "pageSize", defaultValue = "10")Integer pageSize,
                                                   @RequestParam(value = "orderBy", defaultValue = "")String orderBy){
        return iProductService.getProductList(productName, categoryId, pageNum, pageSize, orderBy);
    }
}
