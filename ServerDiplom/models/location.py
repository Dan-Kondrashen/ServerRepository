import sqlalchemy
from sqlalchemy import orm
from sqlalchemy_serializer import SerializerMixin
from models import db_sessions

from .db_sessions import SqlAlchemyBase

location_to_document = sqlalchemy.Table(
    "location_to_document",
    SqlAlchemyBase.metadata,
    sqlalchemy.Column("locId", 
                      sqlalchemy.Integer, 
                      sqlalchemy.ForeignKey("locations.id")),
    sqlalchemy.Column("docId", 
                      sqlalchemy.Integer, 
                      sqlalchemy.ForeignKey("documents.id"))
)

class Location(SqlAlchemyBase, SerializerMixin):
    __tablename__ = "locations"
    id = sqlalchemy.Column(sqlalchemy.Integer, primary_key=True, autoincrement=True)
    city = sqlalchemy.Column(sqlalchemy.String, nullable=False)
    address = sqlalchemy.Column(sqlalchemy.String, nullable=False)
    userId = sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("users.id"))
    user = orm.relationship('User')
    areaId = sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("areas.id"))
    area = orm.relationship('Area')
    document = orm.relation("Document",
                             secondary = 'location_to_document',
                             back_populates = 'location')
    
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