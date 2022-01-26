package com.micall.entity;


import cn.afterturn.easypoi.excel.annotation.Excel;

public class API {
    /**
     * 解析excel中接口信息的数据，使用@Excel注解解析表头名称
     * @author lenovo
     */
    // 接口编号
    @Excel(name="接口编号")
    private String id;
    // 接口名称
    @Excel(name="接口名称")
    private String name;
    // 接口提交方式
    @Excel(name="接口提交方式")
    private String method;
    // 接口地址
    @Excel(name="接口地址")
    private String url;
    // 参数类型
    @Excel(name="参数类型")
    private String contentType;

    public void setId(String id){
        this.id = id;
    }
    public String getApiNumber(){
        return id;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getApiName(){return name;};
    public void setMethod(String method){
        this.method = method;
    }
    public String getApiReqMethod(){
        return method;
    }
    public void setUrl(String url){
        this.url = url;
    }
    public String getApiUrl(){
        return url;
    }
    public void setContentType(String contentType){
        this.contentType = contentType;
    }
    public String getApiSubmitType(){
        return contentType;
    }
//    public String getId(){return id;}
}
