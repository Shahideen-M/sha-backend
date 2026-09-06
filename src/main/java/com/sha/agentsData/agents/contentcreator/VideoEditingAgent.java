package com.sha.agentsData.agents.contentcreator;

import com.sha.agentsData.agents.contentcreator.dto.*;
import com.sha.agentsData.agents.contentcreator.enums.VideoEditorOperation;
import com.sha.agentsData.agents.contentcreator.service.EditPlanBuilder;
import com.sha.agentsData.agents.contentcreator.service.FrameExtractor;
import com.sha.agentsData.agents.contentcreator.service.VideoAnalyzer;
import com.sha.agentsData.enums.AgentType;
import com.sha.agentsData.service.Agent;
import com.sha.brain.dto.OperationPrompt;
import com.sha.brain.prompt.AgentPrompt;
import com.sha.dto.request.ChatRequest;
import com.sha.service.impl.AIRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoEditingAgent implements Agent<VideoEditorRequest, VideoEditorResponse> {

    private final AIRouter aiRouter;
    private final FrameExtractor frameExtractor;
    private final VideoAnalyzer videoAnalyzer;
    private final EditPlanBuilder editPlanBuilder;

    @Value("${sha.video.ffmpeg-path}")
    private String ffmpegPath;

    @Value("${sha.video.ffprobe-path}")
    private String ffprobePath;

    @Value("${sha.video.tts-command:}")
    private String ttsCommand;

    @Value("${sha.video.intro-path:}")
    private String introPath;

    @Value("${sha.video.outro-path:}")
    private String outroPath;

    @Override
    public AgentType getType() {
        return AgentType.VIDEO_EDITING;
    }

    @Override
    public VideoEditorResponse executeTyped(VideoEditorRequest request) {
        if (request == null || request.getOperation() == null) {
            return errorResponse("Video editor operation is required.");
        }

        return switch (request.getOperation()) {
            case ANALYZE -> analyzeBasic(request);
            case ANALYZE_DETAILED -> analyzeDetailed(request);
            case CREATE_EDIT_PLAN -> createEditPlan(request);
            case GENERATE_NARRATION_SCRIPT -> generateNarrationScript(request);
            case EXECUTE_EDIT -> executeEdit(request);
            case EDIT -> editVideo(request);
        };
    }

    @Override
    public Class<VideoEditorRequest> getRequestClass() {
        return VideoEditorRequest.class;
    }

    @Override
    public VideoEditorResponse execute(Object request) {
        return executeTyped((VideoEditorRequest) request);
    }

    @Override
    public AgentPrompt<VideoEditorOperation> describe() {
        return new AgentPrompt<>(
                AgentType.VIDEO_EDITING,
                "Autonomously edit raw videos into a complete long-form video and a short video.",
                List.of(
                        "edit video",
                        "video editing",
                        "create youtube video",
                        "make short video",
                        "analyze video",
                        "create edit plan",
                        "generate narration",
                        "cut video"
                ),
                List.of(
                        new OperationPrompt<>(
                                VideoEditorOperation.EDIT,
                                "Fully edit a raw video from analysis to finished long-form and short videos.",
                                List.of("videoPath"),
                                """
                                {
                                  "videoPath":"D:/Videos/video.mp4",
                                  "operation":"EDIT"
                                }
                                """
                        ),
                        new OperationPrompt<>(
                                VideoEditorOperation.ANALYZE,
                                "Perform basic video analysis and get its duration.",
                                List.of("videoPath"),
                                """
                                {
                                  "videoPath":"D:/Videos/video.mp4",
                                  "operation":"ANALYZE"
                                }
                                """
                        ),
                        new OperationPrompt<>(
                                VideoEditorOperation.ANALYZE_DETAILED,
                                "Perform detailed AI-assisted video analysis.",
                                List.of("videoPath"),
                                """
                                {
                                  "videoPath":"D:/Videos/video.mp4",
                                  "operation":"ANALYZE_DETAILED"
                                }
                                """
                        ),
                        new OperationPrompt<>(
                                VideoEditorOperation.CREATE_EDIT_PLAN,
                                "Create an edit plan from completed video analysis.",
                                List.of("analysis"),
                                """
                                {
                                  "operation":"CREATE_EDIT_PLAN",
                                  "analysis":{}
                                }
                                """
                        ),
                        new OperationPrompt<>(
                                VideoEditorOperation.GENERATE_NARRATION_SCRIPT,
                                "Generate narration from an edit plan.",
                                List.of("editPlan"),
                                """
                                {
                                  "operation":"GENERATE_NARRATION_SCRIPT",
                                  "editPlan":{}
                                }
                                """
                        ),
                        new OperationPrompt<>(
                                VideoEditorOperation.EXECUTE_EDIT,
                                "Execute a prepared edit plan.",
                                List.of("videoPath", "editPlan"),
                                """
                                {
                                  "videoPath":"D:/Videos/video.mp4",
                                  "operation":"EXECUTE_EDIT",
                                  "editPlan":{}
                                }
                                """
                        )
                )
        );
    }

    private VideoEditorResponse analyzeBasic(VideoEditorRequest request) {
        if (!isValidVideo(request.getVideoPath())) {
            return errorResponse(
                    "Video file not found: " + request.getVideoPath()
            );
        }

        return new VideoEditorResponse(
                true,
                "Video analyzed successfully.",
                probeDuration(request.getVideoPath()),
                List.of("ANALYZE"),
                null,
                null,
                null,
                false,
                "ANALYZE_COMPLETE",
                null,
                null
        );
    }

    private VideoEditorResponse analyzeDetailed(VideoEditorRequest request) {
        try {
            if (!isValidVideo(request.getVideoPath())) {
                return errorResponse(
                        "Video file not found: " + request.getVideoPath()
                );
            }

            VideoTimeline timeline =
                    frameExtractor.extractFrames(
                            request.getVideoPath(),
                            1.0
                    );

            if (timeline.getFrames() != null) {
                timeline.setFrames(
                        frameExtractor.deduplicateFrames(
                                timeline.getFrames(),
                                0.95
                        )
                );
            }

            VideoAnalysisData analysis =
                    videoAnalyzer.analyze(
                            timeline,
                            request.getVideoPlan()
                    );

            return new VideoEditorResponse(
                    true,
                    "Detailed analysis complete.",
                    analysis.getDuration(),
                    List.of("ANALYZE_DETAILED"),
                    analysis,
                    null,
                    null,
                    false,
                    "ANALYSIS_READY",
                    null,
                    null
            );

        } catch (Exception e) {
            return errorResponse(
                    "Detailed analysis failed: " + e.getMessage()
            );
        }
    }

    private VideoEditorResponse createEditPlan(
            VideoEditorRequest request
    ) {
        try {
            if (request.getAnalysis() == null) {
                return errorResponse(
                        "Video analysis is required. Run ANALYZE_DETAILED first."
                );
            }

            EditPlan plan =
                    editPlanBuilder.buildPlan(
                            request.getAnalysis()
                    );

            return new VideoEditorResponse(
                    true,
                    "Edit plan created successfully.",
                    plan.getEstimatedDuration(),
                    List.of("CREATE_EDIT_PLAN"),
                    request.getAnalysis(),
                    plan,
                    null,
                    false,
                    "EDIT_PLAN_READY",
                    null,
                    null
            );

        } catch (Exception e) {
            return errorResponse(
                    "Failed to create edit plan: " + e.getMessage()
            );
        }
    }

    private VideoEditorResponse generateNarrationScript(
            VideoEditorRequest request
    ) {
        try {
            if (request.getEditPlan() == null) {
                return errorResponse(
                        "Edit plan is required. Run CREATE_EDIT_PLAN first."
                );
            }

            String text = generateNarrationText(request);

            if (text == null || text.isBlank()) {
                return errorResponse(
                        "AI returned an empty narration script."
                );
            }

            NarrationScript narration =
                    new NarrationScript(
                            text.trim(),
                            true
                    );

            return new VideoEditorResponse(
                    true,
                    "Narration script generated successfully.",
                    null,
                    List.of("GENERATE_NARRATION_SCRIPT"),
                    request.getAnalysis(),
                    request.getEditPlan(),
                    narration,
                    false,
                    "NARRATION_READY",
                    null,
                    null
            );

        } catch (Exception e) {
            return errorResponse(
                    "Narration generation failed: " + e.getMessage()
            );
        }
    }

    private VideoEditorResponse editVideo(
            VideoEditorRequest request
    ) {
        List<String> completedSteps = new ArrayList<>();
        List<Path> tempFiles = new ArrayList<>();

        try {
            VideoEditorResponse basic =
                    analyzeBasic(request);

            if (!basic.isSuccess()) {
                return basic;
            }

            completedSteps.add("ANALYZE");

            VideoEditorResponse analysis =
                    analyzeDetailed(request);

            if (!analysis.isSuccess()) {
                return analysis;
            }

            completedSteps.add("ANALYZE_DETAILED");

            VideoEditorRequest planRequest =
                    new VideoEditorRequest();

            planRequest.setVideoPath(
                    request.getVideoPath()
            );

            planRequest.setAnalysis(
                    analysis.getAnalysis()
            );

            VideoEditorResponse plan =
                    createEditPlan(planRequest);

            if (!plan.isSuccess()) {
                return plan;
            }

            completedSteps.add("CREATE_EDIT_PLAN");

            Path source =
                    Path.of(request.getVideoPath());

            Path outputDirectory =
                    getOutputDirectory(source);

            String fileName =
                    getBaseName(source);

            Path longVideoOutput =
                    outputDirectory.resolve(
                            fileName + "-long.mp4"
                    );

            Path shortVideoOutput =
                    outputDirectory.resolve(
                            fileName + "-short.mp4"
                    );

            String editedVideo =
                    createEditedVideo(
                            request.getVideoPath(),
                            plan.getEditPlan(),
                            outputDirectory,
                            tempFiles
                    );

            completedSteps.add("CUT_AND_EDIT");

            NarrationScript narration = null;
            String narratedVideo = editedVideo;

            try {
                VideoEditorRequest narrationRequest =
                        new VideoEditorRequest();

                narrationRequest.setVideoPath(
                        request.getVideoPath()
                );

                narrationRequest.setAnalysis(
                        analysis.getAnalysis()
                );

                narrationRequest.setEditPlan(
                        plan.getEditPlan()
                );

                narrationRequest.setContext(
                        request.getContext()
                );

                VideoEditorResponse narrationResponse =
                        generateNarrationScript(
                                narrationRequest
                        );

                if (narrationResponse.isSuccess()) {
                    narration =
                            narrationResponse.getNarrationScript();

                    narratedVideo =
                            addNarrationIfPossible(
                                    editedVideo,
                                    narration,
                                    outputDirectory,
                                    tempFiles
                            );

                    completedSteps.add("ADD_NARRATION");
                }

            } catch (Exception ignored) {
                completedSteps.add("NARRATION_SKIPPED");
            }

            createLongFormVideo(
                    narratedVideo,
                    longVideoOutput.toString()
            );

            completedSteps.add("CREATE_LONG_VIDEO");

            createShortVideo(
                    narratedVideo,
                    plan.getEditPlan(),
                    shortVideoOutput.toString()
            );

            completedSteps.add("CREATE_SHORT_VIDEO");

            double duration =
                    probeDuration(
                            longVideoOutput.toString()
                    );

            return new VideoEditorResponse(
                    true,
                    "Long-form and short videos created successfully.",
                    duration,
                    completedSteps,
                    analysis.getAnalysis(),
                    plan.getEditPlan(),
                    narration,
                    false,
                    "EDIT_COMPLETE",
                    longVideoOutput.toString(),
                    shortVideoOutput.toString()
            );

        } catch (Exception e) {
            return errorResponse(
                    "Video editing failed: " + e.getMessage()
            );
        } finally {
            cleanupTempFiles(tempFiles);
        }
    }

    private String createEditedVideo(
            String videoPath,
            EditPlan editPlan,
            Path outputDirectory,
            List<Path> tempFiles
    ) throws Exception {

        Path output =
                createTempFile(
                        outputDirectory,
                        "sha-edited-",
                        ".mp4"
                );

        tempFiles.add(output);

        if (editPlan == null
                || editPlan.getSegments() == null
                || editPlan.getSegments().isEmpty()) {

            Files.copy(
                    Path.of(videoPath),
                    output,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            return output.toString();
        }

        cutAndConcatenateSegments(
                videoPath,
                editPlan.getSegments(),
                output.toString()
        );

        return output.toString();
    }

    private String addNarrationIfPossible(
            String videoPath,
            NarrationScript narration,
            Path outputDirectory,
            List<Path> tempFiles
    ) throws Exception {

        if (narration == null
                || narration.getScript() == null
                || narration.getScript().isBlank()
                || ttsCommand == null
                || ttsCommand.isBlank()) {
            return videoPath;
        }

        Path audio =
                createTempFile(
                        outputDirectory,
                        "sha-narration-",
                        ".mp3"
                );

        tempFiles.add(audio);

        boolean generated =
                generateNarrationAudio(
                        narration.getScript(),
                        audio.toString()
                );

        if (!generated) {
            return videoPath;
        }

        Path output =
                createTempFile(
                        outputDirectory,
                        "sha-narrated-",
                        ".mp4"
                );

        tempFiles.add(output);

        addNarrationAudio(
                videoPath,
                audio.toString(),
                output.toString()
        );

        return output.toString();
    }

    private void createLongFormVideo(
            String videoPath,
            String outputPath
    ) throws Exception {

        boolean hasIntro =
                isValidVideo(introPath);

        boolean hasOutro =
                isValidVideo(outroPath);

        List<String> videos =
                new ArrayList<>();

        if (hasIntro) {
            videos.add(introPath);
        }

        videos.add(videoPath);

        if (hasOutro) {
            videos.add(outroPath);
        }

        concatenateVideos(
                videos,
                outputPath
        );
    }

    private void createShortVideo(
            String videoPath,
            EditPlan editPlan,
            String outputPath
    ) throws Exception {

        VideoSegment bestSegment =
                selectBestShortSegment(
                        editPlan,
                        probeDuration(videoPath)
                );

        double startTime =
                bestSegment.getStartTime();

        double duration =
                Math.min(
                        bestSegment.getEndTime() - startTime,
                        60.0
                );

        List<String> arguments =
                new ArrayList<>();

        arguments.add("-ss");
        arguments.add(String.valueOf(startTime));

        arguments.add("-i");
        arguments.add(videoPath);

        arguments.add("-t");
        arguments.add(String.valueOf(duration));

        arguments.add("-vf");
        arguments.add(
                "scale=1080:1920:force_original_aspect_ratio=decrease,"
                        + "pad=1080:1920:(ow-iw)/2:(oh-ih)/2,"
                        + "setsar=1"
        );

        arguments.add("-c:v");
        arguments.add("libx264");

        arguments.add("-preset");
        arguments.add("veryfast");

        arguments.add("-crf");
        arguments.add("23");

        if (hasAudioStream(videoPath)) {
            arguments.add("-c:a");
            arguments.add("aac");
        } else {
            arguments.add("-an");
        }

        arguments.add(outputPath);

        runFfmpeg(arguments);
    }

    private VideoSegment selectBestShortSegment(
            EditPlan editPlan,
            double videoDuration
    ) {
        if (editPlan != null
                && editPlan.getSegments() != null
                && !editPlan.getSegments().isEmpty()) {

            return editPlan.getSegments()
                    .stream()
                    .filter(this::isValidSegment)
                    .max(
                            Comparator.comparingDouble(
                                    segment ->
                                            Math.min(
                                                    segment.getEndTime()
                                                            - segment.getStartTime(),
                                                    60.0
                                            )
                            )
                    )
                    .orElse(
                            new VideoSegment(
                                    0.0,
                                    Math.min(videoDuration, 60.0),
                                    "Short video"
                            )
                    );
        }

        return new VideoSegment(
                0.0,
                Math.min(videoDuration, 60.0),
                "Short video"
        );
    }

    private void concatenateVideos(
            List<String> videos,
            String outputPath
    ) throws Exception {

        if (videos == null || videos.isEmpty()) {
            throw new IllegalArgumentException(
                    "No videos available for concatenation."
            );
        }

        List<String> arguments =
                new ArrayList<>();

        for (String video : videos) {
            if (!isValidVideo(video)) {
                throw new IllegalArgumentException(
                        "Video file not found: " + video
                );
            }

            arguments.add("-i");
            arguments.add(video);
        }

        StringBuilder filter =
                new StringBuilder();

        for (int i = 0; i < videos.size(); i++) {
            filter.append("[")
                    .append(i)
                    .append(":v]")
                    .append("scale=1920:1080:")
                    .append("force_original_aspect_ratio=decrease,")
                    .append("pad=1920:1080:")
                    .append("(ow-iw)/2:(oh-ih)/2,")
                    .append("setsar=1,")
                    .append("fps=30")
                    .append("[v")
                    .append(i)
                    .append("];");

            if (hasAudioStream(videos.get(i))) {
                filter.append("[")
                        .append(i)
                        .append(":a]")
                        .append("aresample=44100,")
                        .append("aformat=sample_fmts=fltp:"
                                + "sample_rates=44100:"
                                + "channel_layouts=stereo")
                        .append("[a")
                        .append(i)
                        .append("];");
            } else {
                filter.append(
                        "anullsrc="
                                + "channel_layout=stereo:"
                                + "sample_rate=44100,"
                                + "atrim=duration="
                                + probeDuration(videos.get(i))
                                + "[a"
                                + i
                                + "];"
                );
            }
        }

        for (int i = 0; i < videos.size(); i++) {
            filter.append("[v")
                    .append(i)
                    .append("][a")
                    .append(i)
                    .append("]");
        }

        filter.append("concat=n=")
                .append(videos.size())
                .append(":v=1:a=1[outv][outa]");

        arguments.add("-filter_complex");
        arguments.add(filter.toString());

        arguments.add("-map");
        arguments.add("[outv]");

        arguments.add("-map");
        arguments.add("[outa]");

        arguments.add("-c:v");
        arguments.add("libx264");

        arguments.add("-preset");
        arguments.add("veryfast");

        arguments.add("-crf");
        arguments.add("20");

        arguments.add("-pix_fmt");
        arguments.add("yuv420p");

        arguments.add("-c:a");
        arguments.add("aac");

        arguments.add("-b:a");
        arguments.add("192k");

        arguments.add("-ar");
        arguments.add("44100");

        arguments.add("-ac");
        arguments.add("2");

        arguments.add("-movflags");
        arguments.add("+faststart");

        arguments.add(outputPath);

        runFfmpeg(arguments);
    }

    private VideoEditorResponse executeEdit(
            VideoEditorRequest request
    ) {
        try {
            if (request.getEditPlan() == null) {
                return errorResponse(
                        "Edit plan is required."
                );
            }

            if (!isValidVideo(request.getVideoPath())) {
                return errorResponse(
                        "Video file not found: "
                                + request.getVideoPath()
                );
            }

            Path source =
                    Path.of(request.getVideoPath());

            Path outputDirectory =
                    getOutputDirectory(source);

            Path output =
                    outputDirectory.resolve(
                            getBaseName(source)
                                    + "-edited.mp4"
                    );

            cutAndConcatenateSegments(
                    request.getVideoPath(),
                    request.getEditPlan().getSegments(),
                    output.toString()
            );

            return new VideoEditorResponse(
                    true,
                    "Edit executed successfully.",
                    probeDuration(output.toString()),
                    List.of("EXECUTE_EDIT"),
                    request.getAnalysis(),
                    request.getEditPlan(),
                    request.getNarrationScript(),
                    false,
                    "EDIT_COMPLETE",
                    output.toString(),
                    null
            );

        } catch (Exception e) {
            return errorResponse(
                    "Edit execution failed: " + e.getMessage()
            );
        }
    }

    private void cutAndConcatenateSegments(
            String videoPath,
            List<VideoSegment> segments,
            String outputPath
    ) throws IOException, InterruptedException {

        if (segments == null || segments.isEmpty()) {
            throw new IllegalArgumentException(
                    "No valid segments available."
            );
        }

        List<VideoSegment> validSegments =
                segments.stream()
                        .filter(this::isValidSegment)
                        .sorted(
                                Comparator.comparingDouble(
                                        VideoSegment::getStartTime
                                )
                        )
                        .toList();

        if (validSegments.isEmpty()) {
            throw new IllegalArgumentException(
                    "No valid segments available."
            );
        }

        boolean hasAudio =
                hasAudioStream(videoPath);

        StringBuilder filter =
                new StringBuilder();

        for (int i = 0; i < validSegments.size(); i++) {
            VideoSegment segment =
                    validSegments.get(i);

            filter.append(
                    String.format(
                            "[0:v]trim=start=%.3f:end=%.3f,"
                                    + "setpts=PTS-STARTPTS[v%d];",
                            segment.getStartTime(),
                            segment.getEndTime(),
                            i
                    )
            );

            if (hasAudio) {
                filter.append(
                        String.format(
                                "[0:a]atrim=start=%.3f:end=%.3f,"
                                        + "asetpts=PTS-STARTPTS[a%d];",
                                segment.getStartTime(),
                                segment.getEndTime(),
                                i
                        )
                );
            } else {
                filter.append(
                        "anullsrc=channel_layout=stereo:"
                                + "sample_rate=44100,"
                                + "atrim=duration="
                                + (segment.getEndTime()
                                - segment.getStartTime())
                                + ","
                                + "asetpts=PTS-STARTPTS[a"
                                + i
                                + "];"
                );
            }
        }

        for (int i = 0; i < validSegments.size(); i++) {
            filter.append("[v")
                    .append(i)
                    .append("][a")
                    .append(i)
                    .append("]");
        }

        filter.append("concat=n=")
                .append(validSegments.size())
                .append(":v=1:a=1[outv][outa]");

        runFfmpeg(List.of(
                "-i",
                videoPath,
                "-filter_complex",
                filter.toString(),
                "-map",
                "[outv]",
                "-map",
                "[outa]",
                "-c:v",
                "libx264",
                "-preset",
                "veryfast",
                "-crf",
                "20",
                "-pix_fmt",
                "yuv420p",
                "-c:a",
                "aac",
                "-b:a",
                "192k",
                "-movflags",
                "+faststart",
                outputPath
        ));
    }

    private void addNarrationAudio(
            String videoPath,
            String audioPath,
            String outputPath
    ) throws IOException, InterruptedException {

        double videoDuration =
                probeDuration(videoPath);

        runFfmpeg(List.of(
                "-i",
                videoPath,
                "-i",
                audioPath,
                "-filter_complex",
                "[1:a]apad=pad_dur="
                        + Math.max(videoDuration, 0.0)
                        + "[narration]",
                "-map",
                "0:v:0",
                "-map",
                "[narration]",
                "-c:v",
                "copy",
                "-c:a",
                "aac",
                "-b:a",
                "192k",
                "-t",
                String.valueOf(videoDuration),
                "-movflags",
                "+faststart",
                outputPath
        ));
    }

    private String generateNarrationText(
            VideoEditorRequest request
    ) {
        StringBuilder prompt =
                new StringBuilder(
                        """
                        Write concise spoken narration for this YouTube video.

                        Return ONLY the narration text.
                        No markdown.
                        No headings.
                        No explanations.
                        Make it natural and clear.

                        IMPORTANT:
                        Do not describe something that is not supported by
                        the provided video segment descriptions.
                        """
                );

        if (request.getEditPlan() != null
                && request.getEditPlan().getSegments() != null) {

            prompt.append("\nVideo segments:\n");

            for (VideoSegment segment :
                    request.getEditPlan().getSegments()) {

                prompt.append(
                        String.format(
                                "- %.1fs to %.1fs: %s%n",
                                segment.getStartTime(),
                                segment.getEndTime(),
                                segment.getDescription()
                        )
                );
            }
        }

        if (request.getContext() != null
                && !request.getContext().isBlank()) {

            prompt.append("\nContext: ")
                    .append(request.getContext());
        }

        return aiRouter
                .geminiChat(
                        new ChatRequest(
                                prompt.toString()
                        )
                )
                .getResponse();
    }

    private boolean generateNarrationAudio(
            String script,
            String outputPath
    ) {

        if (ttsCommand == null
                || ttsCommand.isBlank()) {
            return false;
        }

        Path scriptFile = null;

        try {
            scriptFile =
                    Files.createTempFile(
                            "sha-narration-",
                            ".txt"
                    );

            Files.writeString(
                    scriptFile,
                    script,
                    StandardCharsets.UTF_8
            );

            String command =
                    ttsCommand
                            .replace(
                                    "{textFile}",
                                    scriptFile.toString()
                            )
                            .replace(
                                    "{output}",
                                    outputPath
                            );

            Process process =
                    new ProcessBuilder(
                            "cmd",
                            "/c",
                            command
                    )
                            .redirectErrorStream(true)
                            .start();

            String output =
                    new String(
                            process.getInputStream()
                                    .readAllBytes(),
                            StandardCharsets.UTF_8
                    );

            int exitCode =
                    process.waitFor();

            if (exitCode != 0) {
                System.err.println(
                        "TTS failed: " + output
                );
                return false;
            }

            return Files.exists(
                    Path.of(outputPath)
            ) && Files.size(
                    Path.of(outputPath)
            ) > 0;

        } catch (Exception e) {
            System.err.println(
                    "TTS generation failed: "
                            + e.getMessage()
            );
            return false;

        } finally {
            if (scriptFile != null) {
                try {
                    Files.deleteIfExists(
                            scriptFile
                    );
                } catch (IOException ignored) {
                }
            }
        }
    }

    private boolean hasAudioStream(
            String videoPath
    ) {
        try {
            Process process =
                    new ProcessBuilder(
                            ffprobePath,
                            "-v",
                            "error",
                            "-select_streams",
                            "a",
                            "-show_entries",
                            "stream=index",
                            "-of",
                            "default=noprint_wrappers=1:nokey=1",
                            videoPath
                    )
                            .redirectErrorStream(true)
                            .start();

            String output =
                    new String(
                            process.getInputStream()
                                    .readAllBytes(),
                            StandardCharsets.UTF_8
                    ).trim();

            process.waitFor();

            return !output.isBlank();

        } catch (Exception e) {
            return false;
        }
    }

    private void runFfmpeg(
            List<String> arguments
    ) throws IOException, InterruptedException {

        List<String> command =
                new ArrayList<>();

        command.add(ffmpegPath);
        command.add("-y");
        command.addAll(arguments);

        Process process =
                new ProcessBuilder(command)
                        .redirectErrorStream(true)
                        .start();

        String output =
                new String(
                        process.getInputStream()
                                .readAllBytes(),
                        StandardCharsets.UTF_8
                );

        int exitCode =
                process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException(
                    "FFmpeg failed: " + output
            );
        }
    }

    private double probeDuration(
            String videoPath
    ) {
        try {
            Process process =
                    new ProcessBuilder(
                            ffprobePath,
                            "-v",
                            "error",
                            "-show_entries",
                            "format=duration",
                            "-of",
                            "default=noprint_wrappers=1:nokey=1",
                            videoPath
                    )
                            .redirectErrorStream(true)
                            .start();

            String output =
                    new String(
                            process.getInputStream()
                                    .readAllBytes(),
                            StandardCharsets.UTF_8
                    ).trim();

            int exitCode =
                    process.waitFor();

            return exitCode == 0
                    && !output.isBlank()
                    ? Double.parseDouble(output)
                    : 0.0;

        } catch (Exception e) {
            return 0.0;
        }
    }

    private Path getOutputDirectory(
            Path source
    ) {
        return source.getParent() != null
                ? source.getParent()
                : Path.of(".");
    }

    private String getBaseName(
            Path path
    ) {
        String fileName =
                path.getFileName().toString();

        int dot =
                fileName.lastIndexOf('.');

        return dot > 0
                ? fileName.substring(0, dot)
                : fileName;
    }

    private Path createTempFile(
            Path directory,
            String prefix,
            String suffix
    ) throws IOException {

        Files.createDirectories(directory);

        return Files.createTempFile(
                directory,
                prefix,
                suffix
        );
    }

    private void cleanupTempFiles(
            List<Path> files
    ) {
        for (Path file : files) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException ignored) {
            }
        }
    }

    private boolean isValidVideo(
            String videoPath
    ) {
        if (videoPath == null
                || videoPath.isBlank()) {
            return false;
        }

        try {
            Path path =
                    Path.of(videoPath);

            return Files.exists(path)
                    && Files.isRegularFile(path);

        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidSegment(
            VideoSegment segment
    ) {
        return segment != null
                && Double.isFinite(segment.getStartTime())
                && Double.isFinite(segment.getEndTime())
                && segment.getStartTime() >= 0
                && segment.getEndTime()
                > segment.getStartTime();
    }

    private VideoEditorResponse errorResponse(
            String message
    ) {
        return new VideoEditorResponse(
                false,
                message,
                null,
                List.of(),
                null,
                null,
                null,
                false,
                "ERROR",
                null,
                null
        );
    }
}