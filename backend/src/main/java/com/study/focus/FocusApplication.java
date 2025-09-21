package com.study.focus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@EnableJpaAuditing
@SpringBootApplication
public class FocusApplication {

	public static void main(String[] args) {
		SpringApplication.run(FocusApplication.class, args);
	}

	// --- 👇 이 부분을 클래스 안에 추가하세요 ---
	@Controller
	public static class HomeController {

		// "/" 경로, 즉 가장 기본 주소로의 요청을 처리합니다.
		@GetMapping("/")
		@ResponseBody // 이 메서드가 반환하는 문자열을 HTML로 그대로 응답합니다.
		public String home() {
			return """
                    <html>
                        <head>
                            <title>배포 성공!</title>
                            <style>
                                body { font-family: sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background: linear-gradient(135deg, #72EDF2 10%, #5151E5 100%); }
                                div { text-align: center; padding: 50px; border-radius: 15px; background-color: rgba(255, 255, 255, 0.9); box-shadow: 0 8px 16px rgba(0,0,0,0.2); }
                                h1 { font-size: 2.5em; color: #333; }
                                p { color: #555; }
                            </style>
                        </head>
                        <body>
                            <div>
                                <h1>🎉 축하합니다! 🎉</h1>
                                <p>StudyGroup_Project 배포가 성공적으로 완료되었습니다.</p>
                            </div>
                        </body>
                    </html>
                   """;
		}
	}
	// --- 👆 여기까지 추가 ---
}
