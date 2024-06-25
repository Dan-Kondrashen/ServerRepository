from flask_restful import reqparse

parser = reqparse.RequestParser()
parser.add_argument('specId')
parser.add_argument('eduId')
parser.add_argument('documents_scan')

parserlink = reqparse.RequestParser()
parserlink.add_argument('id')
