/*
 * Copyright 2018-Present Okta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package quickstart;

import com.okta.sdk.authc.credentials.TokenClientCredentials;
import com.okta.sdk.cache.Caches;
import com.okta.sdk.client.AuthorizationMode;
import com.okta.sdk.client.Clients;
import com.okta.sdk.helper.ApplicationApiHelper;
import com.okta.sdk.helper.PolicyApiHelper;
import com.okta.sdk.resource.common.PagedList;
import com.okta.sdk.resource.group.GroupBuilder;
import com.okta.sdk.resource.user.UserBuilder;
import org.openapitools.client.ApiClient;
import org.openapitools.client.api.*;
import org.openapitools.client.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.okta.sdk.cache.Caches.forResource;

/**
 * Example snippets used for this projects README.md.
 * <p>
 * Manually run {@code mvn okta-code-snippet:snip} after changing this file to update the README.md.
 */
@SuppressWarnings({"unused"})
public class ReadmeSnippets {

    private static final Logger log = LoggerFactory.getLogger(ReadmeSnippets.class);

    private final ApiClient client = Clients.builder().build();
    private final User user = null;

    private void createClient() {
        ApiClient client = Clients.builder()
            .setOrgUrl("https://{yourOktaDomain}")  // e.g. https://dev-123456.okta.com
            .setClientCredentials(new TokenClientCredentials("{apiToken}"))
            .build();
    }

    private void createOAuth2Client() {
        ApiClient client = Clients.builder()
            .setOrgUrl("https://{yourOktaDomain}")  // e.g. https://dev-123456.okta.com
            .setAuthorizationMode(AuthorizationMode.PRIVATE_KEY)
            .setClientId("{clientId}")
            .setKid("{kid}") // optional
            .setScopes(new HashSet<>(Arrays.asList("okta.users.manage", "okta.apps.manage", "okta.groups.manage")))
            .setPrivateKey("/path/to/yourPrivateKey.pem")
            // (or) .setPrivateKey("full PEM payload")
            // (or) .setPrivateKey(Paths.get("/path/to/yourPrivateKey.pem"))
            // (or) .setPrivateKey(inputStream)
            // (or) .setPrivateKey(privateKey)
            // (or) .setOAuth2AccessToken("access token string") // if set, private key (if supplied) will be ignored
            .build();
    }

    private void getUser() {
        UserApi userApi = new UserApi(client);

        User user = userApi.getUser("userId");
    }

    private void listAllUsers() {
        UserApi userApi = new UserApi(client);
        List<User> users = userApi.listUsers(null, null, 5, null, null, null, null);

        // stream
        users.stream()
            .forEach(user -> {
              // do something
            });
    }

    private void userSearch() {
        UserApi userApi = new UserApi(client);
        // search by email
        List<User> users = userApi.listUsers(null, null, 5, null, "profile.email eq \"jcoder@example.com\"", null, null);

        // filter parameter
        users = userApi.listUsers(null, null, null, "status eq \"ACTIVE\"",null, null, null);
    }

    private void createUser() {
        UserApi userApi = new UserApi(client);

        User user = UserBuilder.instance()
            .setEmail("joe.coder@example.com")
            .setFirstName("Joe")
            .setLastName("Code")
            .buildAndCreate(userApi);
    }

    private void createUserWithGroups() {
        UserApi userApi = new UserApi(client);

        User user = UserBuilder.instance()
            .setEmail("joe.coder@example.com")
            .setFirstName("Joe")
            .setLastName("Code")
            .setGroups(Arrays.asList("groupId-1", "groupId-2"))
            .buildAndCreate(userApi);
    }

    private void updateUser() {
        UserApi userApi = new UserApi(client);

        UpdateUserRequest updateUserRequest = new UpdateUserRequest();
        UserProfile userProfile = new UserProfile();
        userProfile.setNickName("Batman");
        updateUserRequest.setProfile(userProfile);

        userApi.updateUser(user.getId(), updateUserRequest, true);
    }

    private void deleteUser() {
        UserApi userApi = new UserApi(client);

        // deactivate first
        userApi.deactivateUser(user.getId(), false);
        // then delete
        userApi.deleteUser(user.getId(), false);
    }

    private void listUsersGroup() {
        GroupApi groupApi = new GroupApi(client);
        List<Group> groups = groupApi.listGroups(null, null, null, 10, null, null, null, null);
    }

    private void createGroup() {
        GroupApi groupApi = new GroupApi(client);

        Group group = GroupBuilder.instance()
            .setName("a-group-name")
            .setDescription("Example Group")
            .buildAndCreate(groupApi);
    }

    private void assignUserToGroup() {
        // create user
        UserApi userApi = new UserApi(client);

        User user = UserBuilder.instance()
            .setEmail("joe.coder@example.com")
            .setFirstName("Joe")
            .setLastName("Code")
            .buildAndCreate(userApi);

        // create group
        GroupApi groupApi = new GroupApi(client);

        Group group = GroupBuilder.instance()
            .setName("a-group-name")
            .setDescription("Example Group")
            .buildAndCreate(groupApi);

        // assign user to group
        groupApi.assignUserToGroup(group.getId(), user.getId());
    }

    private void listUserFactors() {
        UserFactorApi userFactorApi = new UserFactorApi(client);

        List<UserFactor> userFactors = userFactorApi.listFactors("userId");
    }

    private void enrollUserInFactor() {
        UserFactorApi userFactorApi = new UserFactorApi(client);

        SmsUserFactorProfile smsUserFactorProfile = new SmsUserFactorProfile();
        smsUserFactorProfile.setPhoneNumber("555 867 5309");

        SmsUserFactor smsUserFactor = new SmsUserFactor();
        smsUserFactor.setProvider(FactorProvider.OKTA);
        smsUserFactor.setFactorType(FactorType.SMS);
        smsUserFactor.setProfile(smsUserFactorProfile);

        UserFactor userFactor = userFactorApi.enrollFactor("userId", smsUserFactor, true, "templateId", 30, true);
    }

    private void activateFactor() {
        UserFactorApi userFactorApi = new UserFactorApi(client);

        UserFactor userFactor = userFactorApi.getFactor("userId", "factorId");
        ActivateFactorRequest activateFactorRequest = new ActivateFactorRequest();
        activateFactorRequest.setPassCode("123456");

        UserFactor activatedUserFactor = userFactorApi.activateFactor("userId", "factorId", activateFactorRequest);
    }

    private void verifyFactor() {
        UserFactorApi userFactorApi = new UserFactorApi(client);

        UserFactor userFactor = userFactorApi.getFactor("userId", "factorId");
        VerifyFactorRequest verifyFactorRequest = new VerifyFactorRequest();
        verifyFactorRequest.setPassCode("123456");

        VerifyUserFactorResponse verifyUserFactorResponse =
            userFactorApi.verifyFactor("userId", "factorId", "templateId", 10, "xForwardedFor", "userAgent", "acceptLanguage", verifyFactorRequest);
    }

    private void listApplications() {
        List<Application> applications = ApplicationApiHelper.listApplications(client, null, null, null, null, null, true);
    }

    private void getApplication() {
        ApplicationApi applicationApi = new ApplicationApi(client);

        // get generic application type
        Application genericApp = applicationApi.getApplication("app-id", null);

        // get sub-class application type
        BookmarkApplication bookmarkApp = ApplicationApiHelper.getApplication(client, "bookmark-app-id", null);
    }

    private void createSwaApplication() {
        ApplicationApi applicationApi = new ApplicationApi(client);

        SwaApplicationSettingsApplication swaApplicationSettingsApplication = new SwaApplicationSettingsApplication();
        swaApplicationSettingsApplication.buttonField("btn-login")
            .passwordField("txtbox-password")
            .usernameField("txtbox-username")
            .url("https://example.com/login.html");
        SwaApplicationSettings swaApplicationSettings = new SwaApplicationSettings();
        swaApplicationSettings.app(swaApplicationSettingsApplication);
        BrowserPluginApplication browserPluginApplication = new BrowserPluginApplication();
        browserPluginApplication.name("template_swa");
        browserPluginApplication.label("Sample Plugin App");
        browserPluginApplication.settings(swaApplicationSettings);

        // create
        BrowserPluginApplication createdApp =
            applicationApi.createApplication(BrowserPluginApplication.class, browserPluginApplication, true, null);
    }

    private void listPolicies() {
        List<Policy> policies = PolicyApiHelper.listPolicies(client, PolicyType.PASSWORD.name(), LifecycleStatus.ACTIVE.name(), null);
    }

    private void getPolicy() {
        PolicyApi policyApi = new PolicyApi(client);

        // get generic policy type
        Policy genericPolicy = PolicyApiHelper.getPolicy(client, "policy-id", null);

        // get sub-class policy type
        MultifactorEnrollmentPolicy mfaPolicy = PolicyApiHelper.getPolicy(client, "mfa-policy-id", null);
    }

    private void listSysLogs() {
        SystemLogApi systemLogApi = new SystemLogApi(client);

        // use a filter (start date, end date, filter, or query, sort order) all options are nullable
        List<LogEvent> logEvents = systemLogApi.listLogEvents(null, null, null, "interestingURI.com", 100, "ASCENDING", null);
    }

    private void callAnotherEndpoint() {

        ApiClient apiClient = buildApiClient("orgBaseUrl", "apiKey");

        // Create a BookmarkApplication
        BookmarkApplication bookmarkApplication = new BookmarkApplication();
        bookmarkApplication.setName("bookmark");
        bookmarkApplication.setLabel("Sample Bookmark App");
        bookmarkApplication.setSignOnMode(ApplicationSignOnMode.BOOKMARK);
        BookmarkApplicationSettings bookmarkApplicationSettings = new BookmarkApplicationSettings();
        BookmarkApplicationSettingsApplication bookmarkApplicationSettingsApplication =
            new BookmarkApplicationSettingsApplication();
        bookmarkApplicationSettingsApplication.setUrl("https://example.com/bookmark.htm");
        bookmarkApplicationSettingsApplication.setRequestIntegration(false);
        bookmarkApplicationSettings.setApp(bookmarkApplicationSettingsApplication);
        bookmarkApplication.setSettings(bookmarkApplicationSettings);

        ResponseEntity<BookmarkApplication> responseEntity = apiClient.invokeAPI("/api/v1/apps",
            HttpMethod.POST,
            Collections.emptyMap(),
            null,
            bookmarkApplication,
            new HttpHeaders(),
            new LinkedMultiValueMap<>(),
            null,
            Collections.singletonList(MediaType.APPLICATION_JSON),
            MediaType.APPLICATION_JSON,
            new String[]{"API Token"},
            new ParameterizedTypeReference<BookmarkApplication>() {});

        BookmarkApplication createdApp = responseEntity.getBody();
    }

    private void paging() {

        UserApi userApi = new UserApi(client);

        int limit = 2;

        PagedList<User> pagedUserList = userApi.listUsersWithPaginationInfo(null, null, limit, null, null, null, null);

        // loop through all of them
        for (User tmpUser : pagedUserList.getItems()) {
            log.info("User: {}", tmpUser.getProfile().getEmail());
        }

        // or stream
        pagedUserList.getItems().forEach(tmpUser -> log.info("User: {}", tmpUser.getProfile().getEmail()));
    }

    private void complexCaching() {
        Caches.newCacheManager()
            .withDefaultTimeToLive(300, TimeUnit.SECONDS) // default
            .withDefaultTimeToIdle(300, TimeUnit.SECONDS) //general default
            .withCache(forResource(User.class) //User-specific cache settings
                .withTimeToLive(1, TimeUnit.HOURS)
                .withTimeToIdle(30, TimeUnit.MINUTES))
            .withCache(forResource(Group.class) //Group-specific cache settings
                .withTimeToLive(1, TimeUnit.HOURS))
            //... etc ...
            .build(); //build the CacheManager
    }

    private void disableCaching() {
        ApiClient client = Clients.builder()
            .setCacheManager(Caches.newDisabledCacheManager())
            .build();
    }

    private static ApiClient buildApiClient(String orgBaseUrl, String apiKey) {

        ApiClient apiClient = new ApiClient();
        // set your custom rest template and retry template,
        // not setting it would use the default templates
        //apiClient.setRestTemplate();
        //apiClient.setRetryTemplate(retryTemplate(this.clientConfig));
        apiClient.setBasePath(orgBaseUrl);
        apiClient.setApiKey(apiKey);
        apiClient.setApiKeyPrefix("SSWS");
        return apiClient;
    }
}
