package cabo.backend.googlemaps.service.impl;

import cabo.backend.googlemaps.service.GeocodingService;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class GeocodingServiceImpl implements GeocodingService {

    private final GeoApiContext geoApiContext;

    public GeocodingServiceImpl(GeoApiContext geoApiContext) {
        this.geoApiContext = geoApiContext;
    }

    @Override
    public String getAddress(double latitude, double longitude) {

        //GeocodingResult[] results = new GeocodingResult[0];
        GeocodingResult[] results;
        try {
            results = GeocodingApi.reverseGeocode(geoApiContext,
                    new LatLng(latitude, longitude)).await();
        } catch (ApiException | InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
//        try {
//            log.info("Test");
//
////            results = GeocodingApi.newRequest(geoApiContext)
////                    .latlng(new LatLng(latitude, longitude))
////                    .await();
//            log.info("Test1");
//        } catch (ApiException | InterruptedException | IOException e) {
//            throw new RuntimeException(e);
//        }

        if (results != null && results.length > 0) {
            log.info("Test2");
            return results[0].formattedAddress;
        }

        return null;
    }
}
