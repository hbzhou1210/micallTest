package com.micall.utils;

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
}
