package com.qlhl.controller;

import com.qlhl.bean.User;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @Author: ChenTong
 * @create 2020/12/9 10:02
 */

@Controller
public class loginController {

    @RequestMapping(value = "/login", method = {RequestMethod.GET})
    public String login(){
        return "/login";
    }

    @RequestMapping(value = "/login", method = {RequestMethod.POST})
    public String login(User u){
        //获取认证组件subject
        Subject subject = SecurityUtils.getSubject();
        //将登陆表单封装为token对象
        UsernamePasswordToken token = new UsernamePasswordToken(u.getUsername(),u.getPassword());
        try {
            //shiro进行登陆验证
            subject.login(token);
            return "/index";
        }catch (Exception e){
            e.printStackTrace();
            return "/login";
        }
    }


}
