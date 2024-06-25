
from email.policy import default
import sqlalchemy
import datetime
from sqlalchemy import orm, event
from sqlalchemy_serializer import SerializerMixin
from models import db_sessions
from models.doc_response import Doc_response
from models.users import User
from models.levels import Level
from models.user_level_exp import UserLevelExp
from sqlalchemy.orm import sessionmaker
from .db_sessions import SqlAlchemyBase
    
    
class UserCurLevel(SqlAlchemyBase, SerializerMixin):
    __tablename__ = 'user_cur_level_info'
    id = sqlalchemy.Column(sqlalchemy.Integer, primary_key=True, autoincrement=True)
    curPoints = sqlalchemy.Column(sqlalchemy.Integer, nullable=True, default = 0)
    levelId = sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("levels.id"), nullable=True, default = 1)
    level = orm.relationship("Level")
    # nextLevelId = sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("levels.id"), nullable=True, default = 2)
    # levelNext = orm.relationship("Level")
    userId = sqlalchemy.Column(sqlalchemy.Integer, sqlalchemy.ForeignKey("users.id"))
    user = orm.relationship("User", foreign_keys=[userId])
    def add_level_after_user_add(mapper, connection, target):
        if target.__tablename__ != "users":
            return
        Session = sessionmaker(bind=db_sessions.get_engine)
        session = Session(bind=connection)
        user = session.query(User).filter(User.id ==target.id).first()
        if user == None:
            return
        userCurLevel = UserCurLevel(userId = target.id, curPoints = 0, levelId = 1)
        session.add(userCurLevel)
        session.commit()
    event.listen(User, "after_insert", add_level_after_user_add)
    def increase_or_decrice_points_after_add(mapper, connection, target):
        if target.__tablename__ != "user_level_exps":
            return
        session = db_sessions.create_session()
        user = session.query(User).filter(User.id ==target.userId).first()
        userCurLevel = session.query(UserCurLevel).filter(UserCurLevel.userId ==target.userId).first()
        if user is None:
            return
        if target.reason == "" or len(target.reason) < 3 or target.status == "Not confirmed":
            print("Условия не пройдены")
            return 
        if userCurLevel is None:
            print("Левела нет")
            userCurLevel = UserCurLevel(userId = user.id, curPoints = 0, levelId = 1)
        level = session.query(Level).get(userCurLevel.levelId)
        print("Левел"+ str(level))
        if target.type == "increase":
            userCurLevel.curPoints += int(target.points)
            if level.maxPoints < userCurLevel.curPoints:
                level2 =session.query(Level).filter(Level.minPoints< userCurLevel.curPoints, Level.maxPoints > userCurLevel.curPoints).first()
                userCurLevel.levelId = level2.id
        elif target.type == "decrease" and userCurLevel.curPoints > int(target.points):
            userCurLevel.curPoints -= int(target.points)
            if level.minPoints > userCurLevel.curPoints:
                level2 =session.query(Level).filter(Level.minPoints < userCurLevel.curPoints, Level.maxPoints > userCurLevel.curPoints).first()
                userCurLevel.levelId = level2.id
        session.add(userCurLevel)
        session.commit()
    event.listen(UserLevelExp, "after_insert", increase_or_decrice_points_after_add)
    
    def increase_or_decrice_points_before_delete(mapper, connection, target):
        print("done----")
        if target.__tablename__ != "user_level_exps":
            return
        
        session = db_sessions.create_session()
        user = session.query(User).filter(User.id ==target.userId).first()
        userCurLevel = session.query(UserCurLevel).filter(UserCurLevel.userId ==target.userId).first()
        if user is None:
            print("done1----")
            return
        if userCurLevel is None:
            print("done2----")
            return
        if target.status == "comfirmed":
            level = session.query(Level).get(userCurLevel.levelId)
            print("done3----")
            if target.type == "increase" and userCurLevel.curPoints > int(target.points):
                userCurLevel.curPoints -= int(target.points)
                print("done4.1----")
                if level.minPoints > userCurLevel.curPoints:
                    level2 =session.query(Level).filter(Level.minPoints < userCurLevel.curPoints, Level.maxPoints > userCurLevel.curPoints).first()
                    userCurLevel.levelId = level2.id
            elif target.type == "decrease":
                userCurLevel.curPoints += int(target.points)
                print("done4.2----")
                if level.maxPoints < userCurLevel.curPoints:
                    level2 =session.query(Level).filter(Level.minPoints< userCurLevel.curPoints, Level.maxPoints > userCurLevel.curPoints).first()
                    userCurLevel.levelId = level2.id
            session.add(userCurLevel)
            session.commit()
    event.listen(UserLevelExp, "before_delete", increase_or_decrice_points_before_delete)
#     def increase_or_decrice_points_after_update(mapper, connection, target):
#         if target.__tablename__ != "user_level_exps":
#             return
#         if target.type != "Просмотр":
#             return
#         session = db_sessions.create_session()
#         user = session.query(User).filter_by(userId =target.docId).first()
#         if user is None:
#             return
#         else:
#             user.numviews +=1
#         session.add(user)
#         session.commit()
#     event.listen(UserLevelExp, "after_insert", increase_or_decrice_points_after_add)