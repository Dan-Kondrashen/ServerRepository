import sqlalchemy
from sqlalchemy import orm
from sqlalchemy_serializer import SerializerMixin
from models import db_sessions

from .db_sessions import SqlAlchemyBase

class ExperienceTime(SqlAlchemyBase, SerializerMixin):
    __tablename__ = 'experience_time'
    id = sqlalchemy.Column(sqlalchemy.Integer, primary_key=True, autoincrement=True)
    experienceTime = sqlalchemy.Column(sqlalchemy.String, nullable=True)