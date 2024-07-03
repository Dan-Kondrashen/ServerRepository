package ru.kondrashen.diplomappv20.repository.api

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object APIFactory {
    const val url = "https://rsueworksearchers.serveo.net"
    private val gsonBuilder = GsonBuilder()
    private var retrofit = Retrofit.Builder()
//        .baseUrl("https://rsueworksearchers.serveo.net/")
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
        .build()
    val docApi = retrofit.create(DocumentsAPI::class.java)
    val archiveApi = retrofit.create(ArchiveAPI::class.java)
    val userApi = retrofit.create(UsersAPI::class.java)
    val specApi = retrofit.create(SpecializationsAPI::class.java)
    val eduApi = retrofit.create(EducationsAPI::class.java)
    val expApi = retrofit.create(ExperienceAPI::class.java)
    val dependApi = retrofit.create(DocDependenciesAPI::class.java)
    val responseApi = retrofit.create(DocResponseAPI::class.java)
    val roleApi = retrofit.create(RoleAPI::class.java)
    val specToEduApi = retrofit.create(SpecToEduAPI::class.java)
    val commentApi = retrofit.create(CommentsAPI::class.java)
    val analyticApi = retrofit.create(AnalyticAPI::class.java)
    val skillTypeApi = retrofit.create(SkillTypesAPI::class.java)
    val knowledgeApi = retrofit.create(KnowledgesAPI::class.java)
//    val levelApi = retrofit.create(KnowledgesAPI::class.java)
    val FcmApi = retrofit.create(FcmAPI::class.java)
}