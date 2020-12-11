package com.qlhl.service.impl;

import com.qlhl.bean.User;
import com.qlhl.mapper.UserMapper;
import com.qlhl.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author: ChenTong
 * @create 2020/12/9 14:21
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    public User findUser(String name) {
        return userMapper.getUserByUserName(name);
    }
}
