// Create this file: src/main/java/com/mediconnect/config/ApplicationContextProvider.java

package com.mediconnect.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextProvider implements ApplicationContextAware {
    
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static <T> T getBean(Class<T> beanClass) {
        try {
            return context.getBean(beanClass);
        } catch (Exception e) {
            return null; // Return null if bean not found
        }
    }

    public static ApplicationContext getApplicationContext() {
        return context;
    }
}