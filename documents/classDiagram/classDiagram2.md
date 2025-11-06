3.2.6 home

![](https://github.com/StudyGroup-Project/StudyGroup_Project/blob/b1a34117ce075f5b3163118cb3e670d3bba353bd/documents/image/class%20image/%EA%B7%B8%EB%A6%BC%20%23%20class%20326%20home.png)
그림 \# class 326 home

/controller

| HomeController |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 홈 화면 데이터를 반환하는 REST 컨트롤러 클래스 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | homeService | HomeService | private final |
|  | 홈 화면 데이터를 구성하기 위한 서비스 의존성 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getHomeData | ResponseEntity\<HomeResponse\> | public |
|  | 로그인한 사용자의 홈 화면 데이터를 반환하는 메서드 |  |  |

/service

| HomeService |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 홈 화면에 표시될 사용자 정보 및 상위 스터디 목록을 구성하는 서비스 클래스 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | userProfileRepository | UserProfileRepository | private final |
|  | 사용자 프로필 데이터를 조회하기 위한 리포지토리 |  |  |
|  | studyMemberRepository | StudyMemberRepository | private final |
|  | 스터디 멤버 데이터를 조회하기 위한 리포지토리 |  |  |
|  | bookmarkRepository | BookmarkRepository | private final |
|  | 사용자의 스터디 찜 데이터를 조회하기 위한 리포지토리 |  |  |
|  | s3Uploader | S3Uploader | private final |
|  | 프로필 이미지 URL을 가져오기 위한 AWS S3 업로더 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getHomeData | HomeResponse | public |
|  | 사용자 ID를 기반으로 프로필 정보와 상위 스터디 목록을 구성하여 홈 데이터를 반환하는 메서드 |  |  |

3.2.7 notification

![](https://github.com/StudyGroup-Project/StudyGroup_Project/blob/b1a34117ce075f5b3163118cb3e670d3bba353bd/documents/image/class%20image/%EA%B7%B8%EB%A6%BC%20%23%20class%20327%20Notification-Diagram.png)

그림 \# class 327 Notification-Diagram

/domain

| AudienceType |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 알림을 볼 수 있는 타입을 정의하는 열거형(Enum) |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | ALL\_MEMBERS | AudienceType | public |
|  | 스터디 그룹의 모든 멤버가 볼 수 있는 타입 |  |  |
|  | LEADER\_ONLY | AudienceType | public |
|  | 스터디 그룹의 리더만 볼 수 있는 타입 |  |  |

| Notification |  |  |  |
| ----- | :---- | :---- | :---- |
| Class Description | 알림을 저장하는 엔티티 |  |  |
| 구분 | Name | Type | Visibility |
|  | Description |  |  |
| Attributes  | id | Long | private |
|  | 각 알림을 식별하는 고유 식별자 필드  |  |  |
|  | study | Study | private |
|  | 스터디 그룹에 대한 외래키 |  |  |
|  | actor | StudyMember | private |
|  | 알림의 행위자(어떤 멤버때문에 이 알림이 생성됐나) |  |  |
|  | audienceType | AudienceType | private |
|  | 알림의 타입 |  |  |
|  | title | String | private |
|  | 과제 제출물 점수 |  |  |
|  | description | String | private |
|  | 알림의 내용 |  |  |
| 구분 | Name | Type | Visibility |
|  | Description |  |  |
| Operations | builder | NotificationBuilder | public |
|  | 공지 객체 빌더 생성함수  |  |  |
|  | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | constructor | \- | private/protected |
|  | 엔티티 생성자 (기본 및 전체 필드 포함) |  |  |

/controller

| NotificationController |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 알림에 대한 요청을 처리하는 REST 컨트롤러 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | notificationService | NotificationService | private final |
|  | 알림에 대한 요청을 처리하는  비즈니스 클래스  |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getNotifications | ResponseEntity\<List\<GetNotificationsListResponse\>\> | public |
|  | 피드백 목록을 조회하여 반환하는 함수  |  |  |
|  | getNotificationDetail | ResponseEntity\<GetNotificationDetailResponse\> | public |
|  | 알림의 상세 데이터를 가져오는 함수 |  |  |

/repository

| NotificationRepository |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 과제 엔티티에 대한 데이터베이스 접근을 담당하는 JPA 리포지토리 인터페이스 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | JpaRepository\<Notification, Long\> | \- | public |
|  | Notification 엔티티에 대한 CRUD 기능 상속 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | findAllByStudy | List\<Notification\> | public |
|  | 스터디에 존재하는 모든 알림 엔티티를 조회 |  |  |
|  | deleteAllByStudy\_Id | void | public |
|  | 해당 스터디 id의 모든 알림을 삭제 |  |  |

/service

| NotificationService |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 알림 관련 로직을 담당하는 서비스 클래스 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | notificationRepository | NotificationRepository | private final |
|  | 알림 데이터 접근을 위한 리포지토리 |  |  |
|  | studyMemberRepository | StudyMemberRepository | private final |
|  | 스터디 멤버 데이터 접근을 위한 리포지토리 |  |  |
|  | groupService | GroupService | private final |
|  | 멤버의 타당성을 검증하기 위한 로직을 담당하는 서비스 |  |  |
|  | userProfileRepository | UserProfileRepository | private final |
|  | 사용자 프로필 데이터 접근을 위한 리포지토리 |  |  |
|  | studyRepository | StudyRepository | private final |
|  | 스터디 그룹 데이터 접근을 위한 리포지토리 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getNotifications | List\<GetNotificationsListResponse\> | public |
|  | 스터디의 알림 목록을 모두 불러옴 |  |  |
|  | getNotificationDetail | GetNotificationDetailResponse | public |
|  | 알림의 상세 내용을 불러옴 |  |  |
|  | addAssignmentNotification | void | public |
|  | 과제가 생성될 때 알림을 생성하는 함수 |  |  |
|  | addAnnouncementNotification | void | public |
|  | 공지가 생성될 때 알림을 생성하는 함수 |  |  |
|  | addNewMemberNotification | void | public |
|  | 스터디에 새로운 그룹원이 들어왔을 때 알림을 생성하는 함수 |  |  |
|  | addOutMemberNotification | void | public |
|  | 스터디에 기존 회원이 탈퇴했을 때 또는 추방됐을 때 알림을 생성하는 함수 |  |  |
|  | addNewApplicationNotification | void | public |
|  | 새로운 지원서가 생성됐을 때 알림을 생성하는 함수 |  |  |

3.2.8 resource

![](https://github.com/StudyGroup-Project/StudyGroup_Project/blob/b1a34117ce075f5b3163118cb3e670d3bba353bd/documents/image/class%20image/%EA%B7%B8%EB%A6%BC%20%23%20class%20328%20Resource-Diagram.png)
그림 \# class 328 Resource-Diagram

/domain

| Resource |  |  |  |
| ----- | :---- | :---- | :---- |
| Class Description | 스터디 내에서 생성된 자료를 저장하는 엔티티 |  |  |
| 구분 | Name | Type | Visibility |
|  | Description |  |  |
| Attributes  | id | Long | private |
|  | 각 자료를 식별하는 고유 식별자 필드  |  |  |
|  | study | Study | private |
|  | 스터디에 대한 외래키 |  |  |
|  | author | StudyMember | private |
|  | 저자에 대한 외래키 |  |  |
|  | title | String | private |
|  | 자료 제목 |  |  |
|  | description | String | private |
|  | 자료 내용 |  |  |
|  | createdAt | LocalDateTime | private |
|  | 자료 생성 날짜 및 시간 |  |  |
|  | updatedAt | LocalDateTime | private |
|  | 자료 마지막 업데이트 날짜 및 시간 |  |  |
| 구분 | Name | Type | Visibility |
|  | Description |  |  |
| Operations | builder | CommentBuilder | public |
|  | 자료 객체 빌더 생성함수  |  |  |
|  | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | constructor | \- | private/protected |
|  | 엔티티 생성자 (기본 및 전체 필드 포함) |  |  |
|  | updateResource(@NotBlank String title, @NotBlank String description) | void | public |
|  | 매개변수로 받은 값으로 제목과 내용을 업데이트하는 함수  |  |  |

/controller

| ResourceController |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 자료에 대한 요청을 처리하는 REST 컨트롤러 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | resourceService | ResourceService | private |
|  | 자료에 대한 요청을 처리하는  비즈니스 클래스  |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getResources | ResponseEntity\<List\<GetResourcesResponse\>\> | public |
|  | 스터디 자료 목록을 조회하여 반환하는 함수  |  |  |
|  | createResource | ResponseEntity\<Void\> | public |
|  | 스터디에 자료를 생성하는 함수 |  |  |
|  | updateResource | ResponseEntity\<Void\> | public |
|  | 스터디에 있는 기존 자료를 수정하는 함수  |  |  |
|  | deleteResource | ResponseEntity\<Void\> | public |
|  | 스터디에 있는 기존 자료를 삭제하는 함수  |  |  |
|  | getResourceDetail | ResponseEntity\<GetResourceDetailResponse\> | public |
|  | 스터디에 있는 자료의 상세 테이터를 조회하는 함수 |  |  |

/service

| ResourceService |  |  |  |
| ----- | :---- | :---- | :---- |
| Class Description | 자료와 관련된 비즈니스 로직을 처리하는  클래스 |  |  |
| 구분 | Name | Type | Visibility |
|  | Description |  |  |
| Attributes | resourceRepository | ResourceRepository | private |
|  | Resource 엔티티 데이터 접근을 위한 레포지토리 |  |  |
|  | fileRepository | FileRepository | private |
|  | File 엔티티 데이터 접근을 위한 레포지토리 |  |  |
|  | s3Uploader | S3Uploader | private |
|  | AWS S3 파일 업로드 및 메타데이터 관리 유틸리티 |  |  |
|  | userService | UserService | private |
|  | 유저와 관련한 비즈니스 로직 접근을 위한 서비스 |  |  |
|  | groupService | GroupService | private |
|  | 스터디 인증과 관련한 비즈니스 로직 접근을 위한 서비스 |  |  |
| 구분 | Name | Type | Visibility |
|  | Description |  |  |
| Operations | getResources(Long studyId, Long userId) | List\<GetResourcesResponse\> | public  |
|  | 매개변수로 받은 studyId에 해당하는 스터디 그룹의 모든 자료 목록을 찾아  반환 |  |  |
|  | createResource(Long studyId, Long userId, CreateResourceRequest  resourceRequest) | void | public  |
|  | 매개변수로 받은 studyId에 해당하는 스터디 그룹에 자료를 생성하는 함수 |  |  |
|  | deleteResource(Long studyId, Long resourceId, Long userId) | void | public  |
|  | 매개변수로 받은 자료Id에 해당하는 자료를 스터디 그룹 내에서 삭제하는 함수 |  |  |
|  | updateResource(Long studyId, Long resourceId, Long userId, @NonNull UpdateResourceRequest updateResourceRequest) | void | public  |
|  | 매개변수로 받은 자료Id에 해당하는 자료 내용을 업데이트하는 함수 |  |  |
|  | getResourceDetail(Long studyId, Long resourceId,Long userId) | GetResourceDetailResponse | public  |
|  | 매개변수로 받은 자료Id에 해당하는 자료의 상세 내용을 조회하는 함수 |  |  |
|  | resourceValidation(Long resourceId,StudyMember studyUser) | Resource | private |
|  | 매개변수로 받은 resourceId에 해당하는 자료를 조회하고 저자가 요청한 유저와 일치하는지 검증하는 함수 |  |  |
|  | findResource(Long studyId, Long resourceId) | Resource | private |
|  | 매개변수로 받은 resourceId에 해당하는 자료를 조회하여 반환하는 함수 |  |  |
|  | fileUploadDbAndS3(List\<MultipartFile\> files, List\<FileDetailDto\> list, Resource resource) | void | private |
|  | 매개변수로 받은 파일과 메타데이터를 기반으로 파일을 DB와 S3에 저장하는 함수 |  |  |

/repository

| ResourceRepository |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 자료 엔티티에 대한 데이터베이스 접근을 담당하는 JPA 리포지토리 인터페이스 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | JpaRepository\<Resource, Long\> | \- | public |
|  | Resource 엔티티에 대한 CRUD 기능 상속 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | findAllByStudyId(Long studyId) | List\<Resource\> | public |
|  | 매개변수로 받은 스터디id를 기반으로 모든 자료를 조회하여 반환하는 함수 |  |  |
|  | deleteAllByStudy\_Id(Long studyId); | void | public |
|  | 특정 스터디에 모든 자료를 삭제 |  |  |
|  | findByIdAndStudyId(Long id, Long study\_Id); |  Optional\<Resource\> | public |
|  | 매개변수로 받은 id와 studyId가 모두 일치하는 자료를 조회하여 반환하는 함수 |  |  |

3.2.9 study

![](https://github.com/StudyGroup-Project/StudyGroup_Project/blob/b1a34117ce075f5b3163118cb3e670d3bba353bd/documents/image/class%20image/%EA%B7%B8%EB%A6%BC%20%23%20class%20329%20StudyDomain-Diagram.png)

그림 \# class 329 StudyDomain-Diagram

![](https://github.com/StudyGroup-Project/StudyGroup_Project/blob/b1a34117ce075f5b3163118cb3e670d3bba353bd/documents/image/class%20image/%EA%B7%B8%EB%A6%BC%20%23%20class329%20StudyRepository-Diagram.png)

그림 \# class329 StudyRepository-Diagram

![](https://github.com/StudyGroup-Project/StudyGroup_Project/blob/b1a34117ce075f5b3163118cb3e670d3bba353bd/documents/image/class%20image/%EA%B7%B8%EB%A6%BC%20%23%20class%20329%20StudyService-Diagram.png)

그림 \# class 329 StudyService-Diagram

/controller

| StudyController |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 스터디와 관련된 REST 컨트롤러 클래스 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | studyService | StudyService | private final |
|  | 스터디에 대한 요청을 처리하는 서비스 의존성 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | createStudy | ReseponseEntity\<Void\> | public |
|  | 스터디 그룹을 생성하는 메서드 |  |  |
|  | getStudyProfile | ReseponseEntity\<getStudyProfileResponse\> | public |
|  | 그룹 프로필 정보를 가져오는 메서드 |  |  |
|  | updateStudyProfile | ResponseEntity\<Void\> | public |
|  | 그룹 프로필 정보를 수정하는 메서드 |  |  |
|  | getStudyHome | ResponseEntity\<StudyHomeResponse\> | public |
|  | 스터디 메인 데이터(그룹 타이틀) 조회하는 메서드 |  |  |
|  | deleteStudy | ResponseEntity\<Void\> | public |
|  | 그룹을 삭제하는 메서드 |  |  |

| StudyBookmarkController |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 스터디 찜하기와 관련된 REST 컨트롤러 클래스 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | bookmarkService | BookmarkService | private final |
|  | 스터디 찜하기에 대한 요청을 처리하는 서비스 의존성 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | addBookmark | ReseponseEntity\<Void\> | public |
|  | 스터디 그룹 찜하기 |  |  |
|  | removeBookmark | ReseponseEntity\<Void\> | public |
|  | 스터디 그룹 찜 해제하기 |  |  |

| StudyMemberController |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 스터디멤버와 관련된 REST 컨트롤러 클래스 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | studyMemberService | StudyMemberService | private final |
|  | 스터디멤버에 대한 요청을 처리하는 서비스 의존성 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getMembers | ReseponseEntity\<GetStudyMembersResponse\> | public |
|  | 멤버 목록을 가져오는 메서드 |  |  |
|  | leaveStudy | ReseponseEntity\<Void\> | public |
|  | 그룹을 탈퇴하는 메서드 |  |  |
|  | expelMember | ResponseEntity\<Void\> | public |
|  | 그룹 인원을 추방하는 메서드 |  |  |

| StudyQueryController |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 스터디 검색, 내 스터디 및 찜한 스터디 목록을 조회하는 REST 컨트롤러 클래스 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | studyQueryService | StudyQueryService | private final |
|  | 스터디 검색 및 조회 로직을 처리하는 서비스 의존성 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | searchStudies | SearchStudiesResponse | public |
|  | 스터디 그룹을 검색 조건에 따라 조회하는 메서드 |  |  |
|  | getMyStudies | ResponseEntity\<GetStudiesResponse\> | public |
|  | 로그인한 사용자의 참여 스터디 목록을 반환하는 메서드 |  |  |
|  | getBookmarks | ResponseEntity\<GetStudiesResponse\> | public |
|  | 로그인한 사용자의 찜한 스터디 목록을 반환하는 메서드 |  |  |

/domain

| Bookmark |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 사용자가 찜한 스터디 정보를 저장하는 엔티티 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | id | Long | private |
|  | 북마크 고유 식별자 |  |  |
|  | user | User | private |
|  | 북마크를 등록한 사용자 |  |  |
|  | study | Study | private |
|  | 사용자가 찜한 스터디 정보 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | builder | BookmarkBuilder | public |
|  | Bookmark 객체 생성을 위한 빌더 제공 |  |  |
|  | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | constructor | Bookmark() | protected/private |
|  | 엔티티 생성자 (기본 및 전체 필드 포함) |  |  |

| RecruitStatus |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 스터디의 모집 상태를 나타내는 열거형(Enum) |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | OPEN | RecruitStatus | public |
|  | 스터디 모집 중 상태 |  |  |
|  | CLOSED | RecruitStatus | public |
|  | 스터디 모집 마감 상태 |  |  |

| Study |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 스터디 그룹의 기본 정보를 저장하는 엔티티 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | id | Long | private |
|  | 스터디 고유 식별자 |  |  |
|  | maxMemberCount | int | private |
|  | 스터디의 최대 참여 인원 수 |  |  |
|  | recruitStatus | RecruitStatus | private |
|  | 스터디 모집 상태 (OPEN/CLOSED) |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | updateMaxMemberCount | void | public |
|  | 스터디 최대 인원 수를 수정하는 메서드 |  |  |
|  | builder | StudyBuilder | public |
|  | Study 객체 생성을 위한 빌더 제공 |  |  |
|  | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | constructor | Study() | protected/private |
|  | 기본 및 전체 필드 생성자 |  |  |

| StudyMember |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 스터디에 속한 멤버 정보를 저장하는 엔티티 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | id | Long | private |
|  | 스터디 멤버 고유 식별자 |  |  |
|  | user | User | private |
|  | 해당 스터디에 참여한 사용자 |  |  |
|  | study | Study | private |
|  | 소속된 스터디 |  |  |
|  | role | StudyRole | private |
|  | 스터디 내 역할 (리더/멤버 등) |  |  |
|  | status | StudyMemberStatus | private |
|  | 스터디 참여 상태 (JOINED, EXIT 등) |  |  |
|  | exitedAt | LocalDateTime | private |
|  | 스터디 탈퇴(퇴장) 시각 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | updateStatus | void | public |
|  | 스터디 멤버의 참여 상태를 변경하는 메서드 |  |  |
|  | builder | StudyMemberBuilder | public |
|  | StudyMember 객체 생성을 위한 빌더 제공 |  |  |
|  | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | constructor | StudyMember() | protected/private |
|  | 기본 및 전체 필드 생성자 |  |  |

| StudyMemberStatus |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 스터디 멤버의 상태를 정의하는 열거형(Enum) |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | JOINED | StudyMemberStatus | public |
|  | 스터디에 참여 중인 상태 |  |  |
|  | LEFT | StudyMemberStatus | public |
|  | 스터디를 탈퇴한 상태 |  |  |
|  | BANNED | StudyMemberStatus | public |
|  | 스터디에서 강퇴된 상태 |  |  |

| StudyProfile |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 스터디 그룹의 상세 정보(제목, 카테고리, 소개 등)를 관리하는 엔티티 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | id | Long | private |
|  | 스터디 프로필 고유 식별자 |  |  |
|  | study | Study | private |
|  | 스터디 엔티티와 1:1 매핑된 스터디 객체 |  |  |
|  | title | String | private |
|  | 스터디 제목 |  |  |
|  | bio | String | private |
|  | 스터디 한 줄 소개 |  |  |
|  | description | String | private |
|  | 스터디 상세 설명 |  |  |
|  | address | Address | private |
|  | 스터디 위치 정보 |  |  |
|  | category | List\<Category\> | private |
|  | 스터디 카테고리 정보 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | update | void | public |
|  | 스터디 프로필 정보를 수정하는 메서드 (제목, 카테고리, 주소, 소개, 설명 갱신) |  |  |
|  | builder | StudyProfileBuilder | public |
|  | StudyProfile 객체 생성을 위한 빌더 제공 |  |  |
|  | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | constructor | StudyProfile() | protected/private |
|  | 기본 및 전체 필드 생성자 |  |  |

| StudyRole |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 스터디 내에서 사용자의 역할(리더, 멤버)을 정의하는 열거형(Enum) |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | LEADER | StudyRole | public |
|  | 스터디를 생성하고 관리하는 리더 역할 |  |  |
|  | MEMBER | StudyRole | public |
|  | 스터디에 참여하는 일반 멤버 역할 |  |  |

| StudySortType |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 스터디 목록 정렬 방식을 정의하는 열거형(Enum) |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | LATEST | StudySortType | public |
|  | 스터디를 최신순으로 정렬 |  |  |
|  | TRUST\_SCORE\_DESC | StudySortType | public |
|  | 스터디 리더의 신뢰 점수를 기준으로 내림차순 정렬 |  |  |

/repository

| StudyRepository |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 스터디에 대한 데이터베이스 접근을 담당하는 JPA 리포지토리 인터페이스 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | JpaRepository\<Study, Long\>, StudyRepositoryCustom | \- | public |
|  | 스터디 엔티티에 대한 CRUD 기능 상속 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | findBookmarkedStudyProfiles | List\<StudyProfile\> | public |
|  | 특정 사용자가 북마크한 모든 스터디의 스터디 프로필 정보를 조회합니다. |  |  |
|  | findJoinedStudyProfiles | List\<StudyProfile\> | public |
|  | 특정 사용자가 현재 정식 멤버로 가입된 모든 스터디의 정보를 조회합니다. |  |  |

| StudyProfileRepository |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 스터디그룹 프로필에 대한 데이터베이스 접근을 담당하는 JPA 리포지토리 인터페이스 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | JpaRepository\<StudyProfile, Long\> | \- | public |
|  | 스터디 그룹 프로필 엔티티에 대한 CRUD 기능 상속 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | findByStudy | Optional\<StudyProfile\> | public |
|  | 스터디와 연관된 스터디 프로필을 찾아서 반환합니다. |  |  |
|  | findByStudyId | Optional\<StudyProfile\> | public |
|  | 스터디 객체를 가지고 있을 때 해당 스터디의 프로필을 조회하기 위해 사용. |  |  |
|  | deleteByStudy\_Id | void | public |
|  | 스터디가 삭제될 때 그에 속한 프로필 정보도 함께 삭제하기 위해 사용합니다. |  |  |

| StudyMemberRepository |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 스터디 멤버에 대한 데이터베이스 접근을 담당하는 JPA 리포지토리 인터페이스 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | JpaRepository\<StudyMember, Long\> | \- | public |
|  | 스터디 멤버 엔티티에 대한 CRUD 기능 상속 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | findByStudyIdAndRoleAndStatus | Optional\<StudyMember\> | public |
|  | 스터디 ID, 멤버의 역할, 멤버의 상태를 만족하는 정보를 찾는다. |  |  |
|  | findByStudyIdAndUserId | Optional\<StudyMember\> | public |
|  | 유저가 스터디 멤버에 등록되어 있는지 판단하기 위해 사용한다. |  |  |
|  | findByStudyIdAndRole | Optional\<StudyMember\> | public |
|  | 이 스터디의 역할로 스터디 멤버 정보를 찾는다. |  |  |
|  | countByStudyIdAndStatus | Long | public |
|  | 특정 스터디에서 특정 상태를 가진 멤버의 총 인원수를 계산한다. |  |  |
|  | existsByStudyIdAndUserIdAndStatus | boolean | public |
|  | 특정 스터디에 특정 유저가 존재하는지 여부를 확인한다. |  |  |
|  | findByStudyIdAndUserIdAndStatus | Optional\<StudyMember\> | public |
|  | 스터디, 유저, 상태 세가지 조건으로 스터디 멤버 정보를 찾는다. |  |  |
|  | findAllByStudy\_IdAndStatus | List\<StudyMember\> | public |
|  | 특정 스터디에서 특정 상태를 가진 모든 스터디멤버 목록을 가져온다. |  |  |
|  | findTop10StudyProfiles | List\<StudyProfile\> | public |
|  | 방장의 신뢰점수가 높은 순으로 상위 스터디 목록을 조회한다. |  |  |
|  | findLeaderTrustScoreByStudyId | Optional\<Long\> | public |
|  | 특정 스터디의 방장의 신뢰점수만 조회한다. |  |  |
|  | deleteAllByStudy\_Id | void | public |
|  | 특정 스터디에 속한 모든 스터디 멤버들을 삭제한다. |  |  |

| BookmarkRepository |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 스터디그룹 찜하기에 대한 데이터베이스 접근을 담당하는 JPA 리포지토리 인터페이스 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | JpaRepository\<Bookmark, Long\> | \- | public |
|  | 스터디 그룹 찜하기 엔티티에 대한 CRUD 기능 상속 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | findByUserAndStudy | Optional\<Bookmark\> | public |
|  | 유저 객체와 스터디 엔티티 객체로 , 두 조건을 모두 만족하는 Bookmark정보를 찾는다. |  |  |
|  | findAllByUserId | List\<Bookmakr\> | public |
|  | 특정 사용자 ID를 가진 사용자가 찜한 모든 Bookmark목록을 반환한다. |  |  |
|  | countByStudyId | long | public |
|  | 특정 스터디가 찜된 총 횟수를 계산한다. |  |  |
|  | deleteAllByStudy\_Id | void | public |
|  | 특정 스터디 ID와 관련된 모든 Bookmark 를 삭제한다. |  |  |
|  | existsByUserIdAndStudyId | boolean | public |
|  | 특정 사용자가 특정 스터디를 찜했는지 존재 여부만 빠르게 확인한다. |  |  |

| StudyRepositoryCustom |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 스터디 검색을 위한 사용자 정의 쿼리 메서드를 정의하는 Repository 인터페이스 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | searchStudies | Page\<StudyDto\> | public |
|  | 키워드, 카테고리, 지역, 정렬 조건 등을 이용하여 스터디 목록을 검색하는 메서드 |  |  |

| StudyRepositoryImpl |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | QueryDSL을 이용해 StudyRepositoryCustom 인터페이스를 구현한 스터디 검색/정렬용 리포지토리 구현 클래스 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | queryFactory | JPAQueryFactory | private final |
|  | QueryDSL 기반 동적 쿼리 생성을 위한 JPAQueryFactory 인스턴스 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | searchStudies | Page\<StudyDto\> | public |
|  | 조건(키워드, 카테고리, 지역, 정렬 등)에 따라 스터디 목록을 조회하는 QueryDSL 기반 검색 메서드 |  |  |
|  | toOrderSpecifier | OrderSpecifier\<?\> | private |
|  | StudySortType 값에 따른 정렬 기준(OrderSpecifier)을 반환하는 내부 메서드 |  |  |

/service

| StudyService |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 스터디와 관련된 로직을 처리하는 서비스 클래스 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | studyRepository | StudyRepository | private final |
|  | 스터디 엔티티 검색 및 조회를 위한 리포지토리 |  |  |
|  | studyProfileRepository | StudyProfileRepository | private final |
|  | 스터디 프로필 정보를 조회하기 위한 리포지토리 |  |  |
|  | studyMemberRepository | StudyMemberRepository | private final |
|  | 스터디 멤버 정보를 조회하기 위한 리포지토리 |  |  |
|  | userRepository | UserRepository | private final |
|  | 사용자 엔티티를 조회하기 위한 리포지토리 |  |  |
|  | userProfileRepository | UserProfileRepository | private final |
|  | 사용자 프로필 정보를 조회하기 위한 리포지토리 |  |  |
|  | s3Uploader | S3Uploader | private final |
|  | S3 파일 업로더 유틸리티 클래스 |  |  |
|  | bookmarkRepository | BookmarkRepository | private final |
|  | 사용자 찜 데이터를 조회하기 위한 리포지토리 |  |  |
|  | applicationRepository | ApplicationRepository | private final |
|  | 지원서 처리를 위한 리포지토리 |  |  |
|  | annoucementRepository | AnnoucementRepository | private final |
|  | 공지 처리를 위한 리포지토리 |  |  |
|  | commentRepository | CommentRepository | private final |
|  | 댓글 처리를 위한 리포지토리 |  |  |
|  | assignmentRepository | AssignmentRepository | private final |
|  | 과제 처리를 위한 리포지토리 |  |  |
|  | submissionRepository | SubmissionRepository | private final |
|  | 제출물을 위한 리포지토리 |  |  |
|  | chatMessageRepository | ChatMessageRepository | private final |
|  | 채팅 메세지를 위한 리포지토리 |  |  |
|  | notificationRepository | NotificationRepository | private final |
|  | 알림 처리를 위한 리포지토리 |  |  |
|  | resourceRepository | ResourceRepository | private final |
|  | 자료 처리를 위한 리포지토리 |  |  |
|  | feedBackRepository | FeedBackRepository | private final |
|  | 피드백처리를 위한 리포지토리 |  |  |
|  | fileService | FileService | private final |
|  | 파일 서비스를 위한 서비스 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | createStudy | Long | public |
|  | 스터디 그룹을 생성하는 메서드 |  |  |
|  | getStudyProfile | GetStudyProfileResponse | public |
|  | 그룹 프로필 정보를 가져오는 메서드 |  |  |
|  | updateStudyProfile | void | public |
|  | 그룹 프로필 정보를 수정하는 메서드 |  |  |
|  | getStudyHome | StudyHomeResponse | public |
|  | 스터디메인 데이터를 조회하는 메서드(타이틀 정보) |  |  |
|  | deleteStudy | void | public |
|  | 스터디 그룹을 삭제하는 메서드 |  |  |

| BookmarkService |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 스터디 그룹 찜하기를 위한 서비스 클래스 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | notificationRepository | NotificationRepository | private final |
|  | 알림을 위한 리포지토리 |  |  |
|  | groupService | GroupService | private final |
|  | 스터디멤버 목록 가져올때 사용할 그룹 서비스  |  |  |
|  | studyMemberRepository | StudyMemberRepository | private final |
|  | 스터디 멤버 정보를 조회하기 위한 리포지토리 |  |  |
|  | userRepository | UserRepository | private final |
|  | 사용자 엔티티를 조회하기 위한 리포지토리 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getMembers | GetStudyMemberResponse | public |
|  | 스터디 멤버 목록 가져오기 메서드 |  |  |
|  | leaveStudy | void | private |
|  | 그룹 탈퇴하는 메서드 |  |  |
|  | expelMember | void | public |
|  | 그룹인원을 추방하는 메서드 |  |  |

| StudyQueryService |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 스터디 검색, 내 스터디 및 찜 목록 조회 로직을 처리하는 서비스 클래스 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | studyRepository | StudyRepository | private final |
|  | 스터디 엔티티 검색 및 조회를 위한 리포지토리 |  |  |
|  | studyProfileRepository | StudyProfileRepository | private final |
|  | 스터디 프로필 정보를 조회하기 위한 리포지토리 |  |  |
|  | studyMemberRepository | StudyMemberRepository | private final |
|  | 스터디 멤버 정보를 조회하기 위한 리포지토리 |  |  |
|  | userRepository | UserRepository | private final |
|  | 사용자 엔티티를 조회하기 위한 리포지토리 |  |  |
|  | userProfileRepository | UserProfileRepository | private final |
|  | 사용자 프로필 정보를 조회하기 위한 리포지토리 |  |  |
|  | s3Uploader | S3Uploader | private final |
|  | S3 파일 업로더 유틸리티 클래스 |  |  |
|  | bookmarkRepository | BookmarkRepository | private final |
|  | 사용자 찜 데이터를 조회하기 위한 리포지토리 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | searchStudies | SearchStudiesResponse | public |
|  | 사용자 입력 조건을 기반으로 스터디 목록을 검색하여 반환하는 메서드 |  |  |
|  | getSort | Sort | private |
|  | 정렬 타입(최신순, 신뢰 점수순)에 따른 Sort 객체를 반환하는 내부 메서드 |  |  |
|  | getMyStudies | GetStudiesResponse | public |
|  | 로그인한 사용자의 참여 스터디 목록을 반환하는 메서드 |  |  |
|  | getBookmarks | GetStudiesResponse | public |
|  | 로그인한 사용자의 찜한 스터디 목록을 반환하는 메서드 |  |  |

 

 

**3.3 DTO 클래스**

3.3.1 account

| CheckDuplicatedIdResponse |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 사용자 ID 중복 여부를 확인하는 응답 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | available | boolean | private |
|  | ID 사용 가능 여부를 나타내는 불리언 값 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | constructor | CheckDuplicatedIdResponse(boolean available) | public |
|  | 필드를 초기화하는 전체 필드 생성자 |  |  |

| CreateAccessTokenRequest |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 리프레시 토큰을 이용해 새로운 액세스 토큰을 발급받기 위한 요청 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | refreshToken | String | private |
|  | 새로운 액세스 토큰을 발급받기 위한 리프레시 토큰 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | setter | \- | public |
|  | 필드별 setter 메서드 제공 |  |  |
|  | constructor | CreateAccessTokenRequest() | public |
|  | 기본 생성자 |  |  |
|  | constructor | CreateAccessTokenRequest(String refreshToken) | public |
|  | 필드를 초기화하는 전체 필드 생성자 |  |  |

| CreateAccessTokenResponse |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 리프레시 토큰으로 발급된 새로운 액세스 토큰 정보를 담는 응답 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | accessToken | String | private |
|  | 발급된 새로운 액세스 토큰 문자열 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | constructor | CreateAccessTokenResponse(String accessToken) | public |
|  | 필드를 초기화하는 전체 필드 생성자 |  |  |

| CustomUserDetails |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | Spring Security의 UserDetails를 구현하여 사용자 인증 정보를 제공하는 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | userId | Long | private |
|  | 현재 인증된 사용자의 고유 식별자(ID) |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getUsername | String | public |
|  | 사용자 이름을 반환 (현재 빈 문자열 반환) |  |  |
|  | getPassword | String | public |
|  | 사용자 비밀번호를 반환 (현재 빈 문자열 반환) |  |  |
|  | isAccountNonExpired | boolean | public |
|  | 계정 만료 여부 반환 |  |  |
|  | isAccountNonLocked | boolean | public |
|  | 계정 잠금 여부 반환 |  |  |
|  | isCredentialsNonExpired | boolean | public |
|  | 자격 증명(비밀번호) 만료 여부 반환 |  |  |
|  | isEnabled | boolean | public |
|  | 계정 활성화 여부 반환 |  |  |
|  | getAuthorities | Collection\<? extends GrantedAuthority\> | public |
|  | 사용자의 권한 목록을 반환 (ROLE\_USER 기본 권한) |  |  |
|  | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | constructor | CustomUserDetails(Long userId) | public |
|  | userId를 초기화하는 생성자 |  |  |

| GetMyProfileResponse |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 사용자의 프로필 정보를 조회할 때 반환되는 응답 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | id | Long | private |
|  | 사용자의 고유 식별자 |  |  |
|  | nickname | String | private |
|  | 사용자 닉네임 |  |  |
|  | province | String | private |
|  | 사용자가 속한 지역(도 단위) |  |  |
|  | district | String | private |
|  | 사용자가 속한 세부 지역(시/군/구 단위) |  |  |
|  | birthDate | String | private |
|  | 사용자의 생년월일 정보 |  |  |
|  | job | Job | private |
|  | 사용자의 직업 정보 |  |  |
|  | preferredCategory | List\<Category\> | private |
|  | 사용자가 선호하는 스터디 카테고리 목록 |  |  |
|  | profileImageUrl | String | private |
|  | 프로필 이미지의 S3 URL 주소 |  |  |
|  | trustScore | Long | private |
|  | 사용자의 신뢰 점수 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | setter | \- | public |
|  | 필드별 setter 메서드 제공 |  |  |
|  | constructor | GetMyProfileResponse(...) | public |
|  | 전체 필드를 초기화하는 생성자 |  |  |

| InitUserProfileRequest |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 회원가입 또는 초기 설정 시 사용자 프로필 정보를 등록하기 위한 요청 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | nickname | String | private |
|  | @NotBlank \- 사용자 닉네임 |  |  |
|  | province | String | private |
|  | @NotBlank \- 사용자의 지역(도 단위) |  |  |
|  | district | String | private |
|  | @NotBlank \- 사용자의 세부 지역(시/군/구 단위) |  |  |
|  | birthDate | String | private |
|  | @NotBlank \- 사용자 생년월일 |  |  |
|  | job | Job | private |
|  | @NotNull \- 사용자 직업 |  |  |
|  | preferredCategory | List\<Category\> | private |
|  | @NotNull \- 사용자가 선호하는 스터디 카테고리 목록 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | setter | \- | public |
|  | 필드별 setter 메서드 제공 |  |  |
|  | constructor | InitUserProfileRequest(...) | public |
|  | 전체 필드를 초기화하는 생성자 |  |  |

| LoginRequest |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 사용자 로그인 요청 시 전달되는 로그인 ID와 비밀번호 정보를 담는 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | loginId | String | private |
|  | 사용자의 로그인 ID |  |  |
|  | password | String | private |
|  | 사용자의 비밀번호 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | setter | \- | public |
|  | 필드별 setter 메서드 제공 |  |  |
|  | constructor | LoginRequest(String loginId, String password) | public |
|  | 모든 필드를 초기화하는 생성자 |  |  |

| LoginResponse |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 로그인 성공 시 발급되는 액세스 토큰과 리프레시 토큰을 포함하는 응답 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | accessToken | String | private |
|  | 사용자의 인증을 위한 액세스 토큰 |  |  |
|  | refreshToken | String | private |
|  | 액세스 토큰 재발급을 위한 리프레시 토큰 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | setter | \- | public |
|  | 필드별 setter 메서드 제공 |  |  |
|  | constructor | LoginResponse(String accessToken, String refreshToken) | public |
|  | 모든 필드를 초기화하는 생성자 |  |  |

| RegisterRequest |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 회원가입 시 사용자의 로그인 ID와 비밀번호를 전달하기 위한 요청 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | loginId | String | private |
|  | 회원가입 시 사용자의 로그인 ID |  |  |
|  | password | String | private |
|  | 회원가입 시 사용자의 비밀번호 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | setter | \- | public |
|  | 필드별 setter 메서드 제공 |  |  |
|  | constructor | RegisterRequest(String loginId, String password) | public |
|  | 모든 필드를 초기화하는 생성자 |  |  |

| TokenRefreshRequest |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 리프레시 토큰을 이용해 액세스 토큰을 재발급받기 위한 요청 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | refreshToken | String | private |
|  | 액세스 토큰 재발급을 위한 리프레시 토큰 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | setter | \- | public |
|  | 필드별 setter 메서드 제공 |  |  |

| TokenResponse |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 로그인 또는 토큰 재발급 시 반환되는 액세스 토큰, 리프레시 토큰 및 만료 시간을 포함하는 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | accessToken | String | private |
|  | 사용자의 인증을 위한 액세스 토큰 |  |  |
|  | refreshToken | String | private |
|  | 액세스 토큰 재발급을 위한 리프레시 토큰 |  |  |
|  | refreshTokenExpiry | Long | private |
|  | 리프레시 토큰의 만료 시간(밀리초 단위) |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | setter | \- | public |
|  | 필드별 setter 메서드 제공 |  |  |
|  | constructor | TokenResponse(String accessToken, String refreshToken, Long refreshTokenExpiry) | public |
|  | 모든 필드를 초기화하는 생성자 |  |  |

| UpdateUserProfileRequest |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 사용자 프로필 정보를 수정할 때 전달되는 요청 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | nickname | String | private |
|  | 수정할 사용자 닉네임 |  |  |
|  | province | String | private |
|  | 수정할 지역(도 단위) |  |  |
|  | district | String | private |
|  | 수정할 세부 지역(시/군/구 단위) |  |  |
|  | birthDate | String | private |
|  | 수정할 사용자 생년월일 |  |  |
|  | job | Job | private |
|  | 수정할 사용자 직업 |  |  |
|  | preferredCategory | List\<Category\> | private |
|  | 수정할 선호 스터디 카테고리 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | setter | \- | public |
|  | 필드별 setter 메서드 제공 |  |  |
|  | constructor | UpdateUserProfileRequest(...) | public |
|  | 모든 필드를 초기화하는 생성자 |  |  |

3.3.2 announcement

| AnnouncementComments |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 공지의 댓글 하나를 나타내는 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | commentId | Long  | private |
|  | 댓글 고유 id |  |  |
|  | userName | String | private |
|  | 댓글 작성자 |  |  |
|  | userProfileImageUrl | String | private |
|  | 댓글 작성자의 profile url |  |  |
|  | content | String | private |
|  | 댓글 내용 |  |  |
|  | createdAt | LocalDateTime | private |
|  | 댓글 생성 날짜 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | setter | \- | public |
|  | 필드별 setter 메서드 제공 |  |  |
|  | builder | \- | public |
|  | 객체를 생성하는 builder 메서드 제공 |  |  |
|  | constructor | AnnouncementComments(...) | public |
|  | 모든 필드를 초기화하는 생성자 |  |  |

| AnnouncementFiles |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 공지에 첨부된 파일 하나를 나타내는 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | fileId | Long | private |
|  | 파일의 고유 id |  |  |
|  | fileName | String | private |
|  | 파일 원본 이름 |  |  |
|  | fileUrl | String | private |
|  | 파일을 다운받을 수 있는 url |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | setter | \- | public |
|  | 필드별 setter 메서드 제공 |  |  |
|  | builder | \- | public |
|  | 객체를 생성하는 builder 메서드 제공 |  |  |
|  | constructor | AnnouncementFiles()/  | public |
|  | 모든 필드를 초기화하는 생성자  |  |  |

| AnnouncementUpdateDto |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 공지 내용을 업데이트하기 위해 사용되는 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes  | title | String  | private |
|  | 공지 제목 |  |  |
|  | content | String  | private |
|  | 공지 내용 |  |  |
|  | files | List\<MultipartFile\>  | private |
|  | 새로 추가된 파일 목록 |  |  |
|  | deleteFileIds | List\<Long\> | private |
|  | 삭제할 파일의 ID 목록 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | setter | \- | public |
|  | 필드별 setter 메서드 제공 |  |  |
|  | builder | \- | public |
|  | 객체를 생성하는 builder 메서드 제공 |  |  |
|  | constructor | AnnouncementUpdateDto()/ AnnouncementUpdateDto(title,content,files,deleteFileIds) | public |
|  | 모든 필드를 초기화하는 생성자 및 기본 생성자 |  |  |

| CreateCommentRequest |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 공지에 댓글을 추가하기 위해  필요한 댓글 정보를 담고 있는  DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes  | content | String  | private |
|  | 댓글 내용 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | setter | \- | public |
|  | 필드별 setter 메서드 제공 |  |  |
|  | builder | \- | public |
|  | 객체를 생성하는 builder 메서드 제공 |  |  |

| GetAnnouncementDetailResponse |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 공지에 대한 상세 정보와 댓글 정보를 나타내기 위한  DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes  | announcementId | Long | default |
|  | 공지를 식별한 id |  |  |
|  | studyId | Long | default |
|  | 스터디 그룹을 식별하는 id |  |  |
|  | title | String  | default |
|  | 공지 제목 |  |  |
|  | content | String  | default |
|  | 공지 내용 |  |  |
|  | updatedAt |  LocalDateTime | default |
|  | 마지막 업데이트 날짜 |  |  |
|  | userName | String  | default |
|  | 공지 작성자 |  |  |
|  | userProfileImageUrl | String  | default |
|  | 공지 작성자의 프로필 url |  |  |
|  | files | List\<AnnouncementFiles\> | default |
|  | 공지에 첨부된 파일 List |  |  |
|  | comments | List\<AnnouncementComments\> | default |
|  | 공지에 작성된 댓글 List |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | setter | \- | public |
|  | 필드별 setter 메서드 제공 |  |  |
|  | builder | \- | public |
|  | 객체를 생성하는 builder 메서드 제공 |  |  |

| GetAnnouncementsResponse |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 공지 목록에서 하나의 공지 단위를 나타내는  DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes  | AnnouncementId | Long | private  |
|  | 공지를 식별하는 id |  |  |
|  | title | String | private  |
|  | 공지 제목 |  |  |
|  | createdAt | LocalDateTime | private  |
|  | 공지 생성 날짜 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | constructor | GetAnnouncementsResponse()  | public |
|  | 모든 필드를 초기화하는 생성자  |  |  |

3.3.3 application

| GetApplicationDetailResponse |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 지원서를 상세보기할때 요청되는 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | content | String | private |
|  | 지원서 내용 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | constructor | \- | public |
|  | 기본 생성자 |  |  |

| GetApplicationsResponse |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 스터디 지원서를 조회하는 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | applicationId | Long | private |
|  | 지원서 고유 ID |  |  |
|  | applicationId | Long | private |
|  | 지원자의 ID |  |  |
|  | nickname | String | private |
|  | 지원자의 닉네임 |  |  |
|  | profileImageUrl | String | private |
|  | 지원자의 프로필 사진 Url |  |  |
|  | createAt | LocalDateTime | private |
|  | 지원서를 제출한 시간 |  |  |
|  |  |  |  |
|  |  |  |  |
|  | status | ApplicationStatus | private |
|  | 지원서의 현재 상태  |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | constructor | \- | public |
|  | 기본 생성자 |  |  |

| HandleApplicationRequest |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 지원서를 처리할때 사용되는 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | status | ApplicationStatus | private |
|  | 지원서의 상태 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | constructor | \- | public |
|  | 기본 생성자 |  |  |

| submitApplicationRequest |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 지원서 제출할때 사용되는 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | content | String | private |
|  | 지원서 내용 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | constructor | CreateAssignmentRequest() | public |
|  | 기본 생성자 |  |  |

3.3.4 assignment

| AssignmentFileResponse |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 과제 파일의 url을 받는 응답 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes  | url | String | private |
|  | 과제 파일 링크(aws 접근 링크) |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | constructor | AssignmentFileResponse(...) | public |
|  | 모든 필드를 초기화하는 생성자 |  |  |

| CreateAssignmentRequest |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 과제를 만들 때 전달되는 요청 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | title | String | private |
|  | 과제 제목 |  |  |
|  | description | String | private |
|  | 과제 내용(본문) |  |  |
|  | files | List\<MultipartFile\> | private |
|  | 과제 게시물에 동반되는 파일들(사진 등) |  |  |
|  | startAt | LocalDateTime | private |
|  | 과제의 시작 날짜 |  |  |
|  | dueAt | LocalDateTime | private |
|  | 과제의 마감 날짜 |  |  |
|  |  |  |  |
|  |  |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | setter | \- | public |
|  | 필드별 setter 메서드 제공 |  |  |
|  | constructor | CreateAssignmentRequest() | public |
|  | 기본 생성자 |  |  |

| CreateSubmissionRequest |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 과제 제출물을 만들 때 전달되는 요청 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | description | String | private |
|  | 과제 본문(설명) |  |  |
|  | files | List\<MultipartFile\> | private |
|  | 과제 제출물 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | setter | \- | public |
|  | 필드별 setter 메서드 제공 |  |  |
|  | constructor | CreateSubmissionRequest() | public |
|  | 기본 생성자 |  |  |

| EvaluateSubmissionRequest |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 제출물에 대한 평가를 만들 때 전달되는 요청 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | score | Long | private |
|  | 제출물의 점수 |  |  |
|  | content | String | private |
|  | 평가자 코멘트 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | Builder | \- | public |
|  | 클래스의 builder 메서드 제공 |  |  |

| GetAssignmentDetailResponse |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 과제 게시물의 상세정보를 받는 응답 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes  | title | String | private |
|  | 과제 제목 |  |  |
|  | description | String | private |
|  | 과제 내용(본문) |  |  |
|  | files | List\<AssignmentFileResponse\> | private |
|  | 과제 게시물에 동반되는 파일들(사진 등) |  |  |
|  | startAt | LocalDateTime | private |
|  | 과제의 시작 날짜 |  |  |
|  | dueAt | LocalDateTime | private |
|  | 과제의 마감 날짜 |  |  |
|  |  |  |  |
|  |  |  |  |
|  | submissions | List\<SubmissionListResponse\> | private |
|  | 과제 제출물들의 List |  |  |
|  | profileUrls | List\<GetMyProfileResponse\> | private |
|  | 과제 제출자들의 프로필 url List |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | setter | \- | public |
|  | 필드별 setter 메서드 제공 |  |  |
|  | constructor | GetAssignmentDetailResponse(...) | public |
|  | 모든 필드를 초기화하는 생성자 |  |  |

| GetAssignmentsResponse |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 과제 게시글의 목록을 받는 응답 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | id | Long | private |
|  | 각 게시글의 id(DB에서 구분되는 id) |  |  |
|  | title | String | private |
|  | 게시글 이름 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | constructor | GetAssignmentDetailResponse(...) | public |
|  | 모든 필드를 초기화하는 생성자 |  |  |

| GetFeedbackListResponse |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 과제 피드백의 리스트를 받는 응답 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes  | id | Long | private |
|  | 피드백 id(DB에서 구분되는 값) |  |  |
|  | score | Long | private |
|  | 평가자가 평가한 점수 |  |  |
|  | feedback | String | private |
|  | 평가자의 평가말 |  |  |
|  | evaluatedAt | LocalDateTime | private |
|  | 평가시간 |  |  |
|  | evaluaterName | String | private |
|  | 평가자 닉네임 |  |  |
|  |  |  |  |
|  |  |  |  |
|  | evaluaterProfileUrl | String | private |
|  | 평가자 프로필 이미지 링크(aws 접근 링크) |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | setter | \- | public |
|  | 필드별 setter 메서드 제공 |  |  |
|  | constructor | GetFeedbackListResponse(...) | public |
|  | 모든 필드를 초기화하는 생성자 |  |  |

| GetSubmissionDetailResponse |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 과제 제출물의 상세정보를 받는 응답 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes  | id | Long | private |
|  | 과제 제출물 id(DB에서 구분되는 값) |  |  |
|  | submitterName | String | private |
|  | 제출자의 이름 |  |  |
|  | description | String | private |
|  | 제출자의 과제 제출물 본문(설명 등) |  |  |
|  | submitterProfuleUrl | String | private |
|  | 제출자의 프로필 url |  |  |
|  | CreateAt | LocalDateTime | private |
|  | 제출물이 만들어진 시간 |  |  |
|  | files | List\<AssignmentFileResponse\> | private |
|  | 과제 제출물 파일들 |  |  |
|  |  |  |  |
|  |  |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | setter | \- | public |
|  | 필드별 setter 메서드 제공 |  |  |
|  | constructor | GetFeedbackListResponse(...) | public |
|  | 모든 필드를 초기화하는 생성자 |  |  |

| SubmissionListResponse |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 과제 피드백의 리스트를 받는 응답 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes  | id | Long | private |
|  | 피드백 id(DB에서 구분되는 값) |  |  |
|  | submitterId | Long | private |
|  | 제출자 id(DB에서 구분되는 값) |  |  |
|  | createdAt | LocalDateTime | private |
|  | 제출 시간 |  |  |
|  | submitterNickname | String | private |
|  | 제출자 닉네임 |  |  |
|  |  |  |  |
|  |  |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | constructor | SubmissionListResponse(...) | public |
|  | 모든 필드를 초기화하는 생성자 |  |  |

| UpdateAssignmentRequest |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 과제를 수정할 때 전달되는 요청 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | title | String | private |
|  | 과제 제목 |  |  |
|  | description | String | private |
|  | 과제 내용(본문) |  |  |
|  | files | List\<MultipartFile\> | private |
|  | 과제 게시물에 동반되는 파일들(사진 등) |  |  |
|  | deleteFileIds | List\<Long\> | private |
|  | 수정 시 삭제될 파일 id의 리스트(DB에서 구분되는 값) |  |  |
|  | startAt | LocalDateTime | private |
|  | 과제의 시작 날짜 |  |  |
|  | dueAt | LocalDateTime | private |
|  | 과제의 마감 날짜 |  |  |
|  |  |  |  |
|  |  |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | setter | \- | public |
|  | 필드별 setter 메서드 제공 |  |  |
|  | constructor | UpdateAssignmentRequest() | public |
|  | 기본 생성자 |  |  |
|  | constructor | UpdateAssignmentRequest(...) | public |
|  | 모든 필드를 초기화하는 생성자 |  |  |

3.3.5 chat

| ChatMessageRequest |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 클라이언트로부터 수신된 채팅 메시지 요청 정보를 담는 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | content | String | private |
|  | 채팅 메시지의 실제 내용 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | setter | \- | public |
|  | 필드별 setter 메서드 제공 |  |  |
|  | constructor | ChatMessageRequest() | public |
|  | 기본 생성자 (JSON 역직렬화용) |  |  |
|  | constructor | ChatMessageRequest(String content) | public |
|  | 메시지 내용을 초기화하는 생성자 |  |  |

| ChatMessageResponse |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 채팅 메시지 정보를 클라이언트로 전달하기 위한 응답 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | id | Long | private |
|  | 메시지 고유 식별자 |  |  |
|  | userId | Long | private |
|  | 작성자(사용자)의 식별자 |  |  |
|  | nickname | String | private |
|  | 작성자의 닉네임 |  |  |
|  | profileImageUrl | String | private |
|  | 작성자의 프로필 이미지 URL |  |  |
|  | content | String | private |
|  | 메시지 내용 |  |  |
|  | createdAt | LocalDateTime | private |
|  | 메시지 생성 시각 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | builder | ChatMessageResponseBuilder | public |
|  | ChatMessageResponse 객체 생성을 위한 빌더 제공 |  |  |
|  | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | constructor | ChatMessageResponse() | public |
|  | 기본 생성자 |  |  |
|  | constructor | ChatMessageResponse(...) | public |
|  | 모든 필드를 초기화하는 생성자 |  |  |

3.3.6 common

| StudyDto |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 스터디 정보를 전달하기 위한 DTO로, StudyProfile 엔티티의 데이터를 변환하여 제공한다. |  |  |
| **구분**   | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| **Attributes** | id | Long | private |
|  | 스터디 ID |  |  |
|  | title | String | private |
|  | 스터디 제목 |  |  |
|  | maxMemberCount | int | private |
|  | 최대 인원수 |  |  |
|  | memberCount | int | private |
|  | 현재 인원수 |  |  |
|  | bookmarkCount | long | private |
|  | 북마크 수 |  |  |
|  | bio | String | private |
|  | 스터디 소개 |  |  |
|  | category | List\<Category\> | private |
|  | 스터디 카테고리 목록 |  |  |
|  | trustScore | long | private |
|  | 스터디 신뢰도 점수 |  |  |
|  | bookmarked | boolean | private |
|  | 사용자가 북마크했는지 여부 |  |  |
| **구분**   | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | StudyDto(Long, String, int, int, long, String, String, long, boolean) | \- | public |
|  | QueryDSL Projections.constructor()에서 사용되는 생성자 (String categoryString을 List\<Category\>로 변환) |  |  |
|  | StudyDto(Long, String, int, int, long, String, List\<Category\>, long, boolean) | \- | public |
|  | of() 메서드용 생성자, List\<Category\>를 직접 주입받음 |  |  |
|  | of(StudyProfile, int, long, long, boolean) | StudyDto | public static |
|  | StudyProfile 엔티티 기반으로 DTO를 생성하는 정적 팩토리 메서드 |  |  |
|  | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |

 

| FileDetailDto  |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 하나의 파일에 대한  메타 데이터를 담고 있는  DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes  | originalFileName | String | private |
|  | 파일 이름 |  |  |
|  | key | String | private |
|  | s3에서 사용되는 고유한 파일 이름 |  |  |
|  | contentType | String | private |
|  | 파일 원본의 타입 |  |  |
|  | fileSize | long | private |
|  | 파일의 크기 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | constructor | FileDetailDto(...)  | public |
|  | 모든 필드를 초기화하는 생성자  |  |  |
|  | builder | \- | public |
|  | 빌더 패턴으로 객체 생성 |  |  |

3.3.7 home

| HomeResponse |  |  |  |
| ----- | :---- | :---- | :---- |
| Class Description | 홈 화면 데이터(사용자 정보 \+ 인기 스터디 목록)를 전달하는 응답 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | user | UserDto | private |
|  | 사용자 요약 정보 DTO |  |  |
|  | topStudies | List\<StudyDto\> | private |
|  | 상위 스터디 목록 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 각 필드에 대한 getter 메서드 제공 (@Getter) |  |  |
|  | constructor | HomeResponse(UserDto user, List\<StudyDto\> topStudies) | public |
|  | 모든 필드를 초기화하는 생성자 (@AllArgsConstructor) |  |  |

3.3.8 notification

| GetNotificationsListResponse |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 알림 리스트를 받는 응답 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes  | id | Long | private |
|  | 알림 id(DB에서 구분되는 값) |  |  |
|  | title | Long | private |
|  | 알림 제목 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | setter | \- | public |
|  | 필드별 setter 메서드 제공 |  |  |
|  | constructor | GetNotificationsListResponse() | public |
|  | 기본 생성자 |  |  |
|  | constructor | GetNotificationsListResponse(...) | public |
|  | 모든 필드를 초기화하는 생성자 |  |  |

| GetNotificationDetailResponse |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 알림의 상세정보를 받는 응답 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes  | title | String | private |
|  | 알림 제목 |  |  |
|  | description | String | private |
|  | 알림 내용(본문) |  |  |
|  | CreateAt | LocalDateTime | private |
|  | 알림 생성 시간 |  |  |
|  |  |  |  |
|  |  |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | setter | \- | public |
|  | 필드별 setter 메서드 제공 |  |  |
|  | constructor | GetNotificationDetailResponse() | public |
|  | 기본 생성자 |  |  |
|  | constructor | GetNotificationDetailResponse(...) | public |

3.3.9 resource

| CreateResourceRequest |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 스터디 내 자료를 생성을 처리하기 위한 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes  | title | String  | private |
|  | 자료의 제목 |  |  |
|  | content | String  | private |
|  | 자료의 내용 |  |  |
|  | files |  List\<MultipartFile\> | default |
|  | 자료에 첨부된 파일 목록 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | setter | \- | public |
|  | 필드별 setter 메서드 제공 |  |  |
|  | constructor | CreateResourceRequest() | public |
|  | 기본 생성자 |  |  |
|  | constructor | CreateResourceRequest(...) | public |
|  | 모든 필드를 초기화하는 생성자 |  |  |

| GetResourceDetailResponse |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 스터디 내 자료 상세 데이터를 처리하기 위한 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes  | title | String | private |
|  | 자료 제목 |  |  |
|  | content | String | private |
|  | 자료 내용 |  |  |
|  | author | String | private |
|  | 자료 작성자 이름 |  |  |
|  | profileUrl | String | private |
|  | 자료 작성자 프로필 URL |  |  |
|  | createdAt | LocalDateTime | private |
|  | 자료 작성 날짜 |  |  |
|  | files | List\<ResourceDetailFileDto\> | private |
|  | 자료 내 첨부된 파일 목록 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | setter | \- | public |
|  | 필드별 setter 메서드 제공 |  |  |
|  | constructor | GetResourceDetailResponse(...) | public |
|  | 모든 필드를 초기화하는 생성자 |  |  |

| GetResourcesResponse |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 스터디 내 자료 목록 화면에서 자료 하나를 나타내는 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes  | resourceId | Long | private |
|  | 자료 고유 id |  |  |
|  | title | String | private |
|  | 자료 제목 |  |  |
|  | userName | String | private |
|  | 자료 작성자 |  |  |
|  | createdAt | LocalDateTime | private |
|  | 자료 생성일 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | setter | \- | public |
|  | 필드별 setter 메서드 제공 |  |  |
|  | builder | Builder | public |
|  | 빌더 패턴으로 객체 생성 (@Builder) |  |  |

| ResourceDetailFileDto |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 자료에 첨부된 파일 하나를 나타내는 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes  | fileId | Long | private |
|  | 파일 고유 ID |  |  |
|  | fileName | String | private |
|  | 파일 원본 이름 |  |  |
|  | fileUrl | String | private |
|  | 파일을 다운받을 수 있는 Url |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | constructor | ResourceDetailFileDto(fileId, fileName,fileUrl) | public |
|  | 모든 필드를 초기화하는 생성자 (@AllArgsConstructor) |  |  |

| UpdateResourceRequest |  |  |  |
| :---- | :---- | :---- | :---- |
| Class Description | 스터디 내 자료를 업데이트 하기 위한 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes  | title | String | private |
|  | 자료 제목 |  |  |
|  | content | String | private |
|  | 자료 내용 |  |  |
|  | files | List\<MultipartFile\> | private |
|  | 자료에 새롭게 첨부된 파일 목록 |  |  |
|  | deleteFileIds | List\<Long\> | private |
|  | 자료에서 삭제할 파일의 ID 목록 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 필드별 getter 메서드 제공 |  |  |
|  | setter | \- | public |
|  | 각 필드에 대한 setter 메서드 제공  |  |  |
|  | constructor | UpdateResourceRequest() | public |
|  | 기본 생성자 |  |  |
|  | constructor | UpdateResourceRequest(title,content,files,deleteFileIds) | public |
|  | 모든 필드를 초기화하는 생성자 (@AllArgsConstructor) |  |  |

3.3.10 study

| CreateStudyRequest |  |  |  |
| ----- | :---- | :---- | :---- |
| Class Description | 스터디 그룹을 생성할 때 사용되는 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | title | String | private |
|  | 스터디 제목 |  |  |
|  | category | List\<Category\> | private |
|  | 카테고리 필터 |  |  |
|  | province | String | private |
|  | 지역(도 단위) 필터 |  |  |
|  | district | String | private |
|  | 세부 지역(시/군/구) 필터 |  |  |
|  | bio | String | private |
|  | 짧은 소개 |  |  |
|  | description | String | private |
|  | 긴 소개 |  |  |
|  | maxMemberCount | Integer | private |
|  | 최대 인원 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 각 필드에 대한 getter 메서드 제공 (@Getter) |  |  |
|  | constructor | \- | public |
|  | 기본 생성자 |  |  |

| GetStudyMemberResponse |  |  |  |
| ----- | :---- | :---- | :---- |
| Class Description | 스터디멤버목록을 조회할때 사용하는 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | studyId | Long | private |
|  | 스터디 Id |  |  |
|  | members | List\<StudyMemberDto\> | private |
|  | 스터디 멤버 목록 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 각 필드에 대한 getter 메서드 제공 (@Getter) |  |  |
|  | setter | \- | public |
|  | 각 필드에 대한 setter 메서드 제공 (@Setter) |  |  |
|  | constructor | \- | public |
|  | 기본 생성자 |  |  |

| GetStudyProfileResponse |  |  |  |
| ----- | :---- | :---- | :---- |
| Class Description | 스터디 그룹 프로필 정보를 조회하는 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | id | Long | private |
|  | 스터디 id |  |  |
|  | title | String | private |
|  | 스터디 제목 |  |  |
|  | category | List\<Category\> | private |
|  | 카테고리 필터 |  |  |
|  | province | String | private |
|  | 지역(도 단위) 필터 |  |  |
|  | district | String | private |
|  | 세부 지역(시/군/구) 필터 |  |  |
|  | bio | String | private |
|  | 짧은 소개 |  |  |
|  | description | String | private |
|  | 긴 소개 |  |  |
|  | maxMemberCount | Integer | private |
|  | 최대 인원 |  |  |
|  | memberCount | Integer | private |
|  | 최대 인원 |  |  |
|  | recruitStatus | RecruitStatus | private |
|  | 모집 상태  |  |  |
|  | trustScore | Integer | private |
|  | 신뢰 점수 |  |  |
|  | applicationStatus | String | private |
|  | 지원서 상태 |  |  |
|  | canApply | boolean | private |
|  | 지원 가능 여부 |  |  |
|  | leader | LeaderProfile | private |
|  | 그룹장 프로필  |  |  |
|  | id | Long | private |
|  | 그룹장 id |  |  |
|  | nickname | String | private |
|  | 그룹장 닉네임 |  |  |
|  | profileImageUrl | String | private |
|  | 그룹장 프로필 이미지 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 각 필드에 대한 getter 메서드 제공 (@Getter) |  |  |
|  | constructor | \- | public |
|  | 기본 생성자 |  |  |

| StudyHomeResponse |  |  |  |
| ----- | :---- | :---- | :---- |
| Class Description | 스터디 그룹 타이틀 조회 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | title | String | private |
|  | 스터디 타이틀 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 각 필드에 대한 getter 메서드 제공 (@Getter) |  |  |
|  | constructor | \- | public |
|  | 기본 생성자 |  |  |

| StudyMemberResponse |  |  |  |
| ----- | :---- | :---- | :---- |
| Class Description | 스터디멤버를 조회할때 사용하는 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | userId | Long | private |
|  | 유저 id |  |  |
|  | nickname | String | private |
|  | 닉네임 |  |  |
|  | profileImageUrl | String | private |
|  | 프로필이미지 |  |  |
|  | role | String | private |
|  | 스터디 멤버 역할(리더, 멤버) |  |  |
|  | lastLoginAt | LocalDateTime | private |
|  | 마지막 로그인 시간 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 각 필드에 대한 getter 메서드 제공 (@Getter) |  |  |
|  | setter | \- | public |
|  | 각 필드에 대한 setter 메서드 제공 (@Setter) |  |  |
|  | constructor | \- | public |
|  | 기본 생성자 |  |  |

| UpdateStudyProfileRequest |  |  |  |
| ----- | :---- | :---- | :---- |
| Class Description | 스터디 그룹  프로필을  수정할 때 사용되는 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | title | String | private |
|  | 스터디 제목 |  |  |
|  | category | List\<Category\> | private |
|  | 카테고리 필터 |  |  |
|  | province | String | private |
|  | 지역(도 단위) 필터 |  |  |
|  | district | String | private |
|  | 세부 지역(시/군/구) 필터 |  |  |
|  | bio | String | private |
|  | 짧은 소개 |  |  |
|  | description | String | private |
|  | 긴 소개 |  |  |
|  | maxMemberCount | Integer | private |
|  | 최대 인원 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getter | \- | public |
|  | 각 필드에 대한 getter 메서드 제공 (@Getter) |  |  |
|  | constructor | \- | public |
|  | 기본 생성자 |  |  |

| SearchStudiesRequest |  |  |  |
| ----- | :---- | :---- | :---- |
| Class Description | 스터디 검색 요청의 필터/정렬/페이징 정보를 담는 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | keyword | String | private |
|  | 검색 키워드(제목/소개 부분 일치) |  |  |
|  | category | Category | private |
|  | 카테고리 필터 |  |  |
|  | province | String | private |
|  | 지역(도 단위) 필터 |  |  |
|  | district | String | private |
|  | 세부 지역(시/군/구) 필터 |  |  |
|  | page | Integer | private |
|  | 요청 페이지 (기본값 1\) |  |  |
|  | limit | Integer | private |
|  | 페이지 당 개수 (기본값 10\) |  |  |
|  | sort | StudySortType | private |
|  | 정렬 타입 (기본값 LATEST) |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | getPageOrDefault | int | public |
|  | page가 유효하면 (page-1) 반환, 아니면 0 반환 (0-based 페이지 인덱스) |  |  |
|  | getLimitOrDefault | int | public |
|  | limit가 유효하면 limit, 아니면 10 반환 |  |  |
|  | getter | \- | public |
|  | 각 필드에 대한 getter 메서드 제공 (@Getter) |  |  |
|  | setter | \- | public |
|  | 각 필드에 대한 setter 메서드 제공 (@Setter) |  |  |

| SearchStudiesResponse |  |  |  |
| ----- | :---- | :---- | :---- |
| Class Description | 스터디 검색 결과와 메타데이터(페이지/정렬)를 포함하는 응답 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | studies | List\<StudyDto\> | private |
|  | 검색 결과 스터디 목록 (공용 DTO 사용) |  |  |
|  | meta | Meta | private |
|  | 페이지네이션 및 정렬 정보를 담는 메타 데이터 |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | builder | SearchStudiesResponseBuilder | public |
|  | 빌더 패턴으로 객체 생성 (@Builder) |  |  |
|  | getter | \- | public |
|  | 각 필드에 대한 getter 제공 (@Getter) |  |  |
|  | constructor | SearchStudiesResponse(List\<StudyDto\> studies, Meta meta) | public |
|  | 모든 필드를 초기화하는 생성자 (@AllArgsConstructor) |  |  |

| GetStudiesResponse |  |  |  |
| ----- | :---- | :---- | :---- |
| Class Description | 내 찜 목록과 내 그룹 정보를 포함하는 응답 DTO |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Attributes | studies | List\<StudyDto\> | private |
|  | 결과 스터디 목록 (공용 DTO 사용) |  |  |
| **구분** | **Name** | **Type** | **Visibility** |
|  | **Description** |  |  |
| Operations | builder | GetStudiesResponseBuilder | public |
|  | 빌더 패턴으로 객체 생성 (@Builder) |  |  |
|  | getter | \- | public |
|  | 각 필드에 대한 getter 제공 (@Getter) |  |  |
|  | constructor | GetStudiesResponse(List\<StudyDto\> studies) | public |
|  | 모든 필드를 초기화하는 생성자 (@AllArgsConstructor) |  |  |
