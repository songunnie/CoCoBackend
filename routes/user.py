from conf import aws, storage

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


# 회원 정보 수정 라우터
@bp.route('', methods=['PUT'])
def update_user():
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

    password = request.form.get('password')
    new_password = request.form.get('new_password')
    nickname = request.form.get('nickname')
    image = request.files.get('image')
    github_url = request.form.get('github_url')
    portfolio_url = request.form.get('portfolio_url')
    tech_stacks = request.form.getlist('tech_stacks[]')
    introduction = request.form.get('introduction')

    if password == None or password == '' or check_password(password) == False:
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('password')
        }), STATUS_CODE['INVALID_PARAM']

    if new_password != None and (new_password == '' or check_password(new_password) == False):
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('new_password')
        }), STATUS_CODE['INVALID_PARAM']

    if nickname != None and nickname == '':
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('nickname')
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

    doc = {
        'password': user['password'],
        'nickname': user['nickname'],
        'image': user['image'],
        'github_url': user['github_url'],
        'portfolio_url': user['portfolio_url'],
        'tech_stacks': user['tech_stacks'],
        'introduction': user['introduction'],
    }

    if new_password != None:
        doc['password'] = hashlib.sha256(new_password.encode('utf-8')).hexdigest()

    if nickname != None:
        doc['nickname'] = nickname

    if image != None:
        image_name = hashlib.sha256(payload['id'].encode('utf-8')).hexdigest()
        image_ext = image.filename.split('.')[-1]
        image_path = f'images/profile/{image_name}.{image_ext}'

        current_app.s3.put_object(
            Bucket=aws.config['s3_bucket_name'],
            ACL='public-read',
            Key=image_path,
            Body=image,
            ContentType=image.content_type
        )

        doc['image'] = f'{storage.config["host"]}/{image_path}'

    if github_url != None:
        doc['github_url'] = github_url

    if portfolio_url != None:
        doc['portfolio_url'] = portfolio_url

    if len(tech_stacks) != 0:
        doc['tech_stacks'] = tech_stacks

    if introduction != None:
        doc['introduction'] = introduction

    current_app.db.users.update_one({'id': payload['id']}, {'$set': doc})

    return jsonify({
        'status': STATUS_MESSAGE['SUCCESS']
    }), STATUS_CODE['SUCCESS']


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
