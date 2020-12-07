package com.perfree.config;

import com.jfinal.template.Engine;
import com.jfinal.template.ext.spring.JFinalViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnjoyConfig{

    public static JFinalViewResolver jfr = null;

    @Bean(name = "jfinalViewResolver")
    public JFinalViewResolver getJFinalViewResolver() {
        jfr = new JFinalViewResolver();
        jfr.setSuffix(".html");
        jfr.setContentType("text/html;charset=UTF-8");
        jfr.setOrder(0);
        jfr.setSessionInView(true);
        Engine engine  = JFinalViewResolver.engine;
        engine.setDevMode(true);
        engine.setToClassPathSourceFactory();
        engine.setBaseTemplatePath(null);
        return jfr;
    }
}
