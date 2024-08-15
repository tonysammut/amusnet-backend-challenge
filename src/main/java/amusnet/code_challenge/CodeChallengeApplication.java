package amusnet.code_challenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class CodeChallengeApplication {
	public static void main(String[] args) {
		SpringApplication.run(CodeChallengeApplication.class, args);
	}
}
