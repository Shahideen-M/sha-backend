package com.sha.service.skills;

import com.sha.brain.prompt.SkillPrompt;
import com.sha.dto.data.JobResult;
import com.sha.dto.request.BrowserRequest;
import com.sha.dto.request.CareerRequest;
import com.sha.dto.response.BrowserResponse;
import com.sha.dto.response.CareerResponse;
import com.sha.enums.BrowserOperation;
import com.sha.brain.enums.SkillType;
import com.sha.service.Skill;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CareerSkill implements Skill<CareerRequest, CareerResponse> {

    private final BrowserSkill browserSkill;

    @Override
    public SkillType getType() {
        return SkillType.CAREER_ASSISTANT;
    }

    @Override
    public CareerResponse executeTyped(CareerRequest request) {
        return switch (request.getOperation()) {
            case SEARCH_JOBS -> searchJobs(request);
        };
    }

    @Override
    public Class<CareerRequest> getRequestClass() {
        return CareerRequest.class;
    }

    @Override
    public CareerResponse execute(Object request) {
        return executeTyped((CareerRequest) request);
    }

    @Override
    public SkillPrompt<?> describe() {
        return null;
    }

    public CareerResponse searchJobs(CareerRequest request) {
        String linkedInUrl = buildLinkedInSearchUrl(request);

        BrowserRequest browserRequest = new BrowserRequest(
                BrowserOperation.OPEN_URL,
                null,
                linkedInUrl,
                null,
                null,
                null
        );

        BrowserResponse openResponse = browserSkill.executeTyped(browserRequest);
        if (!openResponse.isSuccess()) {
            return new CareerResponse(
                    false,
                    "Failed to open LinkedIn.",
                    null
            );
        }

        List<JobResult> result = browserSkill.extractJobs();
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

}
