package com.opsmx.isd.register.configuration;

import com.opsmx.isd.register.enums.CDType;
import com.opsmx.isd.register.interceptors.ISDSaasTrialInterceptor;
import com.opsmx.isd.register.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Configuration
public class ISDSaasTrialWebMvcConfigurer implements WebMvcConfigurer {

    @Autowired
    private ISDSaasTrialInterceptor isdSaasTrialInterceptor;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(isdSaasTrialInterceptor).addPathPatterns("/webhookTrigger/{cdType}");
    }

    @PostConstruct
    public void populateValuesInCdTypeColumnIfNotExists(){

        Optional<List<CDType>> cdTypes = userRepository.findCdTypes();
        if (cdTypes.isEmpty()){
            log.debug("inside if block");
            populateCdTypes();
            addNotNullConstraints();
            userRepository.dropBusinessEmailUniqueConstraint();
        } else if (cdTypes.get().stream().anyMatch(Objects::isNull)){
            log.debug("inside else if block");
            populateCdTypes();
            addNotNullConstraints();
            userRepository.dropBusinessEmailUniqueConstraint();
        }
    }

    private void populateCdTypes() {
        userRepository.updateCdType(CDType.isdSpinnaker);
    }

    private void addNotNullConstraints() {

        userRepository.addNotNullConstraintOnCDType();
        userRepository.addNotNullConstraintOnBusinessEmail();
        userRepository.addNotNullConstraintOnCompanyName();
        userRepository.addNotNullConstraintOnContactNumber();
        userRepository.addNotNullConstraintOnFirstName();
        userRepository.addNotNullConstraintOnLastName();
    }
}
