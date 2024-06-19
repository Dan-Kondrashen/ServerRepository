import datetime
import os
from flask import jsonify, request
from flask_restful import abort, Resource
from models import db_sessions
from models.users import User
from reqparsers.archive_reqparse import parserarch
from models.document_repository import DocumentRepo

def abort_if_item_not_found(archive, id):
    if not archive:
        abort(404, message=f"User with number {id} not found")

class ArchiveListResource(Resource):
    # @jwt_required(fresh=True)
    def get(self, user_id):
        session = db_sessions.create_session()
        items = session.query(DocumentRepo).filter(DocumentRepo.userId == user_id).all()
        return jsonify([item.to_dict(only=('id', 'name', 'searchableWord'))for item in items])
    def post(self, user_id):
        session = db_sessions.create_session()
        args = parserarch.parse_args()
        name = args["name"]
        item = session.query(DocumentRepo).filter(DocumentRepo.userId == user_id, DocumentRepo.name == name).first()
        if not item:
            archive = DocumentRepo(name = name,
                                   userId = user_id,
                                   searchableWord = name)
            session.add(archive)
            session.commit()
            return jsonify(status ="success", archId =archive.id)
        else:
            return jsonify(status ="find copy")

class ArchiveResource(Resource):
    # @jwt_required(fresh=True)
    def put(self, user_id, arch_id):
        session = db_sessions.create_session()
        args = parserarch.parse_args()
        name = args['name']
        item = session.query(DocumentRepo).filter(DocumentRepo.userId == user_id, DocumentRepo.id == arch_id).first()
        
        if not item:
            archive = DocumentRepo(name = name,
                                   userId = user_id)
            session.add(archive)
            session.commit()
            return jsonify(status ="success, but add new")
        else:
            item.name = name
            session.add(item)
            session.commit()
            return jsonify(status ="success")
    def delete(self, user_id, arch_id):
        session =db_sessions.create_session()
        item = session.query(DocumentRepo).filter(DocumentRepo.userId == user_id,DocumentRepo.id == arch_id).delete()
        session.commit()
        return jsonify(status ="success")
         
        