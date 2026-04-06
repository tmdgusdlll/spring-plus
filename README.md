# SPRING PLUS

## 12. AWS 활용

#### **12-1. EC2 & Health Check**
고정된 접속 주소를 위해 **탄력적 IP(EIP)**를 설정하였습니다.

- **탄력적 IP**: `3.39.154.179`
- **Health Check API**: `GET http://3.39.154.179:8080/health`
- **상태**: Spring Security `permitAll()` 설정을 통해 인증 없이 접근 가능

| 항목 | 상세 정보 |
|------|------|
| 인스턴스 ID | i-07a8bd5b64cc3c8d6 |
| 실행 환경 | Docker 컨테이너 기반 (Spring Boot 3.x) |

[EC2 설정]

<img width="600" height="300" alt="Pasted Graphic 4" src="https://github.com/user-attachments/assets/47ace640-9157-416b-bb2b-e6b054e0238f" />


[헬스체크 인증]

<img width="600" height="300" alt="3 39 154 1798080health" src="https://github.com/user-attachments/assets/27c82ea1-47a5-441d-b047-12fadbbce8f1" />


#### **12-2. RDS 연결**
애플리케이션의 데이터 영속성을 위해 관리형 DB 서비스인 RDS를 연동하였습니다. EC2 보안 그룹 설정을 통해 애플리케이션 서버와의 안전한 통신을 보장합니다.

- **엔드포인트**: `plus-database.czykgqcswy2z.ap-northeast-2.rds.amazonaws.com`
- **포트**: `3306`

[RDS 설정 화면]

<img width="600" height="300" alt="Pasted Graphic 1" src="https://github.com/user-attachments/assets/ad7fbee8-1618-4e56-8431-deb7d790c583" />


#### **12-3. S3 & Presigned URL (파일 업로드)**
**Presigned URL** 을 적용하였습니다. 

[Presigned URL 발급] 

추가 예정

[S3 업로드 확인]

<img width="600" height="300" alt="Pasted Graphic 3" src="https://github.com/user-attachments/assets/c93b4a67-0585-41f8-8a14-546a2c9d0122" />


---

## 13. 대용량 데이터 처리 성능 최적화

### 테스트 환경
- 데이터 건수: 500만 건
- 검색 조건: 닉네임 정확히 일치

---

### DB 조회 속도 비교 (execution 시간)

| 방법 | execution 시간 | 개선율 |
|------|:------------:|:------:|
| 인덱스 X | 689ms | - |
| 인덱스 O | 4ms | **약 172배 향상** |

---

### API 응답 속도 비교 (Postman 기준)

| 방법 | 첫 조회 | 두 번째 조회 (반복) |
|------|:------:|:----------:|
| 인덱스 X, 캐시 X | 726ms | 726ms |
| 인덱스 X, 캐시 O | 726ms | **~9ms** |
| 인덱스 O, 캐시 O | **48ms** | **~7ms** |

---

### 최종 결론
- **인덱스(Index)**: Full Table Scan을 Index Range Scan으로 전환하여 DB 레벨의 근본적인 조회 성능을 획기적으로 개선.
- **캐시(Caffeine)**: 자주 조회되는 데이터를 로컬 메모리에 저장하여 네트워크 및 DB I/O 비용을 제거.
- **최적화 전략**: 인덱스를 통해 '첫 번째 요청'의 지연을 방지하고, 캐시를 통해 '반복되는 요청'의 응답성을 극대화하는 하이브리드 전략을 수립함.
