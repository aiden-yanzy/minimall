package com.minimall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Minimall 单商户商城后端启动类
 */
@SpringBootApplication
@MapperScan("com.minimall.module")
public class MinimallApplication {

    public static void main(String[] args) {
        SpringApplication.run(MinimallApplication.class, args);
        System.out.println("""

                ============================================
                     Minimall Server 启动成功 ✓
                     接口文档: http://localhost:8080/swagger-ui.html
                ============================================
                """);
    }
}
