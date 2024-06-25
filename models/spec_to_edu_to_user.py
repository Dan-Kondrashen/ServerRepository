import sqlalchemy
from sqlalchemy import orm
from sqlalchemy_serializer import SerializerMixin
from models import db_sessions

from .db_sessions import SqlAlchemyBase

class Spec_to_Edu_to_User(SqlAlchemyBase, SerializerMixin):
    __tablename__ = 'special_to_education_to_user'
    id = sqlalchemy.Column(sqlalchemy.Integer, primary_key=True, autoincrement=True)
    type = sqlalchemy.Column(sqlalchemy.String, nullable=True, default="not confirmed")
    status = sqlalchemy.Column(sqlalchemy.String, nullable=True, default="new")
    documents_scan_id= sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("files.id"), nullable=True)
    document_scan =  orm.relationship("File")
    specId = sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("specializations.id"))
    specialization =  orm.relationship("Specialization")
    eduId = sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("educations.id"), nullable = True)
    education =  orm.relationship("Education")
    userId = sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("users.id"), index=True)
    user = orm.relationship('User')
    # dependencies_to_document = orm.relationship("dependencies_to_document", cascade='all, delete')
    document = orm.relation("Document",
                            secondary = 'dependencies_to_document',
                            back_populates = 'spec_to_edu_to_user')
    
    def __repr__(self):
        return f'<Spec_to_Edu_to_Doc> {self.id} {self.specId} {self.userId} {self.eduId} {self.status}'

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