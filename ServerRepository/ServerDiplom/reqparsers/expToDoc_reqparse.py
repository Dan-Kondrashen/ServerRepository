from flask_restful import reqparse

parserlink = reqparse.RequestParser()
parserlink.add_argument('docId')
parserlink.add_argument('expId')
parserlink.add_argument('documents_scan')
