package com.qlhl.service.impl;

import com.qlhl.bean.Role;
import com.qlhl.mapper.RoleMapper;
import com.qlhl.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: ChenTong
 * @create 2020/12/9 14:42
 */

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    RoleMapper roleMapper;


    public Role findRole(int role) {

        return roleMapper.getRoleById(role);
    }
}
