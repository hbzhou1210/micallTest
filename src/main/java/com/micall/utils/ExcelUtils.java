package com.micall.utils;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.micall.constant.Constants;
import com.micall.entity.API;
import com.micall.entity.Cases;
import com.micall.entity.WriteBackData;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ExcelUtils {
    /**
     * 使用easypoi解析excel，把excel里的参数列转换为对象，步骤
     * 1.导入easypoi坐标
     * 2.编写实体类API
     * 3.在API类的字段上使用@excel注解，识别excel的参数名
     * 4.编写读取的导入代码
     * @param <E>
     * @param startSheetIndex 传入需要读取的excel页签
     * @param clazz 传入需求读取的多个实体类API/Case（用泛型）
     * @return Class clazz = API.class
     * importExcel 方法不支持同时读取两个页签因为返回值只有一个
     */
    public static <E> List<E> read(int startSheetIndex,Class<E> clazz) throws IOException {
        // 1.加载excel文件
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(Constants.EXCEL_PATH); //excel的文件路径，设置为一个常量了
            // 2.导入配置（把excel内容读取到Java中），创建空对象
            ImportParams params = new ImportParams();
            // 设置需要读取的excel页签，以索引读取，具体可以点击查看ImportParams方法
            // 默认读取第一个页签，如果需要读取其他页签，通过该参数设置
            params.setStartSheetIndex(startSheetIndex);
            // 导入需要验证的数据，结合实体内上的注解一起使用。即@Excel(name="接口名称")name和excel一样才导入
            params.setNeedVerify(true);
            // 3.执行导入，导入的方法返回的是一个list集合，存的是对象，此时可以把excel接口信息表封装成一个对象了
            List<E> list = ExcelImportUtil.importExcel(fis,clazz,params);
            return list;
            // fis文件流，clazz就是实体的对象，params是参数
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            fis.close();
        }
        return null;
    }
    // 读取excel中第一个sheet，API，通过read方法一次性把所有接口的数据都读取放到list集合里
    public static List<API> apiList;

    static {
        try {
            apiList = ExcelUtils.read(0,API.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 读取excel中第二个sheet，Case，通过read方法一次性把所有用例的数据都读取放到list集合里
    public static List<Cases> casesList;

    static {
        try {
            casesList = ExcelUtils.read(1, Cases.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // excel回写数据集合
    public static List<WriteBackData> wbdList = new ArrayList<WriteBackData>();
    /**
     * 从已经读取好的所有List<API>和所有List<Case>
     *     两个集合中获取符合条件的数据
     * @param apiId 接口编号，用于匹配接口信息和用例中的接口编号
     */
    public static Object[][]getAPIandCaseByApiId(String apiId){
        // 需要的API对象，接口信息一个接口编号只有一个
        API wantAPI = null;
        // 需要的Case对象，用例一个接口编号有多个用例，所以这里定义的是list集合
        List<Cases> wantCasesList = new ArrayList<Cases>();
        // 匹配API对象
        for (API api :apiList){
            if(apiId.equals(api.getApiNumber())){
                // 传入的apiId和API对象中的id相等则返回
                wantAPI = api;
                break;
            }
        }
        // 匹配Case对象
        for(Cases c : casesList){
            // 传入的apiId和Case集合中的apiId相等则返回
            if(apiId.equals(c.getApiId())){
                wantCasesList.add(c);
            }
        }
        // wantCaseList和wantAPI是有关联的，他们的apiId是相等的
        // 测试用例中采用的注解是@DataProvider参数化
        // 该注解返回的是一个二维数组，所以此方法也返回一个二维数组
        // Object[][] datas = new Object[方法运行的次数][方法参数的个数]
        // wantCaseList.size() 是需要循环执行的用例次数，有多少个用例就执行几次，可以固定写几
        Object[][] datas = new Object[wantCasesList.size()][2];
        // Object[][] datas = {{api,case1},{api,case2},{api,case3}}
        // 往二位数组中存储api和case数据，存几次由case确定
        for (int i = 0; i < wantCasesList.size(); i++){
            datas[i][0] =wantAPI;
            datas[i][1] = wantCasesList.get(i);
        }
        return datas;
    }
    /**
     * 批量回写
     */
    public static void batchWrite() throws IOException {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try{
            fis = new FileInputStream(Constants.EXCEL_PATH);
            Workbook workbook = WorkbookFactory.create(fis);
            Sheet sheet = workbook.getSheetAt(1);
            // 回写,操作行和列
            // 1. 遍历wbdList集合
            for (WriteBackData wbd : wbdList){
                //2.获取行号，获取row对象
                int rowNum = wbd.getRowNum();
                Row row = sheet.getRow(rowNum);
                // 3.获取列号，获取cell对象
                int cellNum = wbd.getCellNum();
                Cell cell = row.getCell(cellNum,Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                // 4.获取回写内容，设置到cell中
                cell.setCellType(CellType.STRING);
                String content = wbd.getContent();
                cell.setCellValue(content);

            }
            // 回写到文件中
            fos = new FileOutputStream(Constants.EXCEL_PATH);
            workbook.write(fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fis.close();
            fos.close();
        }
    }
    /**
     * 流关闭方法
     * @param stream
     */
    private static void close(Closeable stream){
        if(stream !=null){
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
