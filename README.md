#  📖 포커스(FOCUS) - **Study Group Project**



##  :star: 프로젝트 소개
오프라인 스터디를 보다 손쉽게 모집하고 관리할 수 있는 웹 애플리케이션입니다. 사용자는 관심 분야별로 스터디 그룹을 개설하거나 참여할 수 있으며, 각 그룹 내에서 과제를 공유하고 피드백을 주고 받으며 개인의 성장을 도모할 수 있습니다.  또한 실시간 채팅 기능을 통해 친목을 다지고, 함께 성장할 수 있는 협력형 학습 생태계를 제공합니다.

<p align ="center">
<img src="./Focus_Logo.png" alt="프로젝트 로고" width="400" height="400">
</p>


---

## 🧑‍🤝‍🧑 팀원 소개
| 이름 | 역할 | 담당 분야 | GitHub |
|------|------|-----------|--------|
| **이시형** | BE | AWS, Announcement, Comment, Resource | [SHNAME](https://github.com/SHNAME) |
| **이현철** | BE | User, ChatMessage, DB | [sevencomma](https://github.com/sevencomma) |
| **강보성** | BE | Study, Bookmark, Application, AWS EC2 | [kang-bs](https://github.com/kang-bs) |
| **장세헌** | BE | Assignment, Notification, Submission, Feedback | [jangseheon](https://github.com/jangseheon) |
| **서주형** | FE | Frontend 개발 | [wnwngud](https://github.com/wnwngud) |
| **권정균** | FE | Frontend 개발 | [gyunii](https://github.com/gyunii) |

---

## ✅ 역할 분담
### 이시형
- **공지(Announcement):** 공지 제목 List, 세부 데이터, 생성, 수정, 삭제 시스템 구현
- **공지댓글(Comment):** 공지 세부화면 댓글 작성 구현
- **자료(Resource): ** 자료 제목 List, 상세 데이터, 생성, 수정, 삭제 구현
- **파일(AWS) :** AWS S3를 통한 파일 업로드/다운로드 관리, 권한 설정 및 파일 백업 시스템을 구축
- **스케줄러(Scheduler):** 예약된 파일을 삭제하기 위한 스케줄러 시스템 구축

### 이현철
- **유저(User):** ouath 로그인, 일반 로그인, 회원가입 구현(JWT Token)
- **채팅방(ChatMessage):** 실시간 채팅방 시스템 구현
- **DB :** 엔티티 작성 

### 강보성
- **스터디 그룹(Study) :** 그룹 검색 요청, 상위 스터디 가져오기, 생성, 삭제, 탈퇴, 인원 추방, 프로필 정보 수정 구현
- **그룹 찜(Bookmark) :** 그룹 찜하기, 찜 해제하기를 구현
- **지원서 (Application) :** 지원서 생성하기, 가져오기를 구현
- **CI/CD :** 지속적 통합 및 배포를 위한 인프라 구축

### 장세헌
- **과제 (Assignment) :** 과제 제목 List, 상세 내용, 생성, 수정, 삭제, 제출 시스템 구현
- **과제 제출물 (Submission) :** 제출물 list, 상세보기 시스템을 구현
- **과제 피드백 (Feedback) :** 과제 평가, 평가 목록을 구현
- **알림(Notification):** 알림 제목 list, 세부 데이터 가져오기 등 알림 시스템을 구현
  

---

## 📂 프로젝트 핵심 구조
<pre>
src
├─main
│  ├─java
│  │  └─com.study.focus
│  │     ├─account         # 회원 인증·인가, JWT/OAuth 설정
│  │     ├─announcement    # 공지사항 
│  │     ├─assignment      # 과제
│  │     ├─application     # 스터디 지원/신청 관리
│  │     ├─chat            # 실시간 채팅 및 메시징
│  │     ├─notification    # 알림 발송 및 수신 관리
│  │     ├─resource        # 자료
│  │     ├─study           # 스터디 생성, 멤버 등 스터디 전체 관리 
│  │     ├─home            # 메인 페이지
│  │     └─common          # 공통 설정, 예외 처리, 유틸, 공용 DTO
│  └─resources
│     ├─static
│     └─application.yml
└─test
   └─java.com.study.focus  # 단위/통합 테스트

  
</pre>
---
## 🚀 기술 스택

| 분야 | 기술 |
|------|----------------------|
| **Frontend** | React , Figma|
| **Backend** | Java, Spring Boot, Spring Security |
| **Database** | MySQL 8.0.43 |
| **Infra** | AWS EC2 |
| **Version Control** | GitHub |
| **File Resource** | AWS S3|

---


## 💾 커밋 컨벤션

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

##  🗺️ 프로젝트 아키텍처
<img width="1176" height="1044" alt="스크린샷 2025-09-14 161957" src="https://github.com/user-attachments/assets/fac6178b-0e16-4980-aeb9-2d0765610285" />


---
## 📑  주요 기능
- **관심별 스터디 그룹 모집**
관심있는 스터디 그룹을 손쉽게 찾아서 지원할 수 있다.
<img width="945" height="998" alt="스터디 1" src="https://github.com/user-attachments/assets/8a094bf7-a6e3-4c06-84bb-830885cbe608" />


- **과제를 통한 평가 및 피드백**
스터디에서 올라온 과제를 다른 사람들이 평가하고 피드백할 수 있도록 한다.
<img width="592" height="752" alt="스터디2" src="https://github.com/user-attachments/assets/b32c1d86-0054-4f62-b174-2e593c2598cf" />

-**공지로 전부에게 알림**
스터디 그룹에서 공지로 사람들에게 전달내용이 있으면 전달하고 그룹원들은 댓글을 달 수 있음.
<img width="548" height="562" alt="스터디3" src="https://github.com/user-attachments/assets/6dcfab8a-c0ec-465d-958f-a9a90d15afd1" />









