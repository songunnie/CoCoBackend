import jwt


# JWT 파싱 함수
def parse_token(token, secret_key, algorithms):
    if token == None:
        return None

    try:
        payload = jwt.decode(token, secret_key, algorithms=algorithms)
    except (jwt.DecodeError, jwt.ExpiredSignatureError):
        return None

    return payload
