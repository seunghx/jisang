package com.jisang.web;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class LoggingHandlerInterceptor extends HandlerInterceptorAdapter {
    private final Logger logger = LoggerFactory.getLogger(LoggingHandlerInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (logger.isInfoEnabled() && handler instanceof HandlerMethod) {
            Method handlerMethod = ((HandlerMethod) handler).getMethod();
            logger.info("Starting handling request.");
            logger.info("Controller : {}#{}", handlerMethod.getDeclaringClass().getSimpleName(),
                    handlerMethod.getName());
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        if (logger.isInfoEnabled() && handler instanceof HandlerMethod) {
            Method handlerMethod = ((HandlerMethod) handler).getMethod();
            logger.info("Handling request succeeded.");
            logger.info("Controller : {}#{}", handlerMethod.getDeclaringClass().getSimpleName(),
                    handlerMethod.getName());
        }
    }
}
