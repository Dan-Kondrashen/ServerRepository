import datetime
import os
from flask import jsonify, request
from flask_restful import abort, Resource
from models import db_sessions
from models.experiens import Experiens
from models.experience_time import ExperienceTime
from models.users import User
from models.Exp_to_Doc  import Exp_to_Doc
from reqparsers.experiens_reqparse import parser
from reqparsers.expToDoc_reqparse import parserlink
from models.files import File
from werkzeug.utils import secure_filename

def find_experience(exp_id):
    session = db_sessions.create_session()
    document = session.query(Experiens).get(exp_id)
    session.commit()
    return document
def find_exp_to_doc(doc_id, exp_id):
    session = db_sessions.create_session()
    document = session.query(Exp_to_Doc).filter_by(docId = doc_id, expId = exp_id).first()
    return document


def abort_if_experience_not_found(exp, exp_id):
    if not exp:
        abort(404, message=f"Experience with number {exp_id} not found")
        
def abort_if_experience_link_not_found(exp, exp_id):
    if not exp:
        abort(404, message=f"Experience to document with number {exp_id} not found")

class ExperiensResurce(Resource):
    def get(self, exp_id):
        experiens = find_experience(exp_id)
        abort_if_experience_not_found(experiens, exp_id)
        return jsonify(experiens.to_dict(only=('id', 'experience', 'role', 'place')))
    def put(self, exp_id):
        args = parser.parse_args()
        experiens = find_experience(exp_id)
        abort_if_experience_not_found(experiens, exp_id)
        if (args['experiens'] == "" or args['role'] == ""):
            return jsonify(status="Чтобы добавить опыт работы, все поля формы должны быть зополнены!")
        else:
            experiens.experiens = args['experience']
            experiens.role = args['role']
            experiens.place = args['place']
            experiens.update_to_db()
            return jsonify(status="Успешно")
    def delete(self, exp_id):
        session = db_sessions.create_session()
        session.query(Experiens).filter(Experiens.id == exp_id).delete()
        session.commit()
        return jsonify({'success': 'OK'})
    
class AllExperiensResource(Resource):
    def get(self):
        session = db_sessions.create_session()
        experiens = session.query(Experiens).all()
        return jsonify([item.to_dict(only=('id', 'experience', 'role', 'place')) for item in experiens])

    def post(self):
        args = parser.parse_args()
        exp = Experiens(**args)
        exp.save_to_db()
        return jsonify({'success': 'OK'})

class ExpeToDocListResource(Resource):
    def get(self, doc_id):
        session = db_sessions.create_session()
        experiens = session.query(Exp_to_Doc).filter_by(docId = doc_id)
        return jsonify([item.to_dict(only=('id','docId', 'expId', 'documents_scan')) for item in experiens])

    def post(self, doc_id):
        args = parserlink.parse_args()
        exp = Exp_to_Doc(**args)
        exp.docId = doc_id
        test1 = find_exp_to_doc(exp.docId, exp.docId)
        if not test1:
            exp.save_to_db()
            return jsonify({'success': 'OK'})
        else:
            return jsonify(status="Вы уже добавили эти данные в документ")

class ExpeToDocResource(Resource):
    def get(self, doc_id, exp_id):
        session = db_sessions.create_session()
        exp_to_doc = session.query(Exp_to_Doc).filter_by(docId = doc_id, expId = exp_id).first()
        return jsonify(exp_to_doc.to_dict(only=('id', 'expId', 'docId', 'documents_scan')))
    def put(self, doc_id, exp_id):
        args = parserlink.parse_args()
        experiens = find_exp_to_doc(doc_id, exp_id)
        abort_if_experience_not_found(experiens, exp_id)
        if (args['documents_scan'] == ""):
            return jsonify(status="Чтобы подтвердить наличие опыта, прикрепите сканы соответствуюших документов ниже!")
        else:
            experiens.docId = args['docId']
            experiens.expId = args['expId']
            experiens.documents_scan = args['documents_scan']
            experiens.update_to_db()
            return jsonify(status="Успешно")
        
class UserExperienceListResource(Resource):
    def get(self,  auth_user_id, user_id):
        session = db_sessions.create_session()
        experiens = session.query(Experiens).filter_by(userId = user_id).all()
        return jsonify([{**item.to_dict(only=('experience', 'role', 'place', 'userId')), 'documentScanId': item.documents_scan_id, "expTimeId": item.expTime.id, 'expId': item.id} for item in experiens])
        
    def post(self, auth_user_id, user_id):
        name = request.form['name']
        # specId = request.form['specId']
        experience = request.form['experience']
        expTimeId = request.form['exp_time_id']
        role = request.form['role']
        place = request.form['place']
        result = request.files['file']
        date = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")
        if name != None and name !="":
            resNameFinal = secure_filename(result.filename)
            resPath ="/Users/larisa/Desktop/serverresult/"+str(date)+resNameFinal
            result.save(resPath)
            file = File(file_name =name,
                        file_path = resPath,
                        userId = user_id,
                        type = "extraExp")
            file.save_to_db()
            session = db_sessions.create_session()
            fileId = session.query(File.id).filter(File.file_name == name, File.userId == user_id)
            exp = Experiens(experience = experience,
                            expTimeId = expTimeId,
                            userId = user_id,
                            role = role,
                            place = place,
                            documents_scan_id = fileId)    
        
            session.add(exp)
            session.commit()
            return jsonify(status= "OK")
        else:
            session = db_sessions.create_session()
            exp = Experiens(experience = experience,
                            expTimeId = expTimeId,
                            userId = user_id,
                            role = role,
                            place = place,
                            documents_scan_id = None)    
            session.add(exp)
            session.commit()
            return jsonify(status= "OK")
            
    
class UserExperienceResource(Resource):
    
    def delete(self, user_id, exp_id):
        session = db_sessions.create_session()
        try:
            experience = session.query(Experiens).filter(Experiens.id == int(exp_id)).first()
            if not experience == None:
                user = session.query(User).filter(User.id == int(user_id)).first()
                if user is None:
                    return jsonify({'status': 'No such user'})
                if experience.userId == int(user_id):
                    session.query(Exp_to_Doc).filter(Exp_to_Doc.expId == int(exp_id)).delete()
                    session.query(Experiens).filter(Experiens.id == int(exp_id)).delete()
                    session.commit()
                    return jsonify({'status': 'OK'})
                elif user.role.name == "admin":
                    session.query(Exp_to_Doc).filter(Exp_to_Doc.expId == int(exp_id)).delete()
                    session.query(Experiens).filter(Experiens.id == int(exp_id)).delete()
                    session.commit()
                    return jsonify({'status': 'OK'})
                else:
                    session.commit()
                    return jsonify({'status': 'not allowed'})
            else:
                session.commit()
                return jsonify({'status': 'not found'})
        finally:
            session.close()
        
    
    def put(self, user_id, exp_id):
        session = db_sessions.create_session()
        try:
            experience = session.query(Experiens).get(exp_id)
            abort_if_experience_not_found(experience, exp_id)
            name = request.form['name']
            result = request.files['file']
            if  not name == "" and not result.filename == "":
                
                file = session.query(File).filter(File.id == experience.documents_scan_id).first()
                if not file == None:
                    date = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")
                    resNameFinal = secure_filename(result.filename)
                    resPath ="/Users/larisa/Desktop/serverresult/"+str(date)+resNameFinal
                    oldpath =file.file_path
                    if os.path.exists(oldpath):
                        os.remove(oldpath)
                    result.save(resPath)
                    file.file_path = resPath
                    file.name = name
                    session.add(file)
                    experience.experiens = request.form['experience']
                    experience.role = request.form['role']
                    experience.expTimeId = request.form['exp_time_id']
                    experience.place = request.form['place']
                    session.add(experience)
                    session.commit()
                    if (request.form['role']== ""):
                        return jsonify(status="Not at all!")
                    else:
                        return jsonify(status ="Success with file")
                    
                else:
                    date = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")
                    resNameFinal = secure_filename(result.filename)
                    resPath ="/Users/larisa/Desktop/serverresult/"+str(date)+resNameFinal
                    result.save(resPath)
                    file = File(
                        file_name =name,
                        file_path = resPath,
                        userId = user_id,
                        type = "extraExp"
                        )
                    session.add(file)
                    session.commit()
                    experience.experience = request.form['experience']
                    experience.role = request.form['role']
                    experience.expTimeId = request.form['exp_time_id']
                    experience.place = request.form['place']
                    experience.documents_scan_id = file.id
                    session.add(experience)
                    session.commit()
                    if (request.form['role']== ""):   
                        return jsonify(status="Not at all!")
                    else:
                        return jsonify(status ="Success with excectly new file")
                    
            elif not name == "":
                file = session.query(File).filter(File.id == experience.documents_scan_id).first()
                if not file == None:
                    file.name = name
                    session.add(file)
                    experience.experience = request.form['experience']
                    experience.role = request.form['role']
                    experience.expTimeId = request.form['exp_time_id']
                    experience.place = request.form['place']
                    session.add(experience)
                    session.commit()
                    if (request.form['role']== ""):
                        return jsonify(status="Not at all!")
                    else:
                        return jsonify(status ="Success with new name")
                else:    
                    experience.experience = request.form['experience']
                    experience.role = request.form['role']
                    experience.expTimeId = request.form['exp_time_id']
                    experience.place = request.form['place']
                    session.add(experience)
                    session.commit()
                    return jsonify(status ="Success, but no file to rename")
            else:
                experience.experience = request.form['experience']
                experience.role = request.form['role']
                experience.expTimeId = request.form['exp_time_id']
                experience.place = request.form['place']
                session.add(experience)
                session.commit()
                if (request.form['role']== ""):
                    return jsonify(status="Not at all!")
                else:
                    return jsonify(status ="Success")
        finally:
            session.close()
            
class ExperiensTimeListResource(Resource):
    def get(self):
        session = db_sessions.create_session()
        experiens = session.query(ExperienceTime).all()
        session.commit
        return jsonify([item.to_dict(only=('id', 'experienceTime')) for item in experiens])
        