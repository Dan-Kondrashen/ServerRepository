import sqlalchemy
import datetime
from sqlalchemy import orm
from sqlalchemy import event
from sqlalchemy_serializer import SerializerMixin
from models import db_sessions
from models.doc_response import Doc_response


from .db_sessions import SqlAlchemyBase

class DocumentViews(SqlAlchemyBase, SerializerMixin):
    __tablename__ = 'document_views'
    id = sqlalchemy.Column(sqlalchemy.Integer, primary_key=True, autoincrement=True)
    numUsages = sqlalchemy.Column(sqlalchemy.Integer, nullable=True)
    type = sqlalchemy.Column(sqlalchemy.String, nullable=False, index=True)
    date = sqlalchemy.Column(sqlalchemy.DateTime, default=datetime.date.today)
    docId = sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("documents.id"), index =True)
    document = orm.relationship("Document")
    def doc_usages_after_add_increment(mapper, connection, target):
        if target.__tablename__ != "doc_responses":
            return
        if target.type != "view" and target.type != "response" and target.type != "dismiss":
            return
        date = datetime.date.today()
        session = db_sessions.create_session()
        docView = session.query(DocumentViews).filter(DocumentViews.docId ==target.docId, DocumentViews.type == target.type, DocumentViews.date == date).first()
        if docView is None:
            docView = DocumentViews(docId = target.docId, numUsages = 1, type = target.type)
        else:
            docView.numUsages +=1
        session.add(docView)
        session.commit()
    event.listen(Doc_response, "after_insert", doc_usages_after_add_increment)
    
    def doc_usages_before_delete_decrement(mapper, connection, target):
        if target.__tablename__ != "doc_responses":
            return
        if target.type != "view" and target.type != "response" and target.type != "dismiss":
            return
        date = datetime.date.today()
        session = db_sessions.create_session()
        docView = session.query(DocumentViews).filter(DocumentViews.docId ==target.docId, DocumentViews.type == target.type, DocumentViews.date == date).first()
        if docView is None:
            docView = DocumentViews(docId = target.docId, numUsages = 0, type = target.type)
        else:
            docView.numUsages -=1
        session.add(docView)
        session.commit()
    event.listen(Doc_response, 'before_delete', doc_usages_before_delete_decrement)