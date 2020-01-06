package com.gogotao.controller.backend;


import com.gogotao.common.Const;
import com.gogotao.common.ResponseCode;
import com.gogotao.common.ServerResponse;
import com.gogotao.pojo.Category;
import com.gogotao.pojo.User;
import com.gogotao.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/manage/category/")
public class CategoryManageController {

    @Autowired
    @Qualifier("iCategoryService")
    private ICategoryService iCategoryService;

    @RequestMapping(value = "add_category.do" )
    @ResponseBody
    public ServerResponse addCategory(HttpSession session, String categoryName, @RequestParam(value = "parentId", defaultValue = "0") int parentId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCode(ResponseCode.NEED_LOGIN.getCode(), "用户没有登录");
        }
        //校验是否是管理员
        if (user.getRole() != Const.Role.ROLE_ADMIN){
            return ServerResponse.createByErrorMessage("权限不足");
        }
        //添加分类
        return iCategoryService.addCategory(categoryName, parentId);
    }

    @RequestMapping(value = "set_category_name.do" )
    @ResponseBody
    public ServerResponse setCategoryName(HttpSession session, Integer categoryId, String categoryName){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCode(ResponseCode.NEED_LOGIN.getCode(), "用户没有登录");
        }
        //校验是否是管理员
        if (user.getRole() != Const.Role.ROLE_ADMIN){
            return ServerResponse.createByErrorMessage("权限不足");
        }
        return iCategoryService.setCategoryName(categoryId, categoryName);
    }

    @RequestMapping(value = "get_category.do" )
    @ResponseBody
    public ServerResponse<List<Category>> getChildrenParallelCategory(HttpSession session, @RequestParam(value = "parentId", defaultValue = "0") Integer parentId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCode(ResponseCode.NEED_LOGIN.getCode(), "用户没有登录");
        }
        //校验是否是管理员
        if (user.getRole() != Const.Role.ROLE_ADMIN){
            return ServerResponse.createByErrorMessage("权限不足");
        }
        return iCategoryService.getChildrenParallelCategory(parentId);
    }


    @RequestMapping(value = "get_deep_category.do" )
    @ResponseBody
    public ServerResponse<List<Integer>> getCategoryAndDeepChildrenCategory(HttpSession session, @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCode(ResponseCode.NEED_LOGIN.getCode(), "用户没有登录");
        }
        //校验是否是管理员
        if (user.getRole() != Const.Role.ROLE_ADMIN){
            return ServerResponse.createByErrorMessage("权限不足");
        }
        return iCategoryService.selectCategoryAndChildrenById(categoryId);
    }
}
