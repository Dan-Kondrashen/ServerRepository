import datetime
from flask import jsonify, request
from flask_restful import abort, Resource
import sqlalchemy as sa
from sqlalchemy import or_
from models import db_sessions
from models.skill_type import SkillType, skill_type_of_spec, skill_type_of_knowledge
from models.knowledge import Knowledge
from models.specializations import Specialization
from reqparsers.skill_type_reqparse import parser, parser_know_type, parser_spec_type
from werkzeug.utils import secure_filename

from models.skill_analitic import SkillAnalitic


class KnowToSkillTypeResource(Resource):
    def get(self, skill_type_id):
        session = db_sessions.create_session()
        skillType = session.query(Knowledge)\
            .join(skill_type_of_knowledge, skill_type_of_knowledge.c.knowledges == Knowledge.id)\
            .filter(skill_type_of_knowledge.c.skill_types == skill_type_id).all()
        session.commit()
        return jsonify([{"knowId": item.id, "name": item.name, "description": item.description} for item in skillType])
    
    def post(self, skill_type_id):
        args = parser_know_type.parse_args()
        session = db_sessions.create_session()
        knowledgesArg = args["knowledges"]
        skill_type= session.query(SkillType).filter(SkillType.id == skill_type_id).first()
        resultKnow =[]
        for item in knowledgesArg:
            knowId = item["know_id"]
            resultKnow.append(int(knowId))
        knowledges = session.query(Knowledge).filter(Knowledge.id.in_(resultKnow)).all()
        if not knowledges == None:
            skill_type.knowledge.extend(knowledges)
        session.add(skill_type)
        session.commit()
        return jsonify({'success': 'OK'})
    
    def delete(self, skill_type_id):
        session = db_sessions.create_session()
        session.query(skill_type_of_knowledge).filter(skill_type_of_knowledge.c.skill_types == int(skill_type_id)).delete()
        session.commit()
    
class SpecToSkillTypeResource(Resource):
    def get(self, skill_type_id):
        session = db_sessions.create_session()
        skillType = session.query(Specialization)\
            .join(skill_type_of_spec, skill_type_of_spec.c.specializations == Specialization.id)\
            .filter(skill_type_of_spec.c.skill_types == skill_type_id)\
            .all()
        session.commit()
        return jsonify([{"specId": int(item.id), "name": item.name, "description": item.description} for item in skillType])
        
    def post(self, skill_type_id):
        args = parser_spec_type.parse_args()
        session = db_sessions.create_session()
        specializationsArg = args["specializations"]
        skill_type= session.query(SkillType).filter(SkillType.id == skill_type_id).first()
        resultSpec =[]
        for item in specializationsArg:
            specId = item["spec_id"]
            resultSpec.append(specId)
        specializations = session.query(Specialization).filter(Specialization.id.in_(resultSpec)).all()
        if not specializations == None:
            skill_type.specialization.extend(specializations)
        session.add(skill_type)
        session.commit()
        return jsonify(statys="success")
    def delete(self, skill_type_id):
        session = db_sessions.create_session()
        session.query(skill_type_of_spec).filter(skill_type_of_spec.c.skill_types == int(skill_type_id)).delete()
        session.commit()
    
class SkillTypeListResource(Resource):
    def get(self, mod):
        session = db_sessions.create_session()
        try:
            if mod == "exists":
                skillTypes = session.query(SkillType).outerjoin(skill_type_of_knowledge).outerjoin(skill_type_of_spec).join(SkillAnalitic, or_(skill_type_of_knowledge.c.knowledges == SkillAnalitic.knowId, skill_type_of_spec.c.specializations == SkillAnalitic.specId)).filter(or_(SkillType.knowledge != None, SkillType.specialization != None)).all()
                session.commit()
                return jsonify([item.to_dict(only=("id", "name", "description", "knowledge.id", "specialization.id")) for item in skillTypes])
            elif mod == "allWithSkill":
                skillTypes = session.query(SkillType).outerjoin(skill_type_of_knowledge).outerjoin(skill_type_of_spec).filter(or_(skill_type_of_knowledge.c.knowledges == SkillAnalitic.knowId, skill_type_of_spec.c.specializations == SkillAnalitic.specId)).all()
                session.commit()
                return jsonify([item.to_dict(only=("id", "name", "description", "knowledge.id", "specialization.id")) for item in skillTypes])
            elif mod == "all":
                skillTypes = session.query(SkillType).all()
                session.commit()
                return jsonify([item.to_dict(only=("id", "name", "description", "knowledge.id", "specialization.id")) for item in skillTypes])
        finally:
            session.close()  