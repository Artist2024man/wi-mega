
package com.wuin.wi_mega;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@Slf4j
@EnableScheduling
@EnableAsync
@SpringBootApplication
@MapperScan({"com.wuin.wi_mega.**.mapper"})
@EnableCaching
public class WiMegaApplication {

    public static void main(String[] args) {
        SpringApplication.run(WiMegaApplication.class, args);
        System.out.println("==================== ~~~WiMegaApplication start ok~~~ ====================");
    }
}
