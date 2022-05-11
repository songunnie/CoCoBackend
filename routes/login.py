import hashlib
from datetime import datetime, timedelta

import jwt
from flask import Blueprint, current_app, jsonify, request, json

from presets.status import STATUS_CODE, STATUS_MESSAGE

# Flask Blueprint 생성
bp = Blueprint('login_user', __name__, url_prefix='/login')


# 로그인 라우터 정의
@bp.route('', methods=['POST'])
def login_user():
    id = request.form['id']
    password = request.form['password']

    if id == None or id == '':
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('id')
        }), STATUS_CODE['INVALID_PARAM']

    if password == None or password == '':
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('password')
        }), STATUS_CODE['INVALID_PARAM']

    pw_hash = hashlib.sha256(password.encode('utf-8')).hexdigest()

    user = current_app.db.users.find_one({'id': id, 'password': pw_hash})

    if user is not None:
        payload = {
            'id': id,
            'exp': datetime.utcnow() + timedelta(seconds=60 * 60 * 24)
        }
        token = jwt.encode(payload, current_app.jwt_secret_key, algorithm='HS256')
        print(token)
        return jsonify(**json.loads(json.htmlsafe_dumps({
            'status': STATUS_MESSAGE['SUCCESS'],
            'token': token
        }))), STATUS_CODE['SUCCESS']
    else:
        return jsonify({
            'status': STATUS_MESSAGE['BAD_REQUEST']
        }), STATUS_CODE['BAD_REQUEST']
