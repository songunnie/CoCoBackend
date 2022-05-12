import os

# 환경 변수에서 이메일 정보 읽기
config = {
    'server': os.getenv('MAIL_SERVER'),
    'port': int(os.getenv('MAIL_PORT')),
    'id': os.getenv('MAIL_ID'),
    'password': os.getenv('MAIL_PASSWORD')
}
