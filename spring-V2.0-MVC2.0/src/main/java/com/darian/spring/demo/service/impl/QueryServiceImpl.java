package com.darian.spring.demo.service.impl;

import com.darian.spring.demo.service.QueryService;

import java.text.SimpleDateFormat;
import java.util.Date;

public class QueryServiceImpl implements QueryService {
    /**
     * 查询
     */
    @Override
    public String query(String name) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(new Date());
        String json = "{name:\"" + name + "\",time:\"" + time + "\"}";
        return json;
    }
}
