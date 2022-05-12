from conf import mail

from datetime import datetime, timedelta

from flask import Blueprint, current_app, jsonify, render_template, request

from flask_mail import Message

from presets.status import STATUS_CODE, STATUS_MESSAGE

from utils.regex import check_id

import random

bp = Blueprint('verification', __name__, url_prefix='/verification')


# 인증 메일 전송 라우터
@bp.route('', methods=['POST'])
def send_verification_mail():
    id = request.form.get('id')

    if id == None or id == '' or check_id(id) == False:
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('id')
        }), STATUS_CODE['INVALID_PARAM']

    user = current_app.db.users.find_one({'id': id})

    if user != None:
        return jsonify({
            'status': STATUS_MESSAGE['DUPLICATED_USER']
        }), STATUS_CODE['DUPLICATED_USER']

    verification = current_app.db.verifications.find_one({'user_id': id})

    if verification != None:
        if datetime.now() - verification['date'] < timedelta(seconds=60):
            return jsonify({
                'status': STATUS_MESSAGE['ALREADY_SENT_VERIFICATION_MAIL']
            }), STATUS_CODE['ALREADY_SENT_VERIFICATION_MAIL']
        else:
            current_app.db.verifications.delete_one({'user_id': id})

    code = str(random.random()).split('.')[-1][:6]

    current_app.db.verifications.insert_one({
        'user_id': id,
        'code': code,
        'date': datetime.now()
    })

    msg = Message('[CoCo] 이메일 인증 코드', sender=mail.config['id'], recipients=[id])
    msg.html = render_template('mail.html', code=code)

    current_app.mail.send(msg)

    return jsonify({
        'status': STATUS_MESSAGE['SUCCESS']
    }), STATUS_CODE['SUCCESS']


@bp.route('', methods=['PUT'])
def verify_mail():
    id = request.form.get('id')
    code = request.form.get('code')

    if id == None or id == '' or check_id(id) == False:
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('id')
        }), STATUS_CODE['INVALID_PARAM']

    if code == None or len(code) != 6:
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('code')
        }), STATUS_CODE['INVALID_PARAM']

    verification = current_app.db.verifications.find_one({'user_id': id})

    if verification == None:
        return jsonify({
            'status': STATUS_MESSAGE['BAD_REQUEST']
        }), STATUS_CODE['BAD_REQUEST']

    if verification['code'] != code:
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_VERIFICATION_CODE']
        }), STATUS_CODE['INVALID_VERIFICATION_CODE']

    current_app.db.verifications.delete_one({'user_id': id})

    doc = {
        'id': id,
        'validation': 'invalid'
    }

    current_app.db.users.insert_one(doc)

    return jsonify({
        'status': STATUS_MESSAGE['SUCCESS']
    }), STATUS_CODE['SUCCESS']
