package com.micall.utils;

import com.micall.constant.Constants;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

public class HttpsUtils {
    public static Logger logger = Logger.getLogger(String.valueOf(HttpsUtils.class));
    private static PoolingHttpClientConnectionManager connectionManager;// 设置连接池
    private static final String ENCODING = "UTF-8";
    private static final String RESULT = "-1";

    //    //设置请求和传输超时时间
//    private static RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(10000).setSocketTimeout(10000).build();
//    private static CloseableHttpClient httpClient = HttpClients.createDefault();
//    public static String send(String httpUrl,String message) throws IOException {
//        String result = null;
//        HttpPost httpPost = new HttpPost(httpUrl);
//        // 设置数据读取超时5s,传输超时5s,连接请求超时5s
//        RequestConfig requestConfig = RequestConfig.custom()
//                .setSocketTimeout(5000)
//                .setConnectTimeout(50000)
//                .setConnectionRequestTimeout(5000).build();
//        httpPost.setConfig(requestConfig);
//        message = URLEncoder.encode(message,"utf-8");
//        StringEntity entity = new StringEntity(message);
//        httpPost.setEntity(entity);
//        CloseableHttpResponse response = httpClient.execute(httpPost);
//        BufferedReader in = null;
//}
    /**
        初始化连接池管理器，配置SSL
     */
    static {
        if (connectionManager == null) {
            try {
                //创建ssl安全访问连接
                //获取创建SSL上下文对象
                SSLContext sslContext = SSLContext.getInstance("SSL");
                // 注册
                Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.INSTANCE)
                        .register("https", new SSLConnectionSocketFactory(sslContext))
                        .build();
                // SSL注册到连接池
                connectionManager = new PoolingHttpClientConnectionManager(registry);
                connectionManager.setMaxTotal(1000); // 设置连接池最大连接数
                connectionManager.setDefaultMaxPerRoute(20); // 设置每个路由最大连接数
            } catch (SSLInitializationException | NoSuchAlgorithmException e) {
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
        get.setHeader(Constants.REQ_HEADER_TYPE_NAME, Constants.REQ_HEADER_TYPE_VALUE_JSON);
        get.setHeader(Constants.REQ_HEADER_LANGUAGE_Name,language);
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
        get.setHeader(Constants.REQ_HEADER_TYPE_NAME,Constants.REQ_HEADER_TYPE_VALUE_FROM);
        get.setHeader(Constants.REQ_HEADER_LANGUAGE_Name,language);
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
        post.setHeader(Constants.REQ_HEADER_TYPE_NAME,Constants.REQ_HEADER_TYPE_VALUE_JSON);
        post.setHeader(Constants.REQ_HEADER_LANGUAGE_Name,language);
        // 添加鉴权头
        if(isAuthentication){
            AuthorizationUtils.setTokenInRequest(post);
        }
        // 传入请求参数
        post.setEntity(new StringEntity(params,"UTF-8"));
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
     * Form格式的post请求
     * @param url 请求地址
     * @param params 请求参数
     * @param isAuthentication 是否需要鉴权
     * @return
     */
    public static String httpPostForm(String url,String params,String language,boolean isAuthentication){
        // 定义post请求对象并传入url
        HttpPost post = new HttpPost(url);
        // 传入请求头
        post.setHeader(Constants.REQ_HEADER_TYPE_NAME,Constants.REQ_HEADER_TYPE_VALUE_FROM);
        post.setHeader(Constants.REQ_HEADER_LANGUAGE_Name,language);
        // 添加鉴权头
        if(isAuthentication){
            AuthorizationUtils.setTokenInRequest(post);
        }
        // 传入请求参数
        post.setEntity(new StringEntity(params,"UTF-8"));
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

}


