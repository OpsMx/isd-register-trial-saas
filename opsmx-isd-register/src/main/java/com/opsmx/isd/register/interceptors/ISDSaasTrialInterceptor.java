package com.opsmx.isd.register.interceptors;

import com.opsmx.isd.register.exception.AccessForbiddenException;
import com.opsmx.isd.register.exception.TooManyRequestsException;
import com.opsmx.isd.register.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class ISDSaasTrialInterceptor implements HandlerInterceptor {

    @Value("${api.key}")
    String apiKey;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if(Boolean.FALSE.equals(Util.rateLimit(request))){
            throw new TooManyRequestsException("You have exceeded the 10 requests in 1 minute limit!");
        }

        String xApiKey = request.getHeader("x-api-key");
        if (xApiKey!=null && xApiKey.trim().equalsIgnoreCase(apiKey)){

            return Boolean.TRUE;
        }
        throw new AccessForbiddenException("You do not have enough privileges to access this resource");
    }
}
