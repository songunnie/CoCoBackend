from flask import Blueprint, current_app, jsonify, request

from presets.status import STATUS_CODE, STATUS_MESSAGE

from utils.token import decode_token

bp = Blueprint('like', __name__, url_prefix='/like')


# 좋아요 전환 라우터
@bp.route('/<post_id>', methods=['PUT'])
def toggle_like(post_id):
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

    doc = {
        'post_id': post_id,
        'user_id': payload['id']
    }

    like = current_app.db.likes.find_one(doc)

    if like == None:
        current_app.db.likes.insert_one(doc)
    else:
        current_app.db.likes.delete_one(doc)

    return jsonify({
        'status': STATUS_MESSAGE['SUCCESS']
    }), STATUS_CODE['SUCCESS']
