package com.darian.spring.framework.context.support;

import com.darian.spring.framework.beans.DarianBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 用来对配置文件进行查找、读取、解析
 * <br>Darian
 **/
public class DarianBeanDefinitionReader {

    private Properties config = new Properties();

    private List<String> registerBeanClasses = new ArrayList<>();

    // 在配置文件中，用来获取自动扫描的包名的类
    private final String SCAN_PACKAGE = "scanPackage";

    public DarianBeanDefinitionReader(String... locations) {
        // 在 Spring 中是通过 Reader 去查找和定位的。
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(locations[0].replace("classpath:", ""));
        try {
            config.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        doScanner(config.getProperty(SCAN_PACKAGE));
    }

    public List<String> loadBeanDefinitions() {
        return this.registerBeanClasses;
    }

    // 递归扫描所有相关联的 Class, 并且保存到一个 List 中。
    private void doScanner(String packageName) {

        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.","/"));


        File classDir = new File(url.getFile());

        for (File file : classDir.listFiles()) {
            if (file.isDirectory()) {
                doScanner(packageName + "." + file.getName());
            } else {
                registerBeanClasses.add(packageName + "." + file.getName().replace(".class", ""));
            }
        }
    }

    // 每注册一个 className，就返回一个 DarianBeanDefinition，我们自己包装
    // 只是为了对配置信息进行一个包装
    public DarianBeanDefinition registerBean(String className) {
        if (this.registerBeanClasses.contains(className)) {
            DarianBeanDefinition darianBeanDefinition = new DarianBeanDefinition();
            darianBeanDefinition.setBeanClassName(className);
            darianBeanDefinition.setFactoryBeanName(
                    StringlowerFirstCase(className.substring(className.lastIndexOf(".") + 1)));
            return darianBeanDefinition;
        }
        return null;
    }

    public Properties getConfig() {
        return this.config;
    }

    /**
     * 变成小写
     **/
    private static String StringlowerFirstCase(String string) {
        char[] chars = string.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
