from flask_restful import reqparse

parser = reqparse.RequestParser()
parser.add_argument('name')
parser.add_argument('description')

parser_know_type = reqparse.RequestParser()
parser_know_type.add_argument('knowledges', location='json', type = list)


parser_spec_type = reqparse.RequestParser()
parser_spec_type.add_argument('specializations', location='json', type = list)
