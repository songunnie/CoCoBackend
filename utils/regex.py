import re


# 아이디 패턴 부합 여부 체크 함수
def check_id(id):
    pattern = re.compile('^[A-Za-z0-9_\.\-]+@[A-Za-z0-9\-]+\.[A-Za-z0-9\-]+')

    return pattern.match(id) != None


# 비밀번호 패턴 부합 여부 체크 함수
def check_password(password):
    pattern = re.compile('^(?=.*\d)(?=.*[a-zA-Z])[0-9a-zA-Z]{8,20}$')

    return pattern.match(password) != None
