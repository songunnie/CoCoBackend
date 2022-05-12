from conf import storage

from flask import Blueprint, current_app, json, jsonify, request

from presets.status import STATUS_CODE, STATUS_MESSAGE

from utils.regex import check_password
from utils.token import decode_token

import hashlib

bp = Blueprint('user', __name__, url_prefix='/user')


# 회원 가입 라우터
@bp.route('', methods=['POST'])
def register():
    id = request.form.get('id')
    password = request.form.get('password')
    nickname = request.form.get('nickname')
    github_url = request.form.get('github_url')
    portfolio_url = request.form.get('portfolio_url')
    tech_stacks = request.form.getlist('tech_stacks[]')

    if id == None or id == '':
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('id')
        }), STATUS_CODE['INVALID_PARAM']

    if password == None or password == '' or check_password(password) == False:
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('password')
        }), STATUS_CODE['INVALID_PARAM']

    if nickname == None or nickname == '':
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('nickname')
        }), STATUS_CODE['INVALID_PARAM']

    if github_url == None:
        github_url = ''

    if portfolio_url == None:
        portfolio_url = ''

    if len(tech_stacks) == 0:
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('tech_stacks')
        }), STATUS_CODE['INVALID_PARAM']

    user = current_app.db.users.find_one({'id': id})

    if user == None:
        return jsonify({
            'status': STATUS_MESSAGE['BAD_REQUEST']
        }), STATUS_CODE['BAD_REQUEST']

    if user['validation'] != 'invalid':
        return jsonify({
            'status': STATUS_MESSAGE['BAD_REQUEST']
        }), STATUS_CODE['BAD_REQUEST']

    hashed_password = hashlib.sha256(password.encode('utf-8')).hexdigest()

    doc = {
        'password': hashed_password,
        'nickname': nickname,
        'image': f'{storage.config["host"]}/images/profile/default.png',
        'github_url': github_url,
        'portfolio_url': portfolio_url,
        'tech_stacks': tech_stacks,
        'introduction': '',
        'validation': 'valid'
    }

    current_app.db.users.update_one({'id': id}, {'$set': doc})

    return jsonify({
        'status': STATUS_MESSAGE['SUCCESS']
    }), STATUS_CODE['SUCCESS']


# 회원 정보 획득 라우터
@bp.route('/<user_id>', methods=['GET'])
def get_user(user_id):
    token = request.cookies.get('token')
    payload = decode_token(token, current_app.jwt_secret_key, 'HS256')

    if payload == None:
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_TOKEN']
        }), STATUS_CODE['INVALID_TOKEN']

    if current_app.db.users.find_one({'id': payload['id']}) == None:
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_TOKEN']
        }), STATUS_CODE['INVALID_TOKEN']

    user = current_app.db.users.find_one({'id': user_id}, {'_id': False})

    if user == None:
        return jsonify({
            'status': STATUS_MESSAGE['BAD_REQUEST']
        }), STATUS_CODE['BAD_REQUEST']

    return jsonify(**json.loads(json.htmlsafe_dumps({
        'status': STATUS_MESSAGE['SUCCESS'],
        'user': user
    }))), STATUS_CODE['SUCCESS']


# 회원 탈퇴 라우터
@bp.route('', methods=['DELETE'])
def delete_user():
    token = request.cookies.get('token')
    payload = decode_token(token, current_app.jwt_secret_key, 'HS256')

    if payload == None:
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_TOKEN']
        }), STATUS_CODE['INVALID_TOKEN']

    if current_app.db.users.find_one({'id': payload['id']}) == None:
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_TOKEN']
        }), STATUS_CODE['INVALID_TOKEN']

    password = request.headers.get('password')

    if password == None or password == '' or check_password(password) == False:
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('password')
        }), STATUS_CODE['INVALID_PARAM']

    user = current_app.db.users.find_one({'id': payload['id']})
    hashed_password = hashlib.sha256(password.encode('utf-8')).hexdigest()

    if user['validation'] != 'valid':
        return jsonify({
            'status': STATUS_MESSAGE['BAD_REQUEST']
        }), STATUS_CODE['BAD_REQUEST']

    if user['password'] != hashed_password:
        return jsonify({
            'status': STATUS_MESSAGE['UNAUTHORIZED_USER']
        }), STATUS_CODE['UNAUTHORIZED_USER']

    current_app.db.users.update_one({'id': payload['id']}, {'$set': {'validation': 'leave'}})

    return jsonify({
        'status': STATUS_MESSAGE['SUCCESS']
    }), STATUS_CODE['SUCCESS']
