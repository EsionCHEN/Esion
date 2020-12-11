package com.qlhl.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: ChenTong
 * @create 2020/12/9 14:34
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    //角色编号
    private Integer id;
    //角色
    private String role;
    //描述
    private String description;
    //父类
    private Integer parentId;
    //激活状态
    private Integer locked;
}
