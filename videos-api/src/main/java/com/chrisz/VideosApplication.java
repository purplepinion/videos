package com.chrisz;

import tk.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan(basePackages = {"com.chrisz.mapper"})
@ComponentScan(basePackages = {"com.chrisz","org.n3r.idworker"})
public class VideosApplication {

	public static void main(String[] args) {
		SpringApplication.run(VideosApplication.class, args);
	}

}
