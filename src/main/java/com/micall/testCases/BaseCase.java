package com.micall.testCases;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.micall.entity.API;
import com.micall.entity.Cases;
import com.micall.entity.JsonPathValidate;
import com.micall.entity.WriteBackData;
import com.micall.utils.AuthorizationUtils;
import com.micall.utils.CreateRegisterName;
import com.micall.utils.ExcelUtils;
import com.micall.utils.HttpsUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.IReporter;
import org.testng.Reporter;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeTest;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseCase {
    private static Logger logger = Logger.getLogger(String.valueOf(BaseCase.class));
    // 执行接口方法
    public void executeAll(API api, Cases cases, String language){
    }
    // 项目支持语言,如果支持更多语言可以在此加入
    public void execute(API api,Cases cases){
        executeAll(api,cases,"zh-Hans");
    }
    // 从excel表格中获取对象，提取出请求信息，用例信息
//    @Step("接口调用")
    public String call(API api,Cases cases,String language,boolean isAuthentication){
        logger.info("----开始调用接口请求----");
        logger.info("----接口编号和接口名称："+api.getApiNumber()+"、"+api.getApiName()+"----");
        logger.info("----用例编号和用例描述："+cases.getCaseNumber()+"、"+cases.getCaseDesc()+"----");
        String url ="";
        if("get".equalsIgnoreCase(api.getApiReqMethod())){
            // 在url添加一个判断，就是进行get无参请求里，一个异常用例，即在url随便加上一个参数去请求
            if(cases.getCaseDesc().contains("URL带参数请求")){
                url = api.getApiUrl() + "?";
                System.out.println(url);
            }else {
                url =api.getApiUrl();
            }
        }else if("post".equalsIgnoreCase(api.getApiReqMethod())){
            url = api.getApiUrl();
        }
        String reqMethod = api.getApiReqMethod();
        String submitType = api.getApiSubmitType();
        String params = cases.getCaseParams();
        String resBody = HttpsUtils.call(url,params,reqMethod,submitType,language,isAuthentication);
        logger.info(params);
        logger.info(resBody);
        return resBody;
    }
    /**
     * 添加回写对象到回写集合里
     * @param rowNum 回写行号
     * @param cellNum 回写列号
     * @param body  回写内容
     */
    public void addWBD(int rowNum,int cellNum,String body){
        // 创建一条回写的内容
        WriteBackData wbd = new WriteBackData(rowNum,cellNum,body);
        ExcelUtils.wbdList.add(wbd);
    }
    /**
     * 接口的响应内容断言
     *  如果case中ExpectValue是数组类型的json格式，那么采用多字段匹配断言逻辑
     *  如果case中ExpectValue 不是数组类型的json格式，那么采用等值匹配
     *  返回一个断言是否成功的flag
     * @param cases
     * @param language
     * @param resBody
     * @return
     */
    private  String expression;
    private  String value;
    public boolean assertResponse(Cases cases, String language, String resBody){
        boolean flag = false;
        // 根据不同的语言，获取不同的期望响应数据
        // cases类中需要定义不同的语言转换
        String expectValue = "";
        if("zh-Hans".equalsIgnoreCase(language)){
            expectValue = cases.getCaseExpectValue();
        }
        // 字符串转化为json对象
        Object jsonObject = JSONObject.parse(expectValue);
        // JSONArray是由JSONObject组成的数组
        if(jsonObject instanceof JSONArray){
            // 对象转为list
            List<JsonPathValidate> list = JSONObject.parseArray(expectValue,JsonPathValidate.class);
            for (JsonPathValidate jsonPathValidate : list){
                // 从用例表中获取断言表达式
                String expression = jsonPathValidate.getExpression();
                // 从用例表中获取断言的值
                String value = jsonPathValidate.getValue();
                // 对响应结果进行一个jsonPath寻找实际值
                String actualValue = JSONPath.read(resBody,expression) == null
                        ? "": JSONPath.read(resBody,expression).toString();
                // 期望值和实际值进行对比
                flag = value.equals(actualValue);
                if(flag == false){
                    // 断言失败
                    System.out.println("断言失败");
                    break;
                }
                System.out.println("实际值："+actualValue +",预期值："+value+",断言结果："+ flag);
            }
            // 如果cases中expectValue不是数组类型的json格式，那么采用等会匹配
            // JSONObject的数据是用{}来表示的
        }else if (jsonObject instanceof JSONObject){
            flag = resBody.equals(expectValue);
        }
        return flag;
//        boolean flag = true;
//        // 判断预期结果是否与实际结果相符，封装成一个方法，放在BaseCase类里面
//        String expectedResult = cases.getCaseExpectValue();
//        Object jsonObject = JSONObject.parse(expectedResult);
//        if (jsonObject instanceof JSONArray){  // 如果预期响应结果数据解析出来是个数组
//            // 将预期结果封装成jsonPathValidate对象
//            List <JsonPathValidate> list = JSONObject.parseArray(expectedResult,JsonPathValidate.class);
//            // 遍历结合，去除表达式的值
//            for (JsonPathValidate jsonPathValidate :list){
//                // 表达式
//                String path = jsonPathValidate.getExpression();
//                // 表达式的值
//                String expectedValue = jsonPathValidate.getValue();
//                // 用jsonpath提取出想要的内容，并判断
//                // JSONPath.read(resBody,path) == null 如果为null的话，是不能转成字符串的，所以先判断是否为null
//                String actualValue = JSONPath.read(resBody,path) == null
//                       ? "": JSONPath.read(resBody,path).toString();
//                if (!actualValue.equals(expectedValue)){
//                    flag = false;
//                    break;
//                }
//            }
//        }else if (jsonObject instanceof JSONObject){ // 如果预期响应结果数据解析出来不是数组
//            flag = expectedResult.equals(resBody);
//        }
//        System.out.println(flag);
//        if (flag){
//            return true;
//        }else {
//            return false;
//        }
    }
    /**
     * 参数化替换方法
     * @param source 被替换的字符串（params或者sql包含${xxx}）
     * @return
     */
        public String replace(String source){
            // 如果传入的字符串为空直接返回
            if(StringUtils.isBlank(source)){
                return source;
            }
            // 遍历环境变量，取出所有的oldStr和newStr
            for (String oldStr : AuthorizationUtils.env.keySet()){
                String newStr = AuthorizationUtils.env.get(oldStr);
                // 判断source中是否包含oldStr，如果包含就替换newStr
                // 查询，如果存在了，生成一个随机码，然后替换
                source = source.replace(oldStr,newStr);
            }
            return source;
        }
    /**
     * 参数化替换方法（使用正则替换掉excel表格）
     * @param params
     * @return
     */
    public String paramsReplace(String params){
        // 判断参数不为空时，进行变量的判断
        // if(StringUtils.isNotBlank(params))
        // 如果参数中包含${},则进行变量替换
        if (StringUtils.isBlank(params)){
            return params;
        }
        // 随机数替换掉变量
        Pattern pattern = Pattern.compile("\\$\\{([^\\$\\{\\}]+)\\}");
        Matcher result = pattern.matcher(params);
        if(result.find()){
            params = params.replace(result.group(0), CreateRegisterName.gerCharAndNumb(6));
        }
        return params;
    }
    @BeforeTest
    public void init() throws Exception {
        logger.info("======项目自动化开始======");
        Reporter.log("生成测试报告");
        // 从params.properties中读取参数化内容
        Properties properties = new  Properties();
        FileInputStream fiStream = new FileInputStream("src/test/resources/params.properties");
        properties.load(fiStream);
        fiStream.close();
        // 从params.properties中读取参数化内容保存到env集合中
        for (Object key : properties.keySet()){
            Object value = properties.get(key);
            // 判断配置文件中是否包含username
            if(key.toString().contains("username")){
                // 包含，随机生成一个用户名替换username
                // 把${username}作为键，随机生成的用户名作为值存到evn集合
                AuthorizationUtils.env.put(key.toString(),CreateRegisterName.gerCharAndNumb(6));
            }
        }
    }
    @AfterSuite
    /**
     * 套件执行完毕之后的操作
     */
    public void finish() throws IOException {
        // 所有接口都已经执行完毕
        // 执行批量回写
        logger.info("=======批量回写======");
        ExcelUtils.batchWrite();
        logger.info("=======项目结束======");
    }
}
