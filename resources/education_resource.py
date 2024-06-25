from flask import jsonify
from flask_restful import abort, Resource
from models import db_sessions

from models.educations import Education

from reqparsers.education_reqparse import parser

def find_education(edu_id):
    session = db_sessions.create_session()
    education= session.query(Education).get(edu_id)
    return education
def abort_if_education_not_found(edu, edu_id):
    if not edu:
        abort(404, message=f"Education with number {edu_id} not found")


class EducationListResource(Resource):
    
    def get(self):
        session = db_sessions.create_session()
        try:
            educations = session.query(Education).all()
            return jsonify([item.to_dict(only=('id', 'name', 'description')) for item in educations])
        finally:
            session.close()
    
    def post(self):
        args = parser.parse_args()
        exp = Education(**args)
        exp.save_to_db()
        return jsonify({'success': 'OK'})
    
class EducationResource(Resource):
    
    def get(self, edu_id):
        session = db_sessions.create_session()
        educations = session.query(Education).get(edu_id)
        return jsonify(educations.to_dict(only=('id', 'name', 'description')))
    
    def put(self,edu_id):
        args = parser.parse_args()
        if (args['name'] == ''):
            return jsonify(status="Место получения образование должно иметь название!")
        else:
            edu = find_education(edu_id)
            edu.name = args['name']
            edu.description = args['description']
            edu.update_to_db()
            return jsonify(status="Успешно")
        
    def delete(self, edu_id):
        session = db_sessions.create_session()
        session.query(Education).filter(Education.id == edu_id).delete()
        session.commit()
        return jsonify({'success': 'OK'})
        