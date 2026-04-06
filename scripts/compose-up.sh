#!/bin/bash
# ================================================================
# Day 17 - 풀스택 기동 스크립트
#
# 실행 순서:
#   1) ./gradlew bootJar   — 실행 가능한 jar 빌드
#   2) docker compose up -d --build  — 이미지 빌드 + 컨테이너 기동
#
# IntelliJ Run Configuration > Shell Script 에서 이 파일을 지정합니다.
# Working directory 를 반드시 프로젝트 루트($ProjectFileDir$)로 설정해야
# Gradle wrapper와 docker-compose.yml 경로가 올바르게 잡힙니다.
# ================================================================
set -e   # 중간 명령 실패 시 즉시 종료 (bootJar 실패 → compose up 건너뜀)

export PATH=$PATH:/home/ssm-user/.local/bin

MODE=${1:-local}

if [ "$MODE" == "prod" ]; then
  echo "=== [0/3] [PROD 모드] AWS Parameter Store 환경변수 로드 ==="
  # EC2 배포 환경: AWS에서 값을 땡겨와서 메모리에 올림
  export DATABASE_URL=$(aws ssm get-parameter --name "/prod/damm/DATABASE_URL" --with-decryption --query "Parameter.Value" --output text)
  export DATABASE_PASSWORD=$(aws ssm get-parameter --name "/prod/damm/DATABASE_PASSWORD" --with-decryption --query "Parameter.Value" --output text)
  export JWT_SECRET_KEY=$(aws ssm get-parameter --name "/prod/damm/JWT_SECRET_KEY" --with-decryption --query "Parameter.Value" --output text)

  export SPRING_PROFILES_ACTIVE=prod
  export SPRING_DATA_REDIS_HOST=redis
  export DATABASE_USERNAME=root

else
  echo "=== [0/3] [LOCAL 모드] 기존 로컬 .env 파일 사용 ==="
  # 로컬 환경: docker-compose가 알아서 프로젝트 폴더의 로컬 .env를 읽음
  export SPRING_PROFILES_ACTIVE=docker

fi

echo "=== [1/2] Gradle bootJar 빌드 시작 ==="
./gradlew bootJar

echo "=== [2/2] Docker Compose 풀스택 기동 ==="
#docker-compose up -d --build
/usr/libexec/docker/cli-plugins/docker-compose up -d --build


echo "=== 기동 완료 — 앱 로그 출력 시작 (중지: Ctrl+C) ==="
#docker-compose logs -f app
/usr/libexec/docker/cli-plugins/docker-compose logs -f app