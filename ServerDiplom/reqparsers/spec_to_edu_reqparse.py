from flask_restful import reqparse

parser_spec_to_edu =reqparse.RequestParser()
parser_spec_to_edu.add_argument("edu_id")
parser_spec_to_edu.add_argument("spec_id")
