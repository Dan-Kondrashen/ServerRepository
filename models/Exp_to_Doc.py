import sqlalchemy
from sqlalchemy import orm
from sqlalchemy_serializer import SerializerMixin
from models import db_sessions

from .db_sessions import SqlAlchemyBase

class Exp_to_Doc(SqlAlchemyBase, SerializerMixin):
    __tablename__ = 'experience_to_document'
    id = sqlalchemy.Column(sqlalchemy.Integer, primary_key=True, autoincrement=True)
    expId = sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("experiences.id"))
    experience =  orm.relationship("Experiens")
    docId = sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("documents.id"))
    document = orm.relationship('Document')
    
    
    def __repr__(self):
        return f'<Exp_to_Doc> {self.id} {self.expId} {self.docId} {self.documents_scan}'

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