package com.sha.dto.request;

import com.sha.enums.BrowserOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrowserRequest {

    private BrowserOperation operation;
    private String searchQuery;
}
