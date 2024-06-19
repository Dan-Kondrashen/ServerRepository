import sqlalchemy as sa
import sqlalchemy.orm as orm
from flask import current_app
from sqlalchemy.orm import Session
import sqlalchemy.ext.declarative as dec
SqlAlchemyBase = dec.declarative_base()

__factory = None
__engine = None

def global_init(login='root', password='123', host='localhost', port='5432', db_file='JobSearcherAppDB'):
    global __factory
    
    if __factory:
        return
    if not db_file or not db_file.strip():
        raise Exception("Нужно указать файл БД")
    
    conn_str = f'postgresql://{login}:{password}@{host}:{port}/{db_file}'
    
    engine = sa.create_engine(conn_str, echo=False)
    __factory = orm.sessionmaker(bind=engine)
    print(f"Подключение к базе данных по адресу {conn_str}")
    
    # noinspection PyUnresolvedReferences
    from . import __all_models
    
    SqlAlchemyBase.metadata.create_all(engine)
    
    return conn_str

def create_session() -> Session:
    global __factory
    return __factory()


def get_engine():
    global __engine
    return __engine
    
    