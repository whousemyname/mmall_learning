package com.gogotao.controller.backend;

import com.gogotao.common.Const;
import com.gogotao.common.ServerResponse;
import com.gogotao.pojo.User;
import com.gogotao.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/user/")
public class UserManageController {
    @Autowired
    @Qualifier("iUserService")
    private IUserService iUserService;

    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(HttpSession session, User user){
        ServerResponse<User> serverResponse = iUserService.login(user.getUsername(), user.getPassword());
        if (serverResponse.isSuccess()){
            User loginUser = serverResponse.getData(); //密码已经置空了
            if ((loginUser.getRole() == Const.Role.ROLE_ADMIN)){
                session.setAttribute(Const.CURRENT_USER, loginUser);
            }else{
                return ServerResponse.createByErrorMessage("非管理员用户");
            }
        }
        return serverResponse;
    }
}
