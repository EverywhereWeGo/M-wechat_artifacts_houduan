package com.a_controller;

import com.b_util.DBUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@RestController
public class HelloController {

    @RequestMapping("/getList")
    public String index() throws SQLException, URISyntaxException, IOException {
        String resultstr = "";
        Connection conn = DBUtil.getConnection();
        conn.setAutoCommit(false);
        String sql = "Select * from jsonarray_data";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            String author = rs.getString(2);
            author = author + ",";
            resultstr += author;
        }
        resultstr = resultstr.substring(0, resultstr.length() - 1);
        System.out.println(resultstr);
        return "[" + resultstr + "]";

    }

}