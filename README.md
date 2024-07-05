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
   </p>

3. Модуль добавления данных, представленный в виде многочисленных диалоговых окон и фрагмента добавления документов пользователя (вакансий или резюме)

<p align="center"> 
     <img src=https://github.com/Dan-Kondrashen/ServerRepository/assets/71755503/19f84310-c240-467b-ba3d-2a643f324601 width="245" />
     <img src=https://github.com/Dan-Kondrashen/ServerRepository/assets/71755503/72ba853e-dc26-4a7d-a064-468f8bfe7119 width="233"/>
</p>

4. Модуль личного кабинета, в котором пользователь может создавать основные документы (вакансии или резюме) или подтверждающие документы, прикрепляемые к основным для подтверждения специальностей пользователя или его опыта работы.

<p align="center"> 
     <img src=https://github.com/Dan-Kondrashen/ServerRepository/assets/71755503/8ccf685e-ea86-46a8-9869-f7fc954093f4 width="265" />
     <img src=https://github.com/Dan-Kondrashen/ServerRepository/assets/71755503/65ed36c5-caa5-4176-b77f-95868dae2ae5 width="268"/>
</p>

5. Модуль сбора статистических данны и построения статистических графиков по компетенциям, используемым в приложении(специальностям и навыкам пользователей), и по документам пользователей.

<p align="center"> 
     <img src=https://github.com/Dan-Kondrashen/ServerRepository/assets/71755503/f7b7de49-ad89-4d16-ba28-1ad80ce39d3d width="215" />
     <img src=https://github.com/Dan-Kondrashen/ServerRepository/assets/71755503/0a15923c-baf4-4706-8bc3-77a0cf77cc16 width="245"/>
     <img src=https://github.com/Dan-Kondrashen/ServerRepository/assets/71755503/1bda895e-24c6-4c76-954d-30164bf7cb14 width="214"/>
</p>

6. Модуль организации чата по откликам, между пользователями приложения.

<p align="center"> 
     <img src=https://github.com/Dan-Kondrashen/ServerRepository/assets/71755503/ca684b1c-4d1d-4326-b4c5-9480540b6def width="227" />
     <img src=https://github.com/Dan-Kondrashen/ServerRepository/assets/71755503/7c724c4b-c96b-40e4-b350-b6ad9299e4cd width="245"/>
</p>
<p align="center"> 
    <img src=https://github.com/Dan-Kondrashen/ServerRepository/assets/71755503/68ebfd8e-428e-4a5a-b907-ddcd74f0ae18 width="300" />
    <img src=https://github.com/Dan-Kondrashen/ServerRepository/assets/71755503/1009d714-d257-4b1b-a680-31bf63325091 width="205" />
</p>


7. Модуль фильтрации, используемый для получения списка документов по указанным фильтрам.

<p align="center">
   <img src=https://github.com/Dan-Kondrashen/ServerRepository/assets/71755503/9d3abf28-2fea-4847-93e8-28e882934a17 width="270"/>  
   <img src=https://github.com/Dan-Kondrashen/ServerRepository/assets/71755503/8167c1b8-30bd-49a8-ac51-d7b873ffa616 width="270"/>
   <img src=https://github.com/Dan-Kondrashen/ServerRepository/assets/71755503/845b19f7-f7d9-42ca-b73d-b1e61c0bb440 width="255"/>
 </p>

8. Модуль настроек, в котором пользователь может сменить язык приложения или выбрать другую тему.
   
 <p align="center">
   <img src=https://github.com/Dan-Kondrashen/ServerRepository/assets/71755503/b7693027-7283-4cf6-9748-96329eff6e31 width="270"/>  
   <img src=https://github.com/Dan-Kondrashen/ServerRepository/assets/71755503/b03d1061-18e4-472a-8d67-b4db13dd1d2c width="273"/>
   <img src=https://github.com/Dan-Kondrashen/ServerRepository/assets/71755503/d034b346-1934-4cee-84e1-039ac54e1096 width="260"/>
 </p>

