import select
from telnetlib import DO
from flask import jsonify
from flask import json
from flask_restful import reqparse
from flask_restful import abort, Resource
from sqlalchemy import and_, or_, outerjoin, desc, asc, func, case, not_
from sqlalchemy.orm import contains_eager
from models.Exp_to_Doc import Exp_to_Doc
from models import db_sessions
from models.documents import Document, dependencies_to_documents
from models.knowledge import Knowledge, knowledge_to_document
from models.experiens import Experiens
from models.users import User
from models.doc_response import Doc_response
from models.comments import Comment
from models.document_views import DocumentViews
from models.spec_to_edu_to_user import Spec_to_Edu_to_User
from reqparsers.document_reqparse import parser, fulldocumentparser, parserFilter

def find_document(doc_id):
    session = db_sessions.create_session()
    document = session.query(Document).get(doc_id)
    return document

def abort_if_document_not_found(doc, doc_id):
    if not doc:
        abort(404, message=f"Document with number {doc_id} not found")
    

class DocumentResource(Resource):
    # def get(self, doc_id):
        # document = find_document(doc_id)
        # abort_if_document_not_found(document, doc_id)
        # return jsonify(document.to_dict(only=('id', 'contactinfo', 'extra_info', 'userId', 'salaryF', 'salaryS', 'type', 'date')))
    def get(self, doc_id):
        session = db_sessions.create_session() 
        try: 
            document = session.query(Document).filter(Document.id == doc_id).first()
            return jsonify(document.to_dict(only=('id', 'title','type', 'date', 'contactinfo', 'extra_info', 'userId', 'salaryF', 'salaryS', 'knowledge.id', 'knowledge.name', 'views.numviews', 'spec_to_edu_to_doc.id', 'spec_to_edu_to_doc.documents_scan_id', 'spec_to_edu_to_doc.education.id', 'spec_to_edu_to_doc.education.name', 'spec_to_edu_to_doc.education.description', 'spec_to_edu_to_doc.specialization.id', 'spec_to_edu_to_doc.specialization.name', 'spec_to_edu_to_doc.specialization.description')))
        finally:
            session.close() 
    def put(self, doc_id):
        args = parser.parse_args()
        document = find_document(doc_id)
        abort_if_document_not_found(document, doc_id)
        document.contactinfo= args['contactinfo']
        document.extra_info= args['extra_info']
        document.salary= args['salary']
        document.userId = args['userId']
        document.date = args['date']
        document.type = args['type']
        document.update_to_db()
        return jsonify(status="Успешно")

    def delete(self, doc_id):
        session = db_sessions.create_session()
        session.query(Document).filter(Document.id == doc_id).delete()
        session.commit()
        return jsonify({'success': 'OK'})
    

class DocumentsListResource(Resource):
    def get(self, type, num):
        session = db_sessions.create_session()
        if type == "all":
            services = session.query(Document).order_by(desc(Document.date)).limit(num).all()
        else:
            services = session.query(Document).order_by(desc(Document.date)).filter(Document.type == type).limit(num).all()
        return jsonify([item.to_dict(only=('id', 'title','type', 'date', 'contactinfo', 'extra_info', 'userId', 'user.id', 'user.fname', 'user.lname', 'user.mname', 'user.roleId', 'user.status' ,'salaryF', 'salaryS', 'knowledge.id', 'knowledge.name', 'views.numviews', 'archive.name', 'spec_to_edu_to_doc.id', 'spec_to_edu_to_doc.documents_scan', 'spec_to_edu_to_doc.education.id', 'spec_to_edu_to_doc.education.name', 'spec_to_edu_to_doc.education.description', 'spec_to_edu_to_doc.specialization.id', 'spec_to_edu_to_doc.specialization.name', 'spec_to_edu_to_doc.specialization.description')) for item in services])
    
    def post(self):
        args = parser.parse_args()
        
        # doc = Document(**args)
        # doc.save_to_db()
        # parsers = reqparse.RequestParser()
        # parsers.add_argument("documents", location='json', type=list)
        # argss = parser.parse_args()
        document = args["documents"]
        
        
        # doc1 = Document(title = "item['title']",
        #                 contactinfo = "item['contactinfo'],",
        #                 extra_info = "dswdf",
        #                 salary = "20000",
        #                 type = "Dfкансия",
        #                 userId = 1)
        # doc1.save_to_db
        for item in document:
            doc = Document(
            title = item['title'],
            contactinfo = item['contactinfo'],
            extra_info = item['extra_info'],
            salary = item['salary'],
            type = item['type'],
            userId = item['userId'])
            doc.save_to_db()
        
        
        return jsonify({'success': 'OK'})
class RegUserDocumentsListResource(Resource):
    def post(self, user_id, type, mod):
        session = db_sessions.create_session()   
        try:
            args = parserFilter.parse_args()
            # subquery = session.query(DocumentViews.docId).filter(DocumentViews.type == "dismiss").subquery() (.filter(not_(Document.id.in_(subquery)))
            subquery = session.query(Doc_response.docId).filter(and_(Doc_response.type == "dismiss", Doc_response.userId == int(user_id))).subquery()
            print(subquery)
            if mod =="new": 
                services = session.query(Document).filter(not_(Document.id.in_(subquery))).filter(Document.type == type).order_by(desc(Document.date))\
                    .limit(args["num"])\
                    .offset(args["startNum"]).all()
            elif mod == "userSkill":
                userSkill =  session.query(Knowledge.id)\
                    .join(knowledge_to_document, Knowledge.id == knowledge_to_document.c.knowledges)\
                    .join(Document, knowledge_to_document.c.documents == Document.id)\
                    .filter(Document.userId == user_id)\
                    .group_by(Knowledge.id).all()
                result =list(map(lambda obj: obj.id, userSkill))
                services = session.query(Document)\
                    .join(Document.knowledge).filter(Document.type == type, Knowledge.id.in_(result))\
                    .order_by(desc(Document.date)).limit(args["num"]).offset(args["startNum"]).all()
            else:
                services = session.query(Document).filter(Document.type == type)\
                    .order_by(desc(Document.date)).limit(args["num"]).offset(args["startNum"]).all()
            
            views =[]
            responses = []
            for i in services:
                numUsages = session.query(func.sum(DocumentViews.numUsages)).filter(DocumentViews.type == "view", DocumentViews.docId == i.id, DocumentViews.numUsages != 0).scalar()
                if not numUsages == None:
                    views.append(DocumentViews(numUsages = numUsages, docId = i.id, type = "view"))
                response = session.query(Doc_response).filter(Doc_response.docId == i.id, Doc_response.userId == user_id).all()
                responses.extend(response)
            session.commit()
        
            return jsonify({"responses": [item.to_dict(only=('id', 'statys', 'type', 'userId', "docId")) for item in responses],
                            "views": [item.to_dict(only=('id', 'type', 'numUsages', "docId")) for item in views],
                            "documents": [item.to_dict(only=('id', 'title','type', 'date', 'contactinfo', 'extra_info', 'salaryF', 'salaryS', 'userId', 
                                            'user.id', 'user.fname', 'user.lname', 'user.mname', 'user.roleId', 'user.status' ,
                                            'knowledge.id', 'knowledge.name', 
                                            'experience.id', 'experience.experience', 'experience.role', 'experience.place', 'experience.expTimeId',
                                            'spec_to_edu_to_user.id', 'spec_to_edu_to_user.documents_scan_id',
                                            'spec_to_edu_to_user.education.id', 'spec_to_edu_to_user.education.name', 'spec_to_edu_to_user.education.description', 
                                            'spec_to_edu_to_user.specialization.id', 'spec_to_edu_to_user.specialization.name', 'spec_to_edu_to_user.specialization.description')) for item in services]})
        finally:
            session.close()  
    def get(self, user_id, type, num, mod):
        session = db_sessions.create_session() 
        try:   
            services = session.query(Document).filter(Document.type == type).order_by(desc(Document.date)).limit(num).all()
            
            views =[]
            responses = []
            for i in services:
                numUsages = session.query(func.sum(DocumentViews.numUsages)).filter(DocumentViews.type == "view", DocumentViews.docId == i.id, DocumentViews.numUsages != 0).scalar()
                if not numUsages == None:
                    views.append(DocumentViews(numUsages = numUsages, docId = i.id, type = "view"))
                response = session.query(Doc_response).filter(Doc_response.docId == i.id, Doc_response.userId == user_id).all()
                responses.extend(response)
            session.commit()
        
            return jsonify({"responses": [item.to_dict(only=('id', 'statys', 'type', 'userId', "docId")) for item in responses],
                            "views": [item.to_dict(only=('id', 'type', 'numUsages', "docId")) for item in views],
                            "documents": [item.to_dict(only=('id', 'title','type', 'date', 'contactinfo', 'extra_info', 'salaryF', 'salaryS', 'userId', 
                                            'user.id', 'user.fname', 'user.lname', 'user.mname', 'user.roleId', 'user.status' ,
                                            'knowledge.id', 'knowledge.name', 
                                            'experience.id', 'experience.experience', 'experience.role', 'experience.place', 'experience.expTimeId',
                                            'spec_to_edu_to_user.id', 'spec_to_edu_to_user.documents_scan_id',
                                            'spec_to_edu_to_user.education.id', 'spec_to_edu_to_user.education.name', 'spec_to_edu_to_user.education.description', 
                                            'spec_to_edu_to_user.specialization.id', 'spec_to_edu_to_user.specialization.name', 'spec_to_edu_to_user.specialization.description')) for item in services]})
        finally:
            session.close()  
class UserRespDocsListResource(Resource):
    def get(self, user_id, type):
        session = db_sessions.create_session() 
        try:   
            if (type == "all"):
                services = session.query(Document).\
                    join(Doc_response, Document.response).\
                    filter(Doc_response.userId == int(user_id)).\
                    options(contains_eager(Document.response)).\
                    all()
                return jsonify([item.to_dict(only=('id', 'title','type', 'date', 'contactinfo', 'extra_info', 'salaryF', 'salaryS', 'userId', 
                                                'user.id', 'user.fname', 'user.lname', 'user.mname', 'user.roleId',  'user.status' ,
                                                'knowledge.id', 'knowledge.name', 
                                                'views.id','views.numUsages', 'views.docId', 'views.type',
                                                'response.id', 'response.type', 'response.statys', 'response.docId',
                                                'experience.id', 'experience.experience', 'experience.role', 'experience.place', 'experience.expTimeId',
                                                'spec_to_edu_to_user.id', 'spec_to_edu_to_user.documents_scan_id',
                                                'spec_to_edu_to_user.education.id', 'spec_to_edu_to_user.education.name', 'spec_to_edu_to_user.education.description', 
                                                'spec_to_edu_to_user.specialization.id', 'spec_to_edu_to_user.specialization.name', 'spec_to_edu_to_user.specialization.description')) for item in services])
            else:
                services = session.query(Document).\
                    join(Doc_response, Document.response).\
                    filter(Doc_response.userId == int(user_id)).\
                    filter(Doc_response.type == str(type)).\
                    options(contains_eager(Document.response)).\
                    all()
                return jsonify([item.to_dict(only=('id', 'title','type', 'date', 'contactinfo', 'extra_info', 'salaryF', 'salaryS', 'userId', 
                                                'user.id', 'user.fname', 'user.lname', 'user.mname', 'user.roleId',  'user.status' ,
                                                'knowledge.id', 'knowledge.name', 
                                                'views.id','views.numUsages', 'views.docId', 'views.type',
                                                'response.id', 'response.type', 'response.statys', 'response.docId',
                                                'experience.id', 'experience.experience', 'experience.role', 'experience.place', 'experience.expTimeId',
                                                'spec_to_edu_to_user.id', 'spec_to_edu_to_user.documents_scan_id',
                                                'spec_to_edu_to_user.education.id', 'spec_to_edu_to_user.education.name', 'spec_to_edu_to_user.education.description', 
                                                'spec_to_edu_to_user.specialization.id', 'spec_to_edu_to_user.specialization.name', 'spec_to_edu_to_user.specialization.description')) for item in services])
        finally:
            session.close()

class UserRespListResource(Resource):
    def get(self, user_id, num):
        session = db_sessions.create_session()    
        types = ["response", "favorite"]
        last_comment_subquery = session.query(
            Doc_response.id.label('doc_id'),
            func.max(Comment.comment_date).label('last_comment_date')
        ).join(Doc_response.сomments).group_by(Doc_response.id).subquery()
        
        services = session.query(Doc_response)\
            .outerjoin(last_comment_subquery, 
                      last_comment_subquery.c.doc_id == Doc_response.id)\
                          .order_by(case([(last_comment_subquery.c.last_comment_date != None, 0)], else_=1),
                                    desc(last_comment_subquery.c.last_comment_date)).join(Doc_response.document)\
                                        .filter(or_(and_(Document.userId == user_id, Doc_response.type.in_(types)), 
                                                    and_(Doc_response.userId == user_id, Doc_response.type.in_(types))))\
                                                        .limit(num).all()
        resIDs = list(map(lambda obj: obj.id, services))
        subq = session.query(
            Comment,
            func.row_number().over(
                partition_by=Comment.respId,
                order_by=desc(Comment.comment_date)
            ).label('rn')).filter(Comment.respId.in_(resIDs)).subquery()
        comments = session.query(
            subq.c.id,
            subq.c.content,
            subq.c.status,
            subq.c.comment_date,
            subq.c.respId,
            subq.c.userId,
            
        ).filter(subq.c.rn == 1)
        resultComm = []
        for i in comments:
            print(i.id, i.content, i.status,  i.comment_date, i.respId, i.userId)
            comm = Comment(id = i.id, content =  i.content, status =i.status, comment_date = i.comment_date, respId = i.respId, userId = i.userId)
            resultComm.append(comm)
        
        return jsonify({"response":[item.to_dict(only=('document.id', 'document.title','document.type', 'document.date', 'document.contactinfo', 
                                           'document.extra_info', 'document.salaryF', 'document.salaryS', 'document.userId',
                                           'user.id', 'user.fname', 'user.lname', 'user.mname', 'user.roleId', 'user.status' ,
                                           'id', 'type', 'statys', 'docId', 'userId')) for item in services],
                        "comments": [item.to_dict(only=('content', 'userId', 'respId', 'id', 'status','comment_date'))for item in resultComm]})
    
class RegUserDocumentResource(Resource):
    def delete(self, user_id, doc_id):
        session = db_sessions.create_session()
        item = session.query(Document).filter(Document.id == doc_id, Document.userId == user_id).all()
        print(item)
        if not item:
            return jsonify({'status': 'Not found'})
        else:
            session.query(DocumentViews).filter(DocumentViews.docId == doc_id).delete()
            session.query(Exp_to_Doc).filter(Exp_to_Doc.docId == doc_id).delete()
            session.query(Doc_response).filter(Doc_response.docId == doc_id).delete()
            session.query(knowledge_to_document).filter(knowledge_to_document.c.documents == doc_id).delete()
            session.query(dependencies_to_documents).filter(dependencies_to_documents.c.documents == doc_id).delete()
            session.query(Document).filter(Document.id == doc_id, Document.userId == user_id).delete()
            session.commit()
            return jsonify({'status': 'OK'})

class CurUserDocumentsList(Resource):
    def get(self, user_id, mod):
        session = db_sessions.create_session() 
        try:
            filtered_views_subquery = session.query(DocumentViews)\
                .filter(and_(
                    DocumentViews.type.in_(["view", "response", "dismiss"])))\
                .subquery()

            # Выполняем outerjoin с подзапросом для получения ВСЕХ доков
            services = session.query(Document)\
                .order_by(desc(Document.date))\
                .outerjoin(filtered_views_subquery, filtered_views_subquery.c.docId == Document.id).filter(Document.userId == int(user_id))\
                .all()
            # services = session.query(Document).order_by(desc(Document.date)).outerjoin(Document.views).filter(and_(DocumentViews.type.in_(["view", "response", "dismiss"]), Document.userId == user_id)).all()
            resIDs = list(map(lambda obj: obj.id, services))
            views =[]
            for i in resIDs:
                numUsages = session.query(func.sum(DocumentViews.numUsages).label('total_usages'),
                DocumentViews.type.label('type'))\
                    .filter(DocumentViews.type.in_(["view", "response", "dismiss"]), 
                            DocumentViews.docId == i, DocumentViews.numUsages != 0)\
                    .group_by(DocumentViews.type).all()
                if not numUsages == None:
                    for item in numUsages:
                        num, type = item
                        views.append(DocumentViews(numUsages = num, docId = i, type = type))
                    
            
                
            if mod == "analysis":
                session.commit()
                return jsonify({"views": [item.to_dict(only=('id', 'type', 'numUsages', "docId")) for item in views],
                                "documents": [item.to_dict(only=('id', 'title', 'type','userId', "date"))
                                            for item in services]})
            # elif mod == "preview":
            #     return jsonify([item.to_dict(only=('id', 'title','type', 'date', 'contactinfo', 'extra_info', 'salaryF', 'salaryS', 'userId', 
            #                                    'knowledge.id', 'knowledge.name')) for item in services])
            elif mod == "all":
                session.commit()
                return jsonify({"views": [item.to_dict(only=('id', 'type', 'numUsages', "docId")) for item in views],
                                "documents": [item.to_dict(only=('id', 'title','type', 'date', 'contactinfo', 'extra_info', 'salaryF', 'salaryS', 'userId', 
                                                'knowledge.id', 'knowledge.name',
                                                'experience.id', 'experience.experience', 'experience.role', 'experience.place', 'experience.expTimeId',
                                                'spec_to_edu_to_user.id', 'spec_to_edu_to_user.documents_scan_id',
                                                'spec_to_edu_to_user.education.id', 'spec_to_edu_to_user.education.name', 'spec_to_edu_to_user.education.description', 
                                                'spec_to_edu_to_user.specialization.id', 'spec_to_edu_to_user.specialization.name', 'spec_to_edu_to_user.specialization.description')) for item in services]})
        finally:
            session.close() 
    def post(self, user_id, mod):
        args = fulldocumentparser.parse_args()
        session = db_sessions.create_session() 
        try:
            type = args['type']
            dependenciesArg = args["dependencies"]
            knowledgesArg = args["knowledges"]
            experienceArg = args["experience"]
            print(str(args) + "Вот здесь")
            if args['salaryF'] == "" and args['salaryS'] == "":
                doc = Document(
                    title = args['title'],
                    contactinfo = args['contactInfo'],
                    extra_info = args['extraInfo'],
                    salaryF = None,
                    salaryS = None,
                    type = args['type'],
                    userId = args['userId']
                )
            elif args['salaryF'] == "":
                doc = Document(
                    title = args['title'],
                    contactinfo = args['contactInfo'],
                    extra_info = args['extraInfo'],
                    salaryF = None,
                    salaryS = args['salaryS'],
                    type = args['type'],
                    userId = args['userId']
                )
            elif args['salaryS'] == "":
                doc = Document(
                    title = args['title'],
                    contactinfo = args['contactInfo'],
                    extra_info = args['extraInfo'],
                    salaryF = args['salaryF'],
                    salaryS = None,
                    type = args['type'],
                    userId = args['userId']
                )
            else:
                doc = Document(
                    title = args['title'],
                    contactinfo = args['contactInfo'],
                    extra_info = args['extraInfo'],
                    salaryF = args['salaryF'],
                    salaryS = args['salaryS'],
                    type = args['type'],
                    userId = args['userId']
                )
            resultKnow =[]
            for item in knowledgesArg:
                    knowId = item
                    resultKnow.append(knowId)
            resultDepend =[]
            resultExp=[]
            if type == "resume":
                
                for item in dependenciesArg:
                    depId = item
                    resultDepend.append(depId)
                
                for item in experienceArg:
                    depId = item
                    resultExp.append(depId)
                knowledges = session.query(Knowledge).filter(Knowledge.id.in_(resultKnow)).all()
                dependencies = session.query(Spec_to_Edu_to_User).filter(Spec_to_Edu_to_User.id.in_(resultDepend), Spec_to_Edu_to_User.userId == args['userId']).all()
                experience = session.query(Experiens).filter(Experiens.id.in_(resultExp), Experiens.userId == user_id).all()
                if not knowledges == None:
                    doc.knowledge.extend(knowledges)
                if not dependencies == None:
                    doc.spec_to_edu_to_user.extend(dependencies)
                if not experience == None:
                    doc.experience.extend(experience)
            elif type == "vacancy":
                for item in dependenciesArg:
                    dep = session.query(Spec_to_Edu_to_User).filter(and_(Spec_to_Edu_to_User.userId == user_id, Spec_to_Edu_to_User.specId == item)).first()
                    if dep == None:
                        dep = Spec_to_Edu_to_User(documents_scan_id= None,
                                                userId = user_id, 
                                                specId = item,
                                                eduId = None)
                        
                        session.add(dep)
                    else:
                        pass
                    resultDepend.append(dep.id)
                for item in experienceArg:
                    exp = session.query(Experiens).filter(and_(Experiens.userId == user_id, Experiens.expTimeId == item)).first()
                    if exp == None:
                        exp = Experiens(documents_scan_id= None,
                                        role = None,
                                        place = None, 
                                        userId = user_id,
                                        experience = None,
                                        expTimeId =item)
                        session.add(exp)
                    else:
                        pass
                    resultExp.append(exp.id)
                dependencies = session.query(Spec_to_Edu_to_User).filter(Spec_to_Edu_to_User.id.in_(resultDepend), Spec_to_Edu_to_User.userId == args['userId']).all()
                experience = session.query(Experiens).filter(Experiens.id.in_(resultExp), Experiens.userId == user_id).all()
                if not dependencies == None:
                    doc.spec_to_edu_to_user.extend(dependencies)
                if not experience == None:
                    doc.experience.extend(experience)
                    
            session.add(doc)
            session.commit()
            return jsonify({'statys': 'OK'})
        finally:
            session.close() 
class EditUserDocument(Resource):
    
    def put(self, auth_user_id, user_id, doc_id):
        session = db_sessions.create_session() 
        try:
            document = session.query(Document).filter(Document.id == doc_id, Document.userId == user_id)
            if document == None:
                return jsonify({'statys': 'Not found'})
            else:
                user = session.query(User).get(auth_user_id)
                if user == None:
                    return jsonify({'statys': 'Not allowed'})
                if user.id != user_id and user.role.name !="администратор":
                    return jsonify({'statys': 'Not allowed'})
                else:
                    session.query(Exp_to_Doc).filter(Exp_to_Doc.docId == doc_id).delete()
                    session.query(knowledge_to_document).filter(knowledge_to_document.c.documents == doc_id).delete()
                    session.query(dependencies_to_documents).filter(dependencies_to_documents.c.documents == doc_id).delete()
                    args = fulldocumentparser.parse_args()
                    dependenciesArg = args["dependencies"]
                    knowledgesArg = args["knowledges"]
                    experienceArg = args["experience"]
                    print(str(args) + "Вот здесь")
                    if args['salaryF'] == "" and args['salaryS'] == "":
                        doc = Document(
                            title = args['title'],
                            contactinfo = args['contactInfo'],
                            extra_info = args['extraInfo'],
                            salaryF = None,
                            salaryS = None,
                            type = args['type'],
                            userId = args['userId']
                        )
                    elif args['salaryF'] == "":
                        doc = Document(
                            title = args['title'],
                            contactinfo = args['contactInfo'],
                            extra_info = args['extraInfo'],
                            salaryF = None,
                            salaryS = args['salaryS'],
                            type = args['type'],
                            userId = args['userId']
                        )
                    elif args['salaryS'] == "":
                        doc = Document(
                            title = args['title'],
                            contactinfo = args['contactInfo'],
                            extra_info = args['extraInfo'],
                            salaryF = args['salaryF'],
                            salaryS = None,
                            type = args['type'],
                            userId = args['userId']
                        )
                    else:
                        doc = Document(
                            title = args['title'],
                            contactinfo = args['contactInfo'],
                            extra_info = args['extraInfo'],
                            salaryF = args['salaryF'],
                            salaryS = args['salaryS'],
                            type = args['type'],
                            userId = args['userId']
                        )
                    resultKnow =[]
                    for item in knowledgesArg:
                        knowId = item
                        resultKnow.append(knowId)
                    resultDepend =[]
                    for item in dependenciesArg:
                        depId = item
                        resultDepend.append(depId)
                    resultExp=[]
                    for item in experienceArg:
                        depId = item
                        resultExp.append(depId)
                    knowledges = session.query(Knowledge).filter(Knowledge.id.in_(resultKnow)).all()
                    dependencies = session.query(Spec_to_Edu_to_User).filter(Spec_to_Edu_to_User.id.in_(resultDepend), Spec_to_Edu_to_User.userId == args['userId']).all()
                    experience = session.query(Experiens).filter(Experiens.id.in_(resultExp), Experiens.userId == user_id).all()
                    if not knowledges == None:
                        doc.knowledge.extend(knowledges)
                    if not dependencies == None:
                        doc.spec_to_edu_to_user.extend(dependencies)
                    if not experience == None:
                        doc.experience.extend(experience)
                    session.add(doc)
                    session.commit()
                    return jsonify({'statys': 'OK'})
        finally:
            session.close() 
        
        