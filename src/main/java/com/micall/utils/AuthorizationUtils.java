package com.micall.utils;

import com.alibaba.fastjson.JSONPath;
import com.iceolive.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpRequest;
import java.util.HashMap;
import java.util.Map;

public class AuthorizationUtils {
    public static final Map<String,String> env = new HashMap<String,String>();
    /**
        1.从接口响应中获取token信息，登录成功后返回token
        2.把token信息存储到环境变量中
        @param response 在HttpUtils类返回的接口响应信息
     */
    public static void storeToken(String response){
        // 从接口响应中获取token信息
        // 从登录接口的响应数据中获取到token的路径$.data.token_info.token,采用JsonPath，格式是以$开头
        Object token = JSONPath.read(response,"$.data.accessToken");
        // token不等于空，说明登录成功
        if (token != null){
            // 存储token到环境变量中
            env.put("${token}",token.toString());
        }
    }
    /**
    *判断环境变量中是否存在token值，如果存在请求中设置token
     * @param request
     */
    public static void setTokenInRequest(HttpRequest request){
        // 环境变量中取出token
        String token = env.get("${token}");
        // 如果token存在，不为空
        // 字符串工具类StringUtils
        if(StringUtils.isNotBlank(token)){
            request.addHeader("mh-access-token",token);
        }
    }
}
