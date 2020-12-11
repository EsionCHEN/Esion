package com.qlhl.mapper;

import com.qlhl.bean.User;
import org.apache.ibatis.annotations.Select;

/**
 * @Author: ChenTong
 * @create 2020/12/9 10:35
 */
public interface UserMapper {

    //根据用户名查询
//    @Select("select * from sys_user where username = #{username}")
    User getUserByUserName(String username);
}
