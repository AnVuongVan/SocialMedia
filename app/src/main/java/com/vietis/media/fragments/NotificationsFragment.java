package com.vietis.media.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vietis.media.R;
import com.vietis.media.adapter.AdapterNotification;
import com.vietis.media.model.ModelNotification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NotificationsFragment extends Fragment {
    private RecyclerView notificationRv;
    private FirebaseAuth firebaseAuth;
    private List<ModelNotification> notificationList;
    private AdapterNotification adapterNotification;

    public NotificationsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        notificationRv = view.findViewById(R.id.notificationRv);

        getAllNotifications();

        return view;
    }

    private void getAllNotifications() {
        notificationList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        ref.child(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid()).child("Notifications")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    notificationList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        ModelNotification model = dataSnapshot.getValue(ModelNotification.class);
                        notificationList.add(model);
                    }
                    adapterNotification = new AdapterNotification(getActivity(), notificationList);
                    notificationRv.setAdapter(adapterNotification);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
    }

}