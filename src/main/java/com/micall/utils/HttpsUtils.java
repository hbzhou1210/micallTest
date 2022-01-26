package com.micall.utils;

import com.alibaba.fastjson.JSONObject;
import com.micall.constant.Constants;
import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLInitializationException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.logging.Logger;

public class HttpsUtils {
    public static Logger logger = Logger.getLogger(String.valueOf(HttpsUtils.class));
    private static PoolingHttpClientConnectionManager connectionManager;// 设置连接池
    private static final String ENCODING = "UTF-8";
    private static final String RESULT = "-1";

    /**
        初始化连接池管理器，配置SSL
     */
    static {
        if (connectionManager == null) {
            try {
                //创建ssl安全访问连接
                //获取创建SSL上下文对象
                SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null,new TrustStrategy(){
                    @Override
                    public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                        return true;
                    }
                }).build();
                // 注册
                Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.INSTANCE)
                        .register("https", new SSLConnectionSocketFactory(sslContext))
                        .build();
                // SSL注册到连接池
                connectionManager = new PoolingHttpClientConnectionManager(registry);
                connectionManager.setMaxTotal(1000); // 设置连接池最大连接数
                connectionManager.setDefaultMaxPerRoute(20); // 设置每个路由最大连接数
            } catch (SSLInitializationException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     *创建HttpClient客户端连接对象，并配置连接池配置项
     *@return
     */
    private static CloseableHttpClient getHttpClient() {
        // 创建http配置请求参数
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(10000)
                .setConnectTimeout(10000)
                .setSocketTimeout(6000)
                .build();
        /**
            设置超时重试机制，为了防止超时不生效
            根据不同的情况进行判断是否需要重试
         */
        HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(IOException e, int i, HttpContext httpContext) {
                if (i >= 3) {
                    return false; //重试总数超过3次就放弃
                }
                if (e instanceof NoHttpResponseException) {
                    return true; //服务器响应异常，重试
                }
                if (e instanceof SSLHandshakeException) {
                    return false;  //SSL握手异常不重试
                }
                if (e instanceof InterruptedIOException) {
                    return true; //超时异常重试
                }
                if (e instanceof UnknownHostException) {
                    return false; //dna解析异常，即服务器不可达异常不重试
                }
                if (e instanceof ConnectTimeoutException) {
                    return false; //连接被拒绝不重试
                }
                HttpClientContext clientContext = HttpClientContext.adapt(httpContext);
                HttpRequest request = clientContext.getRequest();
                //如果请求是幂等的，就再次重试
                if(!(request instanceof HttpEntityEnclosingRequest)){
                    return true;
                }
                return false;
            }
        };
        //创建httpclient请求配置
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager) //把连接管理配置设置到连接的客户端中
                .setDefaultRequestConfig(requestConfig) //把请求的默认配置设置到连接的客户端中
                .setRetryHandler(retryHandler) //把请求重试机制设置到连接的客户端中
                .build();
        //
        return httpClient;
    }
    private static HttpClientBuilder setDefaultRequestConfig(RequestConfig requestConfig){
        return null;
    }
    /**
    *不带参数的get请求
    * @param url 请求地址
    * @param isAuthentication 是否需要鉴权
    * @return
     */
    public static String httpGetJson(String url,String language,boolean isAuthentication){
        // 创建一个get请求连接
        HttpGet get = new HttpGet(url);
        // 传入请求头
        get.setHeader(Constants.REQ_HEADER_TYPE_content_type, Constants.REQ_HEADER_TYPE_VALUE_FROM_content_type);
        get.setHeader(Constants.REQ_HEADER_TYPE_mh_device_name, Constants.REQ_HEADER_TYPE_VALUE_FROM_mh_device_name);
        get.setHeader(Constants.REQ_HEADER_TYPE_mh_device_id, Constants.REQ_HEADER_TYPE_VALUE_FROM_mh_device_id);
        get.setHeader(Constants.REQ_HEADER_TYPE_accept, Constants.REQ_HEADER_TYPE_VALUE_FROM_accept);
        get.setHeader(Constants.REQ_HEADER_TYPE_mh_device_type, Constants.REQ_HEADER_TYPE_VALUE_FROM_mh_device_type);
        get.setHeader(Constants.REQ_HEADER_TYPE_mh_device_describe, Constants.REQ_HEADER_TYPE_VALUE_FROM_mh_device_describe);
        get.setHeader(Constants.REQ_HEADER_TYPE_accept_language, Constants.REQ_HEADER_TYPE_VALUE_FROM_accept_language);
        get.setHeader(Constants.REQ_HEADER_TYPE_accept_encoding, Constants.REQ_HEADER_TYPE_VALUE_FROM_accept_encoding);
        get.setHeader(Constants.REQ_HEADER_TYPE_content_length, Constants.REQ_HEADER_TYPE_VALUE_FROM_content_length);
        get.setHeader(Constants.REQ_HEADER_TYPE_user_agent, Constants.REQ_HEADER_TYPE_VALUE_FROM_user_agent);
//        get.setHeader(Constants.REQ_HEADER_LANGUAGE_Name,language);
        // 添加鉴权头
        if(isAuthentication){
            AuthorizationUtils.setTokenInRequest(get);
        }
        // 定义响应体
        CloseableHttpResponse response = null;
        try{
            // 获取连接客户端
            CloseableHttpClient client =getHttpClient();
            // 发起请求
            response =client.execute(get);
            // 获取响应体
            HttpEntity entity = response.getEntity();
            // 响应体内容转为字符串类型
            String reqBody = EntityUtils.toString(entity);
            return reqBody;
        } catch (IOException e) {
            logger.info("get请求异常");
        }finally {
            if(response != null){
                try{
                    response.close();//关闭连接
                } catch (IOException e) {
                    logger.info("关闭连接出错");
                }
            }
        }
        return null;
    }
    /**
     * Url带参数的表单提交请求
     * @param url 请求地址
     * @param params 请求参数
     * @param isAuthentication 是否需要鉴权
     * @return
     */
    public static String httpGetForm(String url,String params,String language,boolean isAuthentication){
        // 创建一个get请求对象并传入请求地址
        // 地址格式为“http://xxxx/
        HttpGet get = new HttpGet(url+"?"+params);
        // 传入请求头
        get.setHeader(Constants.REQ_HEADER_TYPE_content_type, Constants.REQ_HEADER_TYPE_VALUE_FROM_content_type);
        get.setHeader(Constants.REQ_HEADER_TYPE_mh_device_name, Constants.REQ_HEADER_TYPE_VALUE_FROM_mh_device_name);
        get.setHeader(Constants.REQ_HEADER_TYPE_mh_device_id, Constants.REQ_HEADER_TYPE_VALUE_FROM_mh_device_id);
        get.setHeader(Constants.REQ_HEADER_TYPE_accept, Constants.REQ_HEADER_TYPE_VALUE_FROM_accept);
        get.setHeader(Constants.REQ_HEADER_TYPE_mh_device_type, Constants.REQ_HEADER_TYPE_VALUE_FROM_mh_device_type);
        get.setHeader(Constants.REQ_HEADER_TYPE_mh_device_describe, Constants.REQ_HEADER_TYPE_VALUE_FROM_mh_device_describe);
        get.setHeader(Constants.REQ_HEADER_TYPE_accept_language, Constants.REQ_HEADER_TYPE_VALUE_FROM_accept_language);
        get.setHeader(Constants.REQ_HEADER_TYPE_accept_encoding, Constants.REQ_HEADER_TYPE_VALUE_FROM_accept_encoding);
        get.setHeader(Constants.REQ_HEADER_TYPE_content_length, Constants.REQ_HEADER_TYPE_VALUE_FROM_content_length);
        get.setHeader(Constants.REQ_HEADER_TYPE_user_agent, Constants.REQ_HEADER_TYPE_VALUE_FROM_user_agent);
//        get.setHeader(Constants.REQ_HEADER_LANGUAGE_Name,language);
        // 添加鉴权
        if(isAuthentication){
            AuthorizationUtils.setTokenInRequest(get);
        }
        // 定义响应体
        CloseableHttpResponse response = null;
        try{
            // 获取连接客户端
            CloseableHttpClient client = getHttpClient();
            // 发起请求
            response =client.execute(get);
            // 获取响应体
            HttpEntity entity = response.getEntity();
            // 响应体内容转化字符串类型
            String reqBody = EntityUtils.toString(entity);
            return reqBody;
        } catch (Exception e) {
                logger.info("get请求异常");
        }finally {
            if(response != null){
                try {
                    response.close(); //关闭连接
                } catch (IOException e) {
                    logger.info("关闭连接出错");
                }
            }
        }
        return null;
    }
    /**
     * json格式的post请求
     * @param url 请求地址
     * @param params 请求参数
     * @param isAuthentication 是否需要鉴权
     * @return
     */
    public static String httpPostJson(String url,String params,String language,boolean isAuthentication){
        // 定义post请求对象并传入url
        HttpPost post = new HttpPost(url);
        // 传入请求头
        post.addHeader(Constants.REQ_HEADER_TYPE_content_type,Constants.REQ_HEADER_TYPE_VALUE_FROM_content_type);
        post.addHeader(Constants.REQ_HEADER_TYPE_mh_device_type, Constants.REQ_HEADER_TYPE_VALUE_FROM_mh_device_type);
        post.addHeader(Constants.REQ_HEADER_TYPE_mh_device_name, Constants.REQ_HEADER_TYPE_VALUE_FROM_mh_device_name);
        post.addHeader(Constants.REQ_HEADER_TYPE_mh_device_id, Constants.REQ_HEADER_TYPE_VALUE_FROM_mh_device_id);
        post.addHeader(Constants.REQ_HEADER_TYPE_mh_device_describe, Constants.REQ_HEADER_TYPE_VALUE_FROM_mh_device_describe);
//        post.addHeader(Constants.REQ_HEADER_LANGUAGE_Name,language);
        // 添加鉴权头
        if(isAuthentication){
            AuthorizationUtils.setTokenInRequest(post);
        }
        // 传入请求参数
        post.setEntity(new StringEntity(params,"UTF-8"));
        // 定义响应体
        CloseableHttpResponse response = null;
        try {
            // 获取客户端连接
            CloseableHttpClient client = getHttpClient();
            // 发起请求
            response = client.execute(post);
            // 获取响应体
            HttpEntity entity = response.getEntity();
            // 响应体内容转为字符串类型
            String reqBody = EntityUtils.toString(entity);
            return reqBody;
        }catch (Exception e){
            logger.info("Post请求异常"+e);

        }finally {
            if(response !=null){
                try {
                    response.close(); // 关闭连接
                }catch (IOException e2){
                    logger.info("关闭连接出错"+e2);
                }
            }
        }
        return null;
    }
    /**
     * Form格式的post请求
     * @param url 请求地址
     * @param params 请求参数
     * @param isAuthentication 是否需要鉴权
     * @return
     */
    public static String httpPostForm(String url, Map<String,Object> params, String language, boolean isAuthentication){
        // 定义post请求对象并传入url
        HttpPost post = new HttpPost(url);
        // 传入请求头
//        post.setHeader("Content-type","application/x-www-form-urlencoded");
        post.setHeader(Constants.REQ_HEADER_TYPE_content_type, Constants.REQ_HEADER_TYPE_VALUE_FROM_content_type);
        post.setHeader(Constants.REQ_HEADER_TYPE_mh_device_name, Constants.REQ_HEADER_TYPE_VALUE_FROM_mh_device_name);
        post.setHeader(Constants.REQ_HEADER_TYPE_mh_device_id, Constants.REQ_HEADER_TYPE_VALUE_FROM_mh_device_id);
        post.setHeader(Constants.REQ_HEADER_TYPE_accept, Constants.REQ_HEADER_TYPE_VALUE_FROM_accept);
        post.setHeader(Constants.REQ_HEADER_TYPE_mh_device_type, Constants.REQ_HEADER_TYPE_VALUE_FROM_mh_device_type);
        post.setHeader(Constants.REQ_HEADER_TYPE_mh_device_describe, Constants.REQ_HEADER_TYPE_VALUE_FROM_mh_device_describe);
        post.setHeader(Constants.REQ_HEADER_TYPE_accept_language, Constants.REQ_HEADER_TYPE_VALUE_FROM_accept_language);
        post.setHeader(Constants.REQ_HEADER_TYPE_accept_encoding, Constants.REQ_HEADER_TYPE_VALUE_FROM_accept_encoding);
        post.setHeader(Constants.REQ_HEADER_TYPE_content_length, Constants.REQ_HEADER_TYPE_VALUE_FROM_content_length);
        post.setHeader(Constants.REQ_HEADER_TYPE_user_agent, Constants.REQ_HEADER_TYPE_VALUE_FROM_user_agent);
        // 添加鉴权头
        if(isAuthentication){
            AuthorizationUtils.setTokenInRequest(post);
        }
        // 传入请求参数
        if(null != params && params.size() >0){
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            // 通过map继承entitySet方法获取entity
            Set<Map.Entry<String,Object>> entitySet =params.entrySet();
            // 循环遍历，获取迭代器
            Iterator<Map.Entry<String,Object>> iterator = entitySet.iterator();
            while (iterator.hasNext()){
                Map.Entry<String,Object> mapEntry = iterator.next();
                nameValuePairs.add(new BasicNameValuePair(mapEntry.getKey(),mapEntry.getValue().toString()));
            }
            // 为post设置封装好的请求参数
            try{
                post.setEntity(new UrlEncodedFormEntity(nameValuePairs,"UTF-8"));
            }catch (UnsupportedEncodingException e){
                logger.info("请求参数设置失败"+e);
            }
        }
        // 定义响应体
        CloseableHttpResponse response =null;
        try {
            // 获取客户端连接
            CloseableHttpClient client =getHttpClient();
            // 发起请求
            response=client.execute(post);
            // 获取响应体
            HttpEntity entity = response.getEntity();
            // 响应体内容转为字符串类型
            String reqBody = EntityUtils.toString(entity);
            return reqBody;
        }catch (Exception e){
            logger.info("Post请求异常"+e);
        }finally {
            if(response !=null){
                try {
                    response.close(); // 关闭连接
                }catch (IOException e2){
                    logger.info("关闭连接出错"+e2);
                }
            }
        }
        return null;
    }
    /**
     * json格式的put请求
     * @param url 请求地址
     * @param params 请求参数
     * @param isAuthentication 是否需要鉴权
     * @return
     */
    public static String httpPutJson(String url,String params,String language,boolean isAuthentication){
        // 定义put请求对象并传入url
        HttpPut put = new HttpPut(url);
        // 传入请求头
        put.setHeader(Constants.REQ_HEADER_TYPE_content_type, Constants.REQ_HEADER_TYPE_VALUE_FROM_content_type);
        put.setHeader(Constants.REQ_HEADER_TYPE_mh_device_name, Constants.REQ_HEADER_TYPE_VALUE_FROM_mh_device_name);
        put.setHeader(Constants.REQ_HEADER_TYPE_mh_device_id, Constants.REQ_HEADER_TYPE_VALUE_FROM_mh_device_id);
        put.setHeader(Constants.REQ_HEADER_TYPE_accept, Constants.REQ_HEADER_TYPE_VALUE_FROM_accept);
        put.setHeader(Constants.REQ_HEADER_TYPE_mh_device_type, Constants.REQ_HEADER_TYPE_VALUE_FROM_mh_device_type);
        put.setHeader(Constants.REQ_HEADER_TYPE_mh_device_describe, Constants.REQ_HEADER_TYPE_VALUE_FROM_mh_device_describe);
        put.setHeader(Constants.REQ_HEADER_TYPE_accept_language, Constants.REQ_HEADER_TYPE_VALUE_FROM_accept_language);
        put.setHeader(Constants.REQ_HEADER_TYPE_accept_encoding, Constants.REQ_HEADER_TYPE_VALUE_FROM_accept_encoding);
        put.setHeader(Constants.REQ_HEADER_TYPE_content_length, Constants.REQ_HEADER_TYPE_VALUE_FROM_content_length);
        put.setHeader(Constants.REQ_HEADER_TYPE_user_agent, Constants.REQ_HEADER_TYPE_VALUE_FROM_user_agent);
//        put.setHeader(Constants.REQ_HEADER_LANGUAGE_Name,language);
        // 添加鉴权头
        if(isAuthentication){
            AuthorizationUtils.setTokenInRequest(put);
        }
        // 传入请求参数
        put.setEntity(new StringEntity(params,"UTF-8"));
        // 定义响应体
        CloseableHttpResponse response =null;
        try {
            // 获取客户端连接
            CloseableHttpClient client =getHttpClient();
            // 发起请求
            response=client.execute(put);
            // 获取响应体
            HttpEntity entity = response.getEntity();
            // 响应体内容转为字符串类型
            String reqBody = EntityUtils.toString(entity);
            return reqBody;
        }catch (Exception e){
            logger.info("put请求异常"+e);
        }finally {
            if(response !=null){
                try {
                    response.close(); // 关闭连接
                }catch (IOException e2){
                    logger.info("关闭连接出错"+e2);
                }
            }
        }
        return null;
    }
    /**
     * Form格式的put请求
     * @param url 请求地址
     * @param params 请求参数
     * @param isAuthentication 是否需要鉴权
     * @return
     */
    public static String httpPutForm(String url, Map<String,Object> params, String language, boolean isAuthentication){
        // 定义put请求对象并传入url
        HttpPut put = new HttpPut(url);
        // 传入请求头
        put.setHeader("Content-type","application/x-www-form-urlencoded");
        // 添加鉴权头
        if(isAuthentication){
            AuthorizationUtils.setTokenInRequest(put);
        }
        // 传入请求参数
        if(null != params && params.size() >0){
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            // 通过map继承entitySet方法获取entity
            Set<Map.Entry<String,Object>> entitySet =params.entrySet();
            // 循环遍历，获取迭代器
            Iterator<Map.Entry<String,Object>> iterator = entitySet.iterator();
            while (iterator.hasNext()){
                Map.Entry<String,Object> mapEntry = iterator.next();
                nameValuePairs.add(new BasicNameValuePair(mapEntry.getKey(),mapEntry.getValue().toString()));
            }
            // 为post设置封装好的请求参数
            try{
                put.setEntity(new UrlEncodedFormEntity(nameValuePairs,"UTF-8"));
            }catch (UnsupportedEncodingException e){
                logger.info("请求参数设置失败"+e);
            }
        }
        // 定义响应体
        CloseableHttpResponse response =null;
        try {
            // 获取客户端连接
            CloseableHttpClient client =getHttpClient();
            // 发起请求
            response=client.execute(put);
            // 获取响应体
            HttpEntity entity = response.getEntity();
            // 响应体内容转为字符串类型
            String reqBody = EntityUtils.toString(entity);
            return reqBody;
        }catch (Exception e){
            logger.info("Post请求异常"+e);
        }finally {
            if(response !=null){
                try {
                    response.close(); // 关闭连接
                }catch (IOException e2){
                    logger.info("关闭连接出错"+e2);
                }
            }
        }
        return null;
    }
    public static class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase{
        // 定义一个delete请求方法
        private static final String REQ_METHOD_NAME = "DELETE";
        @Override
        public String getMethod() {
            return REQ_METHOD_NAME;
        }
        public HttpDeleteWithBody(final String uri){
            super();
            setURI(URI.create(uri));
        }
        public HttpDeleteWithBody(final URI uri){
            super();
            setURI(uri);
        }
        public HttpDeleteWithBody(){
            super();
        }
    }
    public static String httpDeleteJson(String url,String param,String language,boolean isAuthentication){
        // 创建delete对象并传入url
        HttpDeleteWithBody delete = new HttpDeleteWithBody(url);
        // 传入请求头
        delete.setHeader(Constants.REQ_HEADER_TYPE_content_type, Constants.REQ_HEADER_TYPE_VALUE_FROM_content_type);
        delete.setHeader(Constants.REQ_HEADER_TYPE_mh_device_name, Constants.REQ_HEADER_TYPE_VALUE_FROM_mh_device_name);
        delete.setHeader(Constants.REQ_HEADER_TYPE_mh_device_id, Constants.REQ_HEADER_TYPE_VALUE_FROM_mh_device_id);
        delete.setHeader(Constants.REQ_HEADER_TYPE_accept, Constants.REQ_HEADER_TYPE_VALUE_FROM_accept);
        delete.setHeader(Constants.REQ_HEADER_TYPE_mh_device_type, Constants.REQ_HEADER_TYPE_VALUE_FROM_mh_device_type);
        delete.setHeader(Constants.REQ_HEADER_TYPE_mh_device_describe, Constants.REQ_HEADER_TYPE_VALUE_FROM_mh_device_describe);
        delete.setHeader(Constants.REQ_HEADER_TYPE_accept_language, Constants.REQ_HEADER_TYPE_VALUE_FROM_accept_language);
        delete.setHeader(Constants.REQ_HEADER_TYPE_accept_encoding, Constants.REQ_HEADER_TYPE_VALUE_FROM_accept_encoding);
        delete.setHeader(Constants.REQ_HEADER_TYPE_content_length, Constants.REQ_HEADER_TYPE_VALUE_FROM_content_length);
        delete.setHeader(Constants.REQ_HEADER_TYPE_user_agent, Constants.REQ_HEADER_TYPE_VALUE_FROM_user_agent);
//        delete.setHeader(Constants.REQ_HEADER_LANGUAGE_Name,language);
        // 添加鉴权头
        if(isAuthentication){
            AuthorizationUtils.setTokenInRequest(delete);
        }
        // 传入请求参数
        delete.setEntity(new StringEntity(param,"UTF-8"));
        // 定义响应体
        CloseableHttpResponse response = null;
        try{
            CloseableHttpClient client = getHttpClient();
            response = client.execute(delete);
            HttpEntity entity = response.getEntity();
            String reqBody = EntityUtils.toString(entity);
            return reqBody;
        }catch (Exception e){
            logger.info("delete请求异常" +e);
        }finally {
            if(response !=null){
                try {
                    response.close();
                }catch (IOException e2){
                    logger.info("关闭连接出错"+e2);
                }
            }
        }
        return null;
    }
    public static String call(String url,String params,String reqMethod,String submitType,String language,boolean isAuthentication){
        try {
            // 如果是json的提交方式
            if("json".equalsIgnoreCase(submitType)){
                // 判断是get请求方式时
                if("get".equalsIgnoreCase(reqMethod)){
                    // 返回get请求的响应体
                    return httpGetJson(url,language,isAuthentication);
                }else if("post".equalsIgnoreCase(reqMethod)){
                    return httpPostJson(url,params,language,isAuthentication);
                }else if ("put".equalsIgnoreCase(reqMethod)){
                    return httpPutJson(url,params,language,isAuthentication);
                }else if("delete".equalsIgnoreCase(reqMethod)){
                    return httpDeleteJson(url,params,language,isAuthentication);
                }
            }else if("form".equalsIgnoreCase(submitType)){
                if("get".equalsIgnoreCase(reqMethod)){
                    params = jsonToKeyValue(params);
                    return httpGetForm(url,params,language,isAuthentication);
                }else if("post".equalsIgnoreCase(reqMethod)){
                    return httpGetForm(url,params,language,isAuthentication);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public static String jsonToKeyValue(String jsonParams){
        // json转换成map
        Map<String,String> map = JSONObject.parseObject(jsonParams,HashMap.class);
        // 把获取到的键值保存到set集合中
        Set<String> keySet =map.keySet();
        // 定义一个参数结果
        String params = "";
        for (String key : keySet){
            // 通过KEY获取到value值
            String value = map.get(key);
            // 最后的请求参数
            params += key + "=" +value +"&";
        }
        // 得到最终的参数值后，把最后一个“&”去掉
        params = params.substring(0,params.length()-1);
        return params;
    }
}


