package com.gogotao.service;

import com.gogotao.common.Const;
import com.gogotao.common.ServerResponse;
import com.gogotao.common.TokenCache;
import com.gogotao.dao.UserMapper;
import com.gogotao.pojo.User;
import com.gogotao.utils.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
        return ServerResponse.createBySuccessMessage("用户名邮箱注册检测通过");
    }

    @Override
    public ServerResponse<String> selectQuestion(String username){
        if (userMapper.checkUsername(username) == 0){
            return ServerResponse.createByErrorMessage("不存在该用户");
        }
        String question = userMapper.selectQuestion(username);
        if (question.isEmpty() || question == null){
            return ServerResponse.createByErrorMessage("该用户没有设置问题,查找问题失败");
        }
        return ServerResponse.createBySuccessData(question);
    }

    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer){
        int count = userMapper.checkAnswer(username, question, answer);
        if (count == 0){
            return ServerResponse.createByErrorMessage("答案错误");
        }
        String forgetToken = UUID.randomUUID().toString();
        TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);
        return ServerResponse.createBySuccessData(forgetToken);
    }

    @Override
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken){
        if (StringUtils.isBlank(passwordNew)){
            return ServerResponse.createByErrorMessage("参数异常:传入的token为空");
        }
        if (userMapper.checkUsername(username) == 0){
            return ServerResponse.createByErrorMessage("不存在该用户");
        }

        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("TokenCache中token不存在或者已经过期");
        }
        if (!StringUtils.equals(forgetToken, token)){
            return ServerResponse.createByErrorMessage("token不相等,token错误");
        }
        String passwordMD5 = MD5Util.MD5EncodeUtf8(passwordNew);
        if (userMapper.updatePasswordByUsername(username, passwordMD5) > 0){
            return ServerResponse.createBySuccessMessage("重置密码成功");
        }
        return ServerResponse.createByErrorMessage("重置密码失败");
    }

    @Override
    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user){
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());
        if (resultCount == 0){
            return ServerResponse.createByErrorMessage("密码不正确");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        resultCount = userMapper.updateByPrimaryKeySelective(user);
        user.setPassword(StringUtils.EMPTY); //重新置空密码, 防止密码泄漏
        if (resultCount > 0){
            return ServerResponse.createBySuccessMessage("重置密码成功");
        }
        return ServerResponse.createByErrorMessage("重置密码失败");
    }

    @Override
    public ServerResponse<User> updateInformation(User user){
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if (resultCount > 0){
            return ServerResponse.createByErrorMessage("邮箱已经被使用了");
        }
        User userNew = new User();
        userNew.setId(user.getId());
        userNew.setUsername(user.getUsername());
        userNew.setPhone(user.getPhone());
        userNew.setEmail(user.getEmail());
        userNew.setQuestion(user.getQuestion());
        userNew.setAnswer(user.getAnswer());
        resultCount = userMapper.updateByPrimaryKeySelective(userNew);
        if (resultCount > 0){
            userNew.setPassword(StringUtils.EMPTY);
            return ServerResponse.createBySuccessData("更新用户信息成功", userNew);
        }
        return ServerResponse.createByErrorMessage("更新用户信息失败");
    }

    @Override
    public ServerResponse<User> getInformation(Integer userId){
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null){
            return ServerResponse.createByErrorMessage("获取用户信息失败");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccessData(user);
    }
}
