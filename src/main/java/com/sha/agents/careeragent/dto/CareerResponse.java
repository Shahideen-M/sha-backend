package com.sha.agents.careeragent.dto;

import com.sha.agents.data.JobResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CareerResponse {

    private boolean success;
    private String message;
    private List<JobResult> jobs;
}
