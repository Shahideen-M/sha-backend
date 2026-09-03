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
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoEditingAgent implements Agent<VideoEditorRequest, VideoEditorResponse> {

    private final AIRouter aiRouter;
    private final FrameExtractor frameExtractor;
    private final VideoAnalyzer videoAnalyzer;
    private final EditPlanBuilder editPlanBuilder;

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
            case EDIT -> editVideoLegacy(request);
            case ANALYZE_DETAILED -> analyzeDetailed(request);
            case CREATE_EDIT_PLAN -> createEditPlan(request);
            case GENERATE_NARRATION_SCRIPT -> generateNarrationScript(request);
            case EXECUTE_EDIT -> executeEdit(request);
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
                "Analyze videos, create AI-assisted edit plans, generate narration scripts, and execute video editing operations.",
                List.of(
                        "analyze video",
                        "video analysis",
                        "edit video",
                        "video editing",
                        "create edit plan",
                        "edit plan",
                        "generate narration",
                        "narration script",
                        "cut video"
                ),
                List.of(
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
                                List.of("videoPath", "videoPlan"),
                                """
                                {
                                  "videoPath":"D:/Videos/video.mp4",
                                  "operation":"ANALYZE_DETAILED"
                                }
                                """
                        ),

                        new OperationPrompt<>(
                                VideoEditorOperation.CREATE_EDIT_PLAN,
                                "Create an edit plan from a completed video analysis.",
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
                                "Generate a narration script from an edit plan.",
                                List.of("editPlan", "context"),
                                """
                                {
                                  "operation":"GENERATE_NARRATION_SCRIPT",
                                  "editPlan":{},
                                  "context":"Explain the video clearly"
                                }
                                """
                        ),

                        new OperationPrompt<>(
                                VideoEditorOperation.EXECUTE_EDIT,
                                "Execute an approved video edit plan.",
                                List.of(
                                        "videoPath",
                                        "editPlan",
                                        "narrationScript",
                                        "outputPath"
                                ),
                                """
                                {
                                  "videoPath":"D:/Videos/video.mp4",
                                  "operation":"EXECUTE_EDIT",
                                  "editPlan":{},
                                  "outputPath":"D:/Videos/edited-video.mp4"
                                }
                                """
                        ),

                        new OperationPrompt<>(
                                VideoEditorOperation.EDIT,
                                "Perform basic video editing based on instructions.",
                                List.of(
                                        "videoPath",
                                        "instructions",
                                        "outputPath"
                                ),
                                """
                                {
                                  "videoPath":"D:/Videos/video.mp4",
                                  "operation":"EDIT",
                                  "instructions":"Speed up the video",
                                  "outputPath":"D:/Videos/edited-video.mp4"
                                }
                                """
                        )
                )
        );
    }

    @Value("${sha.video.ffmpeg-path}")
    private String ffmpegPath;

    @Value("${sha.video.ffprobe-path}")
    private String ffprobePath;

    @Value("${sha.video.tts-command:}")
    private String ttsCommand;

    private VideoEditorResponse analyzeBasic(VideoEditorRequest request) {

        if (!isValidVideo(request.getVideoPath())) {
            return errorResponse("Video file not found: " + request.getVideoPath());
        }

        return new VideoEditorResponse(
                true,
                "Video analyzed successfully.",
                request.getVideoPath(),
                probeDuration(request.getVideoPath()),
                List.of("ANALYZE"),
                null, null, null,
                false,
                "ANALYZE_COMPLETE"
        );
    }

    private VideoEditorResponse analyzeDetailed(VideoEditorRequest request) {

        try {
            if (!isValidVideo(request.getVideoPath())) {
                return errorResponse("Video file not found: " + request.getVideoPath());
            }

            VideoTimeline timeline = frameExtractor.extractFrames(request.getVideoPath(), 1.0);

            timeline.setFrames(frameExtractor.deduplicateFrames(timeline.getFrames(), 0.95));

            VideoAnalysisData analysis = videoAnalyzer.analyze(timeline, request.getVideoPlan());

            return new VideoEditorResponse(
                    true,
                    "Detailed analysis complete.",
                    request.getVideoPath(),
                    analysis.getDuration(),
                    List.of("ANALYZE_DETAILED"),
                    analysis,
                    null,
                    null,
                    true,
                    "ANALYSIS_READY"
            );

        } catch (Exception e) {
            return errorResponse("Detailed analysis failed: " + e.getMessage());
        }
    }

    private VideoEditorResponse createEditPlan(VideoEditorRequest request) {

        try {
            if (request.getAnalysis() == null) {
                return errorResponse("Video analysis is required. Run ANALYZE_DETAILED first.");
            }

            EditPlan plan = editPlanBuilder.buildPlan(request.getAnalysis());

            return new VideoEditorResponse(
                    true,
                    "Edit plan created. Review before executing.",
                    request.getVideoPath(),
                    plan.getEstimatedDuration(),
                    List.of("CREATE_EDIT_PLAN"),
                    request.getAnalysis(),
                    plan,
                    null,
                    true,
                    "EDIT_PLAN_READY"
            );

        } catch (Exception e) {
            return errorResponse("Failed to create edit plan: " + e.getMessage());
        }
    }

    private VideoEditorResponse generateNarrationScript(
            VideoEditorRequest request
    ) {

        try {
            if (request.getEditPlan() == null) {
                return errorResponse("Edit plan is required. Run CREATE_EDIT_PLAN first.");
            }

            NarrationScript narration = new NarrationScript(
                    generateNarrationText(request),
                    false
            );

            return new VideoEditorResponse(
                    true,
                    "Narration script generated. Review and approve it.",
                    request.getVideoPath(),
                    null,
                    List.of("GENERATE_NARRATION_SCRIPT"),
                    request.getAnalysis(),
                    request.getEditPlan(),
                    narration,
                    true,
                    "NARRATION_READY"
            );

        } catch (Exception e) {
            return errorResponse("Narration generation failed: " + e.getMessage());
        }
    }

    private VideoEditorResponse editVideoLegacy(VideoEditorRequest request) {

        try {
            if (!isValidVideo(request.getVideoPath())) {
                return errorResponse("Video file not found: " + request.getVideoPath());
            }

            Path source = Path.of(request.getVideoPath());
            Path output = Path.of(resolveOutputPath(request));
            Path outputDir = getOutputDirectory(output);

            List<String> applied = new ArrayList<>();
            List<Path> tempFiles = new ArrayList<>();

            String current = source.toString();

            if (has(request.getInstructions(), "speed up", "faster", "speedup")) {

                Path temp = createTempFile(outputDir, "sha-speed-", ".mp4");

                tempFiles.add(temp);

                runFfmpeg(List.of(
                        "-i", current,
                        "-vf", "setpts=0.5*PTS",
                        "-af", "atempo=2.0",
                        temp.toString()
                ));

                current = temp.toString();
                applied.add("SPEED_UP");
            }

            current = addIntroIfPresent(
                    current,
                    request.getIntroPath(),
                    outputDir,
                    tempFiles,
                    applied
            );

            if (Boolean.TRUE.equals(request.getGenerateNarration())) {

                String script = generateNarrationText(request);
                Path audio = createTempFile(outputDir, "sha-narration-", ".mp3");

                tempFiles.add(audio);
                applied.add("NARRATION_SCRIPT");

                if (generateNarrationAudio(script, audio.toString())) {

                    Path temp = createTempFile(outputDir, "sha-narrated-", ".mp4");

                    tempFiles.add(temp);

                    addNarrationAudio(
                            current,
                            audio.toString(),
                            temp.toString()
                    );

                    current = temp.toString();
                    applied.add("ADD_NARRATION");

                } else {
                    applied.add("NARRATION_AUDIO_SKIPPED");
                }
            }

            finalizeOutput(source, current, output, applied);
            cleanup(tempFiles, output);

            return successResponse(
                    "Video edited successfully.",
                    output.toString(),
                    probeDuration(output.toString()),
                    applied
            );

        } catch (Exception e) {
            return errorResponse("Video editing failed: " + e.getMessage());
        }
    }

    private VideoEditorResponse executeEdit(VideoEditorRequest request) {

        try {
            if (request.getEditPlan() == null) {
                return errorResponse("Edit plan is required. Run CREATE_EDIT_PLAN first.");
            }

            if (!isValidVideo(request.getVideoPath())) {
                return errorResponse("Video file not found: " + request.getVideoPath());
            }

            Path source = Path.of(request.getVideoPath());
            Path output = Path.of(resolveOutputPath(request));
            Path outputDir = getOutputDirectory(output);

            List<String> applied = new ArrayList<>();
            List<Path> tempFiles = new ArrayList<>();

            String current = source.toString();

            if (request.getEditPlan().getSegments() != null
                    && !request.getEditPlan().getSegments().isEmpty()) {

                Path temp = createTempFile(outputDir, "sha-segments-", ".mp4");

                tempFiles.add(temp);

                cutAndConcatenateSegments(
                        current,
                        request.getEditPlan().getSegments(),
                        temp.toString()
                );

                current = temp.toString();
                applied.add("SEGMENT_CUT");
            }

            current = addIntroIfPresent(
                    current,
                    request.getIntroPath(),
                    outputDir,
                    tempFiles,
                    applied
            );

            NarrationScript narration = request.getNarrationScript();

            if (narration != null
                    && narration.isApproved()
                    && narration.getScript() != null
                    && !narration.getScript().isBlank()) {

                Path audio = createTempFile(outputDir, "sha-narration-", ".mp3");

                tempFiles.add(audio);

                if (generateNarrationAudio(narration.getScript(), audio.toString())) {

                    Path temp = createTempFile(outputDir, "sha-narrated-", ".mp4");

                    tempFiles.add(temp);

                    addNarrationAudio(
                            current,
                            audio.toString(),
                            temp.toString()
                    );

                    current = temp.toString();
                    applied.add("ADD_NARRATION");

                } else {
                    applied.add("NARRATION_AUDIO_SKIPPED");
                }
            }

            finalizeOutput(source, current, output, applied);
            cleanup(tempFiles, output);

            return successResponse(
                    "Video editing completed successfully.",
                    output.toString(),
                    probeDuration(output.toString()),
                    applied
            );

        } catch (Exception e) {
            return errorResponse("Edit execution failed: " + e.getMessage());
        }
    }

    private String addIntroIfPresent(
            String current,
            String introPath,
            Path outputDir,
            List<Path> tempFiles,
            List<String> applied
    ) throws IOException, InterruptedException {

        if (!isValidVideo(introPath)) return current;

        Path temp = createTempFile(outputDir, "sha-intro-", ".mp4");

        tempFiles.add(temp);

        runFfmpeg(List.of(
                "-i", introPath,
                "-i", current,
                "-filter_complex",
                "[0:v][0:a][1:v][1:a]" +
                        "concat=n=2:v=1:a=1[v][a]",
                "-map", "[v]",
                "-map", "[a]",
                "-c:v", "libx264",
                "-c:a", "aac",
                temp.toString()
        ));

        applied.add("ADD_INTRO");

        return temp.toString();
    }

    private void cutAndConcatenateSegments(
            String videoPath,
            List<VideoSegment> segments,
            String outputPath
    ) throws IOException, InterruptedException {

        List<VideoSegment> validSegments = segments.stream()
                .filter(this::isValidSegment)
                .sorted((a, b) ->
                        Double.compare(
                                a.getStartTime(),
                                b.getStartTime()
                        ))
                .toList();

        if (validSegments.isEmpty()) {
            throw new IllegalArgumentException("No valid segments available.");
        }

        StringBuilder filter = new StringBuilder();

        for (int i = 0; i < validSegments.size(); i++) {

            VideoSegment segment = validSegments.get(i);

            filter.append(String.format(
                    "[0:v]trim=start=%.3f:end=%.3f," +
                            "setpts=PTS-STARTPTS[v%d];",
                    segment.getStartTime(),
                    segment.getEndTime(),
                    i
            ));

            filter.append(String.format(
                    "[0:a]atrim=start=%.3f:end=%.3f," +
                            "asetpts=PTS-STARTPTS[a%d];",
                    segment.getStartTime(),
                    segment.getEndTime(),
                    i
            ));
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
                "-i", videoPath,
                "-filter_complex", filter.toString(),
                "-map", "[outv]",
                "-map", "[outa]",
                "-c:v", "libx264",
                "-c:a", "aac",
                outputPath
        ));
    }

    private void addNarrationAudio(
            String videoPath,
            String audioPath,
            String outputPath
    ) throws IOException, InterruptedException {

        runFfmpeg(List.of(
                "-i", videoPath,
                "-i", audioPath,
                "-map", "0:v:0",
                "-map", "1:a:0",
                "-c:v", "copy",
                "-c:a", "aac",
                "-shortest",
                outputPath
        ));
    }

    private String generateNarrationText(VideoEditorRequest request) {

        StringBuilder prompt = new StringBuilder(
                "Write a concise spoken narration for this video.\n" +
                        "Return only narration text. No markdown.\n"
        );

        if (request.getEditPlan() != null
                && request.getEditPlan().getSegments() != null) {

            prompt.append("\nVideo segments:\n");

            for (VideoSegment segment : request.getEditPlan().getSegments()) {

                prompt.append(String.format(
                        "- %.1fs to %.1fs: %s%n",
                        segment.getStartTime(),
                        segment.getEndTime(),
                        segment.getDescription()
                ));
            }
        }

        if (request.getContext() != null
                && !request.getContext().isBlank()) {

            prompt.append("\nContext: ")
                    .append(request.getContext());
        }

        return aiRouter
                .geminiChat(new ChatRequest(prompt.toString()))
                .getResponse();
    }

    private boolean generateNarrationAudio(String script, String outputPath) {

        if (ttsCommand == null || ttsCommand.isBlank()) return false;

        Path scriptFile = null;

        try {
            scriptFile = Files.createTempFile("sha-narration-", ".txt");

            Files.writeString(
                    scriptFile,
                    script,
                    StandardCharsets.UTF_8
            );

            String command = ttsCommand
                    .replace("{textFile}", scriptFile.toString())
                    .replace("{output}", outputPath);

            Process process = new ProcessBuilder(
                    "cmd", "/c", command
            ).inheritIO().start();

            int exitCode = process.waitFor();

            return exitCode == 0 && Files.exists(Path.of(outputPath));

        } catch (Exception e) {
            return false;

        } finally {
            if (scriptFile != null) {
                try {
                    Files.deleteIfExists(scriptFile);
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void runFfmpeg(List<String> arguments)
            throws IOException, InterruptedException {

        List<String> command = new ArrayList<>();

        command.add(ffmpegPath);
        command.add("-y");
        command.addAll(arguments);

        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();

        String output = new String(
                process.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg failed: " + output);
        }
    }

    private double probeDuration(String videoPath) {

        try {
            Process process = new ProcessBuilder(
                    ffprobePath,
                    "-v", "error",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    videoPath
            ).redirectErrorStream(true).start();

            String output = new String(
                    process.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            ).trim();

            int exitCode = process.waitFor();

            return exitCode == 0 && !output.isBlank()
                    ? Double.parseDouble(output)
                    : 0.0;

        } catch (Exception e) {
            return 0.0;
        }
    }

    private String resolveOutputPath(VideoEditorRequest request) {

        String outputPath = request.getOutputPath();

        if (outputPath != null
                && !outputPath.isBlank()
                && !outputPath.equals(request.getVideoPath())) {

            return outputPath;
        }

        Path source = Path.of(request.getVideoPath());
        String fileName = source.getFileName().toString();

        int dot = fileName.lastIndexOf('.');

        String name = dot > 0
                ? fileName.substring(0, dot)
                : fileName;

        String extension = dot > 0
                ? fileName.substring(dot)
                : ".mp4";

        Path parent = source.getParent() != null
                ? source.getParent()
                : Path.of(".");

        return parent.resolve(
                name + "-edited" + extension
        ).toString();
    }

    private Path getOutputDirectory(Path output) {
        return output.getParent() != null
                ? output.getParent()
                : Path.of(".");
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

    private void finalizeOutput(
            Path source,
            String current,
            Path output,
            List<String> applied
    ) throws IOException {

        Path currentPath = Path.of(current);

        if (currentPath.equals(source)) {

            Files.copy(
                    source,
                    output,
                    StandardCopyOption.REPLACE_EXISTING
            );

            applied.add("COPY");

        } else {

            Files.move(
                    currentPath,
                    output,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    private void cleanup(
            List<Path> files,
            Path output
    ) {

        for (Path file : files) {
            try {
                if (!file.equals(output)) {
                    Files.deleteIfExists(file);
                }
            } catch (IOException ignored) {
            }
        }
    }

    private boolean isValidVideo(String videoPath) {

        if (videoPath == null || videoPath.isBlank()) {
            return false;
        }

        try {
            Path path = Path.of(videoPath);

            return Files.exists(path)
                    && Files.isRegularFile(path);

        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidSegment(VideoSegment segment) {

        return segment != null
                && segment.getStartTime() >= 0
                && segment.getEndTime() > segment.getStartTime();
    }

    private boolean has(
            String text,
            String... keywords
    ) {

        if (text == null) {
            return false;
        }

        String lower = text.toLowerCase();

        for (String keyword : keywords) {
            if (lower.contains(keyword.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    private VideoEditorResponse successResponse(
            String message,
            String outputPath,
            double duration,
            List<String> operations
    ) {

        return new VideoEditorResponse(
                true,
                message,
                outputPath,
                duration,
                operations,
                null,
                null,
                null,
                false,
                "EDIT_COMPLETE"
        );
    }

    private VideoEditorResponse errorResponse(String message) {

        return new VideoEditorResponse(
                false,
                message,
                null,
                null,
                List.of(),
                null,
                null,
                null,
                false,
                "ERROR"
        );
    }
}