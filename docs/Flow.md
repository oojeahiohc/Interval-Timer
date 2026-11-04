- app/src/main/AndroidManifest.xml
    - Manifest에서 어느 Activity를 실행할지 확인
- app/src/main/.../ui/MainActivity.java
    - onCreate(): Activity가 생성될 때 초기 설정
    - setContentView(...): Activity에서 사용할 화면을 지정
        ```
        <!-- XML -->
        android:id="@+id/addRoutineButton"

        <!-- Java -->
        Button addButton = findViewById(R.id.addRoutineButton);
        <!-- R은 Android 빌드 도구가 리소스를 바탕으로 생성하는 클래스 -->
        ```
        - XML은 화면 요소의 배치, ID, 입력 속성, 디자인을 정의한다.
        - Java는 클릭 이벤트, 입력 검증, 데이터 요청, 화면 이동과 갱신을 처리한다.
---
- app/src/main/.../data/
    - AppDatabase.java: 실제 Room 데이터베이스 생성/공유
    - Routine.java: 루틴 하나에 어떤 값이 들어가는지 정의
    - RoutineDao.java: 저장/조회/수정/삭제 작업 정의
    - RoutineRepository.java: 화면에서 사용할 데이터 작업 제공, 비동기 작업 연결(화면과 데이터베이스 사이를 연결하는 클래스)
- Room은 안드로이드 기기 내부의 SQLite 데이터베이스를 Java/Kotlin 코드로 훨씬 쉽고 안전하게 다룰 수 있게 감싸놓은 라이브러리(도구)이다.
- 비동기 처리
    - 메인 스레드: 화면 표시와 사용자 입력 처리
    - DB_EXECUTOR: 저장/수정/삭제를 별도 작업 스레드에서 실행
    - MAIN_HANDLER: DB 작업 결과 콜백을 메인 스레드에서 실행
    - DAO의 LiveData 조회는 Room이 별도 스레드에서 실행을 관리
- DB 작업으로 화면이 멈추지 않도록 하고, 화면 갱신은 메인 스레드에서 처리한다.
---
- 데이터를 가져와서 화면에 표시하는 역할
    - RecyclerView: 목록을 배치하고 스크롤/항목 재사용을 관리
    - RoutineAdapter: 루틴 데이터를 각 카드의 글자와 버튼에 연결
        - onCreateViewHolder(): 카드 View가 새로 필요할 때 생성
        - onBindViewHolder(): 해당 카드에 특정 루틴 데이터 연결
        - getItemCount(): 표시할 데이터 개수 반환
    - item_routine.xml: 카드 한 장의 배치와 모양
---
- 데이터 저장 흐름
    1. 저장 버튼 클릭
    2. RoutineEditActivity.saveRoutine()
    3. 입력값 검사 후 Routine 객체 생성
    4. 신규·수정 여부에 따라 저장 요청
        - 신규: Repository.insertRoutine()
        - 수정: 기존 ID를 유지하고 Repository.updateRoutine()
    5. Repository가 별도 작업 스레드에서 DAO 호출
    6. Room을 통해 DB에 저장
    - 저장 결과 처리
        1. Repository가 결과를 메인 스레드로 전달
        2. 편집 화면의 콜백 실행
        3. 성공하면 finish()로 편집 화면 종료
    - 목록 갱신
        1. Room이 테이블 변경을 감지
        2. 목록을 다시 조회해 LiveData로 전달
        3. MainActivity가 활성 상태에서 observe 콜백 실행
        4. Adapter에 새 목록 전달
        5. RecyclerView의 카드 목록 갱신
    - 두 흐름의 실행 순서는 항상 고정되지 않는다.
- 데이터 삭제 흐름
    1. 삭제 버튼
    2. Adapter가 삭제 요청 전달
    3. MainActivity가 확인 대화상자 표시
    4. 확인 버튼
    5. Repository.deleteRoutine()
    6. DAO 삭제
    7. 목록 LiveData 변경
    8. 카드 목록 갱신