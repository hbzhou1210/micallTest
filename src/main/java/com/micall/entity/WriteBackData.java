package com.micall.entity;

public class WriteBackData {
    /**
     * excel回写封装对象
     * @author
     */
    // 如果以后需要回写不同sheet内容，可加上
    // private int sheetNum;
    // 回写行号
    private int rowNum;
    // 回写列好
    private int cellNum;
    // 回写内容
    private String content;

    public WriteBackData(){
        super();
    }
    public int getRowNum(){

        return rowNum;
    }
    public void setRowNum(){
        this.rowNum = rowNum;
    }
    public int getCellNum(){

        return cellNum;
    }
    public void setCellNum(){
        this.cellNum = cellNum;
    }
    public String getContent(){

        return content;
    }
    public void setContent(){
        this.content = content;
    }
    public WriteBackData(int rowNum,int cellNum,String content){
        super();
        this.rowNum = rowNum;
        this.cellNum = cellNum;
        this.content = content;
    }
}
