package cabo.backend.bingmap.service;


import cabo.backend.bingmap.dto.RequestAddress;
import cabo.backend.bingmap.dto.ResponseListAddresses;
import com.google.cloud.firestore.GeoPoint;

import java.util.List;

public interface BingMapService {

    String getAddress(double latitude, double longitude);

    // Lấy danh sách bằng cách gọi api bing-map
    List<ResponseListAddresses> getAddressByQuery(String query);

    List<ResponseListAddresses> getRealPlacesBySearchQuery(String query);

    Double calculateDistance(double latitude_1, double longitude_1, double latitude_2, double longitude_2);

    Boolean checkExistedAddress(String address);
}
