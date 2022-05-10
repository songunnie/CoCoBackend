import jwt


# JWT 디코딩 함수
def decode_token(token, secret_key, algorithms):
    if token == None:
        return None

    try:
        payload = jwt.decode(token, secret_key, algorithms=algorithms)
    except (jwt.DecodeError, jwt.ExpiredSignatureError):
        return None

    return payload
