package com.sha.service.skills;

import com.microsoft.playwright.*;
import com.sha.dto.request.BrowserRequest;
import com.sha.dto.response.BrowserResponse;
import com.sha.enums.SkillType;
import com.sha.service.Skill;
import org.springframework.stereotype.Service;

@Service
public class BrowerSkill implements Skill<BrowserRequest, BrowserResponse> {

    @Override
    public SkillType getType() {
        return SkillType.BROWSER;
    }

    @Override
    public BrowserResponse execute(BrowserRequest request) {
        return switch (request.getOperation()) {
            case SEARCH -> search(request);
            case READ_PAGE -> readPage(request);
            case CLOSE_BROWSER -> closeBrowser();
        };
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
        page.waitForLoadState();
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

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    private BrowserResponse closeBrowser() {
        if (page != null) {
            page.close();
        }
        if (context != null) {
            context.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
        page = null;
        context = null;
        browser = null;
        playwright = null;
        return new BrowserResponse(
                true,
                "Browser closed successfully.",
                null,
                null,
                null
        );
    }

    private void initializeBrowser() {
        if (playwright != null) {
            return;
        }
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setChannel("chrome"));
        context = browser.newContext();
        page = context.newPage();

    }
}
