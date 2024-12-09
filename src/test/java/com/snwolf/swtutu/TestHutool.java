package com.snwolf.swtutu;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="https://github.com/SnowWolf68">SnowWolf68</a>
 * @Version: V1.0
 * @Date: 12/9/2024
 * @Description:
 */
public class TestHutool {

    @Test
    public void test() {
        StrUtil.toCamelCase("");
        IdUtil.fastSimpleUUID();
    }
}
