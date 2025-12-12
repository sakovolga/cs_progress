package com.example.cs_progress;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.example.cs_progress",
        "com.example.cs_common"
})
@EnableRabbit
public class CsProgressApplication {

	public static void main(String[] args) {
		SpringApplication.run(CsProgressApplication.class, args);
	}

}
