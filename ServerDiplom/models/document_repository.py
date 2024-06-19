import sqlalchemy
import datetime
from sqlalchemy import orm
from sqlalchemy import event
from sqlalchemy_serializer import SerializerMixin
from models import db_sessions
from models.doc_response import Doc_response


from .db_sessions import SqlAlchemyBase

class DocumentRepo(SqlAlchemyBase, SerializerMixin):
    __tablename__ = 'document_repository'
    id = sqlalchemy.Column(sqlalchemy.Integer, primary_key=True, autoincrement=True)
    name = sqlalchemy.Column(sqlalchemy.String, nullable=False, index=True)
    searchableWord = sqlalchemy.Column(sqlalchemy.String, nullable=True, index=True)
    userId = sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("users.id"))
    user = orm.relationship('User')
    
    def save_to_db(self):
        session = db_sessions.create_session()
        session.add(self)
        session.commit()
        
    def update_to_db(self):
        session = db_sessions.create_session()
        session.merge(self)
        session.commit()

    def delete_from_db(self):
        session = db_sessions.create_session()
        session.delete(self)
        session.commit()