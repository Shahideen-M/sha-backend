package com.sha.skills.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestResponse {

    private boolean success;
    private int exitCode;
    private String output;
}