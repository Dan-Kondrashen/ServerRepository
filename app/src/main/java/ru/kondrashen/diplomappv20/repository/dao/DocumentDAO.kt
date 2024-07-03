package ru.kondrashen.diplomappv20.repository.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentInfo
import ru.kondrashen.diplomappv20.repository.data_class.Document
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.ChatTitleAndAnotherUserNick
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentAnalysisInfo
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.DocumentInfoWithKnowledge
import ru.kondrashen.diplomappv20.repository.data_class.call_data_class.ResponseInfo
import java.sql.Date

@Dao
interface DocumentDAO: BaseDAO<Document> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDocument(item: Document)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addAnalysisDoc(item: Document)


    @Query("SELECT * FROM document_table")
    fun getDocuments(): LiveData<List<Document>>

    @Query("SELECT document_table.docId as docId, title, salaryF, salaryS, " +
            "extra_info,contactinfo as contact_info, type," +
            "(CASE WHEN user_table.mname IS NULL THEN user_table.fname " +
            "ELSE user_table.fname || ' ' || user_table.mname  END) as userFIO, " +
            "user_table.lname as extraName , " +
            "date, document_views_table.numviews as numViews, userId FROM document_table" +
            " JOIN user_table ON user_table.id = userId LEFT JOIN document_views_table ON document_views_table.docId = document_table.docId  WHERE document_table.docId == :id" +
            " ORDER BY date DESC ")
    fun getDocumentById(id: Int): LiveData<DocumentInfoWithKnowledge>

    @Query("SELECT (SELECT title FROM document_table WHERE docId = :docId) as title, " +
            "(SELECT (CASE WHEN user_table.mname IS NULL " +
            "THEN user_table.fname || ' ' || user_table.lname " +
            "ELSE user_table.fname || ' ' || user_table.mname || ' ' || user_table.lname END) as nick " +
            "FROM user_table WHERE user_table.id =:userId) as nick FROM user_table")
    fun getTitleAndUserNick(docId: Int, userId: Int): LiveData<ChatTitleAndAnotherUserNick>

    @Query("SELECT document_table.docId as docId, title, salaryF, salaryS, extra_info, " +
            "contactinfo as contact_info, type, " +
            "(CASE WHEN user_table.mname IS NULL THEN user_table.fname " +
            "ELSE user_table.fname || ' ' || user_table.mname  END) as userFIO, " +
            "user_table.lname as extraName , " +
            "date, userId  FROM document_table JOIN user_table ON user_table.id = userId WHERE document_table.type = :type")
    fun getCustomMainInfo(type: String): LiveData<List<DocumentInfo>>

    @Query("SELECT  DISTINCT document_table.docId as docId, title, salaryF, salaryS, extra_info," +
            "contactinfo as contact_info, type, " +
            "(CASE WHEN user_table.mname IS NULL THEN user_table.fname " +
            "ELSE user_table.fname || ' ' || user_table.mname  END) as userFIO, " +
            "user_table.lname as extraName , " +
            " date , userId FROM document_table JOIN user_table ON user_table.id = userId JOIN document_views_table WHERE document_table.type = :type ORDER BY date DESC")
    fun getNewDocsMainInfo(type: String): LiveData<List<DocumentInfo>>

    @Query("SELECT  DISTINCT document_table.docId as docId, title, salaryF, salaryS, extra_info, " +
            "contactinfo as contact_info, type, " +
            "(CASE WHEN user_table.mname IS NULL THEN user_table.fname " +
            "ELSE user_table.fname || ' ' || user_table.mname  END) as userFIO, " +
            "user_table.lname as extraName , " +
            "date , userId FROM document_table JOIN user_table ON user_table.id = userId JOIN document_views_table WHERE document_table.userId = :userId ORDER BY date DESC")
    fun getUserDocumentsByUserId(userId: Int): LiveData<List<DocumentInfoWithKnowledge>>

    @Query("SELECT * FROM document_table JOIN user_table ON user_table.id = document_table.userId " +
            "LEFT JOIN document_views_table ON document_views_table.docId == document_table.docId " +
            "AND document_views_table.typeS IN ('view', 'response', 'dismiss') " +
            "WHERE document_table.userId = :userId GROUP BY  document_table.docId ORDER BY document_table.date DESC")
    fun getUserDocAnalysisByUserId(userId: Int): LiveData<List<DocumentAnalysisInfo>>

//    @Query("SELECT * FROM document_table JOIN user_table ON user_table.id = document_table.userId " +
//            "LEFT JOIN document_views_table ON document_views_table.docId == document_table.docId " +
//            "WHERE document_table.userId = :userId AND document_views_table.typeS IN ('view', 'response', 'dismiss') " +
//            "GROUP BY  document_table.docId ORDER BY document_table.date DESC")
//    fun getUserDocAnalysisByUserId(userId: Int): LiveData<List<DocumentAnalysisInfo>>

    @Query("SELECT document_table.docId as docId, title, salaryF, salaryS, extra_info, contactinfo as contact_info, " +
            "document_table.type, " +
            "(CASE WHEN user_table.mname IS NULL THEN user_table.fname " +
            "ELSE user_table.fname || ' ' || user_table.mname  END) as userFIO, " +
            "user_table.lname as extraName , " +
            " date, document_views_table.numviews as numViews , document_table.userId   FROM document_table " +
            "JOIN user_table ON user_table.id = document_table.userId " +
            "LEFT JOIN document_views_table ON document_views_table.docId = document_table.docId  " +
            "LEFT JOIN document_response_table On document_table.docId == document_response_table.docId " +
            "WHERE document_table.type = :type " +
            "AND (document_response_table.type != 'dismiss'  OR document_response_table.type IS NULL)" +
            "GROUP BY document_table.docId ORDER BY date DESC ")
    fun getNewDocsWithKnowMainInfo(type: String): LiveData<List<DocumentInfoWithKnowledge>>

    @Query("SELECT document_table.docId as docId, title, salaryF, salaryS, extra_info, " +
            "contactinfo as contact_info, document_table.type, " +
            "(CASE WHEN user_table.mname IS NULL THEN user_table.fname " +
            "ELSE user_table.fname || ' ' || user_table.mname  END) as userFIO, " +
            "user_table.lname as extraName , " +
            " date, document_views_table.numviews as numViews , document_table.userId   FROM document_table " +
            "JOIN user_table ON user_table.id = document_table.userId " +
            "JOIN document_views_table ON document_views_table.docId = document_table.docId  " +
            "LEFT JOIN document_response_table On document_table.docId == document_response_table.docId " +
            "WHERE document_table.type = :type " +
            "AND (document_response_table.type != 'dismiss'  OR document_response_table.type IS NULL)" +
            "GROUP BY document_table.docId ORDER BY document_views_table.numviews DESC ")
    fun getMostViewedDocsWithKnowMainInfo(type: String): LiveData<List<DocumentInfoWithKnowledge>>

    @Query("SELECT document_table.docId as docId, title, salaryF, salaryS, extra_info, contactinfo as contact_info, " +
            "type, (CASE WHEN user_table.mname IS NULL THEN user_table.fname " +
            "ELSE user_table.fname || ' ' || user_table.mname  END) as userFIO, " +
            "user_table.lname as extraName , date, document_views_table.numviews as numViews , userId   FROM document_table " +
            "JOIN user_table ON user_table.id = userId " +
            "JOIN KnowledgeToDocumentCrossRef ON document_table.docId ==  KnowledgeToDocumentCrossRef.docId " +
            "JOIN knowledge_table ON KnowledgeToDocumentCrossRef.knowId == knowledge_table.knowId " +
            "LEFT JOIN document_views_table ON document_views_table.docId = document_table.docId  " +
            "WHERE document_table.type = :type AND knowledge_table.knowId IN (:knowIDs) " +
            "GROUP BY document_table.docId ORDER BY date DESC ")
    fun getUserKnowledgeDocsWithKnowMainInfo(type: String, knowIDs: List<Int>): LiveData<List<DocumentInfoWithKnowledge>>

    @Query("SELECT document_table.docId as docId, title, salaryF, salaryS, document_table.type, " +
            "extra_info,contactinfo as contact_info, " +
            "(CASE WHEN user_table.mname IS NULL THEN user_table.fname " +
            "ELSE user_table.fname || ' ' || user_table.mname  END) as userFIO, " +
            "user_table.lname as extraName , " +
            "date, document_views_table.numviews as numViews , document_table.userId   " +
            "FROM document_table JOIN user_table ON user_table.id = document_table.userId " +
            "LEFT JOIN document_views_table ON document_views_table.docId = document_table.docId " +
            "JOIN document_response_table ON document_response_table.docId = document_table.docId " +
            "WHERE document_response_table.type = :type GROUP BY document_table.docId ORDER BY date DESC ")
    fun getUserRespDocsWithMoreInfo(type: String): LiveData<List<DocumentInfoWithKnowledge>>

//    @Query("SELECT document_table.title as title, document_table.docId as docId, " +
//            "comment_table.userId as commUserId, " +
//            "(CASE WHEN document_table.userId == :userId THEN document_response_table.userId " +
//            "ELSE document_table.userId END) as userId, " +
//            "document_response_table.id as respId, comment_table.content as commContent," +
//            "(CASE WHEN user_table.mname IS NULL THEN user_table.fname || ' ' || user_table.lname " +
//            "ELSE user_table.fname || ' ' || user_table.mname || ' ' || user_table.lname END) as userFIO " +
//            "FROM document_response_table  " +
//            "JOIN document_table ON document_table.docId == document_response_table.docId " +
//            "JOIN user_table ON user_table.id == (CASE WHEN document_table.userId == :userId " +
//            "THEN document_response_table.userId ELSE document_table.userId END)  " +
//            "LEFT JOIN comment_table ON document_response_table.id == comment_table.respId " +
//            "WHERE document_response_table.type == 'response' " +
//            "or document_response_table.type == 'favorite' " +
//            "ORDER BY comment_table.comment_date, document_table.date")
//    fun getUserRespInfo(userId: Int): LiveData<List<ResponseInfo>>

    @Query("SELECT document_table.title as title, document_table.docId as docId, " +
            "comment_table.userId as commUserId, " +
            "(CASE WHEN document_table.userId == :userId THEN document_response_table.userId " +
            "ELSE document_table.userId END) as userId, " +
            "document_response_table.id as respId, (SELECT content " +
            "FROM comment_table AS latest_comment " +
            "WHERE latest_comment.respId = document_response_table.id " +
            "ORDER BY latest_comment.comment_date DESC LIMIT 1) as commContent," +
            "(CASE WHEN user_table.mname IS NULL THEN user_table.fname || ' ' || user_table.lname " +
            "ELSE user_table.fname || ' ' || user_table.mname || ' ' || user_table.lname END) as userFIO " +
            "FROM document_response_table  " +
            "JOIN document_table ON document_table.docId == document_response_table.docId " +
            "JOIN user_table ON user_table.id == (CASE WHEN document_table.userId == :userId " +
            "THEN document_response_table.userId ELSE document_table.userId END)  " +
            "LEFT JOIN comment_table ON document_response_table.id == comment_table.respId " +
            "WHERE document_response_table.type == 'response' " +
            "or document_response_table.type == 'favorite' " +
            "GROUP BY document_response_table.id " +
            "ORDER BY comment_table.comment_date, document_table.date "
    )
    fun getUserRespInfo(userId: Int): LiveData<List<ResponseInfo>>

    @Query("SELECT document_table.docId as docId, title, salaryF, salaryS, extra_info, contactinfo as contact_info, type, " +
            "(CASE WHEN user_table.mname IS NULL THEN user_table.fname " +
            "ELSE user_table.fname || ' ' || user_table.mname  END) as userFIO, " +
            "user_table.lname as extraName , " +
            "date, userId  FROM document_table JOIN user_table WHERE document_table.type = :type ORDER BY date DESC")
    fun getUserTimeDocsMainInfo(type: String,): LiveData<List<DocumentInfo>>

    @Query("SELECT document_table.docId as docId, title, salaryF, salaryS, extra_info, " +
            "contactinfo as contact_info, type, " +
            "(CASE WHEN user_table.mname IS NULL THEN user_table.fname " +
            "ELSE user_table.fname || ' ' || user_table.mname  END) as userFIO, " +
            "user_table.lname as extraName , " +
            "date, userId  FROM document_table JOIN user_table WHERE document_table.type = :type ORDER BY date DESC")
    fun getMostViewedDocsMainInfo(type: String): LiveData<List<DocumentInfo>>

    @Query("SELECT document_table.docId as docId, title, salaryF, salaryS, document_table.type, " +
            "extra_info,contactinfo as contact_info, " +
            "(CASE WHEN user_table.mname IS NULL THEN user_table.fname " +
            "ELSE user_table.fname || ' ' || user_table.mname  END) as userFIO, " +
            "user_table.lname as extraName , " +
            "date, document_views_table.numviews as numViews , document_table.userId   " +
            "FROM document_table " +
            "JOIN user_table ON user_table.id = document_table.userId " +
            "LEFT JOIN KnowledgeToDocumentCrossRef " +
            "ON document_table.docId = KnowledgeToDocumentCrossRef.docId " +
            "LEFT JOIN knowledge_table ON KnowledgeToDocumentCrossRef.knowId = knowledge_table.knowId " +
            "LEFT JOIN document_views_table ON document_views_table.docId = document_table.docId  " +
            "WHERE document_table.type = :type " +
            "AND (:salaryF IS NULL OR :salaryF < salaryF) " +
            "AND (:salaryS IS NULL OR :salaryS < salaryS) " +
            "AND (:startData IS NULL or:startData < date) " +
            "AND (:endData IS NULL or date < :endData) " +
            "and (:knowList IS NULL " +
            "OR NOT EXISTS (SELECT 1 FROM knowledge_table WHERE knowId IN (:knowList)) " +
            "or knowledge_table.knowId in (:knowList)) " +
            "GROUP BY document_table.docId " +
            "ORDER BY date DESC LIMIT :num")
    fun getFilteredDocsWithKnowMainInfoDesc(endData: String?, startData: String?, salaryF: Float?, salaryS: Float?, type: String, knowList: List<Int>?, num: Int?): LiveData<List<DocumentInfoWithKnowledge>>

    @Query("SELECT document_table.docId as docId, title, salaryF, salaryS, document_table.type, " +
            "extra_info,contactinfo as contact_info, " +
            "(CASE WHEN user_table.mname IS NULL THEN user_table.fname " +
            "ELSE user_table.fname || ' ' || user_table.mname  END) as userFIO, " +
            "user_table.lname as extraName , " +
            "date, document_views_table.numviews as numViews , document_table.userId   " +
            "FROM document_table " +
            "JOIN user_table ON user_table.id = document_table.userId " +
            "LEFT JOIN KnowledgeToDocumentCrossRef " +
            "ON document_table.docId = KnowledgeToDocumentCrossRef.docId " +
            "LEFT JOIN knowledge_table ON KnowledgeToDocumentCrossRef.knowId = knowledge_table.knowId " +
            "LEFT JOIN document_views_table ON document_views_table.docId = document_table.docId  " +
            "WHERE document_table.type = :type " +
            "AND (:salaryF IS NULL OR :salaryF < salaryF) " +
            "AND (:salaryS IS NULL OR :salaryS < salaryS) " +
            "AND (:startData IS NULL or:startData < date) " +
            "AND (:endData IS NULL or date < :endData) " +
            "and (:knowList IS NULL " +
            "OR NOT EXISTS (SELECT 1 FROM knowledge_table WHERE knowId IN (:knowList)) " +
            "or knowledge_table.knowId in (:knowList)) " +
            "GROUP BY document_table.docId " +
            "ORDER BY numViews DESC, date DESC LIMIT :num")
    fun getNumViewsFilteredDocsWithKnowMainInfoDesc(endData: String?, startData: String?, salaryF: Float?, salaryS: Float?, type: String, knowList: List<Int>?, num: Int?): LiveData<List<DocumentInfoWithKnowledge>>

    @Query("SELECT document_table.docId as docId, title, salaryF, salaryS, document_table.type, " +
            "extra_info,contactinfo as contact_info, " +
            "(CASE WHEN user_table.mname IS NULL THEN user_table.fname " +
            "ELSE user_table.fname || ' ' || user_table.mname  END) as userFIO, " +
            "user_table.lname as extraName , " +
            "date, document_views_table.numviews as numViews , document_table.userId   " +
            "FROM document_table " +
            "JOIN user_table ON user_table.id = document_table.userId " +
            "LEFT JOIN KnowledgeToDocumentCrossRef " +
            "ON document_table.docId = KnowledgeToDocumentCrossRef.docId " +
            "LEFT JOIN knowledge_table ON KnowledgeToDocumentCrossRef.knowId = knowledge_table.knowId " +
            "LEFT JOIN document_views_table ON document_views_table.docId = document_table.docId  " +
            "WHERE document_table.type = :type " +
            "AND (:salaryF IS NULL OR :salaryF < salaryF) " +
            "AND (:salaryS IS NULL OR :salaryS < salaryS) " +
            "AND (:startData IS NULL or:startData < date) " +
            "AND (:endData IS NULL or date < :endData) " +
            "and (:knowList IS NULL " +
            "OR NOT EXISTS (SELECT 1 FROM knowledge_table WHERE knowId IN (:knowList)) " +
            "or knowledge_table.knowId in (:knowList)) " +
            "GROUP BY document_table.docId " +
            "ORDER BY salaryS DESC, salaryF DESC, date DESC LIMIT :num")
    fun getSalaryFilteredDocsWithKnowMainInfoDesc(endData: String?, startData: String?, salaryF: Float?, salaryS: Float?, type: String, knowList: List<Int>?, num: Int?): LiveData<List<DocumentInfoWithKnowledge>>

    @Query("SELECT document_table.docId as docId, title, salaryF, salaryS, document_table.type, " +
            "extra_info,contactinfo as contact_info, " +
            "(CASE WHEN user_table.mname IS NULL THEN user_table.fname " +
            "ELSE user_table.fname || ' ' || user_table.mname  END) as userFIO, " +
            "user_table.lname as extraName , " +
            "date, document_views_table.numviews as numViews , document_table.userId   " +
            "FROM document_table " +
            "JOIN user_table ON user_table.id = document_table.userId " +
            "LEFT JOIN KnowledgeToDocumentCrossRef " +
            "ON document_table.docId = KnowledgeToDocumentCrossRef.docId " +
            "LEFT JOIN knowledge_table ON KnowledgeToDocumentCrossRef.knowId = knowledge_table.knowId " +
            "LEFT JOIN document_views_table ON document_views_table.docId = document_table.docId  " +
            "WHERE document_table.type = :type " +
            "AND (:salaryF IS NULL OR :salaryF < salaryF) " +
            "AND (:salaryS IS NULL OR :salaryS < salaryS) " +
            "AND (:startData IS NULL or:startData < date) " +
            "AND (:endData IS NULL or date < :endData) " +
            "and (:knowList IS NULL " +
            "OR NOT EXISTS (SELECT 1 FROM knowledge_table WHERE knowId IN (:knowList)) " +
            "or knowledge_table.knowId in (:knowList)) " +
            "GROUP BY document_table.docId " +
            "ORDER BY date ASC LIMIT :num")
    fun getFilteredDocsWithKnowMainInfoAsc(endData: String?, startData: String?, salaryF: Float?, salaryS: Float?, type: String, knowList: List<Int>?, num: Int?): LiveData<List<DocumentInfoWithKnowledge>>


    @Query("SELECT document_table.docId as docId, title, salaryF, salaryS, document_table.type, " +
            "extra_info,contactinfo as contact_info, " +
            "(CASE WHEN user_table.mname IS NULL THEN user_table.fname " +
            "ELSE user_table.fname || ' ' || user_table.mname  END) as userFIO, " +
            "user_table.lname as extraName , " +
            "date, document_views_table.numviews as numViews , document_table.userId   " +
            "FROM document_table " +
            "JOIN user_table ON user_table.id = document_table.userId " +
            "LEFT JOIN KnowledgeToDocumentCrossRef " +
            "ON document_table.docId = KnowledgeToDocumentCrossRef.docId " +
            "LEFT JOIN knowledge_table ON KnowledgeToDocumentCrossRef.knowId = knowledge_table.knowId " +
            "LEFT JOIN document_views_table ON document_views_table.docId = document_table.docId  " +
            "WHERE document_table.type = :type " +
            "AND (:salaryF IS NULL OR :salaryF < salaryF) " +
            "AND (:salaryS IS NULL OR :salaryS < salaryS) " +
            "AND (:startData IS NULL or:startData < date) " +
            "AND (:endData IS NULL or date < :endData) " +
            "and (:knowList IS NULL " +
            "OR NOT EXISTS (SELECT 1 FROM knowledge_table WHERE knowId IN (:knowList)) " +
            "or knowledge_table.knowId in (:knowList)) " +
            "GROUP BY document_table.docId " +
            "ORDER BY numViews ASC, date ASC LIMIT :num")
    fun getNumViewsFilteredDocsWithKnowMainInfoAsc(endData: String?, startData: String?, salaryF: Float?, salaryS: Float?, type: String, knowList: List<Int>?, num: Int?): LiveData<List<DocumentInfoWithKnowledge>>

    @Query("SELECT document_table.docId as docId, title, salaryF, salaryS, document_table.type, " +
            "extra_info,contactinfo as contact_info, " +
            "(CASE WHEN user_table.mname IS NULL THEN user_table.fname " +
            "ELSE user_table.fname || ' ' || user_table.mname  END) as userFIO, " +
            "user_table.lname as extraName , " +
            "date, document_views_table.numviews as numViews , document_table.userId   " +
            "FROM document_table " +
            "JOIN user_table ON user_table.id = document_table.userId " +
            "LEFT JOIN KnowledgeToDocumentCrossRef " +
            "ON document_table.docId = KnowledgeToDocumentCrossRef.docId " +
            "LEFT JOIN knowledge_table ON KnowledgeToDocumentCrossRef.knowId = knowledge_table.knowId " +
            "LEFT JOIN document_views_table ON document_views_table.docId = document_table.docId  " +
            "WHERE document_table.type = :type " +
            "AND (:salaryF IS NULL OR :salaryF < salaryF) " +
            "AND (:salaryS IS NULL OR :salaryS < salaryS) " +
            "AND (:startData IS NULL or:startData < date) " +
            "AND (:endData IS NULL or date < :endData) " +
            "and (:knowList IS NULL " +
            "OR NOT EXISTS (SELECT 1 FROM knowledge_table WHERE knowId IN (:knowList)) " +
            "or knowledge_table.knowId in (:knowList)) " +
            "GROUP BY document_table.docId " +
            "ORDER BY salaryS ASC, salaryF ASC, date ASC LIMIT :num")
    fun getSalaryFilteredDocsWithKnowMainInfoAsc(endData: String?, startData: String?, salaryF: Float?, salaryS: Float?, type: String, knowList: List<Int>?, num: Int?): LiveData<List<DocumentInfoWithKnowledge>>

    @Query("DELETE FROM document_table WHERE docId = :docId AND userId = :userId")
    fun deleteDocumentByIdFromRoom(docId: Int, userId: Int)

    @Query("DELETE FROM document_response_table WHERE id = :docRespId")
    fun deleteRespById(docRespId: Int)
}


//    @Query("SELECT document_table.id as id, title, salaryF, salaryS, type, (CASE WHEN user_table.mname IS NULL THEN user_table.fname || ' ' || user_table.lname ELSE user_table.fname || ' ' || user_table.mname || ' ' || user_table.lname END)  as userFIO, date, document_views_table.numviews as numViews, (GROUP_CONCAT(knowledge_table.name)) AS knowledgeInfo " +
//            "FROM document_table JOIN user_table ON user_table.id = userId LEFT JOIN document_views_table ON document_views_table.docId = document_table.id LEFT JOIN\n" +
//            "    KnowledgeToDocumentCrossRef ON KnowledgeToDocumentCrossRef.id = document_table.id LEFT JOIN\n" +
//            "    knowledge_table ON knowledge_table.knowId = KnowledgeToDocumentCrossRef.knowId WHERE document_table.type = :type  group by document_table.id ORDER BY date DESC")
//    fun getNewDocsWithMainInfo(type: String): LiveData<List<DocumentInfoForMainView>>