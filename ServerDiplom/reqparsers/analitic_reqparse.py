from flask_restful import reqparse

parser = reqparse.RequestParser()
parser.add_argument('endDate')
parser.add_argument('startDate')
parser.add_argument('skillFamilyId')
parser.add_argument('skillType')
parser.add_argument('skillId')

parseruser = reqparse.RequestParser()
parseruser.add_argument('endDate')
parseruser.add_argument('startDate')
parseruser.add_argument('userId')