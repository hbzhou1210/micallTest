package com.micall.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;

/** 解析excel中用例的表头信息，需要定义参数变量，get set方法
 *
 */
public class Case {
    // 用例编号
    @Excel(name="用例编号")
    private String id;
    // 用例描述
    @Excel(name="用例描述")
    private String desc;
    // 参数
    @Excel(name="参数")
    private String params;
    // 接口编号
    @Excel(name="接口编号")
    private String apiId;
    // 期望响应数据
    @Excel(name="期望响应数据")
    private String expectValue;
    // 检验SQL
    @Excel(name="检验SQL")
    private String sql;

    public String getApiId(){
        return apiId;
    }
}
