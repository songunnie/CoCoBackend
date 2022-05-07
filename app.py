from conf import mongo

from flask import Flask, jsonify

from pymongo import MongoClient

# MongoDB 연결
mongo_client = MongoClient(f'mongodb://{mongo.config["host"]}/{mongo.config["db"]}', mongo.config['port'])

# Flask 애플리케이션 생성
app = Flask(__name__)
app.db = mongo_client.coco


# 루트 라우터 정의
@app.route('/')
def index():
    return jsonify({
        'status': 'success'
    })


# Flask 애플리케이션 실행
if __name__ == '__main__':
    app.run('0.0.0.0', port=5000, debug=True)
