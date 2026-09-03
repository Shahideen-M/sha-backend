package com.sha.service.skills;

import com.sha.brain.dto.OperationPrompt;
import com.sha.enums.IslamicOperation;
import tools.jackson.databind.JsonNode;
import com.sha.brain.prompt.SkillPrompt;
import com.sha.dto.request.AppLauncherRequest;
import com.sha.dto.request.FileRequest;
import com.sha.dto.request.IslamicRequest;
import com.sha.dto.response.AppLauncherResponse;
import com.sha.dto.response.FileResponse;
import com.sha.dto.response.IslamicResponse;
import com.sha.dto.response.PrayerTimes;
import com.sha.enums.FileOperation;
import com.sha.enums.LaunchOperation;
import com.sha.brain.enums.SkillType;
import com.sha.service.Skill;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class IslamicSkill implements Skill<IslamicRequest, IslamicResponse> {

    private final FileSkill fileSkill;
    private final AppLauncherSkill appLauncherSkill;
    private final RestClient restClient;

    public IslamicSkill(FileSkill fileSkill, AppLauncherSkill appLauncherSkill, RestClient restClient) {
        this.fileSkill = fileSkill;
        this.appLauncherSkill = appLauncherSkill;
        this.restClient = restClient;
    }

    @Override
    public SkillType getType() {
        return SkillType.ISLAM;
    }

    @Override
    public IslamicResponse executeTyped(IslamicRequest request) {
        return switch (request.getOperation()) {
            case PRAYER_TIMES -> prayerTimes(request);
            case GET_PRAYER -> getPrayer(request);
            case GET_SURAH -> getSurah(request);
            case PLAY_SURAH -> playSurah(request);
            case SURAH_LIST -> surahList();
        };
    }

    @Override
    public Class<IslamicRequest> getRequestClass() {
        return IslamicRequest.class;
    }

    @Override
    public IslamicResponse execute(Object request) {
        return executeTyped((IslamicRequest) request);
    }

    @Override
    public SkillPrompt<IslamicOperation> describe() {
        return new SkillPrompt<>(
                SkillType.ISLAM,
                "Provides Islamic utilities such as prayer times and Quran Surah management.",
                List.of(
                        "islam",
                        "islamic",
                        "prayer",
                        "salah",
                        "namaz",
                        "fajr",
                        "dhuhr",
                        "asr",
                        "maghrib",
                        "isha",
                        "quran",
                        "surah"
                ),
                List.of(
                        new OperationPrompt<>(
                                IslamicOperation.PRAYER_TIMES,
                                "Get all prayer times for a city and country.",
                                List.of("city", "country"),
                                """
                                {
                                  "operation":"PRAYER_TIMES",
                                  "city":"London",
                                  "country":"United Kingdom"
                                }
                                """
                        ),
                        new OperationPrompt<>(
                                IslamicOperation.GET_PRAYER,
                                "Get the time of a specific prayer.",
                                List.of("city", "country", "prayer"),
                                """
                                {
                                  "operation":"GET_PRAYER",
                                  "city":"London",
                                  "country":"United Kingdom",
                                  "prayer":"fajr"
                                }
                                """
                        ),
                        new OperationPrompt<>(
                                IslamicOperation.GET_SURAH,
                                "Search for a Quran Surah in the local Surah folder.",
                                List.of("surah"),
                                """
                                {
                                  "operation":"GET_SURAH",
                                  "surah":"Al-Fatihah"
                                }
                                """
                        ),
                        new OperationPrompt<>(
                                IslamicOperation.PLAY_SURAH,
                                "Find and play a Quran Surah from the local Surah folder.",
                                List.of("surah"),
                                """
                                {
                                  "operation":"PLAY_SURAH",
                                  "surah":"Al-Fatihah"
                                }
                                """
                        ),
                        new OperationPrompt<>(
                                IslamicOperation.SURAH_LIST,
                                "List all Quran Surahs available in the local Surah folder.",
                                List.of(),
                                """
                                {
                                  "operation":"SURAH_LIST"
                                }
                                """
                        )
                )
        );
    }

    public IslamicResponse prayerTimes(IslamicRequest request) {

        PrayerTimes times = getPrayerTimes(request);

        return new IslamicResponse(
                true,
                "Prayer times fetched successfully.",
                times
        );
    }

    public IslamicResponse getPrayer(IslamicRequest request) {
        PrayerTimes times = getPrayerTimes(request);

        String time = switch (request.getPrayer().toLowerCase()) {
            case "fajr" -> times.getFajr();
            case "sunrise" -> times.getSunrise();
            case "dhuhr" -> times.getDhuhr();
            case "asr" -> times.getAsr();
            case "maghrib" -> times.getMaghrib();
            case "isha" -> times.getIsha();
            default -> throw new RuntimeException(
                    "Unknown prayer: " + request.getPrayer()
            );
        };
        return new IslamicResponse(
                true,
                request.getPrayer() + " time is " + time,
                time
            );
    }

    public IslamicResponse getSurah(IslamicRequest request) {
        FileRequest fileRequest = new FileRequest();
        fileRequest.setPath("D:\\Downloads\\Music\\Surah");
        fileRequest.setSearchKeyword(request.getSurah());
        fileRequest.setOperation(FileOperation.SEARCH);

        FileResponse response = fileSkill.search(fileRequest);
        boolean found = response.isSuccess() && !response.getFiles().isEmpty();

        return new IslamicResponse(
                found,
                response.getFiles().isEmpty() ? "Surah not found" : "Surah found.",
                response.getFiles()
        );
    }

    public IslamicResponse playSurah(IslamicRequest request) {

        IslamicResponse surah = getSurah(request);

        if (!surah.isSuccess()) {
            return new IslamicResponse(
                    false,
                    "Surah not found: "+ request.getSurah(),
                    null
            );
        }
        List<String> files = (List<String>) surah.getData();

        String fileName = files.get(0);
        AppLauncherRequest appRequest = new AppLauncherRequest();
        appRequest.setPath("D:\\Downloads\\Music\\Surah\\" + fileName);
        appRequest.setOperation(LaunchOperation.OPEN_FILE);

        AppLauncherResponse response = appLauncherSkill.openFile(appRequest);

        return new IslamicResponse(
                response.isSuccess(),
                "Surah is playing",
                null
        );
    }

    public IslamicResponse surahList() {
        FileRequest request = new FileRequest();
        request.setPath("D:\\Downloads\\Music\\Surah");
        request.setOperation(FileOperation.LIST);
        FileResponse response = fileSkill.list(request);

        return new IslamicResponse(
                response.isSuccess(),
                "List of surahs:",
                response.getFiles()
        );
    }

    private PrayerTimes getPrayerTimes(IslamicRequest request) {

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        JsonNode response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/timingsByCity/{date}")
                        .queryParam("city", request.getCity())
                        .queryParam("country", request.getCountry())
                        .build(date)
                )
                .retrieve()
                .body(JsonNode.class);

        JsonNode timings = response
                .get("data")
                .get("timings");

        return new PrayerTimes(
                timings.get("Fajr").asString(),
                timings.get("Sunrise").asString(),
                timings.get("Dhuhr").asString(),
                timings.get("Asr").asString(),
                timings.get("Maghrib").asString(),
                timings.get("Isha").asString());
    }
}
