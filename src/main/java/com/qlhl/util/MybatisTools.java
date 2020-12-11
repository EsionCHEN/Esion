package com.qlhl.util;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Author: ChenTong
 * @create 2020/12/9 12:38
 */
public class MybatisTools {

    //静态配置Mybatis SQLsessionFactory
    private  static SqlSessionFactory sqlSessionFactory;


    static {
        try {
            String resource = "mybatis/mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    } /*获取sqlsession实体*/

    public static SqlSession getSqlsession() {
        return sqlSessionFactory.openSession(true);
    }

}
