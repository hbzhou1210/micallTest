package com.micall.utils;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.micall.constant.Constants;

import java.io.FileInputStream;
import java.io.IOException;
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
}
