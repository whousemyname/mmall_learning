package com.gogotao.controller.backend;


import com.github.pagehelper.PageInfo;
import com.gogotao.common.Const;
import com.gogotao.common.ResponseCode;
import com.gogotao.common.ServerResponse;
import com.gogotao.pojo.Product;
import com.gogotao.pojo.User;
import com.gogotao.service.IFileService;
import com.gogotao.service.IProductService;
import com.gogotao.service.impl.FileServiceImpl;
import com.gogotao.utils.PropertiesUtil;
import com.gogotao.vo.ProductDetailVo;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.apache.bcel.generic.FieldOrMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/manage/product/")
public class ProductManageController {

    @Autowired
    private IFileService iFileService;
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
    public ServerResponse<PageInfo> getList(HttpSession session,
                                            @RequestParam(value = "pageNum", defaultValue = "1")Integer pageNum,
                                            @RequestParam(value = "pageSize", defaultValue = "10")Integer pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCode(ResponseCode.NEED_LOGIN.getCode(), "用户没有登录");
        }
        //校验是否是管理员
        if (user.getRole() != Const.Role.ROLE_ADMIN){
            return ServerResponse.createByErrorMessage("权限不足");
        }
        return iProductService.manageGetProductList(pageNum, pageSize);
    }

    @RequestMapping(value = "search.do")
    @ResponseBody
    public ServerResponse<PageInfo> searchProduct(HttpSession session,
                                                  @RequestParam(value = "productId", required = false)Integer productId,
                                                  @RequestParam(value = "productName", required = false) String productName,
                                                  @RequestParam(value = "pageNum", defaultValue = "1")Integer pageNum,
                                                  @RequestParam(value = "pageSize", defaultValue = "10")Integer pageSize){
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
    public ServerResponse<Map<String, String>> upload(HttpSession session, @RequestParam(value = "uploadFile", required = false)MultipartFile file, HttpServletRequest request){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCode(ResponseCode.NEED_LOGIN.getCode(), "用户没有登录");
        }
        //校验是否是管理员
        if (user.getRole() != Const.Role.ROLE_ADMIN){
            return ServerResponse.createByErrorMessage("权限不足");
        }
        String path = request.getSession().getServletContext().getRealPath("/upload");
        String targetName = iFileService.upload(file, path);
        if (StringUtils.isNotBlank(targetName)){
            return ServerResponse.createByErrorMessage("上传文件失败");
        }
        String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetName;
        Map<String, String> fileMap = new HashMap<>();
        fileMap.put("uri", targetName);
        fileMap.put("url", url);
        return ServerResponse.createBySuccessData(fileMap);
    }

    @RequestMapping(value = "richtext_img_upload.do")
    @ResponseBody
    public Map richtextImgUpload(HttpSession session, @RequestParam(value = "uploadFile", required = false)MultipartFile file, HttpServletRequest request){
        Map resultMap = new HashMap();
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            resultMap.put("success", false);
            resultMap.put("msg", "请登录管理员");
            return resultMap;
        }
        //校验是否是管理员
        if (user.getRole() != Const.Role.ROLE_ADMIN){
            resultMap.put("success", false);
            resultMap.put("msg", "没有管理员权限");
            return resultMap;
        }

        String path = request.getSession().getServletContext().getRealPath("/upload");
        String targetName = iFileService.upload(file, path);
        if (StringUtils.isBlank(targetName)){
            resultMap.put("success", false);
            resultMap.put("msg", "上传文件失败");
            return resultMap;
        }
        String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetName;
        resultMap.put("success", true);
        resultMap.put("msg", "上传成功");
        resultMap.put("file_path", url);
        return resultMap;
    }
}
