package com.axway.apim.test.applications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.axway.apim.test.SwaggerImportTestAction;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

@Test(testName="AppSubscriptionALLOrgsTestIT")
public class AppSubscriptionALLOrgsTestIT extends TestNGCitrusTestDesigner {
	
	@Autowired
	private SwaggerImportTestAction swaggerImport;
	
	@CitrusTest(name = "AppSubscriptionALLOrgsTestIT")
	public void setupDevOrgTest() {
		description("Test to validate, that application subscription works, when using an ALL org-mapping");
		
		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/app-subs-mode-add-${apiNumber}");
		variable("apiName", "App-Subscription-Mode Add Test API-${apiNumber}");
		variable("appName1", "App Subscr-ALL-Org-Test 1 ${orgNumber}");
		variable("appName2", "App Subscr-ALL-Org-Test 2 ${orgNumber}");
		variable("appName3", "App Subscr-ALL-Org-Test 3 ${orgNumber}");
		// ############## Creating Test-Application 1 #################
		http().client("apiManager")
			.send()
			.post("/applications")
			.name("orgCreatedRequest")
			.header("Content-Type", "application/json")
			.payload("{\"name\":\"${appName1}\",\"apis\":[],\"organizationId\":\"${orgId}\"}");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.extractFromPayload("$.id", "consumingTestAppId1")
			.extractFromPayload("$.name", "consumingTestAppName1");
		
		echo("####### Created Test-Application 1: '${consumingTestAppName1}' with id: '${consumingTestAppId1}' #######");
		
		// ############## Creating Test-Application 2 #################
		createVariable("extClientId", RandomNumberFunction.getRandomNumber(15, true));
		http().client("apiManager")
			.send()
			.post("/applications")
			.name("orgCreatedRequest")
			.header("Content-Type", "application/json")
			.payload("{\"name\":\"${appName2}\",\"apis\":[],\"organizationId\":\"${orgId2}\"}");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.extractFromPayload("$.id", "consumingTestAppId2")
			.extractFromPayload("$.name", "consumingTestAppName2");
		
		echo("####### Created Test-Application 2: '${consumingTestAppName2}' with id: '${consumingTestAppId2}' #######");
		
		// ############## Creating Test-Application 3 #################
		http().client("apiManager")
			.send()
			.post("/applications")
			.name("orgCreatedRequest")
			.header("Content-Type", "application/json")
			.payload("{\"name\":\"${appName3}\",\"apis\":[],\"organizationId\":\"${orgId2}\"}");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.extractFromPayload("$.id", "consumingTestAppId3")
			.extractFromPayload("$.name", "consumingTestAppName3");
		
		echo("####### Created Test-Application 3: '${consumingTestAppName3}' with id: '${consumingTestAppId3}' #######");
		
		echo("####### Import an API and create a subscription to application: '${appName2}' #######");
		
		createVariable("swaggerFile", "/com/axway/apim/test/files/basic/petstore.json");
		createVariable("configFile", "/com/axway/apim/test/files/applications/1_api-with-1-org-1-app.json");
		createVariable("state", "published");
		createVariable("orgName2", "ALL");
		createVariable("testAppName", "${appName2}"); // This app wants to have a subscription (located in another org)
		createVariable("expectedReturnCode", "0");
		action(swaggerImport);
		
		echo("####### Validate API: '${apiName}' has been created #######");
		http().client("apiManager")
			.send()
			.get("/proxies")
			.name("api")
			.header("Content-Type", "application/json");

		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.[?(@.path=='${apiPath}')].name", "${apiName}")
			.validate("$.[?(@.path=='${apiPath}')].state", "published")
			.extractFromPayload("$.[?(@.path=='${apiPath}')].id", "apiId");
		
		echo("####### API has been created with ID: '${apiId}' #######");
		
		echo("####### Validate created application 2 has an active subscription to the API #######");
		http().client("apiManager")
			.send()
			.get("/applications/${consumingTestAppId2}/apis")
			.name("api")
			.header("Content-Type", "application/json");
		
		http().client("apiManager")
			.receive()
			.response(HttpStatus.OK)
			.messageType(MessageType.JSON)
			.validate("$.*.apiId", "${apiId}");
	}
}
