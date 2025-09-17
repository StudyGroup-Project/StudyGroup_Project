#  포커스(FOCUS) - 오프라인 스터디 그룹을 모집해 공부하는 웹 애플리케이션(Web App)

<img width="1024" height="1024" alt="로고" src="https://github.com/user-attachments/assets/f92258ab-0da8-4f33-aef8-cb34b2a3716d" />


## 프로젝트 소개
FOCUS를 통해서 오프라인 스터디 그룹을 각 필요에 의한 스터디별로 분류하고, 인원들을 모아 과제를 올리고 서로 피드백을 남겨서 평가하는 등, 서로의 공부를 돕기도 하고 스스로 자극도 받으면서 합격을 위한 집중력을 올릴 수 있다.

---

## 주요 기능

- **관심별 스터디 그룹 모집**
관심있는 스터디 그룹을 손쉽게 찾아서 지원할 수 있다.
<img width="945" height="998" alt="스터디 1" src="https://github.com/user-attachments/assets/8a094bf7-a6e3-4c06-84bb-830885cbe608" />


- **과제를 통한 평가 및 피드백**
스터디에서 올라온 과제를 다른 사람들이 평가하고 피드백할 수 있도록 한다.
<img width="592" height="752" alt="스터디2" src="https://github.com/user-attachments/assets/b32c1d86-0054-4f62-b174-2e593c2598cf" />

-**공지로 전부에게 알림**
스터디 그룹에서 공지로 사람들에게 전달내용이 있으면 전달하고 그룹원들은 댓글을 달 수 있음.
<img width="548" height="562" alt="스터디3" src="https://github.com/user-attachments/assets/6dcfab8a-c0ec-465d-958f-a9a90d15afd1" />


---

## 팀원 소개
| 이름 | 역할 | 담당 분야 | GitHub |
|------|------|-----------|--------|
| <img src="https://avatars.githubusercontent.com/u/176521856?v=4" width="80" height="80" /> <br/> **이시형** | BE | AWS, Announcement, Comment, Resource | [SHNAME](https://github.com/SHNAME) |
| <img src="https://avatars.githubusercontent.com/u/175857703?v=4" width="80" height="80" /> <br/> **이현철** | BE | User, ChatMessage, DB | [sevencomma](https://github.com/sevencomma) |
| <img src="https://avatars.githubusercontent.com/u/174807332?v=4" width="80" height="80" /> <br/> **강보성** | BE | Study, Bookmark, Application, AWS EC2 | [kang-bs](https://github.com/kang-bs) |
| <img src="https://avatars.githubusercontent.com/u/121509420?v=4" width="80" height="80" /> <br/> **장세헌** | BE | Assignment, Notification, Submission, Feedback | [jangseheon](https://github.com/jangseheon) |
| <img src="https://avatars.githubusercontent.com/u/131713109?v=4" width="80" height="80" /> <br/> **서주형** | FE | Frontend 개발 | [wnwngud](https://github.com/wnwngud) |
| <img src="https://avatars.githubusercontent.com/u/143116287?v=4" width="80" height="80" /> <br/> **권정균** | FE | Frontend 개발 | [gyunii](https://github.com/gyunii) |

> 팀원들 GitHub: 각 팀원 Github링크 삽입

---

## 역할 분담
### 이시형
- ** 공지(Announcement): ** 공지 제목 List, 세부 데이터, 생성, 수정, 삭제 시스템 구현했습니다.
- ** 공지댓글(Comment): ** 공지 세부화면 댓글 작성 구현했습니다.
- ** 자료(Resource): ** 자료 제목 List, 상세 데이터, 생성, 수정, 삭제 구현했습니다.
- ** 파일(AWS) : ** AWS S3를 통한 파일 업로드/다운로드 관리, 권한 설정 및 파일 백업 시스템을 구축했습니다.

### 이현철
- ** 유저(User): ** ouath 로그인, 일반 로그인, 회원가입 구현 JWT를 이용하여 보안을 더 강화했습니다.
- ** 채팅방(ChatMessage): ** 채팅방 시스템을 구현했습니다.
- ** DB : ** 엔티티 작성 

### 강보성
- ** 스터디 그룹(Study) : ** 그룹 검색 요청, 상위 스터디 가져오기, 생성, 삭제, 탈퇴, 인원 추방, 프로필 정보 수정구현
- ** 그룹 찜(Bookmark) : ** 그룹 찜하기, 찜 해제하기를 구현했습니다.
- ** 지원서 (Application) : ** 지원서 생성하기, 가져오기를 구현했습니다.
- ** AWS EC2: ** 서버 배포 및 운영

### 장세헌
- ** 과제 (Assignment) : ** 과제 제목 List, 상세 내용, 생성, 수정, 삭제, 제출 시스템 구현했습니다.
- ** 과제 제출물 (Submission) : ** 제출물 list, 상세보기 시스템을 구현했습니다.
- ** 과제 피드백 (Feedback) : ** 과제 평가, 평가 목록을 구현했습니다.
- ** 알림(Notification): ** 알림 제목 list, 세부 데이터 가져오기 등 알림 시스템을 구현했습니다.
  

---

##  프로젝트 아키텍처
<img width="1176" height="1044" alt="스크린샷 2025-09-14 161957" src="https://github.com/user-attachments/assets/fac6178b-0e16-4980-aeb9-2d0765610285" />


---

## 📂 프로젝트 구조

---

##컨벤션

### 커밋 메시지 

| Type | Description |
|------|-------------|
| **Feat** | ✨ 새로운 기능 추가 |
| **Fix** | 🐛 버그 수정 |
| **Docs** | 📝 문서 수정 (README, 주석 제외) |
| **Style** | 🎨 코드 포맷팅, 세미콜론 누락 등 코드 자체에 영향 없는 경우 |
| **Refactor** | 🔨 코드 리팩토링 (기능 변화 없음) |
| **Test** | ⚙️ 테스트 코드 추가/수정 |
| **Chore** | 🛠️ 패키지 매니저 수정, 빌드 업무, .gitignore 등 기타 변경 |
| **Design** | 💄 CSS, 사용자 UI 스타일 변경 |
| **Comment** | 💬 필요한 주석 추가/수정 |
| **Rename** | 📂 파일/폴더명 변경 및 이동만 수행한 경우 |
| **Remove** | 🗑️ 파일 삭제만 수행한 경우 |
| **!BREAKING CHANGE** | 💥 대규모 API 변경 (호환성 깨짐) |
| **!HOTFIX** | 🚑 긴급 치명적 버그 수정 |

---

## 🌳 Branch Naming 규칙

### 📌 주요 브랜치
- **main** : 항상 배포 가능한 안정적인 코드가 위치하는 브랜치   

---

### 📌 기능 개발 브랜치
- **feature/** : 새로운 기능 개발  
  - 예시: `feature/login-page`, `feature/user-profile`  

### 📌 버그 수정 브랜치
- **fix/** : 버그 수정  
  - 예시: `fix/login-error`, `fix/signup-validation`  

### 📌 긴급 수정 브랜치
- **hotfix/** : main 브랜치에서 바로 파생되어 긴급 수정 시 사용  
  - 예시: `hotfix/security-patch`  

### 📌 배포 준비 브랜치
- **release/** : 배포 전 테스트 및 안정화 브랜치  
  - 예시: `release/v1.0.0`  

---

## 🚀 기술 스택

| 분야 | 기술 |
|------|----------------------|
| **Frontend** | React , Figma|
| **Backend** | Java, Spring Boot, Spring Security |
| **Database** | MySQL 8.0.43 |
| **Infra** | AWS EC2 |
| **Version Control** | GitHub |

---
