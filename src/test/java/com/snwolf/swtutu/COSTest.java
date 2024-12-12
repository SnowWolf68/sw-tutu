package com.snwolf.swtutu;

import com.qcloud.cos.model.PutObjectResult;
import com.snwolf.swtutu.configuration.CosClientConfig;
import com.snwolf.swtutu.manager.COSManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
@Slf4j
public class COSTest {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSManager cosManager;

    @Test
    public void test(){
        log.info(cosClientConfig.getSecretId());
    }
}
