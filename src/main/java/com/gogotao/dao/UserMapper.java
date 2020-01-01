package com.gogotao.dao;

import com.gogotao.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    int checkUsername(String username);

    int checkEmail(String email);

    User selectUser(@Param("username") String username, @Param("password") String password);

    String selectQuestion(String username);

    int checkAnswer(@Param("username") String username, @Param("question") String question, @Param("answer") String answer);
}