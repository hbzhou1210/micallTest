package com.micall.constant;

import java.util.HashMap;
import java.util.Map;

/*
 常量类
 */
public class Constants {
    //常量类的命名规则：所有英文单词都大写用下划线分割
    //final修饰变量，变量成为常量，常量只能赋值一次
    //final修饰类，类不能被继承
    //final修饰方法，不能被重写
    //Excel路径
    public static final String EXCEL_PATH="E:\\workspace\\micallTest\\src\\test\\resources\\micall接口测试.xls";
    //token鉴权版本
    public static final String HEADER_MEDIA_TYPE_NAME="";
    public static final String HEADER_MEDIA_TYPE_VALUE="";
    //实际响应数据回写列
    public static final int ACTUAL_WAITER_BACK_CELL_NUM= 5;
    //判断结果回写列
    public static final int ACTUAL_result_CALL_CELL_NUM = 6;
    //数据库连接相关常量
    public static final String JDBC_URL="jdbc:mysql://rm-bp1y15g06j7139049go.mysql.rds.aliyuncs.com/";
    public static final String JDBC_USER="yirga_rw_d1117";
    public static final String JDBC_PASSWORD="c28BRo#EtRO6hofR";
    //请求头
    public static final String REQ_HEADER_TYPE_content_type="content-type";
    public static final String REQ_HEADER_TYPE_mh_device_name="mh-device-name";
    public static final String REQ_HEADER_TYPE_mh_device_id="mh-device-id";
    public static final String REQ_HEADER_TYPE_mh_device_type="mh-device-type";
    public static final String REQ_HEADER_TYPE_mh_device_describe="mh-device-describe";
    public static final String REQ_HEADER_TYPE_VALUE_FROM_content_type="application/json";
    public static final String REQ_HEADER_TYPE_VALUE_FROM_mh_device_name="iPhone%207%20Plus";
    public static final String REQ_HEADER_TYPE_VALUE_FROM_mh_device_id="E0F2E486-9EBB-4A81-9C5A-B93DF8ED1A59";
    public static final String REQ_HEADER_TYPE_VALUE_FROM_mh_device_type="0";
    public static final String REQ_HEADER_TYPE_VALUE_FROM_mh_device_describe="iPhone7Plus-15.2.1";
}
