package com.sha.dto.response;

import com.sha.dto.data.VideoPlanData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentCreatorResponse {

    private boolean success;
    private String message;
    private VideoPlanData data;
}
