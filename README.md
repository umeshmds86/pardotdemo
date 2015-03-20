# pardotdemo

pardotdemo is a simple test project which performs below actions

# Log in to demo Pardot https://pi.pardot.com
# Create a list with a random name (Marketing > Segmentation > Lists)
# Attempt to create another list with that same name and ensure the system correctly gives a validation failure
# Rename the original list
# Ensure the system allows the creation of another list with the original name now that the original list is renamed
# Create a new prospect (Prospect > Prospect List)
# Add new prospect to the newly created list
# Ensure the new prospect is successfully added to the list upon save
# Send a text only email to the list (Marketing > Emails)
# Log out

Getting Started-
# Download the project
# Open the project in eclipse as a maven project
# To run the test, right click on workFlowTest.java and RunAs -> Junit Test or mvn clean install 
