package com.ae.sat.master;

import com.ae.sat.preprocessor.common.file.PersistenceAccess;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

@Import(PersistenceAccess.class)
@EnableScheduling
@EnableAsync
@SpringBootApplication
public class App  {

	@Bean
	public AsyncTaskExecutor asyncTaskExecutor() {
		SimpleAsyncTaskExecutor simpleAsyncTaskExecutor =  new SimpleAsyncTaskExecutor();
		return simpleAsyncTaskExecutor;
	}

	@Bean
	public CommonsMultipartResolver multipartResolver() {
		CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
		commonsMultipartResolver.setMaxUploadSize(-1);
		return commonsMultipartResolver;
	}

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
}
