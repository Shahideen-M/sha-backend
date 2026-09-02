package com.sha.service.skills;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import com.sha.brain.dto.OperationPrompt;
import com.sha.brain.prompt.SkillPrompt;
import com.sha.dto.data.JobResult;
import com.sha.dto.request.BrowserRequest;
import com.sha.dto.response.BrowserResponse;
import com.sha.enums.BrowserOperation;
import com.sha.enums.SkillType;
import com.sha.service.Skill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
public class BrowserSkill implements Skill<BrowserRequest, BrowserResponse> {

    @Override
    public SkillType getType() {
        return SkillType.BROWSER;
    }

    @Override
    public BrowserResponse executeTyped(BrowserRequest request) {
        return switch (request.getOperation()) {
            case OPEN_URL -> openUrl(request);
            case SEARCH -> search(request);

            case GET_TITLE -> getTitle();
            case GET_URL -> getUrl();

            case BACK -> goBack();
            case FORWARD -> goForward();
            case REFRESH -> refresh();

            case CLICK -> click(request);
            case TYPE -> type(request);
            case PRESS_KEY -> pressKey(request);

            case READ_PAGE -> readPage(request);
            case CLOSE_BROWSER -> closeBrowser();
        };
    }

    @Override
    public Class<BrowserRequest> getRequestClass() {
        return BrowserRequest.class;
    }

    @Override
    public BrowserResponse execute(Object request) {
        return executeTyped((BrowserRequest) request);
    }

    @Override
    public SkillPrompt<BrowserOperation> describe() {
        return new SkillPrompt<>(
                SkillType.BROWSER,
                "Search the web and read webpages.",
                List.of(
                        "browser",
                        "web",
                        "website",
                        "search",
                        "google",
                        "youtube",
                        "url",
                        "page",
                        "navigate",
                        "open website",
                        "browse"
                ),
                List.of(
                        new OperationPrompt<>(
                                BrowserOperation.SEARCH,
                                "Search Google.",
                                List.of("searchQuery"),
                                """
                                {
                                  "searchQuery":"Spring Boot",
                                  "operation":"SEARCH"
                                }
                                """
                        ),
                        new OperationPrompt<>(
                                BrowserOperation.READ_PAGE,
                                "Read the currently opened webpage.",
                                List.of(),
                                """
                                {
                                  "operation":"READ_PAGE"
                                }
                                """
                        ),
                        new OperationPrompt<>(
                                BrowserOperation.CLOSE_BROWSER,
                                "Close the browser.",
                                List.of(),
                                """
                                {
                                  "operation":"CLOSE_BROWSER"
                                }
                                """
                        )
                )
        );
    }

    public BrowserResponse getTitle() {
        initializeBrowser();
        return new BrowserResponse(
                true,
                "Page title fetched successfully.",
                page.title(),
                null,
                null
        );
    }

    public BrowserResponse getUrl() {
        initializeBrowser();
        return new BrowserResponse(
                true,
                "Current URL fetched successfully.",
                page.url(),
                null,
                null
        );
    }

    public BrowserResponse refresh() {
        initializeBrowser();
        page.reload();
        return new BrowserResponse(
                true,
                "Page refreshed successfully.",
                null,
                null,
                null
        );
    }

    public BrowserResponse goBack() {
        initializeBrowser();
        page.goBack();
        return new BrowserResponse(
                true,
                "Navigated back successfully.",
                null,
                null,
                null
        );
    }

    public BrowserResponse goForward() {
        initializeBrowser();
        page.goForward();
        return new BrowserResponse(
                true,
                "Navigated forward successfully.",
                null,
                null,
                null
        );
    }

    public BrowserResponse click(BrowserRequest request) {
        initializeBrowser();

        Locator locator = page.locator(request.getSelector());

        if (locator.count() == 0) {
            return new BrowserResponse(false, "Element not found.", null, null, null);
        }

        locator.first().click();

        return new BrowserResponse(
                true,
                "Clicked successfully.",
                null,
                null,
                null
        );
    }

    public BrowserResponse type(BrowserRequest request) {
        initializeBrowser();

        page.locator(request.getSelector()).fill(request.getText());

        return new BrowserResponse(
                true,
                "Text entered successfully.",
                null,
                null,
                null
        );
    }

    public BrowserResponse pressKey(BrowserRequest request) {
        initializeBrowser();

        page.keyboard().press(request.getKey());

        return new BrowserResponse(
                true,
                request.getKey() + " Key pressed successfully.",
                null,
                null,
                null
        );
    }

    public BrowserResponse openUrl(BrowserRequest request) {
        initializeBrowser();
        String url = request.getUrl();
        if (!url.startsWith("http")) url = "https://" + url;
        page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        return new BrowserResponse(true, "Opened ", null, request.getUrl(), null);
    }

    public BrowserResponse search(BrowserRequest request) {
        initializeBrowser();
        page.navigate("https://www.google.com");
        Locator searchBox = page.locator("[name='q']");
        searchBox.fill(request.getSearchQuery());
        searchBox.press("Enter");
        page.waitForLoadState();
        return new BrowserResponse(
                true,
                "Search completed.",
                null,
                null,
                null
        );
    }

    public BrowserResponse readPage(BrowserRequest request) {
        initializeBrowser();

        String pageTitle = page.title();
        String url = page.url();
        String text = page.evaluate("() => document.body.innerText").toString();

        return new BrowserResponse(
                true,
                "Page read successfully.",
                pageTitle,
                url,
                text
        );
    }

    private BrowserResponse closeBrowser() {

        if (page != null) page.close();
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();

        if (chromeProcess != null && chromeProcess.isAlive()) {
            chromeProcess.destroy();
        }

        page = null;
        context = null;
        browser = null;
        playwright = null;
        chromeProcess = null;

        return new BrowserResponse(
                true,
                "Browser closed successfully.",
                null,
                null,
                null
        );
    }

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;
    private Process chromeProcess;

    @Value("${chrome.path}")
    private String chromePath;

    @Value("${chrome.profile.path}")
    private String chromeProfilePath;


    private void initializeBrowser() {
        if (playwright != null) {
            return;
        }
        startChrome();
        String webSocketUrl;
        while ((webSocketUrl = getChromeWebSocketUrl()) == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for Chrome."+e);
            }
        }
        playwright = Playwright.create();
        browser = playwright.chromium().connectOverCDP(webSocketUrl);
        context = browser.contexts().getFirst();
        page = context.newPage();
    }

    private void startChrome() {
        try {
            chromeProcess = new ProcessBuilder(
                    chromePath,
                    "--remote-debugging-port=9222",
                    "--user-data-dir=" + chromeProfilePath,
                    "--start-maximized"
            ).start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to start Chrome. "+e);
        }
    }

    private String getChromeWebSocketUrl() {
        try {
            RestTemplate restTemplate = new RestTemplate();

            String response = restTemplate.getForObject("http://127.0.0.1:9222/json/version", String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("webSocketDebuggerUrl").asString();
        } catch (Exception e) {
            return null;
        }
    }

    public List<JobResult> extractJobs() {
        initializeBrowser();

        Locator jobsLocator = page.locator("a[href*='/jobs/view/']");

        List<JobResult> jobs = new ArrayList<>();

        for (int i = 0; i < jobsLocator.count(); i++) {

            Locator jobLink = jobsLocator.nth(i);
            Locator jobCard = jobLink.locator("xpath=ancestor::li");

            String role = jobLink.getAttribute("aria-label");
            String company = jobCard.locator(".artdeco-entity-lockup__subtitle").innerText();
            String location = jobCard.locator(".artdeco-entity-lockup__caption").innerText();
            String href = jobLink.getAttribute("href");

            String cleanUrl = "https://www.linkedin.com" + href.split("\\?")[0];
            boolean remote = location != null && location.toLowerCase().contains("remote");

            JobResult job = new JobResult();
            job.setRole(role);
            job.setCompany(company);
            job.setLocation(location);
            job.setJobUrl(cleanUrl);
            job.setRemote(remote);
            job.setPlatform("LinkedIn");

            jobs.add(job);
        }
        return jobs;
    }
}