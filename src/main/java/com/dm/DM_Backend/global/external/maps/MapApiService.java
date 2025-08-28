package com.dm.DM_Backend.global.external.maps;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class MapApiService {

    private final RestTemplate restTemplate;

    @Value("AIzaSyDkwFYl8D4lp-CR2kc8l6hwB10y7lCz3fk")
    private String googleMapsApiKey;

    public MapApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 출발지와 도착지 주소를 기준으로 예상 소요 시간을 분(minute) 단위로 반환합니다.
     */
    public int getTravelTimeInMinutes(String startAddress, String endAddress, LocalDateTime arrivalTime) {


        // 구글 지도 Directions API의 URL을 만듭니다.
        URI uri =
        UriComponentsBuilder
                .fromUriString("https://maps.googleapis.com")
                .path("/maps/api/distancematrix/json")
                .queryParam("origins", startAddress)   // "37.5665,126.9780"
                .queryParam("destinations", endAddress) // "35.1796,129.0756"
                .queryParam("mode", "transit")
                .queryParam("arrival_time", arrivalTime.toEpochSecond(ZoneOffset.UTC))
                .queryParam("key", googleMapsApiKey)
                .queryParam("language", "ko")
                .build()
                .toUri();

        try {
            // API를 호출하고 응답을 DTO 객체
            // API를 호출하고 응답을 DTO 객체로 변환합니다.
            GoogleMapsDistanceMatrixResponse response = restTemplate.getForObject(uri, GoogleMapsDistanceMatrixResponse.class);

            // 받은 JSON 응답에서 'duration' (소요 시간) 값을 초(second) 단위로 파싱합니다.
            if (response != null && response.getRows() != null && !response.getRows().isEmpty()) {
                Row row = response.getRows().get(0);
                if (row.getElements() != null && !row.getElements().isEmpty()) {
                    Element element = row.getElements().get(0);
                    if (element.getDuration() != null) {
                        // 초 단위를 분 단위로 변환하여 반환
                        return (int) Math.ceil(element.getDuration().getValue() / 60.0);
                    }
                }
            }
        } catch (Exception e) {
            // API 호출 실패 시 에러 로그를 남기고 기본값을 반환하거나 예외 처리를 합니다.
            System.err.println("Google Maps API 호출 실패: " + e.getMessage());
            // 기본값으로 30분을 반환하도록 설정 (실패 시 대응)
            return 30;
        }

        // 경로를 찾지 못한 경우
        return 30; // 기본값
    }

    @Getter @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GoogleMapsDistanceMatrixResponse {
        private List<Row> rows;
    }

    @Getter @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Row {
        private List<Element> elements;
    }

    @Getter @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Element {
        private Duration duration;
    }

    @Getter @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Duration {
        private long value; // 초 단위
        private String text; // "45분"
    }
}