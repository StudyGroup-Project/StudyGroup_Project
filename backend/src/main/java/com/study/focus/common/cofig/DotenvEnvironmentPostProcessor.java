package com.study.focus.common.cofig;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String DOTENV_PROPERTY_SOURCE_NAME = "dotenvProperties";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            // 현재 애플리케이션의 작업 디렉토리 (backend 폴더)에서 .env 파일을 찾습니다.
            File dotenvFile = new File(System.getProperty("user.dir"), ".env");

            if (!dotenvFile.exists() || !dotenvFile.isFile()) {
                System.err.println("### .env 파일을 찾을 수 없습니다. 경로: " + dotenvFile.getAbsolutePath());
                return; // 파일이 없으면 더 이상 진행하지 않고 종료
            }

            // Dotenv.configure()를 사용하여 .env 파일을 로드합니다.
            // .directory()는 .env 파일이 위치한 디렉토리를 지정하고, .filename()은 파일 이름을 지정합니다.
            Dotenv dotenv = Dotenv.configure()
                    .directory(dotenvFile.getParent())
                    .filename(dotenvFile.getName())
                    .load();

            Map<String, Object> dotenvMap = new HashMap<>();
            dotenv.entries().forEach(entry -> dotenvMap.put(entry.getKey(), entry.getValue()));

            if (!dotenvMap.isEmpty()) {
                environment.getPropertySources().addLast(new MapPropertySource(DOTENV_PROPERTY_SOURCE_NAME, dotenvMap));
                System.out.println("### .env 파일에서 환경 변수 로드 성공: " + dotenvMap.keySet());
            } else {
                System.out.println("### .env 파일을 로드했지만, 포함된 환경 변수가 없습니다.");
            }

        } catch (Exception e) { // 그 외 모든 예외 처리
            System.err.println("### DotenvEnvironmentPostProcessor 실행 중 예상치 못한 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}