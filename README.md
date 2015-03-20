# pardotdemo

pardotdemo is a simple test project which performs below actions

 1. Log in to demo Pardot https://pi.pardot.com
 2. Create a list with a random name (Marketing > Segmentation > Lists)
 3. Attempt to create another list with that same name and ensure the system correctly gives a validation failure
 4. Rename the original list
 5. Ensure the system allows the creation of another list with the original name now that the original list is renamed
 6. Create a new prospect (Prospect > Prospect List)
 7. Add new prospect to the newly created list
 8. Ensure the new prospect is successfully added to the list upon save
 9. Send a text only email to the list (Marketing > Emails)
10. Log out

Getting Started-
1. Download the project
2. Open the project in eclipse as a maven project
3. To run the test, right click on workFlowTest.java and RunAs -> Junit Test or mvn clean install 
