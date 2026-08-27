package com.sha.service.skills;

import com.sha.brain.dto.OperationPrompt;
import com.sha.brain.prompt.SkillPrompt;
import com.sha.dto.request.ChatRequest;
import com.sha.dto.request.VideoEditorRequest;
import com.sha.dto.response.ChatResponse;
import com.sha.dto.response.VideoEditorResponse;
import com.sha.enums.SkillType;
import com.sha.enums.VideoEditorOperation;
import com.sha.service.Skill;
import com.sha.service.impl.AIRouter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Service
public class VideoEditorSkill implements Skill<VideoEditorRequest, VideoEditorResponse> {

    private final AIRouter aiRouter;

    @Value("${sha.video.ffmpeg-path}")
    private String ffmpegPath;

    @Value("${sha.video.ffprobe-path}")
    private String ffprobePath;

    @Value("${sha.video.tts-command:}")
    private String ttsCommand;

    public VideoEditorSkill(AIRouter aiRouter) {
        this.aiRouter = aiRouter;
    }

    @Override
    public SkillType getType() {
        return SkillType.VIDEO_EDITING;
    }

    @Override
    public VideoEditorResponse executeTyped(VideoEditorRequest request) {
        return switch (request.getOperation()) {
            case EDIT -> editVideo(request);
            case ANALYZE -> analyzeVideo(request);
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
    public SkillPrompt<VideoEditorOperation> describe() {
        return new SkillPrompt<>(
                SkillType.VIDEO_EDITING,
                "Analyze and automatically edit videos for YouTube and short-form content.",
                List.of(
                        "video",
                        "video editing",
                        "edit video",
                        "edit recording",
                        "video editor",
                        "youtube video",
                        "youtube short",
                        "short",
                        "reels",
                        "instagram reel"
                ),
                List.of(
                        new OperationPrompt<>(
                                VideoEditorOperation.ANALYZE,
                                "Analyze a video and determine useful editing opportunities.",
                                List.of("videoPath", "context"),
                                """
                                {
                                  "videoPath":"D:/Videos/demo.mp4",
                                  "context":"This video demonstrates Sha's Project Reader skill.",
                                  "operation":"ANALYZE"
                                }
                                """
                        ),
                        new OperationPrompt<>(
                                VideoEditorOperation.EDIT,
                                "Automatically edit a video based on the provided context and instructions.",
                                List.of("videoPath", "outputPath", "context", "instructions", "introPath", "generateNarration"),
                                """
                                {
                                  "videoPath":"D:/Videos/demo.mp4",
                                  "outputPath":"D:/Videos/demo-final.mp4",
                                  "context":"This video demonstrates Sha's Project Reader skill.",
                                  "instructions":"Remove unnecessary pauses, speed up slow sections, add an intro and generate narration.",
                                  "introPath":"D:/Videos/intro.mp4",
                                  "generateNarration":true,
                                  "operation":"EDIT"
                                }
                                """
                        )
                )
        );
    }

    public VideoEditorResponse editVideo(VideoEditorRequest request) {
        try {
            Path source = Path.of(request.getVideoPath());
            if (!Files.exists(source) || !Files.isRegularFile(source)) {
                return new VideoEditorResponse(
                        false,
                        "Video file not found: " + request.getVideoPath(),
                        null,
                        null,
                        List.of()
                );
            }

            String outputPath = resolveOutputPath(request);
            Path output = Path.of(outputPath);
            Path outputDir = output.getParent() != null ? output.getParent() : Path.of(".");

            List<String> applied = new ArrayList<>();
            String current = request.getVideoPath();

            if (has(request.getInstructions(), "speed up", "faster", "speedup")) {
                String tmp = tempFile(outputDir, "sha-speed-", ".mp4");
                runFfmpeg(List.of(
                        "-i", current,
                        "-vf", "setpts=0.5*PTS",
                        "-af", "atempo=2.0",
                        tmp
                ));
                current = tmp;
                applied.add("SPEED_UP");
            }

            if (has(request.getInstructions(), "remove pauses", "remove silence", "silence", "pauses")) {
                String tmp = tempFile(outputDir, "sha-silence-", ".mp4");
                runFfmpeg(List.of(
                        "-i", current,
                        "-af", "silenceremove=stop_periods=-1:stop_duration=0.5:stop_threshold=-40dB",
                        tmp
                ));
                current = tmp;
                applied.add("REMOVE_SILENCE");
            }

            if (request.getIntroPath() != null && !request.getIntroPath().isBlank()) {
                Path intro = Path.of(request.getIntroPath());
                if (Files.exists(intro) && Files.isRegularFile(intro)) {
                    String tmp = tempFile(outputDir, "sha-intro-", ".mp4");
                    runFfmpeg(List.of(
                            "-i", current,
                            "-i", intro.toString(),
                            "-filter_complex", "[1][0]concat=n=2:v=1:a=1",
                            "-c:v", "libx264",
                            "-c:a", "aac",
                            tmp
                    ));
                    current = tmp;
                    applied.add("ADD_INTRO");
                }
            }

            if (Boolean.TRUE.equals(request.getGenerateNarration())) {
                String script = generateNarrationScript(request.getContext(), request.getInstructions());
                applied.add("NARRATION_SCRIPT");
                String narrationAudio = tempFile(outputDir, "sha-narration-", ".mp3");
                boolean audioOk = generateNarrationAudio(script, narrationAudio);
                if (audioOk) {
                    String tmp = tempFile(outputDir, "sha-narrated-", ".mp4");
                    runFfmpeg(List.of(
                            "-i", current,
                            "-i", narrationAudio,
                            "-c:v", "copy",
                            "-map", "0:v:0",
                            "-map", "1:a:0",
                            "-shortest",
                            tmp
                    ));
                    current = tmp;
                    applied.add("ADD_NARRATION");
                } else {
                    applied.add("NARRATION_AUDIO_SKIPPED");
                }
            }

            if (!current.equals(outputPath)) {
                Files.move(Path.of(current), output, StandardCopyOption.REPLACE_EXISTING);
            } else if (applied.isEmpty()) {
                Files.copy(source, output, StandardCopyOption.REPLACE_EXISTING);
                applied.add("COPY");
            }

            double duration = probeDuration(outputPath);
            return new VideoEditorResponse(
                    true,
                    "Video edited: " + applied,
                    outputPath,
                    duration,
                    applied
            );

        } catch (Exception e) {
            return new VideoEditorResponse(
                    false,
                    "Error editing video: " + e.getMessage(),
                    null,
                    null,
                    List.of()
            );
        }
    }

    public VideoEditorResponse analyzeVideo(VideoEditorRequest request) {

        try {

            Process process = new ProcessBuilder(
                    ffprobePath,

                    "-v", "error",

                    "-show_entries",
                    "format=duration",

                    "-of", "default=noprint_wrappers=1:nokey=1",

                    request.getVideoPath()

            ).start();

            String output = new String(
                    process.getInputStream().readAllBytes()
            ).trim();

            int exitCode = process.waitFor();

            if (exitCode != 0) {

                String error = new String(
                        process.getErrorStream().readAllBytes()
                );

                return new VideoEditorResponse(
                        false,
                        "Failed to analyze video: " + error,
                        null,
                        null,
                        List.of()
                );
            }

            double duration = Double.parseDouble(output);

            return new VideoEditorResponse(
                    true,
                    "Video analyzed successfully.",
                    request.getVideoPath(),
                    duration,
                    List.of("ANALYZE")
            );

        } catch (Exception e) {

            return new VideoEditorResponse(
                    false,
                    "Error analyzing video: " + e.getMessage(),
                    null,
                    null,
                    List.of()
            );
        }
    }

    private String resolveOutputPath(VideoEditorRequest request) {
        String out = request.getOutputPath();
        if (out == null || out.isBlank() || out.equals(request.getVideoPath())) {
            return autoOutput(request.getVideoPath());
        }
        return out;
    }

    private String autoOutput(String videoPath) {
        Path src = Path.of(videoPath);
        String name = src.getFileName().toString();
        int dot = name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        String ext = dot > 0 ? name.substring(dot) : ".mp4";
        Path parent = src.getParent() != null ? src.getParent() : Path.of(".");
        return parent.resolve(base + "-edited" + ext).toString();
    }

    private String tempFile(Path dir, String prefix, String suffix) throws IOException {
        return Files.createTempFile(dir, prefix, suffix).toString();
    }

    private void runFfmpeg(List<String> args) throws IOException, InterruptedException {
        List<String> cmd = new ArrayList<>();
        cmd.add(ffmpegPath);
        cmd.addAll(args);
        Process process = new ProcessBuilder(cmd).start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            String error = new String(process.getErrorStream().readAllBytes());
            throw new RuntimeException("ffmpeg failed: " + error);
        }
    }

    private double probeDuration(String path) {
        try {
            Process process = new ProcessBuilder(
                    ffprobePath,
                    "-v", "error",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    path
            ).start();
            int exitCode = process.waitFor();
            if (exitCode != 0) return 0.0;
            String output = new String(process.getInputStream().readAllBytes()).trim();
            return output.isEmpty() ? 0.0 : Double.parseDouble(output);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private boolean has(String text, String... needles) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        for (String needle : needles) {
            if (lower.contains(needle.toLowerCase())) return true;
        }
        return false;
    }

    private String generateNarrationScript(String context, String instructions) {
        String prompt = "Write a short spoken narration script for a video.\n" +
                "Context: " + (context == null ? "" : context) + "\n" +
                "Editing instructions: " + (instructions == null ? "" : instructions) + "\n" +
                "Return only the narration text, no markdown, no headings.";
        ChatResponse response = aiRouter.geminiChat(new ChatRequest(prompt));
        return response.getResponse();
    }

    private boolean generateNarrationAudio(String script, String outPath) {
        if (ttsCommand == null || ttsCommand.isBlank()) {
            return false;
        }
        try {
            Path scriptFile = Files.createTempFile("sha-narration-", ".txt");
            Files.writeString(scriptFile, script);
            String command = ttsCommand
                    .replace("{textFile}", scriptFile.toString())
                    .replace("{output}", outPath);
            Process process = new ProcessBuilder("cmd", "/c", command).start();
            int exitCode = process.waitFor();
            Files.deleteIfExists(scriptFile);
            return exitCode == 0 && Files.exists(Path.of(outPath));
        } catch (Exception e) {
            return false;
        }
    }
}
