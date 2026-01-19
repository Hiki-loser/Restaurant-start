package com.sky.service;


import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.vo.UserLoginVO;

public interface UserService {


    UserLoginVO login(UserLoginDTO userLoginDTO) throws Exception;

    User wxLogin(String openid) throws Exception;

    String getOpenid(String code);
}
