from flask_restful import reqparse
from models.documents import Document

parser = reqparse.RequestParser()
parser.add_argument('docId')
parser.add_argument('userId')
parser.add_argument('type')
parser.add_argument('statys')


parserlist = reqparse.RequestParser()
parserlist.add_argument('responses', location='json', type = list)


