package com.sha.skills.dto.request;

import com.sha.skills.enums.BrowserOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrowserRequest {

    private BrowserOperation operation;
    private String searchQuery;
    private String url;
    private String selector;
    private String text;
    private String key;
}
