package com.gogotao.service;

import com.gogotao.common.Const;
import com.gogotao.common.ServerResponse;
import com.gogotao.common.TokenCache;
import com.gogotao.dao.UserMapper;
import com.gogotao.pojo.User;
import com.gogotao.utils.MD5Util;
import com.sun.deploy.security.CertificateHostnameVerifier;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.annotation.WebServlet;
import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService{
    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int checkUsername = userMapper.checkUsername(username);
        if (checkUsername == 0){
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String MD5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectUser(username, MD5Password);
        if (user == null){
            return ServerResponse.createByErrorMessage("密码错误");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccessData("登陆成功", user);
    }

    @Override
    public ServerResponse<String> register(User user) {
        ServerResponse<String> serverResponse = checkValid(user.getUsername(), Const.USERNAME);
        if (!serverResponse.isSuccess()){
            return serverResponse;
        }
        serverResponse = checkValid(user.getEmail(), Const.EMAIL);
        if (!serverResponse.isSuccess()){
            return serverResponse;
        }
        user.setRole(Const.Role.ROLE_CUSTOMER);
        //用户名与邮箱校验通过
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int sqlResult= userMapper.insert(user);
        if (sqlResult == 0){
            return ServerResponse.createByErrorMessage("sql执行失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    @Override
    public ServerResponse<String> checkValid(String str, String type) {
        if (!StringUtils.isNotBlank(type)){
            return ServerResponse.createByErrorMessage("type为空");
        }
        if (type.equals(Const.USERNAME)){   //类型是username
            int count = userMapper.checkUsername(str);
            if (count > 0){
                return ServerResponse.createByErrorMessage("用户名已经存在");
            }
        }
        if (type.equals(Const.EMAIL)){
            int count = userMapper.checkEmail(str);
            if (count > 0){
                return ServerResponse.createByErrorMessage("邮箱已经存在");
            }
        }
        return ServerResponse.createBySuccessMessage("检测通过");
    }

    public ServerResponse<String> selectQuestion(String username){
        ServerResponse<String> validResponse = checkValid(username, Const.USERNAME);
        if (!validResponse.isSuccess()){
            return validResponse;
        }
        String question = userMapper.selectQuestion(username);
        if (question.isEmpty() || question == null){
            return ServerResponse.createByErrorMessage("该用户没有设置问题,查找问题失败");
        }
        return ServerResponse.createBySuccessData(question);
    }

    public ServerResponse<String> checkAnswer(String username, String question, String answer){
        int count = userMapper.checkAnswer(username, question, answer);
        if (count == 0){
            return ServerResponse.createByErrorMessage("答案错误");
        }
        String forgetToken = UUID.randomUUID().toString();
        TokenCache.setKey("token_" + username, forgetToken);
        return ServerResponse.createBySuccessData(forgetToken);
    }
}
