package com.snwolf.swtutu;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 2024/12/12 19:06
 * @Description:
 */
@Slf4j
public class FileTest {

    @Test
    public void test() throws IOException {
        File file = File.createTempFile("test", "png");
        log.info(file.getName());
    }
}
