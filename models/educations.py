import sqlalchemy
from sqlalchemy import orm
from sqlalchemy_serializer import SerializerMixin
from models import db_sessions

from .db_sessions import SqlAlchemyBase

class Education(SqlAlchemyBase, SerializerMixin):
    __tablename__ = 'educations'
    id = sqlalchemy.Column(sqlalchemy.Integer, primary_key=True, autoincrement=True)
    name = sqlalchemy.Column(sqlalchemy.String, nullable=False)
    description = sqlalchemy.Column(sqlalchemy.String, nullable=True)
    specialization = orm.relation("Specialization",
                                   secondary ="education_to_special", 
                                   back_populates ="education")
    
    
    
    
    
    def __repr__(self):
        return f'<Education> {self.id} {self.name} {self.description}'

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