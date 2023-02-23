package com.opsmx.isd.register.interceptors;

import com.opsmx.isd.register.exception.AccessForbiddenException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class ISDSaasTrialInterceptorTest {

    @Spy
    @InjectMocks
    private ISDSaasTrialInterceptor isdSaasTrialInterceptor;

    private static final String apiKey = "validTestApiKey";

    @BeforeEach
    public void beforeTest(){
        ReflectionTestUtils.setField(isdSaasTrialInterceptor, "apiKey", apiKey);
    }

    @Test
    @DisplayName("Should throw Access Forbidden if the user tries to access the API without a valid API key")
    void test1() throws Exception{

        HttpServletRequest httpServletRequest = new MockHttpServletRequest();
        HttpServletResponse httpServletResponse = new MockHttpServletResponse();

        assertThrows(AccessForbiddenException.class, ()-> isdSaasTrialInterceptor.preHandle(httpServletRequest, httpServletResponse, null));
    }

    @Test
    @DisplayName("Should succeed when a valid API key is passed in the header")
    void test2() throws Exception{

        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        HttpServletResponse httpServletResponse = new MockHttpServletResponse();
        httpServletRequest.addHeader("x-api-key", apiKey);
        boolean flag = isdSaasTrialInterceptor.preHandle(httpServletRequest, httpServletResponse, null);

        assertEquals(Boolean.TRUE, flag);
    }
}
