package cabo.backend.bingmap.service.impl;

import cabo.backend.bingmap.dto.*;
import cabo.backend.bingmap.entity.CustomerAddress;
import cabo.backend.bingmap.service.BingMapService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class BingMapServiceImpl implements BingMapService {

    private final WebClient webClient;

    @Value("${bing_map_apiKey}")
    private String apiKey;

    private static final String COLLECTION_NAME_CUSTOMER_ADDRESSES = "customerAddresses";

    private final CollectionReference collectionRefAddress;

    private Firestore dbFirestore;

    public BingMapServiceImpl(WebClient webClient, Firestore firestore) {

        this.webClient = webClient;

        this.dbFirestore = firestore;

        collectionRefAddress = dbFirestore.collection(COLLECTION_NAME_CUSTOMER_ADDRESSES);
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
        String address = "";

        if (response != null && response.getResourceSets() != null && response.getResourceSets().size() > 0) {
            ResourceSet resourceSet = response.getResourceSets().get(0);
            if (resourceSet.getResources() != null && resourceSet.getResources().size() > 0) {
                address = resourceSet.getResources().get(0).getName();
            }
        }

        return address;
    }

    // Lấy danh sách địa chỉ bằng cách gọi api bing-map
    @Override
    public  List<ResponseListAddresses> getAddressByQuery(String query) {

        final  List<ResponseListAddresses> listAddresses = new ArrayList<>();

        String cityQuery = "Thành phố Hồ Chí Minh, Việt Nam";
        String url = String.format("http://dev.virtualearth.net/REST/v1/Locations?query=%s, %s&key=%s&culture=vi-VN", query, cityQuery, apiKey);

        String responseMono = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();


        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode responseJson = objectMapper.readTree(responseMono);

            JsonNode resourceSets = responseJson.get("resourceSets");
            if (resourceSets.isArray() && resourceSets.size() > 0) {
                JsonNode resources = resourceSets.get(0).get("resources");
                if (resources.isArray() && resources.size() > 0) {

                    for (JsonNode resource : resources) {
                        JsonNode addressNode = resource.get("address");

                        String addressLine = addressNode.path("addressLine").asText();
                        String district = addressNode.path("adminDistrict2").asText();
                        String city = addressNode.path("adminDistrict").asText();

                        String address = addressLine + ", " + district + ", " + city;


                        JsonNode point = resource.get("point");
                        double latitude = point.get("coordinates").get(0).asDouble();
                        double longitude = point.get("coordinates").get(1).asDouble();

                        if (!checkExistedAddress(address)) {
                            ResponseListAddresses responseListAddresses = ResponseListAddresses.builder()
                                    .address(address)
                                    .location(new GeoPoint(latitude, longitude))
                                    .build();

                            listAddresses.add(responseListAddresses);

                            collectionRefAddress.document().set(responseListAddresses);

                            log.info("FullAddress: " + address);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("Test");

        return listAddresses;
    }

    // Đầu tiên sẽ kiểm tra sẽ địa chỉ đó có tồn tại trong DB chưa
    // Tồn tại thì trả về, nếu chưa sẽ gọi api Bing-Map để lấy danh sách địa chỉ
    @Override
    public List<ResponseListAddresses> getRealPlacesBySearchQuery(String query) {

        List<ResponseListAddresses> listAddresses = new ArrayList<>();
        listAddresses = getListAddressFromDB(query);

        if (query.trim().length() != 0) {

            if (listAddresses.size() < 1) {
                // Using Bing map get list address
                listAddresses = getAddressByQuery(query);
            }
        }

        return listAddresses;
    }

    @Override
    public double calculateDistance(double latitude_1, double longitude_1, double latitude_2, double longitude_2) {

        String BING_MAPS_API_URL = "https://dev.virtualearth.net/REST/v1/Routes/DistanceMatrix";

        String url = String.format("%s?origins=%f,%f&destinations=%f,%f&travelMode=driving&key=%s",
                BING_MAPS_API_URL, latitude_1, longitude_1, latitude_2, longitude_2, apiKey);

        String response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("Response: " + response);

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode responseJson = objectMapper.readTree(response);

            JsonNode resourceSets = responseJson.get("resourceSets");
            if (resourceSets.isArray() && resourceSets.size() > 0) {
                JsonNode resources = resourceSets.get(0).get("resources");
                if (resources.isArray() && resources.size() > 0) {

                    for (JsonNode resource : resources) {
                        JsonNode results = resource.get("results");

                        return results.get(0).path("travelDistance").asDouble(0);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    @Override
    public TravelInfor getDistanceAndTime(double latitude_1, double longitude_1, double latitude_2, double longitude_2) {

        String BING_MAPS_API_URL = "https://dev.virtualearth.net/REST/v1/Routes/DistanceMatrix";

        String url = String.format("%s?origins=%f,%f&destinations=%f,%f&travelMode=driving&key=%s",
                BING_MAPS_API_URL, latitude_1, longitude_1, latitude_2, longitude_2, apiKey);

        String response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        TravelInfor travelInfor = new TravelInfor();

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode responseJson = objectMapper.readTree(response);

            JsonNode resourceSets = responseJson.get("resourceSets");
            if (resourceSets.isArray() && resourceSets.size() > 0) {
                JsonNode resources = resourceSets.get(0).get("resources");
                if (resources.isArray() && resources.size() > 0) {

                    for (JsonNode resource : resources) {
                        JsonNode results = resource.get("results");

                        double travelDistance = results.get(0).path("travelDistance").asDouble(0);
                        double travelDuration = results.get(0).path("travelDuration").asDouble(0);

                        travelInfor.setTravelDistance(travelDistance);
                        travelInfor.setTravelDuration(travelDuration);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return travelInfor;
    }

    @Override
    public ResponseEstimateCostAndDistance getEstimateCostAndDistance(RequestOriginsAndDestinationsLocation requestOriginsAndDestinationsLocation) {

        double fromLat = requestOriginsAndDestinationsLocation.getFromLocation().getLatitude();
        double fromLon = requestOriginsAndDestinationsLocation.getFromLocation().getLongitude();
        double toLat = requestOriginsAndDestinationsLocation.getToLocation().getLatitude();
        double toLon = requestOriginsAndDestinationsLocation.getToLocation().getLongitude();

        double distance = calculateDistance(fromLat, fromLon, toLat, toLon);

        double estimateCost = getEstimateCost(distance) * 1.0D;

        if (requestOriginsAndDestinationsLocation.getVehicleType().equals("VEHICLE_TYPE_CAR_6")) {
            estimateCost *= 1.5;
        }

        String estimateCostFormat = (int)convertDoubleToDoubleFormat(estimateCost, -3) + " VND";

        String distanceFormat;

        if (distance < 1) {
            distance *= 1000;
            distanceFormat = (int)convertDoubleToDoubleFormat(distance, 0) + " m";
        } else {
            distanceFormat = convertDoubleToDoubleFormat(distance, 1) + " km";
        }

        return ResponseEstimateCostAndDistance.builder()
                .cost(estimateCostFormat)
                .distance(distanceFormat)
                .build();
    }

    @Override
    public Boolean checkExistedAddress(String address) {

        ApiFuture<QuerySnapshot> future = collectionRefAddress.get();

        List<QueryDocumentSnapshot> documents = null;

        try {
            documents = future.get().getDocuments();

            for (QueryDocumentSnapshot document : documents) {

                String addressDB = document.getString("address");

                if (addressDB != null && addressDB.equalsIgnoreCase(address)) {
                    return true;
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return false;
    }


    private String processAddress(String address) {
        // Loại bỏ các từ không cần thiết và dấu câu
        address = address.replaceAll("[Đđ]ường", "");

        // Loại bỏ khoảng trắng thừa và dấu câu ở đầu và cuối chuỗi
        address = address.trim().replaceAll("[\\p{Punct}\\s]+", " ");

        return address;
    }

    private List<ResponseListAddresses> getListAddressFromDB(String query) {

        // Xử lí bỏ "Đường/đường"
        query = processAddress(query);
        log.info(query);

        // Bỏ dấu
        String normalizedQuery = Normalizer.normalize(query, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase();
        log.info("normalizedString: " + normalizedQuery);

        ApiFuture<QuerySnapshot> future = collectionRefAddress.get();

        List<QueryDocumentSnapshot> documents = null;

        List<ResponseListAddresses> listAddresses = new ArrayList<>();

        try {
            documents = future.get().getDocuments();

            for (QueryDocumentSnapshot document : documents) {

                String address = document.getString("address");
                GeoPoint location = document.getGeoPoint("location");

                if (address != null) {
                    String processedAddress = processAddress(address);

                    String normalizedAddress = Normalizer.normalize(processedAddress, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase();

                    if (normalizedAddress.contains(normalizedQuery)) {
                        ResponseListAddresses responseListAddresses = ResponseListAddresses.builder()
                                .address(address)
                                .location(location)
                                .build();
                        listAddresses.add(responseListAddresses);
                    }
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return listAddresses;
    }

    private Double getEstimateCost(Double distance) {

        double baseCostPerKm = 4300.0;

        double cost = 12500.0;

        if (distance > 2.0) {
            cost += baseCostPerKm * (distance - 2.0);
        }

        return cost;
    }

    private double convertDoubleToDoubleFormat(double value, int decimalPlaces) {

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(decimalPlaces, RoundingMode.HALF_UP);

        return bd.doubleValue();
    }
}
