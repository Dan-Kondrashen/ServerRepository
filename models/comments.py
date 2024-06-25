import datetime
import sqlalchemy
from sqlalchemy import orm
from models import db_sessions
from sqlalchemy_serializer import SerializerMixin
from .db_sessions import SqlAlchemyBase


class Comment(SqlAlchemyBase, SerializerMixin):
    __tablename__ = 'comments'

    id = sqlalchemy.Column(sqlalchemy.Integer, primary_key=True, autoincrement=True)
    content = sqlalchemy.Column(sqlalchemy.String, nullable=True)
    status = sqlalchemy.Column(sqlalchemy.String, nullable=True, default="Not viewed")
    comment_date = sqlalchemy.Column(sqlalchemy.DateTime, default=datetime.datetime.now)
    respId =sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("doc_responses.id"))
    document = orm.relation('Doc_response')
    userId = sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("users.id"))
    user = orm.relationship('User')
    
    def __repr__(self):
        return f'<Comment> {self.id} {self.comment_date} {self.content} {self.respId} {self.userId}'

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