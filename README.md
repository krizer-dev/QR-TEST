# QR Code 테스트 앱

이 프로젝트는 크라이저 RK3288 및 RK3399 기반 Android 기기에서 QR 코드 리딩 기능을 테스트하기 위한 Android 앱 소스입니다.


## 지원 기기

- **RK3288 Android Board**
- **RK3399 Android Board**


## 🔧 직렬 포트 정보 (Serial Port Info)

기기 모델별 기본 사용 포트는 아래와 같습니다:

| 모델      | 직렬 포트 (Serial Port) |
|-----------|--------------------------|
| RK3288    | `/dev/ttyS4`             |
| RK3399    | `/dev/ttysWK0`           |

Baud rate는 **9600bps**로 설정

---

## 📦 주요 기능

- QR 코드 스캔 테스트
- Serial 통신을 통한 QR 데이터 송신
- Baud rate 및 포트 설정 지원

---

## 🔧 설정 방법

앱 내 설정 화면에서 아래 항목을 설정할 수 있습니다:

- **Device**: 사용할 직렬 포트 선택 (`/dev/ttyS4`, `/dev/ttysWK0` 등)
- **Baud rate**: 9600 (고정)
