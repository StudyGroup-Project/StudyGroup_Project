**7\. Implementation requirements**

 

Development Environment

Focus는 클라이언트와 서버가 분리된 구조로 설계되며, 각 영역의 개발 환경은 다음과 같다.

* Backend

  * Framework: Spring Boot 3.x (Java 17\)

  * Build Tool: Gradle

  * Database: MySQL 8.x

  * ORM: Spring Data JPA \+ QueryDSL

  * Authentication: Spring Security \+ OAuth2 \+ JWT

  * Cloud Storage: AWS S3

  * API Documentation: Spring REST Docs

  * IDE: IntelliJ IDEA Ultimate

* Frontend

  * Framework: React 18

  * Language: JavaScript

  * IDE: VS Code 

 

 

**8\. Glossary**

 본 장에서는 Study Group System(Focus)에서 사용되는 주요 용어와 약어의 의미를 명확히 정의한다. 모든 참여자는 아래 정의를 공통된 기준으로 사용하여 문서 해석의 일관성을 유지한다.

| 용어 / 약어 | 정의 |
| :---- | :---- |
| Focus | Study Group System의 이름으로, 사용자가 스터디 그룹을 생성하고 참여하여 학습을 관리할 수 있는 플랫폼을 의미한다. |
| User (사용자) | 시스템에 로그인하여 서비스를 이용하는 모든 일반 사용자를 의미한다. |
| Study Group (스터디 그룹) | 동일한 학습 목적을 가진 사용자들이 모여 활동하는 단위로, 그룹장은 그룹 생성 및 운영을 담당한다. |
| Study Leader (그룹장) | 스터디 그룹을 생성하거나 관리할 수 있는 사용자로, 공지 작성, 과제 등록, 그룹원 승인 등의 권한을 가진다. |
| Study Member (그룹원) | 스터디 그룹에 가입하여 활동하는 일반 구성원으로, 공지 확인, 과제 제출, 자료 열람 등의 기능을 수행할 수 있다. |
| OAuth2 | 외부 인증 제공자(Google, Kakao 등)를 통해 로그인할 수 있도록 하는 표준 인증 프로토콜. |
| JWT (JSON Web Token) | 사용자 인증 정보를 안전하게 전달하기 위한 토큰 기반 인증 방식. |
| DTO (Data Transfer Object) | 계층 간 데이터 교환을 위해 사용하는 객체. Controller와 Service 간 데이터 전달에 사용된다. |
| API (Application Programming Interface) | 서버와 클라이언트 간 데이터 통신을 위한 표준 인터페이스. |
| S3 (Amazon Simple Storage Service) | AWS에서 제공하는 클라우드 스토리지 서비스로, 이미지 및 파일 업로드에 사용된다. |
| REST API | HTTP 기반의 통신 규약을 따르는 API로, 자원(Resource)을 중심으로 CRUD 동작을 수행한다. |
| Token Refresh | 만료된 JWT 토큰을 재발급받기 위한 절차. |
| Profile | 사용자의 기본 정보(이름, 이메일, 이미지 등)를 포함한 개인화 데이터. |
| Post | 그룹 내 공지사항 또는 게시글 단위를 의미한다. |
| Assignment (과제) | 스터디 내 학습 진행을 위한 과제나 제출물을 의미하며, 그룹장이 등록하고 그룹원이 제출한다. |
| Notification (알림) | 시스템 내 주요 이벤트 발생 시 사용자에게 전달되는 정보. |
| QueryDSL | 타입 안정성을 보장하는 SQL 빌더 라이브러리로, JPA 기반의 동적 쿼리 생성을 지원한다. |
| CI/CD | 지속적 통합(Continuous Integration) 및 지속적 배포(Continuous Deployment)를 의미하며, 자동 빌드와 배포 환경을 구성한다. |


