package cabo.backend.callcenter.service.impl;

import cabo.backend.callcenter.dto.RequestBookADrive;
import cabo.backend.callcenter.dto.ResponseStatus;
import cabo.backend.callcenter.entity.FcmToken;
import cabo.backend.callcenter.service.CallCenterService;
import cabo.backend.callcenter.service.ReceiverServiceClient;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class CallCenterServiceImpl implements CallCenterService {

    private static final String COLLECTION_NAME_FCMTOKEN = "fcmTokens";

    private final CollectionReference collectionRefFcmToken;

    @Autowired
    private ReceiverServiceClient receiverServiceClient;

    public CallCenterServiceImpl(Firestore firestore) {

        this.collectionRefFcmToken = firestore.collection(COLLECTION_NAME_FCMTOKEN);
    }

    @Override
    public void subscribeNotification(String bearerToken, String fcmToken) {

        String idToken = bearerToken.substring(7);

//        FirebaseToken decodedToken = decodeToken(idToken);
//
//        String uid = decodedToken.getUid();

        FcmToken savedFcmToken = FcmToken.builder()
                .fcmToken(fcmToken)
                .isDriver(false)
                //.uid(uid)
                .build();

        collectionRefFcmToken.document().set(savedFcmToken);
    }

    @Override
    public ResponseStatus sendInfoCustomerFromCallCenter(String bearerToken, RequestBookADrive requestBookADrive) {

        String idToken = bearerToken.substring(7);

//        FirebaseToken decodedToken = decodeToken(idToken);
//
//        String uid = decodedToken.getUid();

        return receiverServiceClient.receiverAndBookDriverFromCallCenter(bearerToken, requestBookADrive);
    }

    private FirebaseToken decodeToken(String idToken) {

        FirebaseToken decodedToken;

        try {
            decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }

        return decodedToken;
    }
}
