package com.example.component.cache;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Author：cedo
 * Date：2020/8/9 0:50
 */
@RunWith(SpringRunner.class)
//配置本地随机端口，服务器会选择一个空闲的端口使用，避免端口冲突
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BaseTest {
}
