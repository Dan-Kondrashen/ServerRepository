from flask_restful import reqparse
from models.documents import Document

parser = reqparse.RequestParser()
parser.add_argument("documents", location='json', type=list)
# parser.add_argument('title')
# parser.add_argument('contactinfo')
# parser.add_argument('extra_info')
# parser.add_argument('userId')
# parser.add_argument('salary')
# parser.add_argument('type')
# parser.add_argument('date')
fulldocumentparser = reqparse.RequestParser()
fulldocumentparser.add_argument('knowledges', location='json', type = list)
fulldocumentparser.add_argument('dependencies', location='json', type = list)
fulldocumentparser.add_argument('experience', location='json', type = list)
fulldocumentparser.add_argument('title')
fulldocumentparser.add_argument('contactInfo')
fulldocumentparser.add_argument('extraInfo')
fulldocumentparser.add_argument('userId')
fulldocumentparser.add_argument('salaryF')
fulldocumentparser.add_argument('salaryS')
fulldocumentparser.add_argument('type')
fulldocumentparser.add_argument('date')


parserFilter = reqparse.RequestParser()
parserFilter.add_argument('startNum')
parserFilter.add_argument('num')
