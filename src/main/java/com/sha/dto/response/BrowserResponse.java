package com.sha.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrowserResponse {

    private boolean success;
    private String message;
    private String title;
    private String url;
    private String content;

}
