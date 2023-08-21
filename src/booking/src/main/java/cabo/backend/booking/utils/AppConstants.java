package cabo.backend.booking.utils;

public class AppConstants {

    public enum StatusDriver {
        ONLINE,
        OFFLINE,
        BUSY
    }

    public enum StatusTrip {
        TRIP_STATUS_SEARCHING,
        TRIP_STATUS_PICKING,
        TRIP_STATUS_INPROGRESS,
        TRIP_STATUS_DONE,
        TRIP_STATUS_CLOSE,
        TRIP_STATUS_NO_DRIVER
    }
}
