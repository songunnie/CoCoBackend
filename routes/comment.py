from bson.objectid import ObjectId

from datetime import datetime

from flask import Blueprint, current_app, json, jsonify, request

from presets.status import STATUS_CODE, STATUS_MESSAGE

from utils.token import decode_token

bp = Blueprint('comment', __name__, url_prefix='/comment')


# 댓글 작성 라우터
@bp.route('/<post_id>', methods=['POST'])
def write_comment(post_id):
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

    comment = request.form.get('comment')

    if comment == None or comment == '':
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('comment')
        }), STATUS_CODE['INVALID_PARAM']

    doc = {
        'post_id': post_id,
        'user_id': payload['id'],
        'comment': comment,
        'date': datetime.now()
    }

    current_app.db.comments.insert_one(doc)

    return jsonify({
        'status': STATUS_MESSAGE['SUCCESS']
    }), STATUS_CODE['SUCCESS']


# 댓글 목록 획득 라우터
@bp.route('/list/<post_id>', methods=['GET'])
def get_comments(post_id):
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

    comments = list(current_app.db.comments.find({'post_id': post_id}))

    for index in range(len(comments)):
        comments[index]['_id'] = str(comments[index]['_id'])

    return jsonify(**json.loads(json.htmlsafe_dumps({
        'status': STATUS_MESSAGE['SUCCESS'],
        'comments': comments
    }))), STATUS_CODE['SUCCESS']


# 댓글 삭제 라우터
@bp.route('/<comment_id>', methods=['DELETE'])
def delete_comment(comment_id):
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

    comment = current_app.db.comments.find_one({'_id': ObjectId(comment_id)})

    if comment == None:
        return jsonify({
            'status': STATUS_MESSAGE['BAD_REQUEST']
        }), STATUS_CODE['BAD_REQUEST']

    if comment['user_id'] != payload['id']:
        return jsonify({
            'status': STATUS_MESSAGE['FORBIDDEN_USER']
        }), STATUS_CODE['FORBIDDEN_USER']

    current_app.db.comments.delete_one({'_id': ObjectId(comment_id)})

    return jsonify({
        'status': STATUS_MESSAGE['SUCCESS']
    }), STATUS_CODE['SUCCESS']
