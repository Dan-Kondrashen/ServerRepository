import sqlalchemy
from sqlalchemy import orm
from sqlalchemy_serializer import SerializerMixin
from models import db_sessions
import datetime

from .db_sessions import SqlAlchemyBase

class Doc_response(SqlAlchemyBase, SerializerMixin):
    __tablename__ = 'doc_responses'
    id = sqlalchemy.Column(sqlalchemy.Integer, primary_key=True, autoincrement=True)
    statys = sqlalchemy.Column(sqlalchemy.String, nullable=True, default="new")
    date = sqlalchemy.Column(sqlalchemy.DateTime, default=datetime.datetime.now)
    type = sqlalchemy.Column(sqlalchemy.String, nullable=False)
    docId =sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("documents.id"), index =True)
    document = orm.relation('Document')
    userId = sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("users.id"), index =True)
    user = orm.relationship('User')
    сomments = orm.relation('Comment')
    
    def __repr__(self):
        return f'<Doc_response> {self.id} {self.date} {self.type} {self.statys} {self.userId} {self.docId}'

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
