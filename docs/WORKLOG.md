
#  Project Work Log

![⏱️](https://img.icons8.com/emoji/48/stopwatch-emoji.png) **Total Hours Worked**: _97 hours_ (Auto-generated)
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
  - [Week 3 Dates from Mars 24 to Mars 30 2025](#week-3-dates-from-mars-24-to-mars-30-2025)
    - [Mars 24 2025](#mars-24-2025)
    - [Mars 25 2025](#mars-25-2025)
    - [Mars 26 2025](#mars-26-2025)
    - [Mars 27 2025](#mars-27-2025)
    - [Mars 28 2025](#mars-28-2025)
    - [Mars 29 2025](#mars-29-2025)
    - [Mars 30 2025](#mars-30-2025)
  - [Week 4 Dates from Mars 31 to Avril 6 2025](#week-4-dates-from-mars-31-to-avril-6-2025)
    - [Mars 31 2025](#mars-31-2025)
    - [Avril 01 2025](#avril-01-2025)
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
  - Create Documentation : DataModel [Data model](Django_Specification.md), MLSpecification, API, Workflow 
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
  - STEP 3: fruit Detection Overview [History App](App_HistoryInit.md)
  - STEP 4: User Preferences (Jetpack DataStore) [History App](App_HistoryInit.md)
  - STEP 5: UI/UX Improvements [History App](App_HistoryInit.md)
  - STEP 6: include a App Theme and a special Font [History App](App_HistoryInit.md)
  - definition of UI and Screen in specification  [specification App](App_Specification.md)
  - modification modele add row in image  [specification API](API.md)
  - correction django modele to support row for image and foreign kew to field from row  [Data model](Django_Specification.md)
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
  - initialisation rows fields fruits table  core/fixtures/initial_TABLEs.json and  python manage.py loaddata core/fixtures/initial_TABLE.json
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
  - Create missing component files  ui/components/ImageCard.kt , FolderPicker.kt,  PermissionManager.kt
  - Create missing screen files ui/screens/ProcessingScreen.kt ResultScreen.kt OrchardScreen.kt LocationScreen.kt ErrorLogScreen.kt ImageHistoryScreen.kt LocalResultScreen.kt PreviewScreen.kt SettingsRepository.kt

- **Theme**: Project Initialization 
- **Progress**: API Specification seems coherent, it is time to code API for Django and ML to offer a service endpoint



 
---

##  Week 3 Dates from Mars 24 to Mars 30 2025

###  Mars 24 2025
- **Hours Worked**: 10 hours
- **Tasks**:
  - added JSON format specification [specification API](API.md)
  - error and response wrapping in API documentation and in Django and Flask Code
  - check Django code with all 4 API docs
  - check flask code with API, API_2 and API_3 docs
  - create ML specification :   [specification ML](ML_Specification.md)  
  - modify README.md to explain ML install and start ML and Django
  - create note book to explain how to train the modele
  - create empty tree folder for PomoloBeeML sub-project
  - create Django/Core/fixtures/initial_farms.json and add id_farm in field
  - modify README.md, Django_HistoryInit.md to specifiy creation with fixture - - script pomoloBeeDjango/scripts/reset_db.sh to reset or install  database
  - Django test => views, urls and API correction
  - pomoloBeeDjango/core/test/test_ml_unavailable.py : ok
  - pomoloBeeDjango/core/test/test_migration.py : ok need to add default farm
  - pomoloBeeDjango/core/test/test_endpoint.py : ok need to add missing
  - pomoloBeeDjango/core/test/test_ml.py : to be done
  - pomoloBeeDjango/core/test/test_worflow.py : to be done
- **Theme**: Project Initialization + Backend Django Code and Test 


###  Mars 25 2025
- **Hours Worked**: 10 hours
- **Tasks**:
  -  init Django_Specification.md base on Workflow. (merge Data Model inside)
  -  the Specification [Django](Django_Specification.md) show impact on database clearly to check endpoint test and validate implementation url and views 
  - Auto-populate HistoryRow after ML updates ImageHistory Needs post_save 
  -  pomoloBeeDjango/core/test/test_migration.py: added farm, superuser and empty tables. add test trigger save on history_image tested OK
  - ML flask add debug mode and config file 
  - ML flask add mok ML engine (detect fruit bypass,  guided return payload and code, still communicating with API and answering django)
  - pomoloBeeDjango/core/test/test_endpoints.py
  - init [Django Test](Django_Test.md) to list all test and how to run them
  - start core.test_endpoints with Flask in Debug + Mok mode
  -  init [ML Test](ML_Test.md) to list all test and how to run them
- **Theme**: Backend Django Code and Test and ML Mok API 

 
###  Mars 26 2025
- **Hours Worked**: 6 hours
- **Tasks**:
  - update [Django Test](Django_Test.md) and test_workflow : use exclusivly API call for workflow test, no direct database update
  - found anomalies: customize standard Django error in handlers.py 
  - keep testing a bit core.test_endpoints with Flask in Debug + Mok mode
  - Integration test : core.test_workflow with Flask in Debug + Mok mode
  - problem nb_apfel or nb_apple ...not always the same replaced in project with singular nb_fruit
- **Theme**: Backend Django Code and Workflow Test  

 
 
###  Mars 27 2025
- **Hours Worked**: 6 hours
- **Tasks**:
 - Renamed ImageHistory ➝ Image to centralize upload + ML results.
 - Merged HistoryRow + HistoryEstimation ➝ single Estimation model.
 - Removed signals, handled Estimation creation directly in view logic.
 - Cleaned API endpoints, renamed /images/ ➝ image-upload, etc.
 - Applied consistent API response format using BaseSuccessMixin.
 - Updated migration tests to match new model fields and structure.
 - Rewrote endpoint tests to reflect API changes and validate logic.
 - Created API-only workflow tests (image upload ➝ ML ➝ estimation).
 - Added robustness tests: missing/failed ML callbacks, retry path.
 - Fixed broken route names in all tests (reverse() consistency).
- **Theme**: Backend Django Model Refactor and Migration,Endpoint,Workflow Tests  


 
###  Mars 28 2025
- **Hours Worked**: 7 hours
- **Tasks**:
 - non-regression test (test/integration_workflows --nonreg )
 - Implemented full test coverage of all documented API endpoints, including robustness check on non-existent resources.
Improved the delete endpoint handling by catching file deletion errors and returning structured warnings.
Fixed issues with invalid JSON and runtime errors by appending missing slashes and handling error responses cleanly.
Standardized snapshot generation and regression comparison to apply consistent filtering (e.g., limiting estimation history).
Ensured all steps (upload, ML callback, deletion, invalid inputs) behave identically in --snapshot, --nonreg, and --integ modes. 
- **Theme**: Backend Django Integration Test + creation non regression test for next time

  
###  Mars 29 2025
- **Hours Worked**: 3 hours
- **Tasks**:
 - added SVG concept in fields : impact on App spec 
 - creation of SVG for field1 "C1"
- **Theme**:  Frontend Spec
 


 
###  Mars 30 2025
- **Hours Worked**: 8 hours
- **Tasks**:
  - Added SVG map support in Django API spec, model, serializer, fixture  
  - Created SVG file for field "C1"  
  - Reset DB and ran unit + non-regression tests (`reset_db.sh`)  
  - Migration tests still weak: `python manage.py test core.tests.test_migration` should fail if needed  
  - Switched to relative image paths (`/media/...`) – updated spec, serializers, Flask app, and views  
  - Introduced `DJANGO_API_URL` vs `DJANGO_MEDIA_URL` distinction – updated Flask + App spec settings  
  - add a installation_test.sh to make all unit test and non regression after an installation
  - correction tests , adapt non red to new SVG
- **Theme**: SVG integration – backend + API spec + app connectivity + non-regression test coverage



---

##  Week 4 Dates from Mars 31 to Avril 6 2025

###  Mars 31 2025
- **Hours Worked**: 9 hours
- **Tasks**:
  - some modification in App specification [specification API](API.md) and start deveoppement App
  - Refactored and structured `MainActivity.kt` to handle permission and asset installation  
  - Implemented `OrchardScreen` to visualize fields and rows from cached JSON  
  - Created `SvgMapScreen` with dynamic field SVG loading from storage  
  - Added dropdown logic and layout for `LocationScreen` with row selection  
  - Integrated `UserPreferences` with DataStore for persistent settings  
  - Started `SettingsScreen` with editable sync mode and API endpoints  
  - Initialized `ConnectionRepository` for testing endpoint and syncing orchard data  
- **Theme**: App developpement
 

 
###  Avril 01 2025
- **Hours Worked**: 2 hours
- **Tasks**:
  - test plan for App : create checklist  and report template  [Template](./Report/App_Test_ReportTemplate.md)
  - look for a solution to run the check list from github page
  - create docs for the github page (renamed documentation folder in docs and changed github hooks), add index.html. main.js, style.cs and multilanguage presentation.md
  - add .nojekill in the github page, list of all docs, link to github, google translate  
  - create a json with a checklist of app install
  - styles of github page in harmony with svg
  - export report in json format that can be reused with picker, reload report for completion
  - also track meta data in report (date,version android...etc)
- **Theme**: App developpement : githubpage to display in multilanguage project and manage App test report
 

  
  
- **PENDING**
  - started a android unit test phase on 2025-04-01  

rqiase error to fix in views.py:
⚠️ Views With Hidden Risks
These are okay-ish, but could fail in edge cases (e.g., filesystem issues, malformed request data, etc.)

🧠 ImageView.post(...)
Risk areas:

default_storage.save(...) → may fail if disk full or permission denied

os.rename(...) → raises FileNotFoundError, PermissionError, etc.

image_file.name may be weird if it's manually crafted

📌 Fix: Wrap file ops in try/except, log, and raise clean APIError

⚠️ MLResultView.post(...)
Risk areas:

Doesn't validate the type of nb_fruit, confidence_score, processed (could be string, not int/bool)

Assumes image has a row and row.fruit → might be None if DB is misconfigured

📌 Fix:

Add type checks (isinstance(nb_fruit, int) etc.)

Validate image.row and row.fruit before accessing

finish workflow test 
- need to django startserver to acess media
- start also flask  


  - **APP**
  - at this point App is just specified but not implented in code. just empty nutshell
  - Check App specification aligned with API_1. 
  - write code for files  ui/components/ImageCard.kt , FolderPicker.kt,  PermissionManager.kt
  - write code for files  ui/screens/ProcessingScreen.kt ResultScreen.kt OrchardScreen.kt LocationScreen.kt ErrorLogScreen.kt ImageHistoryScreen.kt LocalResultScreen.kt PreviewScreen.kt SettingsRepository.kt
  - **DJANGO**
  - at this point DjangoApp is just specified , shortly implented in code but not tested  
  - Django code must be tested. 
  - need to change and test: test_migration, test_endpoint, test_workflow and test_ml.  
  - **ML**
  - at this point ML is not specified (except API) and not implented in code. just empty nutshell to have a endpoint to test app and django
  - make Flask implementation aligned with API_2 and API_3. 
  - ML code empty shell must be tested. 
 

  
 
 
---

##  Tips for Using This Log
1. **Add a Section for Each Week**: It's helpful to group logs into weeks to make it easy to find particular days and also get an overview of progress week by week.
2. **Use Consistent Themes**: Try to use consistent labels for themes (e.g., "Frontend Setup", "API Integration", "Styling") to make it easier to analyze how much time was spent in different project areas.
3. **Summarize Weekly Progress**: At the end of each week, consider adding a summary that helps understand how productive the week was, what blockers were encountered, and what’s planned next.
4. **Daily Reflection**: Adding a short note about challenges faced or lessons learned each day can provide even more insight when reviewing your work.   

 
