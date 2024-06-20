from flask_restful import reqparse

parser = reqparse.RequestParser()
parser.add_argument('experience')
parser.add_argument('role')
parser.add_argument('place')
parser.add_argument('userId')

