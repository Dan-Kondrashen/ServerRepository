import datetime
import os
from flask import jsonify, request
from flask_restful import abort, Resource
from models import db_sessions
from models.spec_to_edu_to_user import Spec_to_Edu_to_User
from models.documents import Document, dependencies_to_documents
from models.users import User
from models.files import File
from reqparsers.document_dependencies_reqparse import parser, parserlink
from werkzeug.utils import secure_filename

def find_link(dependence_id):
    session = db_sessions.create_session()
    dependence = session.query(Spec_to_Edu_to_User).get(dependence_id)
    return dependence

def abort_if_link_not_found(dependence, dependence_id):
    if not dependence:
        abort(404, message=f"Document dependence with number {dependence_id} not found")
    
class DependenciesListResource(Resource):
    def get(self, doc_id):
        session = db_sessions.create_session()
        try:
            dependencies = session.query(Spec_to_Edu_to_User).join(Spec_to_Edu_to_User.document).filter(Document.id == doc_id)
            return jsonify([item.to_dict(only=('id', 'specId', 'userId', 'eduId', 'documents_scan_id')) for item in dependencies])
        finally:
            session.close()
    def post(self, doc_id):
        args = parserlink.parse_args()
        session = db_sessions.create_session()
        try:
            doc = session.query(Document).get(doc_id)
            dep_id = args["id"]
            dependencies= session.query(Spec_to_Edu_to_User).get(dep_id)
            doc.spec_to_edu_to_user.append(dependencies)
            session.add(doc)
            session.commit()
            return jsonify({'success': 'OK'})
        finally:
            session.close()
class UserDependenciesListResource(Resource):
    def get(self, user_id):
        session = db_sessions.create_session()
        try:
            dependencies = session.query(Spec_to_Edu_to_User).filter(Spec_to_Edu_to_User.userId == int(user_id))
            return jsonify([{**item.to_dict(only=('id', 'specId', 'userId', 'eduId')), 'documentsScanId': item.documents_scan_id  }for item in dependencies])
        finally:
            session.close()
    
    def post(self, user_id):
        name = request.form['name']
        specId = request.form['specId']
        eduId = request.form['eduId']
        print(eduId + "_________________")
        date = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")
        session = db_sessions.create_session()
        try:
            if name != None and name !="":
            
                result = request.files['file']
                resNameFinal = secure_filename(result.filename)
                resPath ="/Users/larisa/Desktop/serverresult/"+str(date)+resNameFinal
                result.save(resPath)
                file = File(file_name =name,
                            file_path = resPath,
                            userId = user_id,
                            type = "extraSpec")
                session.add(file)
                session.commit()
                
                
                if eduId != None and eduId != "null":
                    spec_to_edu =Spec_to_Edu_to_User(
                        userId =user_id,
                        documents_scan_id = file.id,
                        specId = specId,
                        eduId = eduId
                    )  
                    session.add(spec_to_edu)
                    session.commit()
                    return jsonify(status= "OK")
                else:
                    spec_to_edu =Spec_to_Edu_to_User(
                        userId =user_id,
                        documents_scan_id = file.id,
                        specId = specId,
                        eduId = None
                    ) 
                    session.add(spec_to_edu)
                    session.commit()
                    return jsonify(status= "OK")
                    
                
            else:
                session = db_sessions.create_session()
                spec_to_edu =Spec_to_Edu_to_User(
                    userId =user_id,
                    documents_scan_id = None,
                    specId = specId,
                    eduId = eduId
                    )    
                session.add(spec_to_edu)
                session.commit()
                return jsonify(status= "OK")
        finally:
            session.close()
        
class UserDependenceResource(Resource):
    
    def delete(self, user_id, depend_id):
        session = db_sessions.create_session()
        depend = session.query(Spec_to_Edu_to_User).filter(Spec_to_Edu_to_User.id == int(depend_id)).first()
        if not depend == None:
            user = session.query(User).filter(User.id == int(user_id)).first()
            if user is None:
                return jsonify({'status': 'No such user'})
            if depend.userId == int(user_id):
                session.query(dependencies_to_documents).filter(dependencies_to_documents.c.special_to_education_to_user == int(depend_id)).delete()
                session.query(Spec_to_Edu_to_User).filter(Spec_to_Edu_to_User.id == int(depend_id)).delete()
                session.commit()
                return jsonify({'status': 'OK'})
            elif user.role.name == "admin":
                session.query(dependencies_to_documents).filter(dependencies_to_documents.c.special_to_education_to_user == int(depend_id)).delete()
                session.query(Spec_to_Edu_to_User).filter(Spec_to_Edu_to_User.id == int(depend_id)).delete()
                session.commit()
                return jsonify({'status': 'OK'})
            else:
                session.commit()
                return jsonify({'status': 'not allowed'})
        else:
            session.commit()
            return jsonify({'status': 'not found'})
            
        
        
    
    def put(self, user_id, depend_id):
        session = db_sessions.create_session()
        try:
            dependence = session.query(Spec_to_Edu_to_User).filter(Spec_to_Edu_to_User.id == depend_id, Spec_to_Edu_to_User.userId == user_id).first()
            abort_if_link_not_found(dependence, depend_id)
            name = request.form['name']
            result = request.files['file']
            print("----------------------------")
            if  not name == "" and not result.filename == "": 
                file = session.query(File).filter(File.id == dependence.documents_scan_id).first()
                if not file == None:
                    print("----------------------------")
                    date = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")
                    resNameFinal = secure_filename(result.filename)
                    print(resNameFinal)
                    resPath ="/Users/larisa/Desktop/serverresult/"+str(date)+resNameFinal
                    oldpath =file.file_path
                    if os.path.exists(oldpath):
                        os.remove(oldpath)
                    result.save(resPath)
                    print("----------------------------")
                    file.file_path = resPath
                    file.name = name
                        
                    session.add(file)
                    dependence.specId = request.form['specId']
                    dependence.eduId = request.form['eduId']
                    session.add(dependence)
                    session.commit()
                    return jsonify(status ="Success with file")
                else:
                    date = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")
                    resNameFinal = secure_filename(result.filename)
                    print("--------------------11--------")
                    resPath ="/Users/larisa/Desktop/serverresult/"+str(date)+resNameFinal
                    result.save(resPath)
                    file = File(
                        file_name =name,
                        file_path = resPath,
                        userId = user_id,
                        type = "extraSpec"
                        )
                    print(file.file_name)
                    
                    session.add(file)
                    session.commit()
                    dependence.specId = request.form['specId']
                    dependence.eduId = request.form['eduId']                   
                    dependence.documents_scan_id = file.id
                    session.add(dependence)
                    session.commit()
                    return jsonify(status ="Success with excectly new file")
                        
            elif not name == "":
                file = session.query(File).filter(File.id == dependence.documents_scan_id).first()
                if not file == None:
                    file.name = name
                    session.add(file)
                    dependence.specId = request.form['specId']
                    dependence.eduId = request.form['eduId']
                    session.add(dependence)
                    session.commit()
                    return jsonify(status ="Success with new name")
                else:    
                    dependence.specId = request.form['specId']
                    dependence.eduId = request.form['eduId']
                    session.add(dependence)
                    session.commit()
                    return jsonify(status ="Success, but no file to rename")
            else:
                print("here")
                dependence.specId = request.form['specId']
                dependence.eduId = request.form['eduId']
                session.add(dependence)
                session.commit()
                return jsonify(status ="Success")
        finally:
            session.close()
        
    
    
    # def post(self, user_id):
    #     args = parser.parse_args()
    #     dependence = Spec_to_Edu_to_User(**args)
    #     dependence.userId= user_id
    #     session = db_sessions.create_session()
    #     name = request.form['file_name']
        
    #     check = session.query(Spec_to_Edu_to_User).filter(Spec_to_Edu_to_User.specId==dependence.specId,
    #                                                             Spec_to_Edu_to_User.eduId == dependence.eduId, 
    #                                                             Spec_to_Edu_to_User.userId == user_id).first()
    #     if not check:
    #         dependence.save_to_db()
    #     else:
    #         return jsonify(status="Вы уже добавили эти данные об образовании, если вы хотите обновить их перейдите в личный кабинет, выберите эти данные в соответствующей выкладке и измените их по своему усмотрению!")
        
    #     return jsonify({'success': 'OK'})
    
    
    # def post(self, user_id):
    #     session = db_sessions.create_session()
    #     addType = request.form['addType']
    #     if addType == "newFile":
    #         name = request.form['file_name']
    #         file = session.query(File).filter(File.file_name == name , File.userId == user_id).first()
    #         session.commit()
    #         if not file:
    #             type = request.form['type']
    #             result = request.files['file']
    #             resName = secure_filename(result.filename)
    #             resPath ="/Users/larisa/Desktop/serverresult/"+resName
    #             result.save(resPath)
    #             file = File(file_name =name,
    #                     file_path = resPath,
    #                     userId = user_id,
    #                     type = type)
    #             file.save_to_db()
    #             check = session.query(Spec_to_Edu_to_User).filter(Spec_to_Edu_to_User.specId==dependence.specId,
    #                                                                 Spec_to_Edu_to_User.eduId == dependence.eduId, 
    #                                                                 Spec_to_Edu_to_User.userId == user_id).first()
    #             if not check:
    #                 dependence = Spec_to_Edu_to_User(user_id =user_id, 
    #                                                 edu_id = request.form['edu_id'], 
    #                                                 spec_id = request.form['spec_id'],
    #                                                 documents_scan_id = file.id)
    #                 dependence.save_to_db()
    #             else:
    #                 return jsonify(status="Вы уже добавили эти данные об образовании, если вы хотите обновить их перейдите в личный кабинет, выберите эти данные в соответствующей выкладке и измените их по своему усмотрению!")
    #         else:
    #             check = session.query(Spec_to_Edu_to_User).filter(Spec_to_Edu_to_User.specId==dependence.specId,
    #                                                                 Spec_to_Edu_to_User.eduId == dependence.eduId, 
    #                                                                 Spec_to_Edu_to_User.userId == user_id).first()
    #             if not check:
    #                 dependence = Spec_to_Edu_to_User(user_id =user_id, 
    #                                                 edu_id = request.form['edu_id'], 
    #                                                 spec_id = request.form['spec_id'],
    #                                                 documents_scan_id = file.id)
    #                 dependence.save_to_db()
    #                 return jsonify(status="")
    #             else:
    #                 return jsonify(status="Вы уже добавили эти данные об образовании, если вы хотите обновить их перейдите в личный кабинет, выберите эти данные в соответствующей выкладке и измените их по своему усмотрению!")
        
        