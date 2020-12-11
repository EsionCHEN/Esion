package com.qlhl.bean;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: ChenTong
 * @create 2020/12/9 10:05
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
public class User  {
    //用户标识
    private  Integer id;
    //用户名
    private String username;
    //用户密码
    private String password;
    //姓名
    private String name;
    //联系方式
    private String tel;
    //授权区域
    private String role_area;
    //店铺编号
    private Integer shop_id;
    //加盐
    private String salt;
    //角色编号
    private Integer roleId;
    //是否激活
    private Integer locked;
}
