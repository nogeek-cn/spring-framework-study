package com.darian.spring.demo.service.impl;


import com.darian.spring.demo.service.ModifyService;
import com.darian.spring.framework.annotation.DarianService;

@DarianService
public class ModifyServiceImpl implements ModifyService {

    /**
     * 增加
     */
    public String add(String name,String addr) {
        return "modifyService add,name=" + name + ",addr=" + addr;
    }

    /**
     * 修改
     */
    public String edit(Integer id,String name) {
        return "modifyService edit,id=" + id + ",name=" + name;
    }

    /**
     * 删除
     */
    public String remove(Integer id) {
        return "modifyService id=" + id;
    }



}
