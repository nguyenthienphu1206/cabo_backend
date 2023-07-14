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

        if (response != null && response.getResourceSets() != null && response.getResourceSets().length > 0) {
            ResourceSet resourceSet = response.getResourceSets()[0];
            if (resourceSet.getResources() != null && resourceSet.getResources().length > 0) {
                address = resourceSet.getResources()[0].getName();
            }
        }

        log.info("Address ---> " + address);

        return address;
    }
}
