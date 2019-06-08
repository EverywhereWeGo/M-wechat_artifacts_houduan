package com.b_util;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpClientHelper {

    public static String sendPost(String urlParam, Map<String, String> requestHeaders, Map<String, String> params) {
        // 创建httpClient实例对象
        HttpClient httpClient = new HttpClient();
        // 设置httpClient连接主机服务器超时时间：15000毫秒
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(15000);
        // 创建post请求方法实例对象
        PostMethod postMethod = new PostMethod(urlParam);
        // 设置post请求超时时间
        postMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 60000);
        if (!(null == requestHeaders)) {
            for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                postMethod.addRequestHeader(entry.getKey(), entry.getValue());
            }
        }

        if (!(null == params)) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                postMethod.setParameter(entry.getKey(), entry.getValue());
            }
        }
        String result = "";
        try {
            httpClient.executeMethod(postMethod);
            postMethod.getResponseBodyAsString();
        } catch (IOException e) {
            e.printStackTrace();
        }


        postMethod.releaseConnection();
        return result;
    }


    public static Map<String, String> sendGet(String url, Map<String, String> requestHeaders) {

        Map<String, String> responseMap = new HashMap<String, String>();
        // 创建httpClient实例对象
        HttpClient httpClient = new HttpClient();
        // 设置httpClient连接主机服务器超时时间：15000毫秒
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(60000);
        // 创建GET请求方法实例对象
        GetMethod getMethod = new GetMethod(url);
        if (!(null == requestHeaders)) {
            for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                getMethod.addRequestHeader(entry.getKey(), entry.getValue());
            }
        }

        try {
            httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            httpClient.executeMethod(getMethod);

            String cookiestr = "";
            Cookie[] cookies = httpClient.getState().getCookies();
            if (!(cookies.length == 0)) {
                for (int i = 0; i < cookies.length; i++) {
                    cookiestr = cookiestr + cookies[i].toString() + ";";
                }
                cookiestr = cookiestr.substring(0, cookiestr.length() - 1);
            }

            String result = getMethod.getResponseBodyAsString();
            responseMap.put("responseCookie", cookiestr);
            responseMap.put("responseContext", result);
        } catch (IOException e) {
            e.printStackTrace();
        }

        getMethod.releaseConnection();

        return responseMap;
    }

    public static Map<String, String> sendGetToGetPicture(String url, Map<String, String> requestHeaders, String title) {

        Map<String, String> responseMap = new HashMap<String, String>();
        // 创建httpClient实例对象
        HttpClient httpClient = new HttpClient();
        // 设置httpClient连接主机服务器超时时间：15000毫秒
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(60000);
        // 创建GET请求方法实例对象
        GetMethod getMethod = new GetMethod(url);
        if (!(null == requestHeaders)) {
            for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                getMethod.addRequestHeader(entry.getKey(), entry.getValue());
            }
        }
        try {
            httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            httpClient.executeMethod(getMethod);


//            File storeFile = new File("C:\\Users\\Administrator\\Desktop\\我的代码\\wechat_artifacts_qianduan\\img\\" + title + ".jpg");
            File storeFile = new File("/opt/" + title + ".jpg");

            FileOutputStream output = new FileOutputStream(storeFile);
            //得到网络资源的字节数组,并写入文件
            output.write(getMethod.getResponseBody());
            output.close();

            String cookiestr = "";
            Cookie[] cookies = httpClient.getState().getCookies();
            if (!(cookies.length == 0)) {
                for (int i = 0; i < cookies.length; i++) {
                    cookiestr = cookiestr + cookies[i].toString() + ";";
                }
                cookiestr = cookiestr.substring(0, cookiestr.length() - 1);
            }

            String result = getMethod.getResponseBodyAsString();
            responseMap.put("responseCookie", cookiestr);
            responseMap.put("responseContext", result);
        } catch (IOException e) {
            e.printStackTrace();
        }

        getMethod.releaseConnection();

        return responseMap;
    }


}
