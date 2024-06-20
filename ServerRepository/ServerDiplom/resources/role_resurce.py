from flask import jsonify
from flask_restful import Resource
from models import db_sessions
from resources.parent_resource import ParentResource
from models.roles import UserRole

class RoleListResource(Resource, ParentResource):
    def get(self):
        items = self.find_all_items(UserRole)
        return jsonify([item.to_dict(only=('id', 'name', 'desc'))for item in items])