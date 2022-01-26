package com.micall.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;

/** 解析excel中用例的表头信息，需要定义参数变量，get set方法
 *
 */
public class Cases {
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


    public void setApiId(String apiId){ this.apiId = apiId;}
    public String getApiId(){
        return apiId;
    }
    public void setId(String id){ this.id = id;}
    public String getCaseNumber(){
        return id;
    }
    public void setDesc(String desc){ this.desc = desc;}
    public String getCaseDesc(){
        return desc;
    }
    public void setParams (String params) { this.params = params;}
    public String getCaseParams(){
        return params;
    }
    public void setExpectValue (String expectValue) { this.expectValue = expectValue;}
    public String getCaseExpectValue(){
        return expectValue;
    }
    public void setSql (String sql) { this.sql = sql;}
    public String getCaseSql(){
        return sql;
    }
}
