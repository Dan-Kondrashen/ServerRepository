import datetime
from flask import jsonify
from models import db_sessions
from models.users import User
from models.location import Location
from models.document_views import DocumentViews
from models.documents import Document
from models.knowledge import Knowledge, knowledge_to_document
from models.levels import Level
from models.user_level_exp import UserLevelExp
from models.user_current_level_info import UserCurLevel
from flask_restful import Resource, abort
from reqparsers.user_reqparse import parserup, logparser, fbparser, adminparser, parserupSecond
from sqlalchemy import and_, desc
from resources.parent_resource import ParentResource

from flask_jwt_extended import create_access_token, create_refresh_token, jwt_required, get_jwt_identity, get_jwt


def abort_if_user_not_found(user, id):
    if not user:
        abort(404, message=f"User with number {id} not found")

class AllUsers(Resource):

    def get(self):
        session = db_sessions.create_session()
        users = session.query(User).all()
        return jsonify([item.to_dict(only=('id','fname', 'lname', 'mname', 'phone', 'email', 'registration_date', 'roleId')) for item in users])
    # , 'location.id', 'location.city', 'location.address', 'location.areaId'
    
    def delete(self):
        session = db_sessions.create_session()
        session.query(User).delete()
        session.commit()
        
class AdminAllUsers(Resource):
    @jwt_required(fresh=False)
    def post(self, user_id):
        if (int(user_id) == get_jwt_identity()):
            session = db_sessions.create_session()
            try:
                user = session.query(User).filter(User.id == user_id, User.roleId == 3).first()
                args = adminparser.parse_args()
                if user != None:
                    uType = args["type"]
                    uStatus = args["status"]
                    if uType != None and uStatus != None:
                        users = session.query(User).filter(User.role.name == uType, User.status == uStatus).all()
                        return jsonify([item.to_dict(only=('id','fname', 'lname', 'mname', 'phone', 'email', 'registration_date', 'roleId', 'status')) for item in users])
                    elif uStatus != None:
                        users = session.query(User).filter(User.role.name == uType).all()
                        return jsonify([item.to_dict(only=('id','fname', 'lname', 'mname', 'phone', 'email', 'registration_date', 'roleId', 'status')) for item in users])
                    elif uType != None:
                        users = session.query(User).filter(User.status == uStatus).all()
                        return jsonify([item.to_dict(only=('id','fname', 'lname', 'mname', 'phone', 'email', 'registration_date', 'roleId', 'status')) for item in users])
                    else:
                        users = session.query(User).all()
                        return jsonify([item.to_dict(only=('id','fname', 'lname', 'mname', 'phone', 'email', 'registration_date', 'roleId', 'status')) for item in users])
                    
                else: 
                    return jsonify(status = "not faund")
            finally:
                session.close()
        else: 
            return jsonify(status = "not allowed")
        
class AdminUserInfo(Resource):
    @jwt_required(fresh=False)
    def post(self, auth_user_id, user_id):
        if (int(auth_user_id) == get_jwt_identity()):
            session = db_sessions.create_session()
            try:
                user = session.query(User).filter(User.id == int(auth_user_id), User.roleId == 3).first()
                if user != None:
                    userInfo = session.query(User).filter(User.id == int(user_id)).first()
                    
                    print(userInfo)
                    if userInfo != None:
                        userLevel = session.query(UserCurLevel).filter(UserCurLevel.userId == user_id).first()
                        curLevel = session.query(Level).get(userLevel.levelId)
                        return jsonify({**user.to_dict(only=('id', 'lname', 'mname', 
                                                'fname', 'phone', 'email', 
                                                'roleId', 'registration_date',
                                                'status' , 'archive.id', 
                                                'archive.name', 'archive.searchableWord')),
                                                'curPoints': userLevel.curPoints, 
                                                'userCurLevelInfoId': userLevel.id,
                                                'levelId': curLevel.id,  
                                                'levelNum': curLevel.number, 
                                                })
                    else:
                        return jsonify(status = "user not faund")
                else: 
                    return jsonify(status = "admin not faund")
            finally:
                session.close()
        else: 
            return jsonify(status = "not allowed")
    
class UserFireBaseResourse(Resource):
    # @jwt_required(fresh=True)
    def post(self, user_id):
        session = db_sessions.create_session()
        try:
            args = fbparser.parse_args()
            userId =args["userId"]
            curUser = session.query(User).get(user_id)
            if int(userId) == int(user_id):
                if curUser is not None:
                    curUser.firebase_token= args["token"]
                    session.merge(curUser)
                    session.commit()
            elif curUser.role.name == "администратор":
                user = session.query(User).get(userId)
                if user is not None:
                    user.firebase_token= args["token"]
                    session.merge(user)
                    session.commit()
            else:
                session.commit()
        finally:
            session.close()
    def get(self, user_id):
        session = db_sessions.create_session()
        try:
            user = session.query(User).get(user_id)
            return jsonify(firebase_token = user.firebase_token)
        finally:
            session.close()
        
class UserResource(Resource):
    # @jwt_required(fresh=True)
    def get(self, user_id):
        session = db_sessions.create_session()
        try:
            user = session.query(User).get(user_id)
            abort_if_user_not_found(user, user_id)
            userLevel = session.query(UserCurLevel).filter(UserCurLevel.userId == user_id).first()
            userSkill =  session.query(Knowledge)\
                    .join(knowledge_to_document, Knowledge.id == knowledge_to_document.c.knowledges)\
                    .join(Document, knowledge_to_document.c.documents == Document.id)\
                    .filter(Document.userId == user_id)\
                    .group_by(Knowledge.id).all()
            result =list(map(lambda obj: obj.id, userSkill))
            
            if userLevel is None:
                return(jsonify({**user.to_dict(only=('id', 'lname', 'mname', 
                                                'fname', 'phone', 'email', 
                                                'roleId', 'registration_date',
                                                'status' , 'archive.id', 
                                                'archive.name', 'archive.searchableWord'))}))
            else:
                curLevel = session.query(Level).get(userLevel.levelId)
                nextLevel = session.query(Level).filter(Level.minPoints == curLevel.maxPoints).first()
                previewsLevel = session.query(Level).filter(Level.maxPoints == curLevel.minPoints).first()
                if nextLevel is None or previewsLevel is None:
                    if nextLevel is None:
                        return(jsonify({**user.to_dict(only=('id', 'lname', 'mname', 
                                                'fname', 'phone', 'email', 
                                                'roleId', 'registration_date',
                                                'status' , 'archive.id', 
                                                'archive.name', 'archive.searchableWord')),
                                                'curPoints': userLevel.curPoints, 
                                                'userCurLevelInfoId': userLevel.id,
                                                'levelId': curLevel.id,  
                                                'levelNum': curLevel.number, 
                                                'documentScanId': curLevel.documents_scan_id,  
                                                'levelMinP': curLevel.minPoints,
                                                'levelMaxP': curLevel.maxPoints,
                                                'prevLevelId': previewsLevel.id,
                                                'prevDocumentScanId': previewsLevel.documents_scan_id,  
                                                'prevLevelMinP': previewsLevel.minPoints,
                                                'respStatus': "no next level"}))
                    elif previewsLevel is None:
                        return(jsonify({**user.to_dict(only=('id', 'lname', 'mname', 
                                                'fname', 'phone', 'email', 
                                                'roleId', 'registration_date',
                                                'status' , 'archive.id', 
                                                'archive.name', 'archive.searchableWord')),
                                                'userCurLevelInfoId': userLevel.id,
                                                'curPoints': userLevel.curPoints, 
                                                'levelId': curLevel.id, 
                                                'levelNum': curLevel.number, 
                                                'documentScanId': curLevel.documents_scan_id, 
                                                'levelMinP': curLevel.minPoints,
                                                'levelMaxP': curLevel.maxPoints,
                                                'nextLevelId': nextLevel.id,
                                                'nextDocumentScanId': nextLevel.documents_scan_id, 
                                                'nextLevelMaxP': nextLevel.maxPoints,
                                                'respStatus': "no previews level"}))
                else:
                    return(jsonify({**user.to_dict(only=('id', 'lname', 'mname', 
                                                'fname', 'phone', 'email', 
                                                'roleId', 'registration_date',
                                                'status' , 'archive.id', 
                                                'archive.name', 'archive.searchableWord')),
                                                "curPoints": userLevel.curPoints, 
                                                'userCurLevelInfoId': userLevel.id,
                                                "levelId": curLevel.id, 
                                                "levelNum": curLevel.number, 
                                                'documentScanId': curLevel.documents_scan_id, 
                                                "levelMinP": curLevel.minPoints,
                                                "levelMaxP": curLevel.maxPoints,
                                                "nextLevelId": nextLevel.id,
                                                'nextDocumentScanId': nextLevel.documents_scan_id, 
                                                "nextLevelMaxP": nextLevel.maxPoints,
                                                "prevLevelId": previewsLevel.id,
                                                'prevDocumentScanId': previewsLevel.documents_scan_id, 
                                                "prevLevelMinP": previewsLevel.minPoints,
                                                "respStatus": "full data level"}))
            
            userInfo = user.to_dict(only=('id', 'lname', 'mname', 'fname', 'phone', 'email', 'roleId', 'registration_date', 'status' , 'archive.id', 'archive.name', 'archive.searchableWord'))
            userInfo['knowledges'] = [{"knowId": num} for num in result]
            return jsonify(userInfo)
        finally:
            session.close()

    # def put(self, user_id):
        # args = parserup.parse_args()
        # session = db_sessions.create_session()
        # try:
        #     user = session.query(User).get(user_id)
        #     abort_if_user_not_found(user, user_id)
        #     if User.find_by_email(args['email']):
        #         user.FIO = args['FIO']
        #         user.phone = args['phone']
        #         user.update_to_db()
        #         return jsonify(
        #             status='Пользователь обновлен успешно! Однако пользователь с таким email уже существует, поэтому email остался прежним!')
        #     else:
        #         user.FIO = args['FIO']
        #         user.phone = args['phone']
        #         user.email = args['email']
        #         user.update_to_db()
        #         return jsonify(status='Пользователь обновлен успешно!')
        # finally:
        #     session.close()

    def delete(self, user_id):
        session = db_sessions.create_session()
        try:
            session.query(User).filter(User.id == user_id).delete()
            session.commit()
            return jsonify({'success': 'OK'})
        finally:
            session.close()
    
class UserLoginAccessResource(Resource, ParentResource):
    def post(self):
        
        args = logparser.parse_args()    
        users = User.find_by_email(args['email'])
        if not users:
            return jsonify( status = "Не найдено пользователей с такой почтой")
        curuser = None
        for user in users:
            if user.roleId == int(args['roleId']):
                curuser = user
                break
        if not curuser:
            return jsonify(status ='В приложении отсутствуют пользователи с таким типом, возможно вам стоит сменить тип пользователя или запросить регистрацию у администратора')
        hashcod = User.generate_hash(args['password'])
        print(hashcod + "- Результат")
        print(curuser.password + "- Результат")
        if User.verify_hash(str(args['password']), str(curuser.password)):
            expire_delta = datetime.timedelta(hours=18)
            access_token = create_access_token(identity=curuser.id,
                                               fresh=expire_delta)
            
            refresh_token = create_refresh_token(identity=curuser.id)
            
            return jsonify(status ='Вы успешно вошли в систему!', id = curuser.id, accessToken=access_token,refreshToken=refresh_token )
        else : return jsonify(status ='Неверный пароль!')
        
class UserRegistrationAccessResource(Resource, ParentResource):
    def post(self):
        args = parserup.parse_args()
        session = db_sessions.create_session()  
        try:
            user = session.query(User).filter(User.email == args['email'], User.roleId == args['roleId']).first()
            session.commit()
            
            if not user:
                hashcod = User.generate_hash(args['password'])
                newuser = User(fname=args['fname'],
                            lname = args['lname'],
                            mname = args['mname'],
                            phone = args['phone'],
                            email = args['email'],
                            password =  hashcod,
                            roleId = args['roleId'])
                session.add(newuser)
                session.commit()
                session = db_sessions.create_session()  
                user = session.query(User).filter(User.email == args['email'], User.roleId == args['roleId']).first()
                expire_delta = datetime.timedelta(hours=18)
                access_token = create_access_token(identity=user.id,
                                                fresh=expire_delta)
                refresh_token = create_refresh_token(identity=user.id)
                return jsonify( status = "Пользователь успешно добавлен!", id = user.id, accessToken=access_token,refreshToken=refresh_token )
            else:
                return jsonify(status ='В приложении уже есть пользователь с таким типом и почтой, используйте для регистрации другую почту/тип пользователя или перейдите на вкладку входа и авторизуйтесь в уже существующем аккаунте')
        finally:
            session.close()
            
class UserAuthResource(Resource):
    @jwt_required(fresh=False)
    def put(self, auth_user_id, user_id):
        args = parserupSecond.parse_args()
        session = db_sessions.create_session()
        try:
            authUser = session.query(User).filter(User.roleId == 3, User.id == auth_user_id).first()
            user = session.query(User).filter(User.id == user_id).first()
            userS = session.query(User).filter(User.email == args['email'], User.roleId == user.roleId).first()
            abort_if_user_not_found(user, user_id)
            if authUser.roleId == 3:
                status = args['status']
            else:
                status = "not confirmed"
            if userS != None:
                user.fname=args['fname'],
                user.lname = args['lname'],
                user.mname = args['mname'],
                user.phone = int(args['phone']),
                user.status = status
                user.update_to_db()
                return jsonify({"status": 'success, but exist email', 
                                "user": user.to_dict(only=("id", "fname", "lname", "mname", "email", "phone", "status"))})
            else:
                user.fname=args['fname'],
                user.lname = args['lname'],
                user.mname = args['mname'],
                user.phone = args['phone'],
                user.email = args['email'],
                user.update_to_db()
                return jsonify({"status": 'success', 
                                "user": user.to_dict(only=("id", "fname", "lname", "mname", "email", "phone", "status"))})
        finally:
            session.close()
            
class TokenRefresh(Resource):
    @jwt_required(refresh=True)
    def post(self):
        current_userId = get_jwt_identity()
        time_delta = datetime.timedelta(hours=18)
        access_token = create_access_token(identity=current_userId, fresh=time_delta)
        return jsonify(access_token=access_token)