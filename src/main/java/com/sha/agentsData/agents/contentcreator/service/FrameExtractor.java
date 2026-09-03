package com.sha.agentsData.agents.contentcreator.service;

import com.sha.agentsData.agents.contentcreator.dto.ExtractedFrame;
import com.sha.agentsData.agents.contentcreator.dto.VideoTimeline;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FrameExtractor {

    @Value("${sha.video.ffmpeg-path}")
    private String ffmpegPath;

    @Value("${sha.video.ffprobe-path}")
    private String ffprobePath;

    public VideoTimeline extractFrames(String videoPath, double fps) {
        validateVideo(videoPath);

        if (fps <= 0) {
            throw new IllegalArgumentException("FPS must be greater than 0");
        }

        try {
            Path outputDir = Files.createTempDirectory("sha-frames-");
            String framePattern =
                    outputDir.resolve("frame_%06d.png").toString();

            List<String> command = List.of(
                    ffmpegPath,
                    "-y",
                    "-i", videoPath,
                    "-vf", "fps=" + fps,
                    framePattern
            );

            runProcess(command, "FFmpeg frame extraction failed");

            List<ExtractedFrame> frames = new ArrayList<>();

            try (var stream = Files.list(outputDir)) {
                stream.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName()
                                .toString()
                                .toLowerCase()
                                .endsWith(".png"))
                        .sorted(Comparator.comparing(
                                path -> path.getFileName().toString()
                        ))
                        .forEach(path -> {

                            String name =
                                    path.getFileName().toString();

                            String number =
                                    name.replaceAll("\\D+", "");

                            if (!number.isBlank()) {

                                int frameNumber =
                                        Integer.parseInt(number);

                                double timestamp =
                                        (frameNumber - 1) / fps;

                                frames.add(new ExtractedFrame(
                                        timestamp,
                                        path.toString()
                                ));
                            }
                        });
            }

            Map<String, String> metadata =
                    extractMetadata(videoPath);

            double duration = parseDouble(
                    metadata.get("duration")
            );

            return new VideoTimeline(
                    duration,
                    videoPath,
                    frames,
                    metadata
            );

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to extract frames: " + e.getMessage(),
                    e
            );
        }
    }

    public List<ExtractedFrame> deduplicateFrames(
            List<ExtractedFrame> frames,
            double threshold
    ) {

        if (frames == null || frames.isEmpty()) {
            return List.of();
        }

        if (threshold < 0 || threshold > 1) {
            throw new IllegalArgumentException(
                    "Threshold must be between 0 and 1"
            );
        }

        List<ExtractedFrame> unique =
                new ArrayList<>();

        unique.add(frames.get(0));

        for (int i = 1; i < frames.size(); i++) {

            ExtractedFrame current = frames.get(i);

            ExtractedFrame previous =
                    unique.get(unique.size() - 1);

            if (!areFramesSimilar(
                    previous.getFilePath(),
                    current.getFilePath(),
                    threshold
            )) {

                unique.add(current);
            }
        }

        return unique;
    }

    /*
     * This is only a lightweight binary comparison.
     *
     * It does NOT perform true visual image comparison.
     * Real visual similarity can be added later when
     * Sha gets image-processing or vision support.
     */
    private boolean areFramesSimilar(
            String path1,
            String path2,
            double threshold
    ) {

        try {

            byte[] bytes1 =
                    Files.readAllBytes(Path.of(path1));

            byte[] bytes2 =
                    Files.readAllBytes(Path.of(path2));

            int maxLength =
                    Math.max(bytes1.length, bytes2.length);

            if (maxLength == 0) {
                return true;
            }

            int sampleCount =
                    Math.min(maxLength, 10_000);

            int differences = 0;

            for (int i = 0; i < sampleCount; i++) {

                int index1 =
                        i * bytes1.length / sampleCount;

                int index2 =
                        i * bytes2.length / sampleCount;

                if (bytes1[index1] != bytes2[index2]) {
                    differences++;
                }
            }

            double similarity =
                    1.0 - ((double) differences / sampleCount);

            return similarity >= threshold;

        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, String> extractMetadata(
            String videoPath
    ) {

        Map<String, String> metadata =
                new HashMap<>();

        try {

            List<String> command = List.of(
                    ffprobePath,
                    "-v", "error",
                    "-show_entries",
                    "format=duration:"
                            + "stream=width,height,codec_name,codec_type",
                    "-of",
                    "default=noprint_wrappers=1",
                    videoPath
            );

            Process process =
                    new ProcessBuilder(command).start();

            String output;

            try (var input =
                         process.getInputStream()) {

                output = new String(
                        input.readAllBytes(),
                        StandardCharsets.UTF_8
                );
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                return metadata;
            }

            String width = null;
            String height = null;

            for (String line : output.split("\\R")) {

                int separator = line.indexOf('=');

                if (separator <= 0) {
                    continue;
                }

                String key =
                        line.substring(0, separator);

                String value =
                        line.substring(separator + 1).trim();

                switch (key) {

                    case "duration" ->
                            metadata.put(
                                    "duration",
                                    value
                            );

                    case "width" -> width = value;

                    case "height" -> height = value;

                    case "codec_name" ->
                            metadata.putIfAbsent(
                                    "codec",
                                    value
                            );

                    case "codec_type" ->
                            metadata.putIfAbsent(
                                    "codecType",
                                    value
                            );
                }
            }

            if (width != null && height != null) {

                metadata.put(
                        "resolution",
                        width + "x" + height
                );
            }

        } catch (Exception ignored) {

        }

        metadata.putIfAbsent("duration", "0");

        return metadata;
    }

    private void validateVideo(String videoPath) {

        if (videoPath == null || videoPath.isBlank()) {
            throw new IllegalArgumentException(
                    "Video path is required"
            );
        }

        Path source = Path.of(videoPath);

        if (!Files.exists(source)
                || !Files.isRegularFile(source)) {

            throw new IllegalArgumentException(
                    "Video file not found: " + videoPath
            );
        }
    }

    private void runProcess(
            List<String> command,
            String errorPrefix
    ) {

        try {

            Process process =
                    new ProcessBuilder(command).start();

            int exitCode =
                    process.waitFor();

            if (exitCode != 0) {

                String error;

                try (var errorStream =
                             process.getErrorStream()) {

                    error = new String(
                            errorStream.readAllBytes(),
                            StandardCharsets.UTF_8
                    );
                }

                throw new RuntimeException(
                        errorPrefix + ": " + error
                );
            }

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    errorPrefix,
                    e
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    errorPrefix,
                    e
            );
        }
    }

    private double parseDouble(String value) {

        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }
}