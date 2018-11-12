package com.jisang.config;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import nz.net.ultraq.thymeleaf.LayoutDialect;

@Configuration
@PropertySource("classpath:application.properties")
public class ViewConfig {

    @Autowired
    private ServletContext servletContext;
    @Autowired
    private MessageSource msgSource;

    @Value("${thymeleaf.template-mode}")
    private String thTemplateMode;

    @Value("${thymeleaf.character-encoding}")
    private String thCharEncoding;

    @Value("${thymeleaf.template-prefix}")
    private String thPrefix;
    @Value("${thymeleaf.template-suffix}")
    private String thSuffix;
    @Value("${thymeleaf.template-order}")
    private int thymeleafOrder;

    @Bean
    public ServletContextTemplateResolver templateResolver() {
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);

        templateResolver.setPrefix(thPrefix);
        templateResolver.setSuffix(thSuffix);
        templateResolver.setTemplateMode(thTemplateMode);
        templateResolver.setCharacterEncoding(thCharEncoding);
        templateResolver.setCacheable(false);

        return templateResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();

        templateEngine.setTemplateResolver(templateResolver());
        templateEngine.setTemplateEngineMessageSource(msgSource);
        templateEngine.addDialect(new LayoutDialect());

        return templateEngine;
    }

    @Bean
    public ThymeleafViewResolver thymeleafViewResolver() {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        resolver.setCharacterEncoding(thCharEncoding);
        resolver.setOrder(thymeleafOrder);

        return resolver;
    }
}
