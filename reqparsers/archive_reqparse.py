from flask_restful import reqparse

parserarch = reqparse.RequestParser()
parserarch.add_argument('name')

