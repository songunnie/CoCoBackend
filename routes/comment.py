from bson.objectid import ObjectId

from flask import Blueprint, current_app, jsonify, request, json

from presets.status import STATUS_CODE, STATUS_MESSAGE

from datetime import datetime

from utils.token import decode_token

# Flask Blueprint 생성
bp = Blueprint('comment', __name__, url_prefix='/comment')


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


@bp.route('/<post_id>', methods=['GET'])
def get_comment(post_id):
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

    comment = current_app.db.comments.find_one({'_id': ObjectId(post_id)}, {'_id': False})

    return jsonify(**json.loads(json.htmlsafe_dumps({
        'status': STATUS_MESSAGE['SUCCESS'],
        'comment': comment
    }))), STATUS_CODE['SUCCESS']


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

    current_app.db.comments.delete_one({'_id': ObjectId(comment_id)})

    return jsonify({
        'status': STATUS_MESSAGE['SUCCESS']
    }), STATUS_CODE['SUCCESS']
