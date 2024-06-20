
from flask_restful import abort
from flask import jsonify
from models import db_sessions

class ParentResource():
    def abort_if_item_not_found(self, item, item_id):
        if not item:
            abort(404, message=f"Item with number {item_id} not found")
            
    # Получение списков   
    def find_all_items(self, T):
        session = db_sessions.create_session()
        item = session.query(T).all()
        return item
    
    # Получение элемента списка   
    def find_item(self, T, id):
        session = db_sessions.create_session()
        item = session.query(T).get(id)
        return item
    
    def delete_item(self, T, id):
        session = db_sessions.create_session()
        session.query(T).filter(T.id == id).delete()
        session.commit()
        