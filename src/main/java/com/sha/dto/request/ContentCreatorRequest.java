package com.sha.dto.request;

import com.sha.enums.ContentCreatorOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentCreatorRequest {

    private String content;
    private ContentCreatorOperation operation;
}
