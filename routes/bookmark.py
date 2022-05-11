from flask import Blueprint, current_app, jsonify, request

from presets.status import STATUS_CODE, STATUS_MESSAGE

from utils.token import decode_token

bp = Blueprint('bookmark', __name__, url_prefix='/bookmark')


# 북마크 전환 라우터
@bp.route('/<post_id>', methods=['PUT'])
def toggle_bookmark(post_id):
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

    bookmark = current_app.db.bookmarks.find_one(doc)

    if bookmark == None:
        current_app.db.bookmarks.insert_one(doc)
    else:
        current_app.db.bookmarks.delete_one(doc)

    return jsonify({
        'status': STATUS_MESSAGE['SUCCESS']
    }), STATUS_CODE['SUCCESS']
