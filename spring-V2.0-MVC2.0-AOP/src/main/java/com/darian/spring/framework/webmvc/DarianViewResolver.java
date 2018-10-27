package com.darian.spring.framework.webmvc;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 * 设计这个类的主要目的是
 * 1. 将一个静态文件变为一个动态文件
 * 2. 根据用户传送的参数不同，产生不同的结果
 * 最终输出字符串，交给 Response 输出
 */
public class DarianViewResolver {

    private String viewName;
    private File templateFile;

    public DarianViewResolver(String viewName, File templateFile) {
        this.viewName = viewName;
        this.templateFile = templateFile;
    }

    public String viewResolver(DarianModelAndView mv) throws Exception {
        StringBuffer sb = new StringBuffer();
        RandomAccessFile ra = new RandomAccessFile(this.templateFile, "r");
        try {
            String line = null;
            while (null != (line = ra.readLine())) {
                line = new String(line.getBytes("ISO-8859-1"), "utf-8");
                Matcher m = matcher(line);
                while (m.find()) {
                    for (int i = 1; i <= m.groupCount(); i++) {
                        // 要把 ￥{} 中间的这个字符串取出来
                        String paramName = m.group(i);
                        Object paramValue = mv.getModel().get(paramName);
                        if (null == paramValue) {
                            continue;
                        }
                        line = line.replaceAll("￥\\{" + paramName + "\\}", paramValue.toString());
                        line = new String(line.getBytes("utf-8"), "ISO-8859-1");
                    }
                }
                sb.append(line);
            }
        } finally {
            ra.close();
        }

        return sb.toString();
    }

    private Matcher matcher(String str) {
        Pattern pattern = Pattern.compile("￥\\{(.+?)\\}", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        return matcher;
    }


    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

}
