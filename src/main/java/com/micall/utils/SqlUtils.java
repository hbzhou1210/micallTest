package com.micall.utils;

import com.micall.constant.Constants;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.jsoup.helper.StringUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class SqlUtils {
    /**
     * 获取数据库连接
     * @return
     */
    public static Connection getConnection(){
        // 定义数据库连接对象
        Connection conn = null;
        try{
            // 导入数据库驱动包
            // 数据库url
            conn = DriverManager.getConnection(
                    Constants.JDBC_URL,
                    Constants.JDBC_USER,
                    Constants.JDBC_PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }
    /**
     * 关闭数据库连接
     * @param conn
     */
    public static void close(Connection conn){
        try{
            if(conn != null){
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     * 传入sql语句执行sql变量，并且返回查询结果
     * @param sql
     * @return
     */
    public static Object querySingle(String sql){
        Connection conn = null;
        // 如果sql语句为空直接返回，加一个sql语句为空的判断，因为用例里不是所以的用例都需要sql，会存在为空的情况，不判断会报错
        if(StringUtil.isBlank(sql)){
            return null;
        }
        Object result = null;
        try {
            // 创建QueryRunner对象，操作数据库
            QueryRunner runner = new QueryRunner();
            // 调用查询方法，传入数据库连接、sql语句、返回值类型。根据具体查询的类型，返回对应的数据类型
            conn = JDBCUtils.getConnection();
            // ScalarHandler方法，将单个值封装成一个结果，因为只需要查询一条数据
            result = runner.query(conn,sql,new ScalarHandler());
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            JDBCUtils.close(conn);
        }
        return result;
    }
}
