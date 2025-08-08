package com.dm.DM_Backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DmBackendApplication {

	public static void main(String[] args) {
        // .env 파일 로드
        Dotenv dotenv = Dotenv.configure()
                .directory(System.getenv().getOrDefault("DOTENV_PATH", "./")) // 환경변수 있으면 사용, 없으면 로컬 기본
                .filename(".env")
                .ignoreIfMalformed() // 에러 방지 옵션
                .ignoreIfMissing()   // 파일 없을 경우 무시
                .load();
        System.setProperty("AWS_ACCESS_KEY_ID", dotenv.get("AWS_ACCESS_KEY_ID"));
        System.setProperty("AWS_SECRET_ACCESS_KEY", dotenv.get("AWS_SECRET_ACCESS_KEY"));
        System.setProperty("JWT_SECRET_KEY", dotenv.get("JWT_SECRET_KEY"));
        System.setProperty("POSTGRES_HOST", dotenv.get("POSTGRES_HOST"));
        System.setProperty("POSTGRES_USERNAME", dotenv.get("POSTGRES_USERNAME"));
        System.setProperty("POSTGRES_PASSWORD", dotenv.get("POSTGRES_PASSWORD"));
        System.setProperty("REDPANDA_HOST", dotenv.get("REDPANDA_HOST"));
        System.setProperty("GOOGLE_CLIENT_ID", dotenv.get("GOOGLE_CLIENT_ID"));
        System.setProperty("GOOGLE_CLIENT_SECRET", dotenv.get("GOOGLE_CLIENT_SECRET"));
        System.setProperty("KAKAO_CLIENT_ID", dotenv.get("KAKAO_CLIENT_ID"));
        System.setProperty("NAVER_CLIENT_ID", dotenv.get("NAVER_CLIENT_ID"));
        System.setProperty("NAVER_CLIENT_SECRET", dotenv.get("NAVER_CLIENT_SECRET"));
        System.setProperty("MAIL_APP_PASSWORD", dotenv.get("MAIL_APP_PASSWORD"));

        SpringApplication.run(DmBackendApplication.class, args);
	}

}
