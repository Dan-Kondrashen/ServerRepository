package ru.kondrashen.diplomappv20.presentation.adapters

import ru.kondrashen.diplomappv20.databinding.CreateDocumentEmployeeFragmentBinding
import ru.kondrashen.diplomappv20.databinding.CreateDocumentEmployerFragmentBinding
import java.lang.IllegalStateException

class EditDocViewBindingAdapter(empViewBind: CreateDocumentEmployeeFragmentBinding?, emrViewBind: CreateDocumentEmployerFragmentBinding?) {
    val root = empViewBind?.root?: emrViewBind?.root?: throw IllegalStateException("Не инициализирована разметка")
    val dateText = empViewBind?.dateInfo?: emrViewBind!!.dateInfo
    val salaryCheckBox = empViewBind?.salaryCheckbox?: emrViewBind?.salaryCheckbox
    val salaryMin = empViewBind?.minSalary?: emrViewBind?.minSalary
    val title = empViewBind?.titleInput?: emrViewBind?.titleInput
    val salaryF = empViewBind?.salaryF?: emrViewBind?.salaryF
    val salaryS = empViewBind?.salaryS?: emrViewBind?.salaryS
    val extraInfo = empViewBind?.extraInfo?: emrViewBind?.extraInfo
    val contactInfo = empViewBind?.contactInfo?: emrViewBind?.contactInfo
    val specSpinner = emrViewBind?.specializationInfo
    val expSpinner = emrViewBind?.experienceInfo
    val specView = empViewBind?.specializationView
    val knowView = empViewBind?.knowledgeView?: emrViewBind?.knowledgeView
    val knowButton = empViewBind?.buttonKnowledge?: emrViewBind?.buttonKnowledge
    val knowTypeSpiner = empViewBind?.knowledgeInfo?: emrViewBind?.knowledgeInfo
    val knowFilter = empViewBind?.filter?: emrViewBind?.filter
    val expView = empViewBind?.experienceView
    val availableKnowRecycle = empViewBind?.availableKnowledgeRecycle?: emrViewBind?.availableKnowledgeRecycle
    val chosenKnowRecycle = empViewBind?.chosenKnowledgeRecycle?: emrViewBind?.chosenKnowledgeRecycle
    val specButton = empViewBind?.buttonSpecialization
    val specAddButton = empViewBind?.addSpecializationBtn
    val specInfoButton = empViewBind?.specializationInfoButton
    val specRecycler = empViewBind?.chosenSpecializationRecycle
    val expButton = empViewBind?.buttonExperience
    val expAddButton = empViewBind?.addExperienceBtn
    val expInfoButton = empViewBind?.experienceInfoButton
    val expRecycler = empViewBind?.chosenExperienceRecycle
    val saveBtn = empViewBind?.saveBtn
    val saveBtnEmr = emrViewBind?.saveBtn
    val cancelBtn = emrViewBind?.cancelBtn?: empViewBind?.cancelBtn
}