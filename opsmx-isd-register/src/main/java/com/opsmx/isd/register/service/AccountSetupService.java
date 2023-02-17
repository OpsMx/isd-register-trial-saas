package com.opsmx.isd.register.service;

import com.opsmx.isd.register.dto.DatasourceRequestModel;
import com.opsmx.isd.register.dto.DatasourceResponseModel;
import com.opsmx.isd.register.enums.CDType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("AccountSetupService-v1")
public interface AccountSetupService {
    DatasourceResponseModel setup(DatasourceRequestModel datasourceRequestModel);
    DatasourceResponseModel setup(DatasourceRequestModel datasourceRequestModel, CDType cdType);
    @Transactional
    void store(DatasourceRequestModel datasourceRequestModel);

    @Transactional
    void store(DatasourceRequestModel datasourceRequestModel, CDType cdType);
}
