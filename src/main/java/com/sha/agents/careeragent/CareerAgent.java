package com.sha.agents.careeragent;

import com.sha.agents.careeragent.service.CareerJobService;
import com.sha.agents.enums.AgentType;
import com.sha.agents.service.Agent;
import com.sha.brain.prompt.AgentPrompt;
import com.sha.agents.careeragent.dto.CareerRequest;
import com.sha.agents.careeragent.dto.CareerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CareerAgent implements Agent<CareerRequest, CareerResponse> {

    private final CareerJobService careerJobService;

    @Override
    public AgentType getType() {
        return AgentType.CAREER;
    }

    @Override
    public CareerResponse executeTyped(CareerRequest request) {
        return switch (request.getOperation()) {
            case SEARCH_JOBS -> careerJobService.searchJobs(request);
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
    public AgentPrompt<?> describe() {
        return null;
    }

}
