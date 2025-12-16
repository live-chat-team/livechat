package kr.sparta.livechat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class LivechatApplication {

	public static void main(String[] args) {
		SpringApplication.run(LivechatApplication.class, args);
	}

}
