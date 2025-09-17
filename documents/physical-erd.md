# 🗄️ Physical ERD (DB Schema)

## 1. 전체 구조
<img width="940" height="601" alt="Image" src="https://github.com/user-attachments/assets/8cc9fbb9-61fd-4929-ad00-0dad3aca3fcf" />

---

## 2. 영역별 설명

### 2.1 사용자 계정 영역
<img width="940" height="349" alt="Image" src="https://github.com/user-attachments/assets/57975fdc-22b4-48c4-b75c-79157a63c5f6" />

| 테이블 | 설명 |
|--------|------|
| **User** | 기본 사용자 정보 (생성일시, 최근 접속일시, 신뢰점수) |
| **UserProfile** | 닉네임, 지역, 선호 카테고리 등 확장 프로필 |
| **SystemCredential / OAuthCredential** | 로그인 인증 수단 (자체 계정, 외부 OAuth) |

---

### 2.2 스터디 기본 영역
<img width="940" height="362" alt="Image" src="https://github.com/user-attachments/assets/c55023f8-c3da-4fb5-8a89-bfe264cc0aec" />

| 테이블 | 설명 |
|--------|------|
| **Study** | 스터디 그룹의 기본 속성 (최대 인원, 상태 등) |
| **StudyProfile** | 스터디 소개/설명, 대표 이미지 등 |
| **StudyMember** | 사용자 ↔ 스터디 관계 (역할, 가입일, 상태) |
| **Bookmark** | 사용자가 스터디를 찜해둔 기록 |

---

### 2.3 지원/알림 영역
<img width="940" height="239" alt="Image" src="https://github.com/user-attachments/assets/80fdd1ad-c155-4916-8928-ef7e1b3fcb96" />

| 테이블 | 설명 |
|--------|------|
| **Application** | 스터디 참여 지원서 (내용, 상태, 제출일) |
| **Notification** | 알림 내역 (대상, 내용, 생성일, 수신자 범위) |

---

### 2.4 과제/제출/피드백 영역
<img width="710" height="754" alt="Image" src="https://github.com/user-attachments/assets/e70b91c7-363a-4002-b56b-3557bc075d0a" />

| 테이블 | 설명 |
|--------|------|
| **Assignment** | 과제 정보 (제목, 설명, 기간, 생성자) |
| **Submission** | 스터디 멤버가 제출한 과제물 |
| **Feedback** | 제출물에 대한 평가 (점수, 코멘트, 평가자) |

---

### 2.5 자료/파일 영역
<img width="782" height="592" alt="Image" src="https://github.com/user-attachments/assets/77a75e62-bbbc-4014-8fc5-edf833a7bb6c" />

| 테이블 | 설명 |
|--------|------|
| **Resource** | 스터디 내 공유 자료 (제목, 설명, 작성자) |
| **File** | 업로드된 실제 파일 정보 (이름, 크기, MIME 타입, 연결 대상) |

---

### 2.6 공지/채팅 영역
<img width="940" height="325" alt="Image" src="https://github.com/user-attachments/assets/ee3c9c61-c89d-4ebe-a3e4-b7e323e9768f" />

| 테이블 | 설명 |
|--------|------|
| **Announcement** | 스터디 공지사항 (제목, 내용, 생성/수정 시각) |
| **Comment** | 공지사항에 대한 댓글 (작성자, 내용) |
| **ChatMessage** | 스터디 실시간 채팅 메시지 기록 (작성자, 내용, 전송 시각) |

---

> 🔗 [원본 ERD-Cloud 문서](https://www.erdcloud.com/d/m2AJex8cqWfsRQK52) (권한 필요)