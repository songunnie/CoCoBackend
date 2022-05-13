# CoCo

**IT 프로젝트 구인 플랫폼**

IT 프로젝트 구인 서비스로 원하는 프로젝트를 직접 기획하고, 같이 수행할 개발자와 디자이너를 모집할 수 있는 플랫폼입니다.

## 요구 사항

### Python

의존성 패키지는 requirements.txt에 명시되어 있습니다.

``` bash
pip install -r requirements.txt
```

### 환경 변수

``` bash
# MongoDB 환경 변수
export MONGODB_HOST="<MONGODB HOST>"
export MONGODB_PORT="<MONGODB PORT>"
export MONGODB_USER="<MONGODB USER>"
export MONGODB_PASSWORD="<MONGODB PASSWORD>"

# JWT 환경 변수
export JWT_SECRET_KEY="<JWT SECRET KEY>"

# Email 환경 변수
export MAIL_SERVER="<MAIL SERVER>"
export MAIL_PORT="<MAIL PORT>"
export MAIL_ID="<MAIL ID>"
export MAIL_PASSWORD="<MAIL PASSWORD>"

# AWS 환경 변수
export AWS_ACCESS_KEY_ID="<AWS IAM ACCESS KEY ID>"
export AWS_SECRET_ACCESS_KEY="<AWS IAM SECRET ACCESS KEY>"
export AWS_S3_BUCKET_NAME="<AWS S3 BUCKET NAME>"

# 이미지 저장 경로 환경 변수
export STORAGE_HOST="<IMAGE SAVE PATH>"
```

## 사용법

``` bash
python app.py
```