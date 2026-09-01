package com.sha.dto.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobResult {
    private String company;
    private String role;
    private String location;
    private String experience;
    private String jobUrl;
    private String description;
}
