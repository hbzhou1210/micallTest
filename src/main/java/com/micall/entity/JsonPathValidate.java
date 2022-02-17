package com.micall.entity;

public class JsonPathValidate {
    /**
     * JSONPath多关键字段匹配实体类
     * @author
     */
    // jsonpath表达式
    private String expression;
    // jsonpath表达式期望找到的值
    private String value;
    private String uin;
    public String getExpression(){
       return expression;
    }
    public void setExpression(String expression) {
        this.expression = expression;
    }
    public String getValue(){
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public void getUin(String uin){
        this.uin = uin;
    }
    public String setUin(){
        return uin;
    }
}
