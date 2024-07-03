package ru.kondrashen.diplomappv20.repository.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.kondrashen.diplomappv20.presentation.baseClasses.DateConverter
import ru.kondrashen.diplomappv20.repository.dao.AnalyticDAO
import ru.kondrashen.diplomappv20.repository.dao.ArchiveDAO
import ru.kondrashen.diplomappv20.repository.dao.CommentsDAO
import ru.kondrashen.diplomappv20.repository.dao.DependenciesDAO
import ru.kondrashen.diplomappv20.repository.dao.DocumentDAO
import ru.kondrashen.diplomappv20.repository.dao.EducationDAO
import ru.kondrashen.diplomappv20.repository.dao.ExperienceDAO
import ru.kondrashen.diplomappv20.repository.dao.ExperienceTimeDAO
import ru.kondrashen.diplomappv20.repository.dao.KnowledgeDAO
import ru.kondrashen.diplomappv20.repository.dao.LevelDAO
import ru.kondrashen.diplomappv20.repository.dao.ResponseDAO
import ru.kondrashen.diplomappv20.repository.dao.RoleDAO
import ru.kondrashen.diplomappv20.repository.dao.SkillTypeDAO
import ru.kondrashen.diplomappv20.repository.dao.SpecializationDAO
import ru.kondrashen.diplomappv20.repository.dao.UserDAO
import ru.kondrashen.diplomappv20.repository.dao.ViewsDAO
import ru.kondrashen.diplomappv20.repository.data_class.AnaliticSkill
import ru.kondrashen.diplomappv20.repository.data_class.Archive
import ru.kondrashen.diplomappv20.repository.data_class.Comment
import ru.kondrashen.diplomappv20.repository.data_class.DocDependencies
import ru.kondrashen.diplomappv20.repository.data_class.DocResponse
import ru.kondrashen.diplomappv20.repository.data_class.Document
import ru.kondrashen.diplomappv20.repository.data_class.Education
import ru.kondrashen.diplomappv20.repository.data_class.Experience
import ru.kondrashen.diplomappv20.repository.data_class.ExperienceTime
import ru.kondrashen.diplomappv20.repository.data_class.Knowledge
import ru.kondrashen.diplomappv20.repository.data_class.KnowledgeToUser
import ru.kondrashen.diplomappv20.repository.data_class.Level
import ru.kondrashen.diplomappv20.repository.data_class.Role
import ru.kondrashen.diplomappv20.repository.data_class.SkillType
import ru.kondrashen.diplomappv20.repository.data_class.Specialization
import ru.kondrashen.diplomappv20.repository.data_class.User
import ru.kondrashen.diplomappv20.repository.data_class.UserExperience
import ru.kondrashen.diplomappv20.repository.data_class.UserLevel
import ru.kondrashen.diplomappv20.repository.data_class.Views
import ru.kondrashen.diplomappv20.repository.data_class.relationship.DependenciesToDocumentCrossRef
import ru.kondrashen.diplomappv20.repository.data_class.relationship.ExperienceToDocumentCrossRef
import ru.kondrashen.diplomappv20.repository.data_class.relationship.KnowledgeToDocumentCrossRef
import ru.kondrashen.diplomappv20.repository.data_class.relationship.KnowledgeTypeCrossRef
import ru.kondrashen.diplomappv20.repository.data_class.relationship.SpecializationToEducationCrossRef
import ru.kondrashen.diplomappv20.repository.data_class.relationship.SpecializationToUserCrossRef
import ru.kondrashen.diplomappv20.repository.data_class.relationship.SpecializationTypeCrossRef

@Database(entities = [User::class, Comment::class, Education::class, Specialization::class,
    Document::class, DocDependencies::class, Archive::class, DocResponse::class,
    Views::class, Role::class, Knowledge::class, ExperienceTime::class,
    Experience::class, KnowledgeToDocumentCrossRef::class, ExperienceToDocumentCrossRef::class,
    SpecializationToUserCrossRef::class, SpecializationToEducationCrossRef::class,
    DependenciesToDocumentCrossRef::class, SkillType::class, AnaliticSkill::class,
    KnowledgeTypeCrossRef::class, SpecializationTypeCrossRef::class, Level::class, UserLevel::class,
    KnowledgeToUser::class, UserExperience::class] ,
    version = 4, exportSchema = true, autoMigrations = [
        AutoMigration (from = 1, to = 2),
        AutoMigration (from = 2, to = 3),
        AutoMigration (from = 3, to = 4)
    ])
@TypeConverters(DateConverter::class)

abstract class WorkSearcherDB: RoomDatabase() {
    abstract fun authDao(): UserDAO
    abstract fun authDopDao(): RoleDAO
    abstract fun docDao(): DocumentDAO
    abstract fun docViewDao(): ViewsDAO
    abstract fun specDao(): SpecializationDAO
    abstract fun eduDao(): EducationDAO
    abstract fun expDao(): ExperienceDAO
    abstract fun expTimeDao(): ExperienceTimeDAO
    abstract fun knowDao(): KnowledgeDAO
    abstract fun docDependDao(): DependenciesDAO
    abstract fun docResponseDao(): ResponseDAO
    abstract fun commentDao(): CommentsDAO
    abstract fun archiveDao(): ArchiveDAO
    abstract fun skillTypeDao(): SkillTypeDAO
    abstract fun analyticDao(): AnalyticDAO
    abstract fun levelDao(): LevelDAO

    companion object {
        @Volatile
        private var INSTANCE: WorkSearcherDB? = null

        fun getDatabase(context: Context): WorkSearcherDB{
            val tempInstance = INSTANCE
            if (tempInstance != null)
                return tempInstance
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkSearcherDB::class.java,
                    "work_searcher_database.db")
//                    .createFromAsset("work_searcher_database.db")
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}