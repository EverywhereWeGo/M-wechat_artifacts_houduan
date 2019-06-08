package com.c_SpliderWechat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.b_util.DBUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.b_util.GetUrlPic.getpic;
import static com.b_util.HttpClientHelper.sendGet;

public class SpliderWechat {
    public static void picToLocal(String result) {
        JSONArray jsonArray = JSON.parseArray(result);
        for (int j = 0; j < jsonArray.size(); j++) {
            String cover = jsonArray.getJSONObject(j).getJSONObject("app_msg_ext_info").getString("cover");
            String id = jsonArray.getJSONObject(j).getJSONObject("comm_msg_info").getString("id");
            if (null == cover) {
                cover = jsonArray.getJSONObject(j).getJSONObject("app_msg_ext_info").getJSONArray("multi_app_msg_item_list").getJSONObject(0).getString("cover");
            }
            getpic(cover, id);
        }
        System.out.println("图片下载完毕");
    }


    public static void listToMysql(String datasource, List<JSONObject> needlist) {
        Connection conn = DBUtil.getConnection();
        try {
            conn.setAutoCommit(false);
            String sql = "INSERT INTO jsonarray_data "
                    + "(id,article_date,article_source,article_jsonarray,title) "
                    + "VALUES "
                    + "(?,?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            for (int i = 0; i < needlist.size(); i++) {
                String id = "1";
                String article_date = needlist.get(i).getString("datetime");
                String article_source = datasource;
                String article_jsonarray = needlist.get(i).getString("jsonarray");
                String title = needlist.get(i).getString("title");
                ps.setString(1, id);
                ps.setString(2, article_date);
                ps.setString(3, article_source);
                ps.setString(4, article_jsonarray);
                ps.setString(5, title);
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        System.out.println("入库完毕");
    }

    public static List<JSONObject> getInfoList(String msgList) {
        List<JSONObject> resultlist = new LinkedList<JSONObject>();
        JSONArray jsonArray = JSON.parseArray(msgList);
        for (int i = 0; i < 3; i++) {
            JSONObject resultjson = new JSONObject();
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String title = jsonObject.getJSONObject("app_msg_ext_info").getString("title");
            String datetime = jsonObject.getJSONObject("comm_msg_info").getString("datetime");
            resultjson.put("title", title);
            resultjson.put("datetime", datetime);
            resultjson.put("jsonarray", jsonObject.toString());
            resultlist.add(resultjson);
        }
        return resultlist;

    }

    public static List<String> getLasttimeArticleTitle() {
        Connection conn = DBUtil.getConnection();
        try {
            String sql = "select title from jsonarray_data";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String title = rs.getString("title");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public static void resultStrToMysql(String datasource, String htmlstr) {
        getLasttimeArticleTitle();
        List<JSONObject> infolist = getInfoList(htmlstr);
        listToMysql(datasource, infolist);
    }

    public static String startThreeTimeAccess(String urlname) {
        String urls = null;
        try {
            urls = URLEncoder.encode(urlname, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url1 = "https://weixin.sogou.com/weixin?type=1&s_from=input&query=" + urls;
        Map<String, String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put("Connection", "keep-alive");
        requestHeaders.put("Host", "weixin.sogou.com");
        requestHeaders.put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        requestHeaders.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
        requestHeaders.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");

        //第一次访问获取cookie以及url
        Map<String, String> resultUrl1 = sendGet(url1, requestHeaders);
        String responseContext = resultUrl1.get("responseContext");
        String begstr = "<a target=\"_blank\" uigs=\"account_name_0\" href=\"";
        String endstr = "\"><em><!--red_beg-->" + urlname + "<!--red_end--></em></a>";
        String linkUrl = responseContext.substring(responseContext.indexOf(begstr) + begstr.length(), responseContext.indexOf(endstr)).replace("&amp;", "&");
        //这一段由前端js得出，做了验证，拼接了url
        int a = linkUrl.indexOf("url=");
        int c = linkUrl.indexOf("&k=");
        int b = (int) Math.random() * 100;
        if (-1 != a && -1 == c) {
            String check = linkUrl.substring(a + 4 + 26 + b, a + 4 + 26 + b + 1);
            linkUrl = linkUrl + "&k=" + b + "&h=" + check;
        }
        linkUrl = "https://weixin.sogou.com" + linkUrl;
        System.out.println("linkUrl：" + linkUrl);
        String cookie = resultUrl1.get("responseCookie");
        requestHeaders.put("Cookie", cookie);
        requestHeaders.put("Referer", url1);


        //第二次带cookie访问
        Map<String, String> resultUrl2 = sendGet(linkUrl, requestHeaders);
        String spliturlr = resultUrl2.get("responseContext");
        String trueUrl = "";
        String[] spsstr = spliturlr.split(";");
        for (int i = 1; i < spsstr.length - 2; i++) {
            trueUrl = trueUrl + spsstr[i].substring(spsstr[i].indexOf("'") + 1, spsstr[i].lastIndexOf("'"));
        }
        System.out.println("trueUrl:" + trueUrl);

        //第三次访问真正的url
        Map<String, String> resultUrl3 = sendGet(trueUrl, null);
        String htmlstr = resultUrl3.get("responseContext");
        String resultStr = "";
        if (htmlstr.contains("为了保护你的网络安全，请输入验证码")) {
            resultStr = "";
        } else {
            resultStr = htmlstr.substring(htmlstr.indexOf("var msgList = ") + 22, htmlstr.indexOf("seajs.use") - 11);
        }
        System.out.println("context：" + resultStr);
        return resultStr;

    }


    public static void startSplider() {
        String wechatNames[] = {
                "程序员小灰",
                "码农翻身",
                "码农有道",
                "网络大数据"
        };
        for (int i = 0; i < wechatNames.length; i++) {
            String wechatName = wechatNames[i];
            String result = startThreeTimeAccess(wechatName);
            try {
                //每次间隔10min
                Thread.sleep(5 * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if ("".equals(result)) {
                System.out.println(wechatName + "抓取失败");
                continue;
            }
            resultStrToMysql(wechatName, result);
            picToLocal(result);

        }
    }


//    public static void main(String args[]) throws IOException {
//        String urls[] = {
//                "程序员小灰",
//                "码农翻身",
//                "码农有道"
//        };
//        System.out.println("urls.length:" + urls.length);
//        for (int i = 0; i < urls.length; i++) {
//            String url = urls[i];
//            String result = HttpClientHelper.asdfasdfsfasdfa(url);
//            List<JSONObject> needlist = getInfoList(result);
//            listToMysql(String.valueOf(i), needlist);
//            picToLocal(result);
//        }
//    }


}