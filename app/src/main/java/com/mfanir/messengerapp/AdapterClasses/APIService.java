package com.mfanir.messengerapp.AdapterClasses;

import com.mfanir.messengerapp.Notifications.MyResponse;
import com.mfanir.messengerapp.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAgKCce20:APA91bEprzWpgGSe8hIn5u3PQcwC6MBIvL95pH1rs4mB10Oz_ThlsGlN3vJ58Fv5wqp8nduBKfCdhKL4b70Jido14yfAoqYFAMDrL2AzzLXvj3XJ6UtQj8E0dL_22LJ6pbWTN5nI6A3b"
    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
