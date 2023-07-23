package cabo.backend.bingmap.service.impl;

import cabo.backend.bingmap.dto.BingMapsResponse;
import cabo.backend.bingmap.dto.ResourceSet;
import cabo.backend.bingmap.service.BingMapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class BingMapServiceImpl implements BingMapService {

    private final WebClient webClient;

    @Value("${bing.map.apiKey}")
    private String apiKey;

    public BingMapServiceImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public String getAddress(double latitude, double longitude) {

        String url = String.format("https://dev.virtualearth.net/REST/v1/Locations/%s,%s?key=%s",
                latitude, longitude, apiKey);

        BingMapsResponse response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(BingMapsResponse.class)
                .block();

        //log.info("Address ---> " + response);

        String address = null;

        if (response != null && response.getResourceSets() != null && response.getResourceSets().size() > 0) {
            ResourceSet resourceSet = response.getResourceSets().get(0);
            if (resourceSet.getResources() != null && resourceSet.getResources().size() > 0) {
                address = resourceSet.getResources().get(0).getName();
            }
        }

        log.info("Address ---> " + address);

        return address;
    }

    @Override
    public Double calculateDistance(double latitude_1, double longitude_1, double latitude_2, double longitude_2) {

        String BING_MAPS_API_URL = "https://dev.virtualearth.net/REST/v1/Routes/DistanceMatrix";

        String url = String.format("%s?origins=%f,%f&destinations=%f,%f&travelMode=driving&key=%s",
                BING_MAPS_API_URL, latitude_1, longitude_1, latitude_2, longitude_2, apiKey);

        BingMapsResponse response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(BingMapsResponse.class)
                .block();

        log.info("Response: " + response);

        if (response != null) {

            Double distance = response.getResourceSets().get(0)
                    .getResources().get(0)
                    .getResults().get(0)
                    .getTravelDistance();

            return distance;
        }

        return 0.0;
    }
}
