import sqlalchemy
from sqlalchemy import orm
from sqlalchemy_serializer import SerializerMixin
from models import db_sessions

from .db_sessions import SqlAlchemyBase

class Area(SqlAlchemyBase, SerializerMixin):
    __tablename__ = "areas"
    id = sqlalchemy.Column(sqlalchemy.Integer, primary_key=True, autoincrement=True)
    name = sqlalchemy.Column(sqlalchemy.String, nullable=False)
    
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