from flask import jsonify, json
import datetime
from dateutil.relativedelta import *
from sqlalchemy import and_, desc
from flask_restful import abort, Resource, reqparse
from models.documents import Document
from models.knowledge import Knowledge
from models import db_sessions
from models.doc_response import Doc_response
from models.skill_analitic import SkillAnalitic
from models.document_views import DocumentViews
from reqparsers.analitic_reqparse import parser, parseruser
from models.skill_type import SkillType, skill_type_of_knowledge, skill_type_of_spec
from models.specializations import Specialization




class DocViewsResource(Resource):
    def get(self):
        session = db_sessions.create_session()
        services = session.query(DocumentViews).all()
        return jsonify([item.to_dict(only=('id', 'docId','numviews')) for item in services])
    
class SkillAnaliticsResourse(Resource):
    def post(self, mod):
        session = db_sessions.create_session()
        args =parser.parse_args()
        date_format = "%Y-%m-%d %H:%M:%S"
        skillFamilyId = int(args["skillFamilyId"])
        skillType = args["skillType"]
        skillId = args["skillId"]
        endDate = datetime.datetime.strptime(args["endDate"], date_format)
        startDate = datetime.datetime.strptime(args["startDate"], date_format)
        if mod == "year":
            endDate = endDate + relativedelta(years=+1)
        elif mod == "month":
            endDate = endDate + relativedelta(months=+1)
        elif mod == "week":
            endDate = endDate + relativedelta(weeks=+1)
        elif mod == "day":
            pass
        else:
            pass
        try:
            if skillFamilyId == None:
                if mod != "all":
                    analitic = session.query(SkillAnalitic).filter(SkillAnalitic.date >= startDate, SkillAnalitic.date < endDate).all()
                else:
                    analitic = session.query(SkillAnalitic).order_by(SkillAnalitic.date).all()
                return jsonify([item.to_dict(only=("id", "respType", "numUsage", "date", "knowId", "specId")) for item in analitic])
            elif skillType == "knowledge":
                if skillId == None:
                    analitic = session.query(SkillAnalitic)\
                        .join(Knowledge, Knowledge.id == SkillAnalitic.knowId) \
                        .join(skill_type_of_knowledge) \
                        .filter(skill_type_of_knowledge.c.skill_types == int(skillFamilyId), 
                                SkillAnalitic.date >= startDate, 
                                SkillAnalitic.date < endDate).all()
                
                else:
                    analitic = session.query(SkillAnalitic)\
                        .join(Knowledge) \
                        .filter(Knowledge.id == skillId,
                                SkillAnalitic.date >= startDate, 
                                SkillAnalitic.date < endDate).all()
                return jsonify([item.to_dict(only=("id", "respType", "numUsage", "date", "knowId", "specId")) for item in analitic])
            elif skillType == "specialization":
                if skillId == None:
                    analitic = session.query(SkillAnalitic)\
                        .join(Specialization, Specialization.id == SkillAnalitic.specId) \
                        .join(skill_type_of_spec) \
                        .filter(skill_type_of_spec.c.skill_types == skillFamilyId, 
                                SkillAnalitic.date >= startDate, 
                                SkillAnalitic.date < endDate).all()
                else:
                    analitic = session.query(SkillAnalitic)\
                        .join(Specialization) \
                        .filter(Specialization.id == skillId,
                                SkillAnalitic.date >= startDate, 
                                SkillAnalitic.date < endDate).all()
                session.commit()
                return jsonify([item.to_dict(only=("id", "respType", "numUsage", "date", "knowId", "specId")) for item in analitic])
        finally:
            session.close()   
class UserAnaliticsResourse(Resource):
    
    def post(self, mod):
        session = db_sessions.create_session()
        args =parseruser.parse_args()
        date_format = "%Y-%m-%d %H:%M:%S"
        userId = int(args["userId"])
        # print(str(skillType) + "-----------------------------------\n")
        # print(mod + "-----------------------------------\n")
        # print(str(skillFamilyId) + "-----------------------------------\n")
        endDate = datetime.datetime.strptime(args["endDate"], date_format)
        startDate = datetime.datetime.strptime(args["startDate"], date_format)
        if mod == "year":
            endDate = endDate + relativedelta(years=+1)
        elif mod == "month":
            endDate = endDate + relativedelta(months=+1)
        elif mod == "week":
            endDate = endDate + relativedelta(weeks=+1)
        elif mod == "day":
            pass
        else:
            pass
        if mod != "all":
            analitic = session.query(DocumentViews).join(Document, Document.id == DocumentViews.docId).filter(DocumentViews.date >= startDate, DocumentViews.date < endDate, Document.userId == userId).all()
        else:
            analitic = session.query(DocumentViews).order_by(DocumentViews.date).all()
        return jsonify([item.to_dict(only=("id", "type", "numUsages", "date", "docId")) for item in analitic])
        
        # elif skillType == "spec":
            
            # .join(SkillType, skill_type_of_knowledge.skill_types == SkillType.id) \
        
        
    # def get(self, dateMode1, dateMode2):
    #     session = db_sessions.create_session()
    #     if dateMode1 == "all":
    #         analitic = session.query(SkillAnalitic).order_by(SkillAnalitic.date).all()
    #         return jsonify([item.to_dict(only=("id", "skillType", "numUsage", "date", "knowId", "specId")) for item in analitic])
    #     else:
    #         date = datetime.date.today()
    #         days = datetime.timedelta(days = dateMode1)
    #         analitic = session.query(SkillAnalitic).filter(SkillAnalitic.date > (date - days)).all()
    #         return jsonify([item.to_dict(only=("id", "skillType", "numUsage", "date", "knowId", "specId")) for item in analitic])