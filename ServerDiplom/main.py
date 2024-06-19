
from flask import Flask
from flask_migrate import Migrate
from flask_sqlalchemy import SQLAlchemy
from bs4 import BeautifulSoup
from models import db_sessions
from resources import  user_resurce, comment_resurce, document_resurce, experiens_resurce,specialization_resource, \
education_resource, document_dependencies_resource, knowledge_resource, docresponse_resources, role_resurce, \
  file_resource, archive_resource, skill_type_resource, analitic_resource, level_resource
from flask_restful import Api, Resource, reqparse, abort
from flask_jwt_extended import JWTManager
from datetime import timedelta


app = Flask(__name__)
conn_str = db_sessions.global_init()
app.config['SECRET_KEY'] = 'kondrashen_secret_key'
app.config['JWT_SECRET_KEY'] = 'jwt-secret-kondrashin'
app.config["JWT_ACCESS_TOKEN_EXPIRES"] = timedelta(hours=36)
app.config["JWT_REFRESH_TOKEN_EXPIRES"] = timedelta(days=5)
app.config['JWT_BLACKLIST_ENABLED'] = True
app.config['JWT_BLACKLIST_TOKEN_CHECKS'] = ['access', 'refresh']
app.config['SQLALCHEMY_DATABASE_URI'] = conn_str

db = SQLAlchemy(app)

migrate = Migrate(app, db_sessions)
jwt = JWTManager(app)
api = Api(app, catch_all_404s=True)
api.add_resource(user_resurce.AllUsers, "/users")
api.add_resource(user_resurce.AdminAllUsers, "/admin/<user_id>/users")
api.add_resource(user_resurce.AdminUserInfo, "/admin/<auth_user_id>/users/<user_id>")
api.add_resource(user_resurce.UserLoginAccessResource, "/users/login")
api.add_resource(user_resurce.UserFireBaseResourse, "/users/<user_id>/token")
api.add_resource(user_resurce.UserRegistrationAccessResource, "/users/registration")
api.add_resource(role_resurce.RoleListResource, "/roles")
api.add_resource(user_resurce.UserResource, "/users/<user_id>")
api.add_resource(comment_resurce.CommentResource, '/users/<user_id>/comments/<comm_id>')
api.add_resource(comment_resurce.CommentOnResponseResource, '/users/<user_id>/response/<resp_id>/comments')
api.add_resource(archive_resource.ArchiveResource, '/users/<user_id>/archives/<arch_id>')
api.add_resource(archive_resource.ArchiveListResource, '/users/<user_id>/archives/')
api.add_resource(document_resurce.DocumentResource, "/documents/<doc_id>")
api.add_resource(document_resurce.DocumentsListResource, "/documents/type/<type>/items/<num>")
api.add_resource(document_resurce.RegUserDocumentsListResource, "/users/<user_id>/documents/type/<type>/mod/<mod>")
#  Получение элементов по откликам пользователей
api.add_resource(document_resurce.UserRespDocsListResource, "/users/<user_id>/responses/type/<type>")
api.add_resource(document_resurce.UserRespListResource, "/users/<user_id>/responses/num/<num>")
api.add_resource(document_resurce.CurUserDocumentsList, "/users/<user_id>/documents/mod/<mod>")
api.add_resource(document_resurce.RegUserDocumentResource, "/users/<user_id>/documents/<doc_id>")
api.add_resource(file_resource.FileIntroResource, "/users/<user_id>/files/<name>")
api.add_resource(file_resource.FileResource, "/files/<file_id>")
api.add_resource(file_resource.FileDownlodResource, "/users/<user_id>/files/<file_id>/download")
api.add_resource(document_dependencies_resource.UserDependenciesListResource, "/users/<user_id>/dependencies")
api.add_resource(document_dependencies_resource.UserDependenceResource, "/users/<user_id>/dependencies/<depend_id>")
api.add_resource(knowledge_resource.AllKnowledgesResource, "/knowledges")
api.add_resource(knowledge_resource.ListKnowledgesResource, "/knowledges/mod/<mod>")
api.add_resource(knowledge_resource.KnowledgeResurce, "/knowledges/<know_id>")
api.add_resource(experiens_resurce.ExperiensResurce, "/experiens/<exp_id>")
api.add_resource(experiens_resurce.UserExperienceListResource, "/users/<auth_user_id>/experience/searchable-user/<user_id>")
api.add_resource(experiens_resurce.UserExperienceResource, "/users/<user_id>/experience/<exp_id>")
api.add_resource(experiens_resurce.ExpeToDocListResource, "/documents/<doc_id>/experience")
api.add_resource(experiens_resurce.ExpeToDocResource, "/documents/<doc_id>/experiens/<exp_id>")
api.add_resource(experiens_resurce.ExperiensTimeListResource, "/experience/time")
api.add_resource(experiens_resurce.AllExperiensResource, "/experiens")
api.add_resource(specialization_resource.SpecToEduResource, "/specializations-to-educations")
api.add_resource(specialization_resource.ListSpecializationModResource, "/specializations/mod/<mod>")
api.add_resource(specialization_resource.SpecializationListResource, "/specializations")
api.add_resource(specialization_resource.SpecializationResource, "/specializations/<spec_id>")
api.add_resource(education_resource.EducationListResource, "/educations")
api.add_resource(education_resource.EducationResource, "/educations/<edu_id>")
api.add_resource(document_dependencies_resource.DependenciesListResource, "/documents/<doc_id>/dependencies")
api.add_resource(knowledge_resource.KnowToDocListResource, "/documents/<doc_id>/knowledges")
api.add_resource(docresponse_resources.DocResponseListResource, "/responses")
api.add_resource(docresponse_resources.UserDocResponseResource, "/users/<user_id>/responses/<resp_id>")
# api.add_resource(docresponse_resources.UserDocResponseWithTypeListResource, "/users/<user_id>/responses/type/<type>")
api.add_resource(docresponse_resources.UserDocResponseListResource, "/users/<user_id>/responses")
api.add_resource(analitic_resource.DocViewsResource, "/docviews")
api.add_resource(analitic_resource.SkillAnaliticsResourse, "/analytics-skill-info/mod/<mod>")
api.add_resource(analitic_resource.UserAnaliticsResourse, "/analytics-user-info/mod/<mod>")
api.add_resource(skill_type_resource.SkillTypeListResource, "/skill-type/mod/<mod>")
api.add_resource(skill_type_resource.KnowToSkillTypeResource, "/skill-type/<skill_type_id>/knowledges")
api.add_resource(skill_type_resource.SpecToSkillTypeResource, "/skill-type/<skill_type_id>/specializations")
api.add_resource(user_resurce.UserAuthResource, "/users/<auth_user_id>/searchable-user/<user_id>")
api.add_resource(level_resource.LevelExperienseListResource, "/users/<auth_user_id>/searchable-user/<user_id>/level-exp")
api.add_resource(level_resource.LevelUserExperienseResource, "/users/<auth_user_id>/searchable-user/<user_id>/level-exp/<appExp_id>")
api.add_resource(level_resource.LevelExperienseResource, "/users/<user_id>/level/experience/<appExp_id>")
api.add_resource(level_resource.CurUserLevelInfoResource, "/users/<user_id>/level-info")




@app.route("/")
def index():
  return "Все работает?"

def main():
  from waitress import serve
  # app.run(host='0.0.0.0', port=8080)
  serve(app, host='127.0.0.1', port=8080)
  
if __name__ == '__main__':
  main()