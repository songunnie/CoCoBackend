from bson.objectid import ObjectId

from datetime import datetime

from flask import Blueprint, current_app, json, jsonify, request

from presets.status import STATUS_CODE, STATUS_MESSAGE

from utils.token import decode_token

# Flask Blueprint 생성
bp = Blueprint('message', __name__, url_prefix='/message')


# 쪽지 작성 라우터
@bp.route('', methods=['POST'])
def write_message():
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

    receiver_id = request.form.get('receiver_id')
    title = request.form.get('title')
    content = request.form.get('content')

    if receiver_id == None or receiver_id == '':
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('receiver_id')
        }), STATUS_CODE['INVALID_PARAM']

    if title == None or title == '':
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('title')
        }), STATUS_CODE['INVALID_PARAM']

    if content == None or content == '':
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('content')
        }), STATUS_CODE['INVALID_PARAM']

    doc = {
        'sender_user_id': payload['id'],
        'receiver_user_id': receiver_id,
        'title': title,
        'content': content,
        'date': datetime.now(),
        'read': False
    }

    current_app.db.messages.insert_one(doc)

    return jsonify({
        'status': STATUS_MESSAGE['SUCCESS']
    }), STATUS_CODE['SUCCESS']


# 쪽지 조회 라우터
@bp.route('/<message_id>', methods=['GET'])
def get_message(message_id):
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

    message = current_app.db.messages.find_one({'_id': ObjectId(message_id)}, {'_id': False})

    if message['receiver_user_id'] != payload['id']:
        return jsonify({
            'status': STATUS_MESSAGE['FORBIDDEN_USER']
        }), STATUS_CODE['FORBIDDEN_USER']

    current_app.db.messages.update_one({'_id': ObjectId(message_id)}, {'$set': {'read': True}})

    return jsonify(**json.loads(json.htmlsafe_dumps({
        'status': STATUS_MESSAGE['SUCCESS'],
        'data': message
    }))), STATUS_CODE['SUCCESS']


# 쪽지 삭제 라우터
@bp.route('/<message_id>', methods=['DELETE'])
def delete_message(message_id):
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

    message = current_app.db.messages.find_one({'_id': ObjectId(message_id)}, {'_id': False})

    if message['receiver_user_id'] != payload['id']:
        return jsonify({
            'status': STATUS_MESSAGE['FORBIDDEN_USER']
        }), STATUS_CODE['FORBIDDEN_USER']

    current_app.db.messages.delete_one({'_id': ObjectId(message_id)})

    return jsonify({
        'status': STATUS_MESSAGE['SUCCESS']
    }), STATUS_CODE['SUCCESS']


# 쪽지 목록 라우터
@bp.route('/list', methods=['GET'])
def get_message_list():
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

    message_list = list(
        current_app.db.messages.find({'receiver_user_id': payload['id']}, {'content': False}).sort('_id', -1))

    for index in range(len(message_list)):
        message_list[index]['_id'] = str(message_list[index]['_id'])

    return jsonify(**json.loads(json.htmlsafe_dumps({
        'status': STATUS_MESSAGE['SUCCESS'],
        'data': message_list
    }))), STATUS_CODE['SUCCESS']
