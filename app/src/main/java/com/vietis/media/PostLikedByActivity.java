package com.vietis.media;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vietis.media.adapter.AdapterUsers;
import com.vietis.media.model.ModelUsers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PostLikedByActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<ModelUsers> usersList;
    private AdapterUsers adapterUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_liked_by);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Post Liked By");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        recyclerView = findViewById(R.id.recyclerView);
        actionBar.setSubtitle(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getEmail());

        Intent intent = getIntent();
        String postId = intent.getStringExtra("postId");

        usersList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Likes");
        assert postId != null;
        ref.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String hisUid = dataSnapshot.getRef().getKey();
                    getUsers(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getUsers(String hisUid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            ModelUsers model = dataSnapshot.getValue(ModelUsers.class);
                            usersList.add(model);
                        }
                        adapterUsers = new AdapterUsers(getApplicationContext(), usersList);
                        recyclerView.setAdapter(adapterUsers);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}