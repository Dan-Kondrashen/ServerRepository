from flask import jsonify
from flask import json
from flask_restful import reqparse
from flask_restful import abort, Resource
from models import db_sessions
from models.doc_response import Doc_response
from models.document_views import DocumentViews
from sqlalchemy import func
from reqparsers.docresponse_reqparse import parser, parserlist

class DocResponseListResource(Resource):
    def get(self):
        session = db_sessions.create_session()
        services = session.query(Doc_response).all()
        return jsonify([item.to_dict(only=('id', 'docId','type', 'date', 'statys','userId')) for item in services])
    def post(self):
        args = parser.parse_args()
        docr = Doc_response(**args)
        docr.save_to_db()
        return jsonify({'status': 'succes'})
    
class UserDocResponseListResource(Resource):
    def get(self, user_id):
        session = db_sessions.create_session()
        services = session.query(Doc_response).filter(Doc_response.userId == user_id).all()
        return jsonify([item.to_dict(only=('id', 'docId','type', 'date', 'statys','userId')) for item in services])
    
    def post(self, user_id):
        args = parserlist.parse_args()
        items = args['responses']
        session = db_sessions.create_session()
        resultList =[]
        docIDs = []
        for item in items:
            print(item)
            docIDs.append(item['docId'])
            copyItem = session.query(Doc_response).filter(Doc_response.userId == item['userId'], Doc_response.type == item['type'], Doc_response.docId == item['docId']).first()
            if copyItem is None:
                docr = Doc_response(
                    userId = item['userId'],
                    docId = item['docId'],
                    statys = item['status'],
                    type = item['type'],
                )
                
                session.add(docr)
                resultList.append(docr)
            else:
                print("----Dublicate----"+ str(item))
        
        session.commit()
        views = []
        # iDsList =  list(map(lambda obj: obj.docId, resultList))
        
        # for i in iDsList:
        for i in docIDs:
            numUsages = session.query(func.sum(DocumentViews.numUsages)).filter(DocumentViews.type == "view", DocumentViews.docId == i, DocumentViews.numUsages != 0).scalar()
            if not numUsages == None:
                views.append(DocumentViews(numUsages = numUsages, docId = i, type = "view"))
        return jsonify({"responses": [item.to_dict(only=('id', 'docId','type', 'date', 'statys','userId')) for item in resultList],
                       "views": [item.to_dict(only=('docId','type', 'numUsages')) for item in views]})
        

class UserDocResponseResource(Resource):
    def get(self, user_id, resp_id):
        session = db_sessions.create_session()
        services = session.query(Doc_response).filter(Doc_response.id == resp_id, Doc_response.userId == int(user_id)).all()
        return jsonify([item.to_dict(only=('id', 'docId','type', 'date', 'statys','userId')) for item in services])
    def delete(self, user_id, resp_id):
        session = db_sessions.create_session()
        item = session.query(Doc_response).filter(Doc_response.id == resp_id).first()
        if not item:
            return jsonify({'status': 'Not found'})
        else:
            itemRes = session.query(Doc_response).filter(Doc_response.id == resp_id, Doc_response.userId == int(user_id)).first()
            session.delete(itemRes)
            session.commit()
            return jsonify({'status': 'OK'})
        
# class UserDocResponseWithTypeListResource(Resource):
#     def get(self, user_id, type):
#         session = db_sessions.create_session()
#         responces = session.query(Doc_response).filter(Doc_response.userId == user_id, Doc_response.type == type).all()
#         return jsonify([item.to_dict(only=('id', 'docId','type', 'date', 'statys','userId')) for item in responces])
    

    