import sqlalchemy
import datetime
from sqlalchemy import orm
from sqlalchemy import event
from sqlalchemy_serializer import SerializerMixin
from models import db_sessions
from models.documents import dependencies_to_documents, Document
from models.doc_response import Doc_response



from .db_sessions import SqlAlchemyBase

class SkillAnalitic(SqlAlchemyBase, SerializerMixin):
    __tablename__ = 'skill_analitic'
    id = sqlalchemy.Column(sqlalchemy.Integer, primary_key=True, autoincrement=True)
    numUsage = sqlalchemy.Column(sqlalchemy.Integer, nullable=False)
    respType = sqlalchemy.Column(sqlalchemy.String, nullable=False)
    date = sqlalchemy.Column(sqlalchemy.DateTime, default=datetime.date.today)
    knowId = sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("knowledges.id"), nullable = True)
    knowledge = orm.relationship("Knowledge")
    specId = sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("specializations.id"), nullable = True)
    specialization =  orm.relationship("Specialization")

    def skill_after_add_increment(mapper, connection, target):
        if target.__tablename__ != "doc_responses":
            return
        
        session = db_sessions.create_session()
        document = session.query(Document).filter_by(id = target.docId).first()
        knowledgData =[]
        specData =[]
        type = ""
        if target.type == "view":
            type = "view"
        elif target.type == "response":
            type = "response"
        elif target.type == "favorite":
            type = 'favorite'
        else:
            type = "userArchive"
        date = datetime.date.today()
        for item in document.knowledge:
            skillUsage1 = session.query(SkillAnalitic).filter(SkillAnalitic.knowId ==item.id, SkillAnalitic.respType == type, SkillAnalitic.date == date).first()
            if skillUsage1 is None:
                skillUsage1 = SkillAnalitic(respType = type, numUsage = 1, knowId = item.id, specId = None)
                knowledgData.append(skillUsage1)
            else:
                skillUsage1.numUsage +=1
                knowledgData.append(skillUsage1)
        for item in document.spec_to_edu_to_user:
            skillUsage2 = session.query(SkillAnalitic).filter(SkillAnalitic.specId == item.specId, SkillAnalitic.respType == type, SkillAnalitic.date == date).first()
            if skillUsage2 is None:
                skillUsage2 = SkillAnalitic(respType = type, numUsage = 1, knowId = None, specId = item.specId)
                specData.append(skillUsage2)
            else:
                skillUsage2.numUsage +=1
                specData.append(skillUsage2)
        session.add_all(specData)
        session.add_all(knowledgData)
        session.commit()
        
    def skill_before_delete_decrement(mapper, connection, target):
        if target.__tablename__ != "doc_responses":
            return
        session = db_sessions.create_session()
        document = session.query(Document).filter_by(id = target.docId).first()
        knowledgData =[]
        specData =[]
        type = ""
        if target.type == "view":
            type = "view"
        elif target.type == "response":
            type = "response"
        elif target.type == "favorite":
            type = 'favorite'
        else:
            type = "userArchive"
        date = datetime.date.today()
        for item in document.knowledge:
            skillUsage1 = session.query(SkillAnalitic).filter(SkillAnalitic.knowId ==item.id, SkillAnalitic.respType == type, SkillAnalitic.date == date).first()
            if skillUsage1 is None:
                skillUsage1 = SkillAnalitic(respType = type, numUsage = (-1), knowId = item.id, specId = None)
                knowledgData.append(skillUsage1)
            else:
                skillUsage1.numUsage -=1
                knowledgData.append(skillUsage1)
        for item in document.spec_to_edu_to_user:
            skillUsage2 = session.query(SkillAnalitic).filter(SkillAnalitic.specId == item.specId, SkillAnalitic.respType == type, SkillAnalitic.date == date).first()
            if skillUsage2 is None:
                skillUsage2 = SkillAnalitic(respType = type, numUsage = (-1), knowId = None, specId = item.specId)
                specData.append(skillUsage2)
            else:
                skillUsage2.numUsage -=1
                specData.append(skillUsage2)
                
        session.add_all(specData)
        session.add_all(knowledgData)
        session.commit()
    event.listen(Doc_response, "after_insert", skill_after_add_increment)
    event.listen(Doc_response, 'before_delete', skill_before_delete_decrement)
    