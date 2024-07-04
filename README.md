В представленном проекте использованы такие инструментальные средства, как:
1) Паттерн проектирования MVVM.
2) Пакет библиотек Android jatpack, для организации эффективной навигации в приложения и асинхронной подгрузки данных с помощью LiveData.
3) Kotlin coroutines, для организации асинхронной подгрузки данныцх с сервера (и использования  launch для подгрузки данных из Room БД)
В данном проекте представлено приложение, оптимизирующее процесс организации трудоустрйства студентов.
Администраторами приложения, назначающими баллы и редактирующими информацию по пользователям работодателям и сискателям, выступают представители отдела трудостуройства университета (размещающего сервер на хостинге или другим доступным способом).
В приложении присутствуют такие основные модули как:
1. Модуль аутентификации содержащий страницы регистрации и авторизации.
   ![image](https://github.com/Dan-Kondrashen/ServerRepository/assets/71755503/271aafe0-a89e-4350-89cc-898ff31a6ccf) ![image](https://github.com/Dan-Kondrashen/ServerRepository/assets/71755503/e6d6f5fd-109b-4428-a1a1-34a00a472779)

3. Модуль главной страницы, в котором пользователи могут:
   - просматривать документы, подбираемые по заранее определенным фильтрам;
   - просматривать более подробную информацию по документу и организовывать работу с ним;
   - оставлять отклики на документы, добавлять их в избранное или скрывать их от пользователя;
   - добавлять документы в кастомные архивы, созхданные пользователем.
4. Модуль личного кабинета, в котором пользователь может создавать основные документы (вакансии или резюме) или подтверждающие документы, прикрепляемые к основным для подтверждения специальностей пользователя или его опыта работы.
5. Модуль сбора статистических данны и построения статистических графиков по компетенциям, используемым в приложении(специальностям и навыкам пользователей), и по документам пользователей.
6. Модуль организации чата по откликам, между пользователями приложения.
7. Модуль фильтрации, используемый для получения списка документов по указанным фильтрам.
8. Модуль настроек, в котором пользователь может сменить язык приложения или выбрать другую тему.

