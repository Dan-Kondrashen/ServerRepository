import sqlalchemy
from sqlalchemy import orm
from sqlalchemy_serializer import SerializerMixin
from models import db_sessions

from .db_sessions import SqlAlchemyBase

skill_type_of_knowledge = sqlalchemy.Table(
    "skill_type_of_knowledge",
    SqlAlchemyBase.metadata,
    sqlalchemy.Column("knowledges", 
                      sqlalchemy.Integer, 
                      sqlalchemy.ForeignKey("knowledges.id")),
    sqlalchemy.Column("skill_types", 
                      sqlalchemy.Integer, 
                      sqlalchemy.ForeignKey("skill_types.id"))
)
skill_type_of_spec = sqlalchemy.Table(
    "skill_type_of_spec",
    SqlAlchemyBase.metadata,
    sqlalchemy.Column("specializations", 
                      sqlalchemy.Integer, 
                      sqlalchemy.ForeignKey("specializations.id")),
    sqlalchemy.Column("skill_types", 
                      sqlalchemy.Integer, 
                      sqlalchemy.ForeignKey("skill_types.id"))
)



class SkillType(SqlAlchemyBase, SerializerMixin):
    __tablename__ = 'skill_types'
    id = sqlalchemy.Column(sqlalchemy.Integer, primary_key=True, autoincrement=True)
    name = sqlalchemy.Column(sqlalchemy.String, nullable=False)
    description = sqlalchemy.Column(sqlalchemy.String, nullable=True)
    specialization = orm.relation('Specialization',
                         secondary = 'skill_type_of_spec',
                         back_populates="skill")
    knowledge = orm.relation('Knowledge',
                         secondary = 'skill_type_of_knowledge',
                         back_populates="skill")
    
    def __repr__(self):
        return f'<Chosen type> {self.id} {self.name} {self.description}'

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