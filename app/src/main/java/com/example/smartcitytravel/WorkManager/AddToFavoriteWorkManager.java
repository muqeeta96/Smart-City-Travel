package com.example.smartcitytravel.WorkManager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.smartcitytravel.DataModel.Favorite;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddToFavoriteWorkManager extends Worker {
    private FirebaseFirestore db;

    public AddToFavoriteWorkManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public Result doWork() {
        String userId = getInputData().getString("userId");
        String placeId = getInputData().getString("placeId");

        Favorite favorite = new Favorite(userId, placeId);

        addToFavorite(favorite);

        return Result.success();
    }

    public void addToFavorite(Favorite favorite) {
        db.collection("favorite")
                .add(favorite)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if (documentReference != null) {

                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Add to favorites", Toast.LENGTH_SHORT).show();

                                }
                            });

                        }
                    }
                });
    }
}
