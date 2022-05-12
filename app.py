from conf import aws, jwt, mail, mongo

from flask import Flask, jsonify

from flask_cors import CORS

from flask_mail import Mail

from presets.status import STATUS_CODE, STATUS_MESSAGE

from pymongo import MongoClient

from routes import bookmark, comment, like, login, message, post, user, verification

import boto3

# MongoDB 연결
mongo_client = MongoClient(f'mongodb://{mongo.config["host"]}', mongo.config['port'])

# Flask 애플리케이션 생성
app = Flask(__name__)
app.config['MAIL_SERVER'] = mail.config['server']
app.config['MAIL_PORT'] = mail.config['port']
app.config['MAIL_USERNAME'] = mail.config['id']
app.config['MAIL_PASSWORD'] = mail.config['password']
app.config['MAIL_USE_TLS'] = False
app.config['MAIL_USE_SSL'] = True

app.db = mongo_client.coco
app.jwt_secret_key = jwt.config['secret_key']
app.mail = Mail(app)
app.s3 = boto3.client(
    service_name='s3',
    aws_access_key_id=aws.config['access_key_id'],
    aws_secret_access_key=aws.config['secret_access_key']
)

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
app.register_blueprint(verification.bp)


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
