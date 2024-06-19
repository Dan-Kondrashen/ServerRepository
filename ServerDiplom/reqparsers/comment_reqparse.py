from flask_restful import reqparse

parser = reqparse.RequestParser()
parser.add_argument('content', help='This field cannot be blank', required=True)
parser.add_argument('comment_date')
parser.add_argument('userId')
parser.add_argument('respId')

parserdialog = reqparse.RequestParser()
parserdialog.add_argument('content', help='This field cannot be blank', required=True)
parserdialog.add_argument('userId', required=True)
