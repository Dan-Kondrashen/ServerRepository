import sqlalchemy
import datetime
from sqlalchemy import orm
from sqlalchemy_serializer import SerializerMixin
from models import db_sessions

from .db_sessions import SqlAlchemyBase

dependencies_to_documents = sqlalchemy.Table(
    "dependencies_to_document",
    SqlAlchemyBase.metadata,
    sqlalchemy.Column("special_to_education_to_user", 
                      sqlalchemy.Integer, 
                      sqlalchemy.ForeignKey("special_to_education_to_user.id")),
    sqlalchemy.Column("documents", 
                      sqlalchemy.Integer, 
                      sqlalchemy.ForeignKey("documents.id"))
)
# exp_time_to_documents = sqlalchemy.Table(
#     "exp_time_to_document",
#     SqlAlchemyBase.metadata,
#     sqlalchemy.Column("experience_time", 
#                       sqlalchemy.Integer, 
#                       sqlalchemy.ForeignKey("experience_time.id")),
#     sqlalchemy.Column("documents", 
#                       sqlalchemy.Integer, 
#                       sqlalchemy.ForeignKey("documents.id"))
# )
# specialization_to_documents = sqlalchemy.Table(
#     "specialization_to_document",
#     SqlAlchemyBase.metadata,
#     sqlalchemy.Column("specializations", 
#                       sqlalchemy.Integer, 
#                       sqlalchemy.ForeignKey("specializations.id")),
#     sqlalchemy.Column("documents", 
#                       sqlalchemy.Integer, 
#                       sqlalchemy.ForeignKey("documents.id"))
# )
class Document(SqlAlchemyBase, SerializerMixin):
    __tablename__ = 'documents'
    id = sqlalchemy.Column(sqlalchemy.Integer, primary_key=True, autoincrement=True)
    title = sqlalchemy.Column(sqlalchemy.String, nullable=False)
    contactinfo = sqlalchemy.Column(sqlalchemy.String, nullable=True, default="Не указаны")
    extra_info = sqlalchemy.Column(sqlalchemy.String, nullable=True)
    salaryF = sqlalchemy.Column(sqlalchemy.String, nullable =True)
    salaryS = sqlalchemy.Column(sqlalchemy.String, nullable =True)
    type = sqlalchemy.Column(sqlalchemy.String, nullable = False)
    date = sqlalchemy.Column(sqlalchemy.DateTime, default=datetime.datetime.now)
    userId = sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("users.id"))
    user = orm.relationship('User')
    views = orm.relationship('DocumentViews')
    response = orm.relation('Doc_response')
    spec_to_edu_to_user = orm.relation("Spec_to_Edu_to_User",
                             secondary = 'dependencies_to_document',
                             back_populates = 'document')
    knowledge = orm.relation("Knowledge",
                                   secondary ="knowledge_to_document", 
                                   back_populates ="document")
    experience = orm.relation("Experiens",
                             secondary = 'experience_to_document',
                             back_populates = 'document')
    
    # expTime = orm.relation("ExperienceTime",
    #                          secondary = 'exp_time_to_document',
    #                          back_populates = 'document')
    # specialization = orm.relation("Specialization",
    #                          secondary = 'specialization_to_document',
    #                          back_populates = 'document')
    location = orm.relation("Location",
                             secondary = 'location_to_document',
                             back_populates = 'document')
    
    def __repr__(self):
        return f'<Dociment> {self.id} {self.title} {self.contactinfo} {self.salaryF} {self.extra_info} {self.type} {self.date} {self.userId}'

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
