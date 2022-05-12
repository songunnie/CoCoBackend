import os

# 환경 변수에서 AWS 연결 정보 읽기
config = {
    'access_key_id': os.getenv('AWS_ACCESS_KEY_ID'),
    'secret_access_key': os.getenv('AWS_SECRET_ACCESS_KEY'),
    's3_bucket_name': os.getenv('AWS_S3_BUCKET_NAME')
}
