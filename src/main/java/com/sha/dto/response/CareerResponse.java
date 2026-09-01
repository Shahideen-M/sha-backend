package com.sha.dto.response;

import com.sha.dto.data.JobResult;
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
