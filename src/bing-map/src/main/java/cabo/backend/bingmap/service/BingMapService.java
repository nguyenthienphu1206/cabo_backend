package cabo.backend.bingmap.service;

public interface BingMapService {

    String getAddress(double latitude, double longitude);

    Double calculateDistance(double latitude_1, double longitude_1, double latitude_2, double longitude_2);
}
