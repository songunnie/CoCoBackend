import hashlib

from flask import Blueprint, current_app, jsonify, request

from presets.status import STATUS_CODE, STATUS_MESSAGE

# Flask Blueprint 생성
bp = Blueprint('register_user', __name__, url_prefix='/user')


# 회원가입 라우터 정의
@bp.route('', methods=['POST'])
def register_user():
    id = request.form.get('id')

    users = current_app.db.users.find_one({'id': id})

    if users == None:
        return jsonify({
            'status': STATUS_MESSAGE['BAD_REQUEST']
        }), STATUS_CODE['BAD_REQUEST']

    if not users['verification'] == True:
        return jsonify({
            'status': STATUS_MESSAGE['BAD_REQUEST']
        }), STATUS_CODE['BAD_REQUEST']

    password = request.form.get('password')
    nickname = request.form.get('nickname')
    github_url = request.form.get('github_url')
    portfolio_url = request.form.get('portfolio_url')
    tech_stack = request.form.getlist('tech_stack[]')

    if id == None or id == '':
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('id')
        }), STATUS_CODE['INVALID_PARAM']

    if password == None or password == '':
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('password')
        }), STATUS_CODE['INVALID_PARAM']

    if nickname == None or nickname == '':
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('nickname')
        }), STATUS_CODE['INVALID_PARAM']

    if len(tech_stack) == 0:
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('tech_stack')
        }), STATUS_CODE['INVALID_PARAM']

    pw_hash = hashlib.sha256(password.encode('utf-8')).hexdigest()

    current_app.db.users.update_one({'id': id},
                                    {'$set': {
                                        'password': pw_hash,
                                        'nickname': nickname,
                                        'github_url': github_url,
                                        'portfolio_url': portfolio_url,
                                        'tech_stack': tech_stack
                                    }})

    return jsonify({
        'status': STATUS_MESSAGE['SUCCESS']
    }), STATUS_CODE['SUCCESS']
