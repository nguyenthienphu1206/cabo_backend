package cabo.backend.driver.service.impl;

import cabo.backend.driver.entity.Attendance;
import cabo.backend.driver.entity.Driver;
import cabo.backend.driver.utils.StatusDriver;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class BackgroundService {

    private static final String COLLECTION_NAME_DRIVER = "drivers";

    private static final String COLLECTION_NAME_ATTENDANCE = "attendance";

    public BackgroundService() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }

    @Scheduled(cron = "0 20 11 * * *", zone = "Asia/Ho_Chi_Minh")
    public void checkOutAtSchedule() {

        log.info("Test");

        Firestore dbFirestore = FirestoreClient.getFirestore();

        CollectionReference collectionReferenceDriver = dbFirestore.collection(COLLECTION_NAME_DRIVER);

        CollectionReference collectionReferenceAttendance = dbFirestore.collection(COLLECTION_NAME_ATTENDANCE);

        // Lấy tất cả các tài liệu trong collection
        ApiFuture<QuerySnapshot> future = collectionReferenceAttendance.get();

        // Set time
        Instant now = Instant.now();
        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDateTime currentTime = LocalDateTime.ofInstant(now, zoneId);
        LocalTime lastTime = LocalTime.of(11, 20);

        log.info("zoneId: " + zoneId);
        log.info("currentTime.toLocalTime(): " + currentTime.toLocalTime());
        log.info("lastTime: " + lastTime);

        if (currentTime.toLocalTime().compareTo(lastTime) > 0) {

            try {
                List<QueryDocumentSnapshot> documents = future.get().getDocuments();

                for (QueryDocumentSnapshot document : documents) {

                    Object checkOutAt = document.get("checkOutAt");

                    if (checkOutAt == null) {

                        Attendance attendance = document.toObject(Attendance.class);

                        setCheckOutAtOfAttendance(document, attendance, collectionReferenceAttendance);

                        setWorkingOfDriver(collectionReferenceDriver, attendance.getDriverId());
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void setWorkingOfDriver(CollectionReference collectionReferenceDriver, String driverId) {

        // Set isWorking = false
        DocumentReference documentReferenceDriver = collectionReferenceDriver.document(driverId);

        ApiFuture<DocumentSnapshot> future = documentReferenceDriver.get();

        try {
            DocumentSnapshot document = future.get();

            Driver driver = null;

            if (document.exists()) {
                driver = document.toObject(Driver.class);

                if (driver != null) {
                    driver.setDriverStatus(StatusDriver.OFFLINE.name());

                    ApiFuture<WriteResult> writeResult = documentReferenceDriver.set(driver);

                    log.info("Successfully");
                }

            }

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void setCheckOutAtOfAttendance(QueryDocumentSnapshot document, Attendance attendance, CollectionReference collectionReferenceAttendance) {

        attendance.setCheckOutAt(1689699600L);

        DocumentReference documentReferenceAttendance = collectionReferenceAttendance.document(document.getId());

        ApiFuture<WriteResult> writeResult = documentReferenceAttendance.set(attendance);
    }
}
