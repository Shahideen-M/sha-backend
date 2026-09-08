package com.sha.agents.careeragent.service;

import com.microsoft.playwright.Locator;
import com.sha.agents.careeragent.dto.CareerRequest;
import com.sha.agents.careeragent.dto.CareerResponse;
import com.sha.agents.data.JobResult;
import com.sha.skills.dto.request.BrowserRequest;
import com.sha.skills.dto.response.BrowserResponse;
import com.sha.skills.enums.BrowserOperation;
import com.sha.skills.BrowserSkill;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CareerJobService {

    private final BrowserSkill browserSkill;

    public CareerResponse searchJobs(CareerRequest request) {
        String linkedInUrl = buildLinkedInSearchUrl(request);

        BrowserRequest openUrlRequest = new BrowserRequest(
                BrowserOperation.OPEN_URL,
                null,
                linkedInUrl,
                null,
                null,
                null
        );
        BrowserResponse openResponse = browserSkill.executeTyped(openUrlRequest);
        BrowserRequest getHtmlPageRequest = new BrowserRequest(
                BrowserOperation.GET_PAGE_HTML,
                null,
                null,
                null,
                null,
                null
        );
        BrowserResponse getPage = browserSkill.executeTyped(getHtmlPageRequest);
        if (!getPage.isSuccess()) {
            return new CareerResponse(
                    false,
                    "Failed to open LinkedIn.",
                    null
            );
        }

        String content = getPage.getContent();

        List<JobResult> result = extractJobs(content);
        CareerResponse response = new CareerResponse(
                true,
                "Found " + result.size() + " jobs.",
                result
        );
        return response;
    }

    private String buildLinkedInSearchUrl(CareerRequest request) {
        String keywords = URLEncoder.encode(request.getRole(), StandardCharsets.UTF_8);
        String location = URLEncoder.encode(request.getLocation(), StandardCharsets.UTF_8);
        return "https://www.linkedin.com/jobs/search/"
                + "?keywords=" + keywords
                + "&location=" + location;
    }

    public List<JobResult> extractJobs(String content) {
        return null;
    }

//        Locator jobsLocator = page.locator("a[href*='/jobs/view/']");

//        List<JobResult> jobs = new ArrayList<>();
//
//        for (int i = 0; i < jobsLocator.count(); i++) {
//
//            Locator jobLink = jobsLocator.nth(i);
//            Locator jobCard = jobLink.locator("xpath=ancestor::li");
//
//            String role = jobLink.getAttribute("aria-label");
//            String company = jobCard.locator(".artdeco-entity-lockup__subtitle").innerText();
//            String location = jobCard.locator(".artdeco-entity-lockup__caption").innerText();
//            String href = jobLink.getAttribute("href");
//
//            String cleanUrl = "https://www.linkedin.com" + href.split("\\?")[0];
//            boolean remote = location != null && location.toLowerCase().contains("remote");
//
//            JobResult job = new JobResult();
//            job.setRole(role);
//            job.setCompany(company);
//            job.setLocation(location);
//            job.setJobUrl(cleanUrl);
//            job.setRemote(remote);
//            job.setPlatform("LinkedIn");
//
//            jobs.add(job);
//        }
//        return jobs;
//    }

}
