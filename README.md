Project Year : 2018 <br />
Project name : WIFI 네트워크 취약점 점검 어플리케이션 <br />
TARGET : ANDROID 6.0 ~ 9.0 <br />
ENVIRONMENT :  WINDOWS / JAVA

--- Description ---
1. WIFI가 연결되어 있을 경우 다음 검사를 진행합니다.
    - WIFI 비밀번호 입력 받고 복잡도 파악
    - WIFI 환경 파악 (SSID, OPEN PORT, 암호형식)
    - WIFI(공유기)와 연결된 기기(IOT)를 탐색
    - 연결된 기기(IOT)를 선택하여 검사 (환경 파악 - OPEN PORT, 암호형식)
    - 연결된 기기(IOT)의 비밀번호를 입력 받고 복잡도 파악
    - 비밀번호가 자주사용하는 비밀번호 1만개와 일치할경우 경고합니다.
2. 다음 기술 특징이 있습니다.
    - Thread를 사용하여 검사시간을 1분 미만으로 진행합니다.
    - async를 사용하여 사용자에게 실시간 진행 상태를 알립니다.
    - 정규식으로 파일을 탐색하여 Delay를 줄입니다.
3. 검사 로그를 저장하고 불러올 수 있습니다.
