import datetime
from flask import jsonify, request
from flask_restful import abort, Resource
import sqlalchemy as sa
from models import db_sessions
from models.specializations import Specialization, special_to_education
from models.spec_to_edu_to_user import Spec_to_Edu_to_User
from models.educations import Education
from models.files import File
from reqparsers.specialization_reqparse import parser
from reqparsers.spec_to_edu_reqparse import parser_spec_to_edu
from reqparsers.expToDoc_reqparse import parserlink
from models.skill_analitic import SkillAnalitic
from werkzeug.utils import secure_filename

def find_specialization(spec_id):
    session = db_sessions.create_session()
    document = session.query(Specialization).get(spec_id)
    return document

def abort_if_specialization_not_found(spec, spec_id):
    if not spec:
        abort(404, message=f"Specialization with number {spec_id} not found")

class SpecToEduResource(Resource):
    
    def post(self):
        args = parser_spec_to_edu.parse_args()
        session = db_sessions.create_session()
        spec_id = args["spec_id"]
        spec = session.query(Specialization).get(spec_id)
        edu_id = args["edu_id"]
        education = session.query(Education).get(edu_id)
        spec.education.append(education)
        session.add(spec)
        session.commit()
        return jsonify({'success': 'OK'})
    
    def get(self):
        session = db_sessions.create_session()
        specializations = session.query(Specialization).join(Specialization.education).all()
        session.commit()
        
        return jsonify([item.to_dict(only=("id", "name", "education.id", "education.name")) for item in specializations])
        # specializations = session.query(Specialization).join(Specialization.education).all()
        # for specialization in specializations:
        #     for education in specialization.education:
        #         link_data = {
        #             'edu_id': education.id,
        #             'spec_id': specialization.id,
        #         }
        #         result.append(link_data)
        # return jsonify(result)
    def delete(self):
        args = parser_spec_to_edu.parse_args()
        spec_id = args["spec_id"]
        edu_id = args["edu_id"]
        session = db_sessions.create_session()
        spec = session.query(Specialization).get(spec_id)
        edu = session.query(Education).get(edu_id)
        spec.education.remove(edu)
        session.commit()
        return jsonify({'success': 'OK'})
        
        
class SpecializationListResource(Resource):
    
    def get(self):
        session = db_sessions.create_session()
        specializations = session.query(Specialization).all()
        return jsonify([item.to_dict(only=('id', 'name', 'description')) for item in specializations])
    
    def post(self):
        args = parser.parse_args()
        exp = Specialization(**args)
        exp.save_to_db()
        return jsonify({'success': 'OK'})
        
class SpecializationResource(Resource):
    
    def get(self, spec_id):
        specializations = find_specialization(spec_id)
        abort_if_specialization_not_found(specializations, spec_id)
        return jsonify(specializations.to_dict(only=('id', 'name', 'description')))
    
    def put(self,spec_id):
        args = parser.parse_args()
        if (args['name'] == ''):
            return jsonify(status="Специальность должна иметь название!")
        else:
            spec = find_specialization(spec_id)
            spec.name = args['name']
            spec.description = args['description']
            spec.update_to_db()
            return jsonify(status="Успешно")
        
    def delete(self, spec_id):
        session = db_sessions.create_session()
        session.query(Specialization).filter(Specialization.id == spec_id).delete()
        session.commit()
        return jsonify({'success': 'OK'})
    
class UserSpecToEduResource(Resource):
    def get(self, user_id):
        session = db_sessions.create_session()
        # item = session.query(File).filter(File.userId == user_id).first()
        
    def post(self, user_id):
        name = request.form['name']
        specId = request.form['specId']
        eduId = request.form['eduId']
        
        date = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")
        if not name == None:
            result = request.files['file']
            resNameFinal = secure_filename(result.filename)
            resPath ="/Users/larisa/Desktop/serverresult/"+str(date)+resNameFinal
            result.save(resPath)
            file = File(file_name =name,
                        file_path = resPath,
                        userId = user_id,
                        type = "extraSpec")
            file.save_to_db()
            
            session = db_sessions.create_session()
            fileId = session.query(File.id).filter(File.file_name == name, File.userId == user_id)
            spec_to_edu =Spec_to_Edu_to_User(
                userId =user_id,
                documents_scan_id = fileId,
                specId = specId,
                eduId = eduId
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
        
class ListSpecializationModResource(Resource):
    def get(self, mod):
        session = db_sessions.create_session()
        try:
            if mod =="exists":
                knowledges = session.query(Specialization).join(SkillAnalitic, Specialization.id == SkillAnalitic.knowId).all()
                return jsonify([item.to_dict(only=('id', 'name', 'description')) for item in knowledges]) 
            elif mod == "all":
                knowledges = session.query(Specialization).all()
                return jsonify([item.to_dict(only=('id', 'name', 'description')) for item in knowledges])   
        finally:
            session.close()