package com.darian.spring.orm.demo.dao;


import com.darian.spring.orm.demo.model.User;
import com.darian.spring.orm.framework.BaseDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * @author Darian
 */
@Repository
public class UserDao extends BaseDaoSupport<User,Integer> {

    @Override
    protected String getPKColumn() {return "id";}

    @Resource(name="dynamicDataSource")
    protected void setDataSource(DataSource dataSource) {

    }
}
