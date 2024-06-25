from flask import jsonify
from flask_restful import abort, Resource
from models import db_sessions
from models.comments import Comment
from models.users import User
from models.doc_response import Doc_response
from reqparsers.comment_reqparse import parser, parserdialog
import firebase_admin
import firebase_admin.messaging as messaging
from firebase_admin import credentials

cred = credentials.Certificate("/Users/larisa/Desktop/ServerDiplom/resources/worksearcher_firebase.json")
firebase_admin.initialize_app(cred)


def abort_if_comment_not_found(comm, id):
    if not comm:
        abort(404, message=f"Comment with number {id} not found")
        
def abort_if_response_not_found(resp, id):
    if not resp:
        abort(404, message=f"Response with number {id} not found")


def find_comment(comm_id):
    session = db_sessions.create_session()
    comment = session.query(Comment).get(comm_id)
    return comment

def pushComment(ftoken, title, body, comm):
    message = messaging.Message(
                    notification=messaging.Notification(
                        title=str(title),
                        body =body,
                    ),
                    android=messaging.AndroidConfig(
                        priority="high",                            
                        notification=messaging.AndroidNotification(
                            color='#f45342'
                        )
                    ),
                    token=ftoken,
                             
        )
    datamess = messaging.Message(
            token=ftoken,
            data= {"messType": "newComm",
                "id" : str(comm.id), 
                "status" : str(comm.status), 
                "content" : str(comm.content),
                "comment_date" : str(comm.comment_date), 
                "userId" : str(comm.userId), 
                "respId": str(comm.respId) }  
        )
    try:
        messaging.send(datamess)
        messaging.send(message)
    except ValueError:
        print("Невалидный firebase токен пользователя")
        

class CommentOnResponseResource(Resource):
    def get(self, user_id, resp_id):
        session = db_sessions.create_session()
        response = session.query(Doc_response).filter(Doc_response.id == resp_id).first()        
        if response.type == "response" or response.type == "favourit":
            if response.userId == int(user_id):
                comments = session.query(Comment).filter_by(respId=resp_id)
                session.commit()
                return jsonify([item.to_dict(only=('id', 'content', 'status', 'comment_date', 'userId', 'respId')) for item in comments])
            elif response.document.userId == int(user_id):
                comments = session.query(Comment).filter_by(respId=resp_id)
                session.commit()
                return jsonify([item.to_dict(only=('id', 'content','status', 'comment_date', 'userId', 'respId')) for item in comments])
            else: 
                return jsonify(status="Not available") 
        else:
            return jsonify(status="Not available")  
        
    def post(self, user_id, resp_id):
        session = db_sessions.create_session()
        response = session.query(Doc_response).filter(Doc_response.id == resp_id).first()
        abort_if_response_not_found(response, resp_id)
        if response.type == "response" or response.type == "favorite":
            args = parserdialog.parse_args()
            content = args['content']
            postedUserId = args['userId']
            user = session.query(User).filter(User.id == user_id).first()
            if user_id == postedUserId or user.role.name == "администратор":
                comm = Comment(content = content, 
                                        respId = resp_id,
                                        userId = user_id)
                if response.userId == int(user_id):
                    session.add(comm)
                    session.commit()
                    pushComment(response.user.firebase_token, response.document.title, comm.content, comm)
                    return jsonify(status="success", commId = comm.id, comm_date = comm.comment_date, statusComm = comm.status)
                elif response.document.userId == int(user_id):
                    session.add(comm)
                    session.commit()
                    pushComment(response.user.firebase_token, response.document.title, comm.content, comm)
                    return jsonify(status="success", commId = comm.id, comm_date = comm.comment_date, statusComm = comm.status)
                else: 
                    return jsonify(status="Not available") 
            else:
                return jsonify(status="Not allowed for you") 
        else:
            return jsonify(status="Not available") 
class CommentResource(Resource):

    def get(self, user_id, comm_id):
        comm = find_comment(comm_id)
        abort_if_comment_not_found(comm, comm_id)
        return jsonify(comm.to_dict(only=('id', 'content', 'comment_date', 'userId', 'respId')))

    def put(self, user_id, comm_id):
        args = parserdialog.parse_args()
        comm = find_comment(comm_id)
        abort_if_comment_not_found(comm, comm_id)
        session = db_sessions.create_session()
        user = session.query(User).outerjoin(User.role).filter(User.id == user_id).first()
        if  int(user_id) == comm.userId:
            if (args['content'] == ""):
                return jsonify(status="Empty body not allowed")
            else:
                comm.content = args['content']
                comm.update_to_db()
                return jsonify(status="success", statusComm = comm.status,  comm_date =comm.comment_date)
        elif user.role.name == "администратор":
            if (args['content'] == ""):
                return jsonify(status="Empty body not allowed")
            else:
                comm.content = args['content']
                comm.userId = args['userId']
                comm.update_to_db()
                return jsonify(status="success", statusComm = comm.status, comm_date =comm.comment_date)
        else: 
            return jsonify(status ='Not allowed for you')
            
    # def put(self, user_id, comm_id):
    #     args = parser.parse_args()
    #     comm = find_comment(comm_id)
    #     abort_if_comment_not_found(comm, comm_id)
    #     if (args['content'] == ""):
    #         return jsonify(status="Комментарий не может быть пустым")
    #     else:
    #         comm.content = args['content']
    #         comm.comment_date = args['comment_date']
    #         comm.userId = args['userId']
    #         comm.respId = args['respId']
    #         comm.update_to_db()
    #         return jsonify(status="Успешно")

    def delete(self, user_id, comm_id):
        session = db_sessions.create_session()
        
        comm = find_comment(comm_id)
        abort_if_comment_not_found(comm, comm_id)
        user = session.query(User).outerjoin(User.role).filter(User.id == user_id).first()
        if comm.userId == int(user_id):
            session.query(Comment).filter(Comment.id == comm_id).delete()
            session.commit()
            return jsonify(status ='success')
        elif user.role.name == "администратор":
            session.query(Comment).filter(Comment.id == comm_id).delete()
            session.commit()
            return jsonify(status ='success')
        else: 
            return jsonify(status ='Not allowed for you')
        
    
class CommentListResource(Resource):
    def get(self, resp_id):
        session = db_sessions.create_session()
        comments = session.query(Comment).filter_by(respId=resp_id)
        return jsonify([item.to_dict(only=('id', 'content', 'comment_date', 'userId', 'respId')) for item in comments])

    def post(self, resp_id):
        args = parser.parse_args()
        comm = Comment(**args)
        comm.respId = resp_id
        comm.save_to_db()
        return jsonify({'success': 'OK'})