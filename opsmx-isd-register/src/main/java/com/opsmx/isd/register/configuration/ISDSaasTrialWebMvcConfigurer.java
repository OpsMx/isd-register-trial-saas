package com.opsmx.isd.register.configuration;

import com.opsmx.isd.register.interceptors.ISDSaasTrialInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ISDSaasTrialWebMvcConfigurer implements WebMvcConfigurer {

    @Autowired
    private ISDSaasTrialInterceptor isdSaasTrialInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(isdSaasTrialInterceptor).addPathPatterns("/webhookTrigger/{cdType}");
    }
}
