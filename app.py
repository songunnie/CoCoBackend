from conf import jwt, mongo

from flask import Flask, jsonify

from flask_cors import CORS

from presets.status import STATUS_CODE, STATUS_MESSAGE

from pymongo import MongoClient

from routes import bookmark, comment, like, login, message, post, user

# MongoDB 연결
mongo_client = MongoClient(f'mongodb://{mongo.config["host"]}', mongo.config['port'])

# Flask 애플리케이션 생성
app = Flask(__name__)
app.db = mongo_client.coco
app.jwt_secret_key = jwt.config['secret_key']

# Flask CORS 설정
cors = CORS(app, resources={r'/*': {'origins': '*'}})

# Flask Blueprint 연결
app.register_blueprint(bookmark.bp)
app.register_blueprint(comment.bp)
app.register_blueprint(like.bp)
app.register_blueprint(login.bp)
app.register_blueprint(message.bp)
app.register_blueprint(post.bp)
app.register_blueprint(user.bp)


# 요청 핸들러
@app.after_request
def after_request(response):
    response.headers.add('Access-Control-Allow-Credentials', 'true')

    return response


# 루트 라우터
@app.route('/')
def index():
    return jsonify({
        'status': STATUS_MESSAGE['SUCCESS']
    }), STATUS_CODE['SUCCESS']


# Flask 애플리케이션 실행
if __name__ == '__main__':
    app.run('0.0.0.0', port=5000, debug=True)
