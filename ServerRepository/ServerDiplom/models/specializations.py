import sqlalchemy
from sqlalchemy import orm
from sqlalchemy_serializer import SerializerMixin
from models import db_sessions

from .db_sessions import SqlAlchemyBase

special_to_education = sqlalchemy.Table(
    "education_to_special",
    SqlAlchemyBase.metadata,
    sqlalchemy.Column("specializations", 
                      sqlalchemy.Integer, 
                      sqlalchemy.ForeignKey("specializations.id")),
    sqlalchemy.Column("educations", 
                      sqlalchemy.Integer, 
                      sqlalchemy.ForeignKey("educations.id"))
)


class Specialization(SqlAlchemyBase, SerializerMixin):
    __tablename__ = 'specializations'
    id = sqlalchemy.Column(sqlalchemy.Integer, primary_key=True, autoincrement=True)
    name = sqlalchemy.Column(sqlalchemy.String, nullable=True)
    description = sqlalchemy.Column(sqlalchemy.String, nullable=True)
    education = orm.relationship("Education",
                            secondary = "education_to_special",
                            back_populates = "specialization")
    skill = orm.relation('SkillType',
                         secondary = 'skill_type_of_spec',
                         back_populates="specialization")
    

    
    
    def __repr__(self):
        return f'<Specialization> {self.id} {self.name} {self.description}'

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