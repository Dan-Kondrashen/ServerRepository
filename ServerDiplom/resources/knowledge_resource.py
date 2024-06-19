from flask import jsonify
from flask_restful import abort, Resource
from models import db_sessions
from models.knowledge import Knowledge
from models.documents import Document
from reqparsers.knowledge_reqparse import parser, parserlink
from models.skill_type import SkillType, skill_type_of_spec, skill_type_of_knowledge
from models.skill_analitic import SkillAnalitic


def find_knowledge(know_id):
    session = db_sessions.create_session()
    document = session.query(Knowledge).get(know_id)
    return document


def abort_if_knowledge_not_found(know, know_id):
    if not know:
        abort(404, message=f"Knowladge with number {know_id} not found")
        

class KnowledgeResurce(Resource):
    def get(self, know_id):
        knowledge = find_knowledge(know_id)
        abort_if_knowledge_not_found(knowledge, know_id)
        return jsonify(knowledge.to_dict(only=('id', 'name', 'description')))
    def put(self, know_id):
        args = parser.parse_args()
        knowledge = find_knowledge(know_id)
        abort_if_knowledge_not_found(knowledge, know_id)
        if (args['name'] == ""):
            return jsonify(status="Чтобы добавить навык, нужно указать его название!")
        else:
            knowledge.name = args['name']
            knowledge.description = args['description']
            knowledge.update_to_db()
            return jsonify(status="Успешно")
    def delete(self, know_id):
        session = db_sessions.create_session()
        session.query(Knowledge).filter(Knowledge.id == know_id).delete()
        session.commit()
        return jsonify({'success': 'OK'})
    
class ListKnowledgesResource(Resource):
    def get(self, mod):
        session = db_sessions.create_session()
        if mod =="exists":
            knowledges = session.query(Knowledge).join(SkillAnalitic, Knowledge.id == SkillAnalitic.knowId).all()
            session.commit()
            return jsonify([{"knowId": item.id, "name": item.name, "description": item.description} for item in knowledges]) 
        elif mod == "all":
            knowledges = session.query(Knowledge).all()
            session.commit()
            return jsonify([{"knowId": item.id, "name": item.name, "description": item.description} for item in knowledges])   
        

class AllKnowledgesResource(Resource):
    def get(self):
        session = db_sessions.create_session()
        knowledges = session.query(Knowledge).all()
        return jsonify([{"knowId": item.id, "name": item.name, "description": item.description} for item in knowledges])

    def post(self):
        args = parser.parse_args()
        know = Knowledge(**args)
        know.save_to_db()
        return jsonify({'success': 'OK'})

# class KnowToDocResource(Resource):
#     def get(self, doc_id, know_id):
        
#         result=[]
#         session = db_sessions.create_session()
#         documents = session.query(Document).filter(Document.id == doc_id).join(Document.knowledge).filter(Knowledge.id == know_id).first()
#         for document in documents:
#             for knowledge in document.knowledge:
#                 link_data = {
#                     'doc_id': document.id,
#                     'doc_title': document.title,
#                     'know_id': knowledge.id,
#                     'know_name': knowledge.name
#                 }
#                 result.append(link_data)
#         return jsonify(result)
    
class KnowToDocListResource(Resource):
    def get(self, doc_id):
        
        result=[]
        session = db_sessions.create_session()
        documents = session.query(Document).filter(Document.id == doc_id).join(Document.knowledge).all()
        for document in documents:
            for knowledge in document.knowledge:
                link_data = {
                    'doc_id': document.id,
                    'doc_title': document.title,
                    'know_id': knowledge.id,
                    'know_name': knowledge.name
                }
                result.append(link_data)
        return jsonify(result)
    def post(self, doc_id):
        
        args = parserlink.parse_args()
        session = db_sessions.create_session()

        doc = session.query(Document).get(doc_id)
        know_id = args["knowId"]
        knowledge = session.query(Knowledge).get(know_id)
        doc.knowledge.append(knowledge)
        session.add(doc)
        session.commit()
        return jsonify({'success': 'OK'})