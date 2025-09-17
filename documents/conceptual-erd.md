# 🌐 Conceptual ERD (Domain Model)

## 1. 전체 구조
<img width="628" height="589" alt="Conceptual ERD"  src="https://github.com/user-attachments/assets/b7ecf333-c96a-4c49-983a-e6f2b08c3ff4" />

---

## 2. 주요 엔티티 설명

### 👤 User (유저)
- 서비스의 기본 사용자
- 스터디 참여, 지원서 제출, 알림 수신 등 모든 활동의 주체

### 📚 Study (스터디)
- 스터디 그룹 기본 정보
- 멤버, 공지, 과제, 채팅 등과 연결되는 중심 엔티티

### 👥 StudyMember (스터디 멤버)
- User ↔ Study 다대다 관계를 표현
- 역할(방장/멤버), 상태(승인/대기/탈퇴) 관리
- 스터디 내 작성/제출 행위(공지, 과제, 채팅 등)의 주체

### 📝 Assignment (과제)
- 스터디 내 과제 정보
- 제출물(Submission)과 연결되어 학습 활동 관리

### 📢 Notice (공지)
- 스터디 공지사항
- 작성자는 StudyMember
- 댓글(Comment)로 토론 가능

### 💬 Chat (채팅)
- 스터디 멤버 간 실시간 대화 메시지
- 작성자(StudyMember), 메시지 내용, 생성 시각 포함

---

## 3. 관계 요약
- **User ↔ Study**: 다대다 (중간 엔티티 `StudyMember`)  
- **Study ↔ Assignment / Notice / Chat**: 1:N  
- **StudyMember ↔ Notice / Assignment / Chat**: 1:N (작성자/제출자 관계)  
