package com.sha.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IslamicResponse {
    private boolean success;
    private String message;
    private Object data;
}