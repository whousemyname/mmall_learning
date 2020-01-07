package com.gogotao.controller.backend;


import com.github.pagehelper.PageInfo;
import com.gogotao.common.Const;
import com.gogotao.common.ResponseCode;
import com.gogotao.common.ServerResponse;
import com.gogotao.pojo.Product;
import com.gogotao.pojo.User;
import com.gogotao.service.IProductService;
import com.gogotao.vo.ProductDetailVo;
import com.sun.org.apache.bcel.internal.generic.IUSHR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/product/")
public class ProductManageController {

    @Autowired
    private IProductService iProductService;

    @RequestMapping(value = "save.do")
    @ResponseBody
    public ServerResponse addProduct(HttpSession session, Product product){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCode(ResponseCode.NEED_LOGIN.getCode(), "用户没有登录");
        }
        //校验是否是管理员
        if (user.getRole() != Const.Role.ROLE_ADMIN){
            return ServerResponse.createByErrorMessage("权限不足");
        }
        return iProductService.saveOrUpdateProduct(product);
    }

    @RequestMapping(value = "set_sale_status.do")
    @ResponseBody
    public ServerResponse setSaleStatus(HttpSession session, @RequestParam(value = "productId")Integer productId, @RequestParam(value = "status")Integer status){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCode(ResponseCode.NEED_LOGIN.getCode(), "用户没有登录");
        }
        //校验是否是管理员
        if (user.getRole() != Const.Role.ROLE_ADMIN){
            return ServerResponse.createByErrorMessage("权限不足");
        }
        return iProductService.setSaleStatus(productId, status);
    }

    @RequestMapping(value = "detail.do")
    @ResponseBody
    public ServerResponse<ProductDetailVo> getDetail(HttpSession session, @RequestParam(value = "productId")Integer productId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCode(ResponseCode.NEED_LOGIN.getCode(), "用户没有登录");
        }
        //校验是否是管理员
        if (user.getRole() != Const.Role.ROLE_ADMIN){
            return ServerResponse.createByErrorMessage("权限不足");
        }
        return iProductService.manageGetDetail(productId);
    }

    @RequestMapping(value = "list.do")
    @ResponseBody
    public ServerResponse<PageInfo> getList(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1")Integer pageNum, @RequestParam(value = "pageSize", defaultValue = "10")Integer pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCode(ResponseCode.NEED_LOGIN.getCode(), "用户没有登录");
        }
        //校验是否是管理员
        if (user.getRole() != Const.Role.ROLE_ADMIN){
            return ServerResponse.createByErrorMessage("权限不足");
        }
        return iProductService.getProductList(pageNum, pageSize);
    }

    @RequestMapping(value = "search.do")
    @ResponseBody
    public ServerResponse<PageInfo> searchProduct(HttpSession session, @RequestParam("productId")Integer productId, @RequestParam("productName") String productName, @RequestParam(value = "pageNum", defaultValue = "1")Integer pageNum, @RequestParam(value = "pageSize", defaultValue = "10")Integer pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCode(ResponseCode.NEED_LOGIN.getCode(), "用户没有登录");
        }
        //校验是否是管理员
        if (user.getRole() != Const.Role.ROLE_ADMIN){
            return ServerResponse.createByErrorMessage("权限不足");
        }
        return iProductService.searchProduct(productId, productName, pageNum, pageSize);
    }

    @RequestMapping(value = "upload.do")
    @ResponseBody
    public ServerResponse upload(MultipartFile file, HttpServletRequest request){
        String path = request.getSession().getServletContext().getRealPath("upload");

    }
}
