package pardot.test;
/*
 *Author - Umesh Marappa Reddy
 *Assumptions
 *          1. Ajax call response time is 5 seconds
 *          2. For keeping test simple, I am not try/catching exceptions
 *          3. No logging api is used
 */
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class workflowTest {
	private static WebDriver webDriver;
	private static WebDriverWait driverWait;

	//login info
	private final static String LoginEmail = "pardot.applicant@pardot.com";
	private final static String LoginPassword = "Applicant2012";

	private final static int AjaxWaitTime = 5000;

	@BeforeClass
	public static void setup() throws Exception {
		webDriver = new FirefoxDriver();
		webDriver.manage().window().maximize();
		driverWait = new WebDriverWait(webDriver, 10);
		//log into application
		login();                       
	}

	@AfterClass
	public static void cleanup() throws Exception {
		//logout and quit
		logout();
		webDriver.quit();
	}

	//To keep the exercise simple, I am not using list/prospect objects (tracking with ids as strings)
	@Test
	public void testPardotWorkFlow() throws InterruptedException {
		//simple datetime to create unique segment list
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Date currentDate = new Date();

		//create new list and grab listid to verify creation of list
		String listName = "SampleList-" + dateFormat.format(currentDate);
		String listId = createSegmentList(listName);
		assertTrue(!listId.equals("0"));

		//create duplicate list and verify the creation fails
		String duplicateListId = createSegmentList(listName);
		assertTrue(duplicateListId.equals("0"));

		//rename list
		renameSegmentList(listId);

		//create list with original name and verify new list is created
		String newListId = createSegmentList(listName);
		assertTrue(!newListId.equals("0") && !newListId.equals(listId));

		//create new prospect and grab prospectid to verify creation
		String prospectName = "SampleProspect-" + dateFormat.format(currentDate);
		String prospectId = createProspect(prospectName);

		//addProspectToList
		addProspectToList(prospectId,listName);

		//verify the prospect is added to list
		List<String> listProspects = getListProspects(newListId);
		assertTrue(listProspects.contains(prospectId));

		//send email to list
		sendEmailtoList(listName);
	}

	//This method creates a new segment list with given name and returns listid for success and 0 for failure
	private String createSegmentList(String listName) throws InterruptedException {
		webDriver.get("https://pi.pardot.com/list");
		webDriver.findElement(By.id("listxistx_link_create")).click();
		driverWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#listx form #name")));
		webDriver.findElement(By.cssSelector("#listx form #name")).sendKeys(listName);
		webDriver.findElement(By.cssSelector("#listx form #name")).submit();    
		Thread.sleep(AjaxWaitTime);
		return (webDriver.findElements(By.id("error_for_name")).size() > 0) ? "0" : checkListCreation(listName);
	}

	//This method searches for segment list with name and returns listId [Assumption - the list will be in the 1st page of searchresult]
	private String checkListCreation(String listName) throws InterruptedException {
		webDriver.get("https://pi.pardot.com/list");
		webDriver.findElement(By.cssSelector("#listxistx_module #listx_table_filter")).sendKeys(listName);
		Thread.sleep(AjaxWaitTime);
		return (webDriver.findElements(By.xpath("//a[text()='"+listName+"']")).size() > 0) ?
				webDriver.findElement(By.xpath("//a[text()='"+listName+"']")).getAttribute("href").replace("https://pi.pardot.com/list/read/id/", "") :
					"0";
	}

	//This method takes list-id as input and renames and adds "-testAppend" to existing name
	private void renameSegmentList(String listId) throws InterruptedException {
		webDriver.get("https://pi.pardot.com/list/read/id/" + listId);
		webDriver.findElement(By.xpath("//a[text()='Edit list']")).click();
		driverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("list-form" + listId)));
		//Adding test append to initial list
		webDriver.findElement(By.cssSelector("#li_form_update"+listId+" form #name")).sendKeys("-testAppend");
		webDriver.findElement(By.cssSelector("#li_form_update"+listId+" form #name")).submit();     
		Thread.sleep(AjaxWaitTime);
	}

	//This method takes String as input and creates a prospect selecting random campaign and profile
	private String createProspect(String prospectName) throws InterruptedException {
		Random random = new Random();
		webDriver.get("https://pi.pardot.com/prospect");
		webDriver.findElement(By.id("pr_link_create")).click();
		driverWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#prospect form #email")));
		webDriver.findElement(By.cssSelector("#prospect form #email")).sendKeys(prospectName+"@salesforce.com");
		Select cSelection = new Select(webDriver.findElement(By.id("campaign_id")));
		int campaigns =  cSelection.getOptions().size();
		cSelection.selectByIndex(random.nextInt(campaigns));
		Select pSelection = new Select(webDriver.findElement(By.id("profile_id")));
		int profiles =  pSelection.getOptions().size();
		pSelection.selectByIndex(random.nextInt(profiles));		
		webDriver.findElement(By.cssSelector("#prospect form #email")).submit();
		driverWait.until(ExpectedConditions.titleContains(prospectName));
		return webDriver.getCurrentUrl().replace("https://pi.pardot.com/prospect/read/id/", "");
	}

	//This method takes prospect-id and list name and adds prospect to list
	private void addProspectToList(String prospectId, String listName) {
		webDriver.get("https://pi.pardot.com/list/prospect/prospect_id/"+prospectId);
		WebElement listDropDown = webDriver.findElement(By.xpath("//span[text()='Select a list to add...']"));
		listDropDown.click();
		webDriver.findElement(By.xpath("//li[text()='"+listName+"']")).click();
		driverWait.until(ExpectedConditions.textToBePresentInElement(listDropDown, "Select a list to add..."));
		webDriver.findElement(By.xpath("//span[text()='Select a list to add...']")).submit();
		driverWait.until(ExpectedConditions.not(ExpectedConditions.titleIs("Lists - Pardot")));
	}

	//This method takes listid and returns ids of all prospects linked to the list
	private List<String> getListProspects(String listId) throws InterruptedException {
		webDriver.get("https://pi.pardot.com/list/read/id/" + listId);
		Thread.sleep(AjaxWaitTime);
		List<String> prospects = new ArrayList<String>();
		List<WebElement> listProspects = webDriver.findElements(By.xpath("//table[@id='listxProspect_table']//input[@class='datatable-checkbox']"));
		for (Iterator<WebElement> prospect = listProspects.iterator(); prospect.hasNext();)
			prospects.add(prospect.next().getAttribute("value"));
		return prospects;                   
	}
	//This method takes listname and send emails to all prospects in the list
	private void sendEmailtoList(String listName) throws InterruptedException {
		Random random = new Random();
		webDriver.get("https://pi.pardot.com/email/draft/edit");
		driverWait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#information_modal .choose-asset")));
		webDriver.findElement(By.cssSelector("#information_form #name")).sendKeys(listName+"Email");
		webDriver.findElement(By.xpath("//form[@id='information_form']//span[contains(text(),'Choose a Campaign')]")).click();
		Thread.sleep(AjaxWaitTime);
		List<WebElement> campaigns = webDriver.findElements(By.cssSelector("#folder-contents .folder-list-item"));
		WebElement randomCampaign = campaigns.get(random.nextInt(campaigns.size()));
		randomCampaign.click();
		webDriver.findElement(By.cssSelector("#asset-chooser-app-modal #select-asset")).click();
		if (!webDriver.findElement(By.cssSelector("#information_form #email_type_text_only")).isSelected())
			webDriver.findElement(By.cssSelector("#information_form #email_type_text_only")).click();
		if (webDriver.findElement(By.cssSelector("#information_form #from_template")).isSelected())
			webDriver.findElement(By.cssSelector("#information_form #from_template")).click();
		webDriver.findElement(By.id("save_information")).click();
		driverWait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("#information_bar #control_name"), listName+"Email"));
		webDriver.findElement(By.id("flow_sending")).click();
		driverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Select a list to add...']")));
		WebElement listDropDown = webDriver.findElement(By.xpath("//span[text()='Select a list to add...']"));
		listDropDown.click();
		webDriver.findElement(By.xpath("//li[text()='"+listName+"']")).click();
		driverWait.until(ExpectedConditions.textToBePresentInElement(listDropDown, "Select a list to add..."));
		Select senderSelection = new Select(webDriver.findElement(By.xpath("//ul[@id='sender_options_a']/li/select")));
		senderSelection.selectByVisibleText("Specific User");
		webDriver.findElement(By.cssSelector("#subject_options #subject_a")).sendKeys(listName+"Subject");
		webDriver.findElement(By.id("save_footer")).click();
		driverWait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("save_footer"), "Save"));
		//looks like the send button is disabled in demo app
		webDriver.findElement(By.xpath("//div[@id='schedule_sidebar']/a[text()='Send Now']")).click();
	}

	//log into application using static user and pass
	private static void login() {
		webDriver.get("https://pi.pardot.com/");
		webDriver.findElement(By.cssSelector("#log-in #email_address")).sendKeys(LoginEmail);
		webDriver.findElement(By.cssSelector("#log-in #password")).sendKeys(LoginPassword);
		webDriver.findElement(By.cssSelector("#log-in #password")).submit();
		driverWait.until(ExpectedConditions.titleIs("Dashboard - Pardot"));
	}

	//logout of application
	private static void logout() {
		webDriver.findElement(By.id("menu-account")).click();;
		webDriver.findElement(By.cssSelector(".icon-signout")).click();
		driverWait.until(ExpectedConditions.titleIs("Sign In - Pardot"));
	}
}
