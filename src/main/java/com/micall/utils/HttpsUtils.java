package com.micall.utils;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLInitializationException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
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
    /*
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
    /*
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
        /*
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
}


