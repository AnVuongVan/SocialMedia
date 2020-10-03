package com.vietis.media;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vietis.media.adapter.AdapterAddParticipant;
import com.vietis.media.model.ModelUsers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupParticipantsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ActionBar actionBar;
    private FirebaseAuth firebaseAuth;

    private List<ModelUsers> usersList;
    private AdapterAddParticipant adapterAddParticipant;
    private String groupId, myGroupRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_participants);

        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Add Participants");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        recyclerView = findViewById(R.id.usersRv);

        groupId = getIntent().getStringExtra("groupId");
        loadGroupInfo();

    }

    private void getAllUsers() {
        usersList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ModelUsers model = dataSnapshot.getValue(ModelUsers.class);
                    assert model != null;
                    if (!Objects.equals(firebaseAuth.getUid(), model.getUid())) {
                        usersList.add(model);
                    }
                }
                adapterAddParticipant = new AdapterAddParticipant(GroupParticipantsActivity.this, usersList, groupId, myGroupRole);
                recyclerView.setAdapter(adapterAddParticipant);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadGroupInfo() {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    final String groupTitle = (String) dataSnapshot.child("groupTitle").getValue();

                    reference.child(groupId).child("Participants").child(Objects.requireNonNull(firebaseAuth.getUid()))
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        myGroupRole = (String) snapshot.child("role").getValue();
                                        actionBar.setTitle(groupTitle);
                                        actionBar.setSubtitle(myGroupRole);
                                        getAllUsers();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
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