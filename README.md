В представленном проекте использованы такие инструментальные средства, как:
1) Паттерн проектирования MVVM.
2) Пакет библиотек Android jatpack, для организации эффективной навигации в приложения и асинхронной подгрузки данных с помощью LiveData.
3) Kotlin coroutines, для организации асинхронной подгрузки данныцх с сервера (и использования  launch для подгрузки данных из Room БД).
4) База данных Room, для организации хранения данных и автономной работы пользователя.
5) FCM, для организации отправки PUSH уведомлений и чата в реальном времени.
В данном проекте представлено приложение, оптимизирующее процесс организации трудоустрйства студентов.

Администраторами приложения, назначающими баллы и редактирующими информацию по пользователям работодателям и сискателям, выступают представители отдела трудостуройства университета (размещающего сервер на хостинге или другим доступным способом).

В приложении реализованы такие основные модули как:
1. Модуль аутентификации содержащий страницы регистрации и авторизации.
  <p align="center"> <img src=https://github.com/Dan-Kondrashen/ServerRepository/assets/71755503/271aafe0-a89e-4350-89cc-898ff31a6ccf width="400" />
                     <img src=https://github.com/Dan-Kondrashen/ServerRepository/assets/71755503/e6d6f5fd-109b-4428-a1a1-34a00a472779 width="295" />
  </p>

2. Модуль главной страницы, в котором пользователи могут:
   - просматривать документы, подбираемые по заранее определенным фильтрам;
   - просматривать более подробную информацию по документу и организовывать работу с ним;
   - оставлять отклики на документы, добавлять их в избранное или скрывать их от пользователя;
   - добавлять документы в кастомные архивы, созданные пользователем.
   
   <p align="center"> 
     <img src=https://github.com/Dan-Kondrashen/ServerRepository/assets/71755503/45fb4870-9948-4f60-83a1-b27309ce6bc0 width="265" />
     <img src=https://github.com/Dan-Kondrashen/ServerRepository/assets/71755503/9bc5e7d7-a0e5-4a8a-830b-8e59ab2bfde2 width="267"/>
     <img src=https://github.com/Dan-Kondrashen/ServerRepository/assets/71755503/eaa08f70-5d71-4c03-bdea-5b178c9a1269 width="267"/>
 width="267"/>
   </p>

3. Модуль добавления данных, представленный в виде многочисленных диалоговых окон и фрагмента добавления документов пользователя (вакансий или резюме)
4. Модуль личного кабинета, в котором пользователь может создавать основные документы (вакансии или резюме) или подтверждающие документы, прикрепляемые к основным для подтверждения специальностей пользователя или его опыта работы.
5. Модуль сбора статистических данны и построения статистических графиков по компетенциям, используемым в приложении(специальностям и навыкам пользователей), и по документам пользователей.
6. Модуль организации чата по откликам, между пользователями приложения.
7. Модуль фильтрации, используемый для получения списка документов по указанным фильтрам.
8. Модуль настроек, в котором пользователь может сменить язык приложения или выбрать другую тему.
   
 <p align="center">
   <img src=https://github.com/Dan-Kondrashen/ServerRepository/assets/71755503/b7693027-7283-4cf6-9748-96329eff6e31 width="270"/>  
   <img src=https://github.com/Dan-Kondrashen/ServerRepository/assets/71755503/b03d1061-18e4-472a-8d67-b4db13dd1d2c width="273"/>
   <img src=https://github.com/Dan-Kondrashen/ServerRepository/assets/71755503/d034b346-1934-4cee-84e1-039ac54e1096 width="260"/>

 </p>

