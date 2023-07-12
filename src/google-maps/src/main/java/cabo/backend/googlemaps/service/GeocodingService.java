package cabo.backend.googlemaps.service;

public interface GeocodingService {

    String getAddress(double latitude, double longitude);
}
