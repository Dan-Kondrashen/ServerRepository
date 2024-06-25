import os
import fitz
from io import BytesIO
from flask import jsonify, send_file, request
from flask_restful import abort, Resource
from models import db_sessions
from resources.parent_resource import ParentResource
from models.files import File
from models.users import User
from reqparsers.file_reqparse import parser
from werkzeug.utils import secure_filename

class FileIntroResource(Resource, ParentResource):
    def get(self, user_id, name):
        session = db_sessions.create_session()
        try:
            item = session.query(File).filter(File.file_name == name , File.userId == user_id, File.type =="main").first()
            session.commit()
            self.abort_if_item_not_found(item, user_id)
            resPath =item.file_path
            return send_file(resPath, as_attachment=True)
        finally:
            session.close()
    
class FileResource(Resource, ParentResource):
    def get(self, file_id):
        session = db_sessions.create_session()
        try:
            item = session.query(File).filter(File.id ==file_id).first()
            if item is None:
                self.abort_if_item_not_found(item, file_id)
            else:
                split_path = os.path.splitext(item.file_path)
                if split_path[1] == ".pdf":
                    doc= fitz.open(item.file_path)
                    page = doc.load_page(0)  # number of page
                    pix = page.get_pixmap()
                    output = BytesIO()
                    img_data = pix.tobytes("jpeg") 
                    output.write(img_data)  # Записываем данные изображения в BytesIO
                    output.seek(0)
                    doc.close()
                    return send_file(output, mimetype="image/jpeg",as_attachment=True, download_name="page.jpeg")
                else:
                # if item.
                    return send_file(item.file_path, as_attachment=True)
        finally:
            session.close()

class FileDownlodResource(Resource, ParentResource):
    def get(self, user_id, file_id):
        session = db_sessions.create_session()
        try:
            user = session.query(User).filter(User.id ==int(user_id)).first()
            item = session.query(File).filter(File.id ==int(file_id)).first()
            if user is None:
                return jsonify(status="not allowed")
            if user.id != user_id:
                if item is None:
                    self.abort_if_item_not_found(item, int(file_id))
                else:
                    split_path = os.path.splitext(item.file_path)
                    if split_path[1] == ".pdf":
                        return send_file(item.file_path, mimetype="application/pdf",as_attachment=True, download_name="page.pdf")
                    else:
                        return send_file(item.file_path, mimetype="image/jpeg", as_attachment=True)
            elif user.status != "confirmed" and user.role.name !="admin":
                return jsonify(status="not allowed")
            else:
                return jsonify(status="not allowed")
        finally:
            session.close()
        
    
        
        
    
    
    # def post(self, user_id):
    #     args = parser.parse_args()
    #     name = args["name"]
    #     session = db_sessions.create_session()
    #     if name is None:
    #         items = session.query(File).filter(File.userId == user_id).all()
            
    #         return jsonify([item.to_dict(only=('id', 'file_name', 'userId'))for item in items])
            
    #     else:
    #         item = session.query(File).filter(File.file_name == name , File.userId == user_id).first()
    #         resPath ="/Users/larisa/Desktop/serverresult/"+name
    #         self.abort_if_item_not_found(item, user_id)
    #         return send_file(resPath, as_attachment=True)

class UserFileResource(Resource, ParentResource):
    
    # def get(self, user_id):
    #     session = db_sessions.create_session()
    #     item = session.query(File).filter(File.userId == user_id, File.type != "main").all()
    def post(self, user_id):
        session = db_sessions.create_session()
        try:
            name = request.form['file_name']
            item = session.query(File).filter(File.file_name == name , File.userId == user_id).first()
            if not item:
                type = request.form['type']
                result = request.files['file']
                resName = secure_filename(result.filename)
                resPath ="/Users/larisa/Desktop/serverresult/"+resName
                result.save(resPath)
                file = File(file_name =name,
                        file_path = resPath,
                        userId = user_id,
                        type = type)
                file.save_to_db()
                return jsonify(status=" success")
        finally:
            session.close()