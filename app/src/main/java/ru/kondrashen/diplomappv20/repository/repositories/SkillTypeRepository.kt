package ru.kondrashen.diplomappv20.repository.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.kondrashen.diplomappv20.repository.api.APIFactory
import ru.kondrashen.diplomappv20.repository.dao.AnalyticDAO
import ru.kondrashen.diplomappv20.repository.dao.KnowledgeDAO
import ru.kondrashen.diplomappv20.repository.dao.SkillTypeDAO
import ru.kondrashen.diplomappv20.repository.dao.SpecializationDAO
import ru.kondrashen.diplomappv20.repository.data_class.Knowledge
import ru.kondrashen.diplomappv20.repository.data_class.SkillType
import ru.kondrashen.diplomappv20.repository.data_class.Specialization
import ru.kondrashen.diplomappv20.repository.data_class.relationship.KnowledgeToDocumentCrossRef
import ru.kondrashen.diplomappv20.repository.data_class.relationship.KnowledgeTypeCrossRef
import ru.kondrashen.diplomappv20.repository.data_class.relationship.SpecializationTypeCrossRef
import kotlin.coroutines.CoroutineContext

class SkillTypeRepository(private val skillTypeDAO: SkillTypeDAO, private val knowDAO: KnowledgeDAO,private val specDAO: SpecializationDAO): CoroutineScope {

    private val skillTypeAPI = APIFactory.skillTypeApi
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = job

    fun getSkillTypesFromServ(mod: String){
        launch(Dispatchers.IO) {
            try{
                val resp = skillTypeAPI.getSkillTypesAsync(mod)
                if (resp.isSuccessful){
//                    val skillTypeList = resp.body()
                    val jsonObject = resp.body()
                    jsonObject?.let {
                        var skillTypes = mutableListOf<SkillType>()
                        for (i in it) {
                            println(it)
                            var skillTypeId = i.get("id").asInt
                            var skillType = SkillType(
                                id = i.get("id").asInt,
                                name = i.get("name").asString,
                                description =  if (i.get("description").isJsonNull) null else i.get("description").asString
                            )
                            skillTypes.add(skillType)
                            println(skillType.toString()+ "DJndj")
//                            skillTypeDAO.addItem(skillType)
                            val specJson =i.getAsJsonArray("specialization")
                            val knowJson = i.getAsJsonArray("knowledge")
                            if (knowJson.size() != 0){
                                for (j in 0..< knowJson.size()) {
                                    val item = knowJson.get(j).asJsonObject
                                    item.let { iteml ->

                                        var knowId = iteml.get("id").asInt

                                        knowDAO.addKnowsType(KnowledgeTypeCrossRef(skillTypeId, knowId))
                                    }
                                }
                            }
                            if (specJson.size() != 0){
                                for (j in 0..< specJson.size()) {
                                    val item = specJson.get(j).asJsonObject
                                    item.let { iteml ->
                                        val specId = iteml.get("id").asInt
                                        specDAO.addSpecTyped(SpecializationTypeCrossRef(skillTypeId, specId))
                                    }
                                }
                            }
                        }
                        skillTypeDAO.addItems(*skillTypes.toTypedArray())

                    }

//                    skillTypeList?.let {
//                        skillTypeDAO.addItems(*it.toTypedArray())
//                    }
                }
            } catch (e: Exception){
                e.printStackTrace()
            }
        }
    }
    fun getSkillTypesFromRoom(): LiveData<List<SkillType>>{
        return skillTypeDAO.getSkillTypes()
    }
    fun getSkillTypeIdByNameFromRoom(name: String): LiveData<Int>{
        return skillTypeDAO.getSkillTypeIdByName(name)
    }
    fun getSkillTypeNamesFromRoom(): LiveData<List<String>>{
        return skillTypeDAO.getSkillTypeNames()
    }

}
