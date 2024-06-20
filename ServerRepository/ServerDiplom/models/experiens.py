import sqlalchemy
from sqlalchemy import orm
from sqlalchemy_serializer import SerializerMixin
from models import db_sessions

from .db_sessions import SqlAlchemyBase


class Experiens(SqlAlchemyBase, SerializerMixin):
    __tablename__ = 'experiences'
    id = sqlalchemy.Column(sqlalchemy.Integer, primary_key=True, autoincrement=True)
    experience = sqlalchemy.Column(sqlalchemy.String, nullable=True)
    role = sqlalchemy.Column(sqlalchemy.String, nullable=True)
    place = sqlalchemy.Column(sqlalchemy.String, nullable=True)
    userId = sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("users.id"), index=True)
    user = orm.relationship('User')
    expTimeId = sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("experience_time.id"))
    documents_scan_id= sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("files.id"), nullable=True)
    document_scan =  orm.relationship("File")
    expTime = orm.relationship('ExperienceTime')
    document = orm.relation("Document",
                             secondary = 'experience_to_document',
                             back_populates = 'experience')
    
    
    def __repr__(self):
        return f'<Experience> {self.id} {self.role} {self.experience} {self.place} {self.userId}'

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
