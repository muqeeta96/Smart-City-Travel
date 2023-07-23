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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class RemoveFromFavoriteWorkManager extends Worker {
    private FirebaseFirestore db;

    public RemoveFromFavoriteWorkManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public Result doWork() {
        String userId = getInputData().getString("userId");
        String placeId = getInputData().getString("placeId");

        getFavoritePlace(userId, placeId);

        return Result.success();
    }

    public void getFavoritePlace(String userId, String placeId) {
        db.collection("favorite")
                .whereEqualTo("userId", userId)
                .whereEqualTo("placeId", placeId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                Favorite favorite = querySnapshot.toObject(Favorite.class);
                                favorite.setFavoriteId(querySnapshot.getId());

                                removeFromFavorite(favorite);

                            }
                        }
                    }
                });

    }

    public void removeFromFavorite(Favorite favorite) {
        db.collection("favorite")
                .document(favorite.getFavoriteId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Remove from favorites", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                });
    }
}
