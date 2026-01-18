package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(
	 exclude = {
             org.springframework.cloud.aws.autoconfigure.context.ContextInstanceDataAutoConfiguration.class,
             org.springframework.cloud.aws.autoconfigure.context.ContextStackAutoConfiguration.class,
             org.springframework.cloud.aws.autoconfigure.context.ContextRegionProviderAutoConfiguration.class
     }
)
@EnableFeignClients
public class CognitoAuthAndTokenValidateApplication {
	public static void main(String[] args) {
		SpringApplication.run(CognitoAuthAndTokenValidateApplication.class, args);
		System.out.println("Application Running....................");

	}

	@Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}