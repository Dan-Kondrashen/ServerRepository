import sqlalchemy
import datetime
from sqlalchemy import orm, event
from sqlalchemy_serializer import SerializerMixin
from models import db_sessions
from models.doc_response import Doc_response

from .db_sessions import SqlAlchemyBase
    
class UserLevelExp(SqlAlchemyBase, SerializerMixin):
    __tablename__ = 'user_level_exps'
    id = sqlalchemy.Column(sqlalchemy.Integer, primary_key=True, autoincrement=True)
    reason = sqlalchemy.Column(sqlalchemy.String, nullable=True)
    status = sqlalchemy.Column(sqlalchemy.String, nullable=True, default="Not comfirmed")
    type = sqlalchemy.Column(sqlalchemy.String, nullable=True, default="increase")
    points = sqlalchemy.Column(sqlalchemy.Integer, nullable=False)
    userId = sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("users.id"))
    user = orm.relationship("User")
    documents_scan_id= sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("files.id"), nullable=True)
    document_scan =  orm.relationship("File")