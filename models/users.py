import datetime

import sqlalchemy
from sqlalchemy import orm, event

from sqlalchemy_serializer import SerializerMixin
from werkzeug.security import generate_password_hash, check_password_hash
from passlib.hash import pbkdf2_sha256 as sha256

from models import db_sessions
from models.user_level_exp import UserLevelExp

from .db_sessions import SqlAlchemyBase


class User(SqlAlchemyBase, SerializerMixin):
    
    __tablename__ = 'users'
    id = sqlalchemy.Column(sqlalchemy.Integer, primary_key=True, autoincrement=True)
    fname = sqlalchemy.Column(sqlalchemy.String, nullable=False)
    lname = sqlalchemy.Column(sqlalchemy.String, nullable=True)
    mname = sqlalchemy.Column(sqlalchemy.String, nullable=True)
    phone = sqlalchemy.Column(sqlalchemy.BIGINT, nullable=True)
    email = sqlalchemy.Column(sqlalchemy.String, index=True)
    
    firebase_token=sqlalchemy.Column(sqlalchemy.String, nullable=True)
    # unique=True
    password = sqlalchemy.Column(sqlalchemy.String, nullable=False)
    status =sqlalchemy.Column(sqlalchemy.String, nullable=False, default="Not comfirmed")
    registration_date = sqlalchemy.Column(sqlalchemy.DateTime, default=datetime.datetime.now)
    roleId = sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("userroles.id"))
    curLevelId = sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("user_cur_level_info.id"), nullable=True)
    curLevel = orm.relationship('UserCurLevel', foreign_keys=[curLevelId], cascade='all, delete')
    role = orm.relationship('UserRole')
    location = orm.relationship('Location') 
    archive = orm.relationship('DocumentRepo', cascade='all, delete')
    file = orm.relationship('File')
    

    def __repr__(self):
        return f'<User> {self.id} {self.fname} {self.email} {self.roleId}'

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
        try:
            session.delete(self)
            session.commit()
        finally:
            session.close()

    @classmethod
    def find_by_email(cls, email):
        session = db_sessions.create_session()
        try:
            return session.query(User).filter(User.email == email).all()
        finally:
            session.close()
    @classmethod
    def find_duplicate(cls, email, roleId):
        session = db_sessions.create_session()
        try:
            return session.query(User).filter(User.email == email, User.roleId == roleId).first()
        finally:
            session.close()

    @staticmethod
    def return_all():
        def to_json(x):
            return {
                'email': x.email,
                'password': x.hashed_password
            }

        session = db_sessions.create_session()
        return {'users': list(map(lambda x: to_json(x), session.query(User).all()))}

    @staticmethod
    def generate_hash(password):
        return sha256.hash(password)

    @staticmethod
    def verify_hash(password, hash):
        return sha256.verify(password, hash)