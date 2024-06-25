from flask_restful import reqparse

parser = reqparse.RequestParser()

parser.add_argument('userId')
parser.add_argument('points')
parser.add_argument('type')
parser.add_argument('status')
parser.add_argument('reason')
parser.add_argument('document_scan_id')

