import sqlalchemy
import datetime
from sqlalchemy import orm
from sqlalchemy import event
from sqlalchemy_serializer import SerializerMixin
from models import db_sessions
from models.doc_response import Doc_response


from .db_sessions import SqlAlchemyBase

class Level(SqlAlchemyBase, SerializerMixin):
    __tablename__ = 'levels'
    id = sqlalchemy.Column(sqlalchemy.Integer, primary_key=True, autoincrement=True)
    number = sqlalchemy.Column(sqlalchemy.Integer, nullable=False)
    minPoints = sqlalchemy.Column(sqlalchemy.Integer, nullable=False)
    maxPoints = sqlalchemy.Column(sqlalchemy.Integer, nullable=False)
    documents_scan_id= sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("files.id"), nullable=True)
    document_scan =  orm.relationship("File")