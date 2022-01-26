package com.micall.utils;

import com.micall.constant.Constants;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCUtils {
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
}
