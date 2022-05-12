from datetime import datetime, timedelta

from flask import Blueprint, current_app, jsonify, request

from presets.status import STATUS_CODE, STATUS_MESSAGE

import hashlib
import jwt

bp = Blueprint('login', __name__, url_prefix='/login')


# 로그인 라우터
@bp.route('', methods=['POST'])
def login():
    id = request.form.get('id')
    password = request.form.get('password')

    if id == None or id == '':
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('id')
        }), STATUS_CODE['INVALID_PARAM']

    if password == None or password == '':
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('password')
        }), STATUS_CODE['INVALID_PARAM']

    user = current_app.db.users.find_one({'id': id})
    hashed_password = hashlib.sha256(password.encode('utf-8')).hexdigest()

    if user == None:
        return jsonify({
            'status': STATUS_MESSAGE['BAD_REQUEST']
        }), STATUS_CODE['BAD_REQUEST']

    if user['validation'] != 'valid':
        return jsonify({
            'status': STATUS_MESSAGE['BAD_REQUEST']
        }), STATUS_CODE['BAD_REQUEST']

    if user['password'] != hashed_password:
        return jsonify({
            'status': STATUS_MESSAGE['UNAUTHORIZED_USER']
        }), STATUS_CODE['UNAUTHORIZED_USER']

    payload = {
        'id': id,
        'exp': datetime.utcnow() + timedelta(seconds=60 * 60 * 24)
    }

    token = jwt.encode(payload, current_app.jwt_secret_key, algorithm='HS256')

    return jsonify({
        'status': STATUS_MESSAGE['SUCCESS'],
        'token': token
    }), STATUS_CODE['SUCCESS']
