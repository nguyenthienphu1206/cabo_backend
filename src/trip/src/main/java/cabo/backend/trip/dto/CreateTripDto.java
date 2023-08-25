package cabo.backend.trip.dto;

import cabo.backend.trip.deserializer.DocumentReferenceDeserializer;
import cabo.backend.trip.entity.GeoPoint;
import cabo.backend.trip.serializer.DocumentReferenceSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.cloud.firestore.DocumentReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateTripDto {

    private long cost;

    private String customerId;

    private String driverId;

    private double distance;

    private long startTime;

    private long pickUpTime;

    private long endTime;

    private GeoPoint customerOrderLocation;

    private GeoPoint driverStartLocation;

    private GeoPoint toLocation;

    private int paymentType;
}
