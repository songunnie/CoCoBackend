import os

# 환경 변수에서 JWT 비밀 키 읽기
config = {
    'secret_key': os.getenv('JWT_SECRET_KEY')
}
