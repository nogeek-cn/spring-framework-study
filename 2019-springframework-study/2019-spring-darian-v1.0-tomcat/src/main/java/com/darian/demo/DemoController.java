package com.darian.demo;

import com.darian.mvc.v1.annotation.DarianAutowrited;
import com.darian.mvc.v1.annotation.DarianController;
import com.darian.mvc.v1.annotation.DarianRequestMapping;
import com.darian.mvc.v1.annotation.DarianRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger;

@DarianController
@DarianRequestMapping("/demo")
public class DemoController {
    Logger LOGGER = Logger.getLogger(DemoController.class.getName());

    @DarianAutowrited
    private DemoService demoService;

    @DarianRequestMapping("/query")
    public String query(HttpServletRequest req, HttpServletResponse resp,
                        @DarianRequestParam("name") String name) {
        LOGGER.info("request--name:[" + name + "]");
        String result = demoService.get(name);
        LOGGER.info("response-result:[" + result + "]");
        return result;
    }

    @DarianRequestMapping("/remove")
    public String remove(@DarianRequestParam("id") Integer id) {
        return "" + id;
    }
}
