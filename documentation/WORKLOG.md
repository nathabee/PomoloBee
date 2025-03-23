
#  Project Work Log

![⏱️](https://img.icons8.com/emoji/48/stopwatch-emoji.png) **Total Hours Worked**: _36 hours_ (Auto-generated)
---
<details>
<summary>Table of Content</summary>

<!-- TOC -->
- [Project Work Log](#project-work-log)
  - [Detailed Work Log](#detailed-work-log)
  - [Week 1 Dates from Mars 14 to Mars 16 2025](#week-1-dates-from-mars-14-to-mars-16-2025)
    - [Mars 14 2025](#mars-14-2025)
    - [Mars 15 2025](#mars-15-2025)
    - [Mars 16 2025](#mars-16-2025)
  - [Week 2 Dates from Mars 17 to Mars 23 2025](#week-2-dates-from-mars-17-to-mars-23-2025)
    - [Mars 17 2025](#mars-17-2025)
    - [Mars 18 2025](#mars-18-2025)
    - [Mars 22 2025](#mars-22-2025)
    - [Mars 23 2025](#mars-23-2025)
  - [Tips for Using This Log](#tips-for-using-this-log)
<!-- TOC END -->
 
</details>
---

This document tracks the number of hours worked each day and provides a brief description of what was accomplished. It is useful to analyze the distribution of time across various activities in the project.

---
##  Detailed Work Log

 
---

##  Week 1 Dates from Mars 14 to Mars 16 2025

###  Mars 14 2025
- **Hours Worked**: 2 hours
- **Tasks**:
  - Created initial github , Requirements , README, tradename, slogan and Logo
- **Theme**: Project Initialization 



###  Mars 15 2025
- **Hours Worked**: 5 hours
- **Tasks**:
  - Create Documentation : DataModel, MLSpecification, API, Workflow 
  - Add LICENSES
- **Theme**:  Project Initialization 
- **Progress**: 



###  Mars 16 2025
- **Hours Worked**: 10 hours
- **Tasks**:
  - Create Documentation : WORKLOG
  - convert logo in vector image : from png to svg using Krita und Inkscape
  - Initialisation Android App PomoloBeeApp with JetPack Compose
  - STEP 1: PomoloBeeApp Initialization [History App](App_HistoryInit.md)
  - STEP 2: Screen Management and Navigation [History App](App_HistoryInit.md)
  - STEP 3: Apple Detection Overview [History App](App_HistoryInit.md)
  - STEP 4: User Preferences (Jetpack DataStore) [History App](App_HistoryInit.md)
  - STEP 5: UI/UX Improvements [History App](App_HistoryInit.md)
  - STEP 6: include a App Theme and a special Font [History App](App_HistoryInit.md)
  - definition of UI and Screen in specification  [specification App](App_Specification.md)
  - modification modele add raw in image  [specification API](API.md)
  - correction django modele to support raw for image and foreign kew to field from raw [Data Model](Django_DataModel.md)
  - synchronised API with Workflow and DataModel   [specification API](API.md) 
- **Theme**:  App Initialization 
- **Progress**: Code compile need to be tested- UI not coded. Empty App
- **PendingAnomalies**: 
    - About screen to be done - Home Remode- dev must nbe done like UI def 


 
---

##  Week 2 Dates from Mars 17 to Mars 23 2025

###  Mars 17 2025
- **Hours Worked**: 7 hours
- **Tasks**:
  - Refined App Documentation :   [specification App](App_Specification.md) and  [specification API](API.md)
  - sync other doc : Requirements and README
  - initialise empty PomoloBeeDjango with python venv, dotenv [Django HistoryInit](Django_HistoryInit.md)
  - creation postgreSQL database  [Django PostgresSQL](Django_PostgresSQL.md)
  - creation PomoloBeeDjango/core/models.py and admin.py
  - initialisation raws fields fruits table  core/fixtures/initial_TABLEs.json and  python manage.py loaddata core/fixtures/initial_TABLE.json
  - keep updating documentation to explain table creation and initialisation [Django HistoryInit](Django_HistoryInit.md)
- **Theme**: Project Initialization 



###  Mars 18 2025
- **Hours Worked**: 7 hours
- **Tasks**:
  -  create PomoloBeeDjango/core/urls.py with all acess point mentionned in the spec  [specification API](API.md)
  -  add a reference to it in PomoloBeeDjango/PomoloBeeDjango/urls.py path('api/', include('core.urls')), 
  -  installed and configured  drf-spectacular (pip install, settings.py.REST_FRAMEWORK,urls.py)
  -  serializers.py and views.py are also initialized from the API.md document
  -  init empty shell to get endpoint for PomoloBeeML using Flask technoloy. ML is not implemented 
  -  init test on django, set creatdb perimossion to pomolo_user (postgresql )
  -  created test cases in PomoloBeeDjango/core/tests
  -  python manage.py test core.tests.test_migration
  -  python manage.py test core.tests.test_endpoint
  -  python manage.py test core.tests.test_workflow   (not working yet)
  -- python manage.py test core.tests.test_ml   (not working yet)

- **Theme**: Project Initialization 
- **Progress**: Django and ML code must be checked and tested. need to test test_workflow and test_ml


 
###  Mars 22 2025
- **Hours Worked**: 2 hours
- **Tasks**:
  -  check and correct [specification API](API.md) create separate document per interface
  -  App → Django [App → Django specification](API_1_App_to_Django.md)
  -  Django → ML  [Django → ML specification](API_2_Django_to_ML.md)
  -  ML  → Django [ML → Django specification](API_3_ML_to_Django.md)
  -  add OrchardScreen in UI and Screen in specification  [specification App](App_Specification.md)
  - check coherence between API App → Django specification and specification App
  
 
- **Theme**: Project Initialization 
- **Progress**: check django is aligned with specification API_1 and API_2 spec. Check App specification aligned with API_1. check Flask implementation aligned with API_2 and API_3. Django and ML code must be tested. need to test test_workflow and test_ml. 
 
    
###  Mars 23 2025
- **Hours Worked**: 3 hours
- **Tasks**:
  - Added a pre-commit hook to remove emojis from Markdown headers, fixing broken TOC anchor links caused by ChatGPT’s emoji formatting.
  - Reinstalled hook with ./scripts/setup-hooks.sh and added scripts/remove_emojis.py to the repo.
  - change remove_emojis.py to modifiy "keycap emojis" (1️⃣, 2️⃣,) : composed of multiple Unicode codepoints
  - to avoid this mess later on the specification : i customized chatGPT :"Always avoid emojis in Markdown, especially in headers and TOC links." in "Anything else ChatGPT should know about you" trait of my setting
  - check coherence between App specification  [specification App](App_Specification.md) and update app structure doc [App Structure](App_Structure.md) 
   

- **Theme**: Project Initialization 
- **Progress**: see 22 Mars 

  
 
 
---

##  Tips for Using This Log
1. **Add a Section for Each Week**: It's helpful to group logs into weeks to make it easy to find particular days and also get an overview of progress week by week.
2. **Use Consistent Themes**: Try to use consistent labels for themes (e.g., "Frontend Setup", "API Integration", "Styling") to make it easier to analyze how much time was spent in different project areas.
3. **Summarize Weekly Progress**: At the end of each week, consider adding a summary that helps understand how productive the week was, what blockers were encountered, and what’s planned next.
4. **Daily Reflection**: Adding a short note about challenges faced or lessons learned each day can provide even more insight when reviewing your work.   

 
