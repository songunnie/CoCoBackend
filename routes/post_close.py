import jwt
from bson import ObjectId
from flask import Blueprint, current_app, jsonify, request, json

from presets.status import STATUS_CODE, STATUS_MESSAGE

# Flask Blueprint 생성
bp = Blueprint('recruitment_close', __name__, url_prefix='/post')


# 모집 마감 기능
@bp.route('/close/<post_id>', methods=['PUT'])
def recruitment_close(post_id):
    token = request.cookies.get('token')
    payload = jwt.decode(token, current_app.jwt_secret_key, algorithms='HS256')

    if payload == None:
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_TOKEN']
        }), STATUS_CODE['INVALID_TOKEN']

    if current_app.db.users.find_one({'id': payload['id']}) == None:
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_TOKEN']
        }), STATUS_CODE['INVALID_TOKEN']

    post = current_app.db.posts.find_one({'_id': ObjectId(post_id)}, {'_id': False})

    if post['user_id'] != payload['id']:
        return jsonify({
            'status': STATUS_MESSAGE['FORBIDDEN_USER']
        }), STATUS_CODE['FORBIDDEN_USER']

    current_app.db.posts.update_one({'_id': ObjectId(post_id)}, {'$set': {'recruitment_status': False}})

    return jsonify({
        'status': STATUS_MESSAGE['SUCCESS'],
    }), STATUS_CODE['SUCCESS']
