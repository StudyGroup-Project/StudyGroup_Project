FROM openjdk:17-jdk-slim

# 2. 컨테이너 내 작업 디렉토리 설정
WORKDIR /app

# 3. Gradle로 빌드된 JAR 파일을 컨테이너 내부로 복사
# build/libs/ 경로에 있는 .jar 파일을 app.jar 라는 이름으로 복사합니다.
COPY build/libs/*.jar app.jar

# 5. 컨테이너가 시작될 때 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]