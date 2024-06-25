import datetime
import sqlalchemy
from sqlalchemy_serializer import SerializerMixin
from models import db_sessions
from .db_sessions import SqlAlchemyBase
from sqlalchemy import orm

class Privilege(SqlAlchemyBase, SerializerMixin):
    __tablename__ = "privileges"
    id = sqlalchemy.Column(sqlalchemy.Integer, primary_key=True, autoincrement=True)
    name = sqlalchemy.Column(sqlalchemy.String, nullable=False)
    desc = sqlalchemy.Column(sqlalchemy.String, nullable=True)
    role = orm.relation("UserRole",
                                   secondary ="privileges_to_role", 
                                   backref ="roles")
    
    def __repr__(self):
        return f'<Privilege> {self.id} {self.name} {self.desc}'

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