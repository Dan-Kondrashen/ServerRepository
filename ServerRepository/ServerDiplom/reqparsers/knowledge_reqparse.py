from flask_restful import reqparse

parser = reqparse.RequestParser()
parser.add_argument('name')
parser.add_argument('description')


parserlink = reqparse.RequestParser()
parserlink.add_argument('knowId')
