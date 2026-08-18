package com.sha.dto.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoPlanData {

    private String title;
    private String topic;
    private String hook;

    private List<String> recordingSteps;

    private String script;

    private List<String> visuals;

    private List<String> editingNotes;

    private String youtubeTitle;
    private String youtubeDescription;

    private String shortVersion;
}