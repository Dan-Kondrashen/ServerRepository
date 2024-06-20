import sqlalchemy
from sqlalchemy import orm
from sqlalchemy_serializer import SerializerMixin
from models import db_sessions

from .db_sessions import SqlAlchemyBase


class File(SqlAlchemyBase, SerializerMixin):
    __tablename__ = 'files'
    id = sqlalchemy.Column(sqlalchemy.Integer, primary_key=True, autoincrement=True)
    type = sqlalchemy.Column(sqlalchemy.String, nullable=False)
    file_path = sqlalchemy.Column(sqlalchemy.String, nullable=False)
    file_name = sqlalchemy.Column(sqlalchemy.String, nullable=False)
    userId = sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("users.id"))
    user = orm.relationship('User')
    
    def save_to_db(self):
        session = db_sessions.create_session()
        session.add(self)
        session.commit()

    def delete_from_db(self):
        session = db_sessions.create_session()
        session.delete(self)
        session.commit()