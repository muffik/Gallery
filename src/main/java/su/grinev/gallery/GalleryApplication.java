package su.grinev.gallery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.util.unit.DataSize;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.MultipartConfigElement;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan
@EnableSwagger2
public class GalleryApplication {
	@Bean
	MultipartConfigElement multipartConfigElement() {
		MultipartConfigFactory factory = new MultipartConfigFactory();
		factory.setMaxFileSize(DataSize.parse("20MB"));
		factory.setMaxRequestSize(DataSize.parse("20MB"));
		return factory.createMultipartConfig();
	}
	public static void main(String[] args) {
		SpringApplication.run(GalleryApplication.class, args);
	}

}
