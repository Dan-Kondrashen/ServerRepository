import datetime
import os
from flask import jsonify, json, make_response, request
from dateutil.relativedelta import *
from sqlalchemy import and_
from flask_restful import abort, Resource, reqparse
from models.knowledge import Knowledge
from models import db_sessions
from models.levels import Level
from models.files import File
from models.user_level_exp import UserLevelExp
from reqparsers.levels_reqparse import parser
from models.user_current_level_info import UserCurLevel
from models.users import User
from werkzeug.utils import secure_filename

class LevelExperienseResource(Resource):
    def delete(self, user_id, appExp_id):
        session = db_sessions.create_session()  
        try:     
            
            item = session.query(UserLevelExp).get(appExp_id)
            session.delete(item)
            session.commit()
            return(jsonify(status="success"))
        finally:
            session.close()
    def put(self, user_id, appExp_id):
        session = db_sessions.create_session()        
        item = session.query(UserLevelExp).get(appExp_id)
        session.delete(item)
        session.commit()
        return(jsonify(status="success"))
    
class LevelUserExperienseResource(Resource):
    def delete(self, auth_user_id, user_id, appExp_id):
        session = db_sessions.create_session()  
        try:     
            user = session.query(User).filter(User.id ==int(auth_user_id)).first()
            if user == None:
                return make_response(jsonify(status="not find auth user"), 404)
            if user.roleId != 3:
                return make_response(jsonify(status="not allowed"), 403)
            item = session.query(UserLevelExp).filter(UserLevelExp.userId == int(user_id), UserLevelExp.id == int(appExp_id)).first()
            if not item == None:
                session.delete(item)
                session.commit()
                return(jsonify(status="success"))
            else:
                return make_response(jsonify(status="not find item"), 404)
        finally:
            session.close()
    def put(self, auth_user_id, user_id, appExp_id):
        session = db_sessions.create_session()        
        try: 
            item = session.query(UserLevelExp).get(appExp_id)
            user = session.query(User).filter(User.id ==int(auth_user_id)).first()
            if not item == None:
                if user ==None:     
                    return make_response(jsonify(status="not find auth user"), 404)
                if user.id == user_id or user.roleId == 3:
                    name = request.form['name']
                    status = request.form['status']
                    points = request.form['points']
                    type = request.form['type']
                    reason = request.form['reason']
                    result = request.files['file']
                    
                    if name != None and name !="" and result.filename != "":
                        file = session.query(File).filter(File.id == item.documents_scan_id).first()
                        if file == None:
                            date = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")
                            resNameFinal = secure_filename(result.filename)
                            resPath ="/Users/larisa/Desktop/serverresult/"+str(date)+resNameFinal
                            result.save(resPath)
                            file = File(file_name =name,
                                        file_path = resPath,
                                        userId = user_id,
                                        type = "extraExp")
                            session.add(file)
                            session.commit()
                            session = db_sessions.create_session()
                            fileId = session.query(File.id).filter(File.file_name == name, File.userId == user_id)
                            item.status = status
                            item.points = points
                            item.userId = user_id
                            item.type = type
                            item.reason = reason,
                            item.documents_scan_id = fileId      
                        
                            session.add(item)
                            session.commit()
                            if (request.form['reason']== ""):
                                return jsonify(status="not at all!")
                            else:
                                return(jsonify({"status": "success",
                                                "userExp": item.to_dict(only=("id", "reason", "status", "type", "points", "userId", "documents_scan_id"))}))
                        else:
                            date = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S.%f")
                            resesNameFinal = secure_filename(result.filename)
                            resPath ="/Users/larisa/Desktop/serverresult/"+str(date)+resNameFinal
                            oldpath =file.file_path
                            if os.path.exists(oldpath):
                                os.remove(oldpath)
                            result.save(resPath)
                            file.file_path = resPath
                            file.name = name
                            session.add(file)
                            session.commit()
                            fileId = session.query(File.id).filter(File.file_name == name, File.userId == user_id)
                            item.status = status
                            item.points = points
                            item.userId = user_id
                            item.type = type
                            item.reason = reason,
                            item.documents_scan_id = fileId
                            session.add(item)
                            session.commit()
                            if (request.form['reason']== ""):
                                return jsonify(status="not at all!")
                            else:
                                return(jsonify({"status": "success",
                                                "userExp": item.to_dict(only=("id", "reason", "status", "type", "points", "userId", "documents_scan_id"))}))
                    elif not name == "":
                        file = session.query(File).filter(File.id == item.documents_scan_id).first()
                        if not file == None:
                            file.name = name
                            session.add(file)
                            session.commit()
                            item.status = status
                            item.points = points
                            item.userId = user_id
                            item.type = type
                            item.reason = reason,
                            item.documents_scan_id = file.id
                            session.add(item)
                            session.commit()
                            if (request.form['reason']== ""):
                                return jsonify(status="not at all!")
                            else:
                                return(jsonify({"status": "success",
                                                "userExp": item.to_dict(only=("id", "reason", "status", "type", "points", "userId", "documents_scan_id"))}))
                    else:
                        item.status = status
                        item.points = points
                        item.userId = user_id
                        item.type = type
                        item.reason = reason
                        session.add(item)
                        session.commit()
                        if (request.form['reason']== ""):
                            return jsonify(status="not at all!")
                        else:
                            return(jsonify({"status": "success",
                                            "userExp": item.to_dict(only=("id", "reason", "status", "type", "points", "userId", "documents_scan_id"))}))
                else:
                    return make_response(jsonify(status="not allowed"), 403)
            else:
                return make_response(jsonify(status="not find item"), 404)
        finally:
            session.close()

class LevelExperienseListResource(Resource):
    def get(self, auth_user_id, user_id):
        session = db_sessions.create_session()    
        try:
            user = session.query(User).filter(User.id ==int(auth_user_id)).first()
            if user == None:
                return make_response(jsonify(status="not find auth user"), 404)
            if user.id == user_id or user.roleId == 3:
                items = session.query(UserLevelExp).filter(UserLevelExp.userId == user_id).all()
                return jsonify([item.to_dict(only=("id", "reason", "status", "type", "points", "userId", "documents_scan_id")) for item in items])
            else:
                return make_response(jsonify(status="not allowed"), 403)
        finally:
            session.close()
    # def post(self, auth_user_id, user_id):
    #     session = db_sessions.create_session()  
    #     try: 
    #         user = session.query(User).filter(User.id ==int(auth_user_id)).first()
    #         if user !=None:     
    #             return make_response(jsonify(status="not find auth user"), 404)
    #         if user.id == user_id or user.roleId == 3:
                
    #             args = parser.parse_args()
    #             points = args['points']
    #             userId = args['userId']
    #             reason = args['reason']
    #             status = args['status']
    #             type = args['type']
    #             document_scan_id = args['document_scan_id']
    #             userExp = UserLevelExp(reason= reason,
    #                                 points = points,
    #                                 userId = userId,
    #                                 status = status,
    #                                 type = type)
    #             session.add(userExp)
    #             session.commit()
    #             return(jsonify({"status": "success",
    #                             "userExp": userExp.to_dict(only=("id", "reason", "status", "type", "points", "userId", "documents_scan_id"))}))
    #         else:
    #             return make_response(jsonify(status="not allowed"), 403)
    #     finally:
    #         session.close()
    
    def post(self, auth_user_id, user_id):
        session = db_sessions.create_session()  
        try: 
            user = session.query(User).filter(User.id ==int(auth_user_id)).first()
            if user ==None:     
                return make_response(jsonify(status="not find auth user"), 404)
            if user.id == user_id or user.roleId == 3:
                name = request.form['name']
                # specId = request.form['specId']
                status = request.form['status']
                points = request.form['points']
                type = request.form['type']
                reason = request.form['reason']
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
                    userExp = UserLevelExp(status = status,
                                    points = points,
                                    userId = user_id,
                                    type = type,
                                    reason = reason,
                                    documents_scan_id = fileId)    
                
                    session.add(userExp)
                    session.commit()
                    return(jsonify({"status": "success",
                                    "userExp": userExp.to_dict(only=("id", "reason", "status", "type", "points", "userId", "documents_scan_id"))}))
                else:
                    userExp = UserLevelExp(status = status,
                                    points = points,
                                    userId = user_id,
                                    type = type,
                                    reason = reason,
                                    documents_scan_id = None)  
                    session.add(userExp)
                    session.commit()
                    return(jsonify({"status": "success",
                                    "userExp": userExp.to_dict(only=("id", "reason", "status", "type", "points", "userId", "documents_scan_id"))}))
            else:
                return make_response(jsonify(status="not allowed"), 403)
        finally:
            session.close()

class CurUserLevelInfoResource(Resource):
    def get(self, user_id):
        session = db_sessions.create_session()
        userLevel = session.query(UserCurLevel).filter(UserCurLevel.userId == user_id).first()
        if userLevel is None:
            pass
        else:
            curLevel = session.query(Level).get(userLevel.levelId)
            nextLevel = session.query(Level).filter(Level.minPoints == curLevel.maxPoints).first()
            previewsLevel = session.query(Level).filter(Level.maxPoints == curLevel.minPoints).first()
            if nextLevel is None or previewsLevel is None:
                if nextLevel is None:
                    return(jsonify(curPoints = userLevel.curPoints, 
                                levelId = curLevel.id, 
                                levelNum = curLevel.number, 
                                levelMinP = curLevel.minPoints,
                                levelMaxP = curLevel.maxPoints,
                                previewsLevelId = previewsLevel.id,
                                previewsLevelMinP = previewsLevel.minPoints,
                                respStatus = "no next level"))
                elif previewsLevel is None:
                    return(jsonify(curPoints = userLevel.curPoints, 
                                levelId = curLevel.id, 
                                levelNum = curLevel.number, 
                                levelMinP = curLevel.minPoints,
                                levelMaxP = curLevel.maxPoints,
                                nextLevelId = curLevel.id,
                                nextLevelMaxP = curLevel.maxPoints,
                                respStatus = "no previews level"))
            else:
                return(jsonify(curPoints = userLevel.curPoints, 
                            levelId = curLevel.id, 
                            levelNum = curLevel.number, 
                            levelMinP = curLevel.minPoints,
                            levelMaxP = curLevel.maxPoints,
                            nextLevelId = nextLevel.id,
                            nextLevelMaxP = nextLevel.maxPoints,
                            previewsLevelId = previewsLevel.id,
                            previewsLevelMinP = previewsLevel.minPoints,
                            respStatus = "full data level"))
            