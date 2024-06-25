from flask_restful import reqparse

parser = reqparse.RequestParser()
parser.add_argument('FIO', help='This field cannot be blank', required=True)
parser.add_argument('email', help='This field cannot be blank', required=True)
parser.add_argument('phone', help='This field cannot be blank', required=True)
parser.add_argument('password', help='This field cannot be blank', required=True)

adminparser = reqparse.RequestParser()
adminparser.add_argument('type')
adminparser.add_argument('status')
adminparser.add_argument('startNum')
adminparser.add_argument('num')

parserup = reqparse.RequestParser()
parserup.add_argument('fname', help='This field cannot be blank')
parserup.add_argument('lname', help='This field cannot be blank')
parserup.add_argument('mname')
parserup.add_argument('email', help='This field cannot be blank')
parserup.add_argument('password')
parserup.add_argument('phone')
parserup.add_argument('roleId')

parserupSecond = reqparse.RequestParser()
parserupSecond.add_argument('fname', help='This field cannot be blank')
parserupSecond.add_argument('lname', help='This field cannot be blank')
parserupSecond.add_argument('mname')
parserupSecond.add_argument('email', help='This field cannot be blank')
parserupSecond.add_argument('password')
parserupSecond.add_argument('status')
parserupSecond.add_argument('phone')
parserupSecond.add_argument('roleId')


logparser = reqparse.RequestParser()
logparser.add_argument('email')
logparser.add_argument('roleId')
logparser.add_argument('password')

fbparser = reqparse.RequestParser()
fbparser.add_argument('token')
fbparser.add_argument('userId')