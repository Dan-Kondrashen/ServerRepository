import datetime
import sqlalchemy
from sqlalchemy_serializer import SerializerMixin
from models import db_sessions
from .db_sessions import SqlAlchemyBase

privileges_to_role = sqlalchemy.Table("privileges_to_role",
                                      SqlAlchemyBase.metadata,
    sqlalchemy.Column("privileges", 
                      sqlalchemy.Integer, 
                      sqlalchemy.ForeignKey("privileges.id")),
    sqlalchemy.Column("roles", 
                      sqlalchemy.Integer, 
                      sqlalchemy.ForeignKey("userroles.id"))
    )

class UserRole(SqlAlchemyBase, SerializerMixin):
    __tablename__ = 'userroles'
    id = sqlalchemy.Column(sqlalchemy.Integer, primary_key=True, autoincrement=True)
    name = sqlalchemy.Column(sqlalchemy.String, nullable=False)
    desc = sqlalchemy.Column(sqlalchemy.String, nullable=True)

    def __repr__(self):
        return f'<UserRole> {self.id} {self.name}'

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