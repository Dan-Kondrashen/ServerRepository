import sqlalchemy
from sqlalchemy import orm
from sqlalchemy_serializer import SerializerMixin
from models import db_sessions

from .db_sessions import SqlAlchemyBase

knowledge_to_document = sqlalchemy.Table(
    "knowledge_to_document",
    SqlAlchemyBase.metadata,
    sqlalchemy.Column("knowledges", 
                      sqlalchemy.Integer, 
                      sqlalchemy.ForeignKey("knowledges.id")),
    sqlalchemy.Column("documents", 
                      sqlalchemy.Integer, 
                      sqlalchemy.ForeignKey("documents.id"))
)

class Knowledge(SqlAlchemyBase, SerializerMixin):
    __tablename__ = 'knowledges'
    id = sqlalchemy.Column(sqlalchemy.Integer, primary_key=True, autoincrement=True)
    name = sqlalchemy.Column(sqlalchemy.String, nullable=False)
    description = sqlalchemy.Column(sqlalchemy.String, nullable=True)
    skill = orm.relation('SkillType',
                         secondary = 'skill_type_of_knowledge',
                         back_populates="knowledge")
    document = orm.relation("Document",
                             secondary = 'knowledge_to_document',
                             back_populates = 'knowledge')
    
    
    
    def __repr__(self):
        return f'<Knowledge> {self.id} {self.name} {self.description}'

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