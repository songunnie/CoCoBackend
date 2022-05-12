from bson.objectid import ObjectId

from datetime import datetime

from flask import Blueprint, current_app, json, jsonify, request

from presets.status import STATUS_CODE, STATUS_MESSAGE

from utils.token import decode_token

bp = Blueprint('post', __name__, url_prefix='/post')


# 글 작성 라우터
@bp.route('', methods=['POST'])
def write_post():
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

    title = request.form.get('title')
    tech_stacks = request.form.getlist('tech_stacks[]')
    recruitment_fields = request.form.getlist('recruitment_fields[]')
    region = request.form.get('region')
    period = request.form.get('period')
    contact = request.form.get('contact')
    content = request.form.get('content')

    if title == None or title == '':
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('title')
        }), STATUS_CODE['INVALID_PARAM']

    if len(tech_stacks) == 0:
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('tech_stacks')
        }), STATUS_CODE['INVALID_PARAM']

    if len(recruitment_fields) == 0:
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('recruitment_fields')
        }), STATUS_CODE['INVALID_PARAM']

    if region == None or region == '':
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('region')
        }), STATUS_CODE['INVALID_PARAM']

    if period == None or period == '':
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('period')
        }), STATUS_CODE['INVALID_PARAM']

    if contact == None or contact == '':
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('contact')
        }), STATUS_CODE['INVALID_PARAM']

    if content == None or content == '':
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('content')
        }), STATUS_CODE['INVALID_PARAM']

    doc = {
        'user_id': payload['id'],
        'title': title,
        'tech_stacks': tech_stacks,
        'recruitment_fields': recruitment_fields,
        'region': region,
        'period': period,
        'contact': contact,
        'content': content,
        'date': datetime.now(),
        'hits': 0,
        'recruitment_status': True
    }

    current_app.db.posts.insert_one(doc)

    return jsonify({
        'status': STATUS_MESSAGE['SUCCESS']
    }), STATUS_CODE['SUCCESS']


# 글 내용 획득 라우터
@bp.route('/<post_id>', methods=['GET'])
def get_post(post_id):
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

    post = current_app.db.posts.find_one({'_id': ObjectId(post_id)}, {'_id': False})

    if post == None:
        return jsonify({
            'status': STATUS_MESSAGE['BAD_REQUEST']
        }), STATUS_CODE['BAD_REQUEST']

    user = current_app.db.users.find_one({'id': post['user_id']})
    post['user_nickname'] = user['nickname']

    likes = current_app.db.likes.count_documents({'post_id': post_id})
    post['likes'] = likes

    current_user_like = current_app.db.likes.find_one({
        'post_id': post_id,
        'user_id': payload['id']
    })

    current_user_bookmark = current_app.db.bookmarks.find_one({
        'post_id': post_id,
        'user_id': payload['id']
    })

    post['current_user_like'] = current_user_like != None
    post['current_user_bookmark'] = current_user_bookmark != None

    current_app.db.posts.update_one({'_id': ObjectId(post_id)}, {'$inc': {'hits': 1}})

    return jsonify(**json.loads(json.htmlsafe_dumps({
        'status': STATUS_MESSAGE['SUCCESS'],
        'post': post
    }))), STATUS_CODE['SUCCESS']


# 글 목록 획득 라우터
@bp.route('/list', methods=['GET'])
def get_posts():
    token = request.cookies.get('token')
    payload = decode_token(token, current_app.jwt_secret_key, 'HS256')
    token_validation = payload != None and current_app.db.users.find_one({'id': payload['id']}) != None

    posts = list(current_app.db.posts.find({}, {
        'contact': False,
        'content': False
    }).sort('_id', -1))

    for index in range(len(posts)):
        posts[index]['_id'] = str(posts[index]['_id'])

        likes = current_app.db.likes.count_documents({'post_id': posts[index]['_id']})
        posts[index]['likes'] = likes

        if token_validation == True:
            user_like = current_app.db.likes.find_one({
                'post_id': posts[index]['_id'],
                'user_id': payload['id']
            })

            user_bookmark = current_app.db.bookmarks.find_one({
                'post_id': posts[index]['_id'],
                'user_id': payload['id']
            })

            posts[index]['user_like'] = user_like != None
            posts[index]['user_bookmark'] = user_bookmark != None

    return jsonify(**json.loads(json.htmlsafe_dumps({
        'status': STATUS_MESSAGE['SUCCESS'],
        'posts': posts
    }))), STATUS_CODE['SUCCESS']


# 글 수정 라우터
@bp.route('/<post_id>', methods=['PUT'])
def update_post(post_id):
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

    title = request.form.get('title')
    tech_stacks = request.form.getlist('tech_stacks[]')
    recruitment_fields = request.form.getlist('recruitment_fields[]')
    region = request.form.get('region')
    period = request.form.get('period')
    contact = request.form.get('contact')
    content = request.form.get('content')

    if title == None or title == '':
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('title')
        }), STATUS_CODE['INVALID_PARAM']

    if len(tech_stacks) == 0:
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('tech_stacks')
        }), STATUS_CODE['INVALID_PARAM']

    if len(recruitment_fields) == 0:
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('recruitment_fields')
        }), STATUS_CODE['INVALID_PARAM']

    if region == None or region == '':
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('region')
        }), STATUS_CODE['INVALID_PARAM']

    if period == None or period == '':
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('period')
        }), STATUS_CODE['INVALID_PARAM']

    if contact == None or contact == '':
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('contact')
        }), STATUS_CODE['INVALID_PARAM']

    if content == None or content == '':
        return jsonify({
            'status': STATUS_MESSAGE['INVALID_PARAM']('content')
        }), STATUS_CODE['INVALID_PARAM']

    post = current_app.db.posts.find_one({'_id': ObjectId(post_id)}, {'_id': False})

    if post == None:
        return jsonify({
            'status': STATUS_MESSAGE['BAD_REQUEST']
        }), STATUS_CODE['BAD_REQUEST']

    if post['user_id'] != payload['id']:
        return jsonify({
            'status': STATUS_MESSAGE['FORBIDDEN_USER']
        }), STATUS_CODE['FORBIDDEN_USER']

    doc = {
        'title': title,
        'tech_stacks': tech_stacks,
        'recruitment_fields': recruitment_fields,
        'region': region,
        'period': period,
        'contact': contact,
        'content': content
    }

    current_app.db.posts.update_one({'_id': ObjectId(post_id)}, {'$set': doc})

    return jsonify({
        'status': STATUS_MESSAGE['SUCCESS']
    }), STATUS_CODE['SUCCESS']


# 모집 마감 라우터
@bp.route('/close/<post_id>', methods=['PUT'])
def close_post(post_id):
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

    post = current_app.db.posts.find_one({'_id': ObjectId(post_id)}, {'_id': False})

    if post == None:
        return jsonify({
            'status': STATUS_MESSAGE['BAD_REQUEST']
        }), STATUS_CODE['BAD_REQUEST']

    if post['user_id'] != payload['id']:
        return jsonify({
            'status': STATUS_MESSAGE['FORBIDDEN_USER']
        }), STATUS_CODE['FORBIDDEN_USER']

    current_app.db.posts.update_one({'_id': ObjectId(post_id)}, {'$set': {'recruitment_status': False}})

    return jsonify({
        'status': STATUS_MESSAGE['SUCCESS']
    }), STATUS_CODE['SUCCESS']


# 글 삭제 라우터
@bp.route('/<post_id>', methods=['DELETE'])
def delete_post(post_id):
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

    post = current_app.db.posts.find_one({'_id': ObjectId(post_id)}, {'_id': False})

    if post == None:
        return jsonify({
            'status': STATUS_MESSAGE['BAD_REQUEST']
        }), STATUS_CODE['BAD_REQUEST']

    if post['user_id'] != payload['id']:
        return jsonify({
            'status': STATUS_MESSAGE['FORBIDDEN_USER']
        }), STATUS_CODE['FORBIDDEN_USER']

    current_app.db.posts.delete_one({'_id': ObjectId(post_id)})

    return jsonify({
        'status': STATUS_MESSAGE['SUCCESS']
    }), STATUS_CODE['SUCCESS']
