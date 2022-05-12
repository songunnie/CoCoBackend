import os

# 환경 변수에서 저장소 연결 정보 읽기
config = {
    'host': os.getenv('STORAGE_HOST')
}
