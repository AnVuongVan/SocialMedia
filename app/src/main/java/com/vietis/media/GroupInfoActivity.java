package com.vietis.media;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.vietis.media.adapter.AdapterAddParticipant;
import com.vietis.media.model.ModelUsers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class GroupInfoActivity extends AppCompatActivity {
    private String groupId;
    private String myGroupRole;

    private FirebaseAuth firebaseAuth;
    private ActionBar actionBar;

    private ImageView groupIconIv;
    private TextView descriptionTv, createdByTv, editGroupTv,
            leaveGroupTv, addParticipantTv, participantsTv;
    private RecyclerView recyclerView;

    private List<ModelUsers> usersList;
    private AdapterAddParticipant adapterAddParticipant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        mapper();
        firebaseAuth = FirebaseAuth.getInstance();
        groupId = getIntent().getStringExtra("groupId");

        loadGroupInfo();
        loadMyGroupRole();

        addParticipantTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupInfoActivity.this, GroupParticipantsActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        leaveGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dialogTitle, dialogDescription, positiveBtn;
                if (myGroupRole.equals("creator")) {
                    dialogTitle = "Remove Group";
                    dialogDescription = "Are you sure to want to remove this group permanently?";
                    positiveBtn = "Remove";
                } else {
                    dialogTitle = "Leave Group";
                    dialogDescription = "Are you sure to want to leave this group permanently?";
                    positiveBtn = "Leave";
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfoActivity.this);
                builder.setTitle(dialogTitle)
                        .setMessage(dialogDescription)
                        .setPositiveButton(positiveBtn, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (myGroupRole.equals("creator")) {
                                    removeGroup();
                                } else {
                                    leaveGroup();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
            }
        });

        editGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupInfoActivity.this, GroupUpdateActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

    }

    private void leaveGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(Objects.requireNonNull(firebaseAuth.getUid())).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(GroupInfoActivity.this, "Left group successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(GroupInfoActivity.this, DashboardActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupInfoActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(GroupInfoActivity.this, "Remove group successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(GroupInfoActivity.this, DashboardActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupInfoActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMyGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").orderByChild("uid")
                .equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            myGroupRole = (String) dataSnapshot.child("role").getValue();
                            actionBar.setSubtitle(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getEmail() + " (" + myGroupRole + ")");

                            switch (myGroupRole) {
                                case "participant":
                                    editGroupTv.setVisibility(View.GONE);
                                    addParticipantTv.setVisibility(View.GONE);
                                    break;
                                case "admin":
                                    editGroupTv.setVisibility(View.GONE);
                                    addParticipantTv.setVisibility(View.VISIBLE);
                                    break;
                                case "creator":
                                    editGroupTv.setVisibility(View.VISIBLE);
                                    addParticipantTv.setVisibility(View.VISIBLE);
                                    leaveGroupTv.setText("Remove Group");
                                    break;
                            }
                        }
                        loadParticipants();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadParticipants() {
        usersList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String uid = (String) dataSnapshot.child("uid").getValue();
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                    reference.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                ModelUsers model = dataSnapshot.getValue(ModelUsers.class);
                                usersList.add(model);
                            }
                            adapterAddParticipant = new AdapterAddParticipant(GroupInfoActivity.this, usersList, groupId, myGroupRole);
                            recyclerView.setAdapter(adapterAddParticipant);
                            participantsTv.setText("Participants (" + usersList.size() + ")");
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

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String title = (String) dataSnapshot.child("groupTitle").getValue();
                    String description = (String) dataSnapshot.child("groupDescription").getValue();
                    String icon = (String) dataSnapshot.child("groupIcon").getValue();
                    String createdBy = (String) dataSnapshot.child("createdBy").getValue();
                    String timestamp = (String) dataSnapshot.child("timestamp").getValue();

                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    assert timestamp != null;
                    calendar.setTimeInMillis(Long.parseLong(timestamp));
                    String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                    actionBar.setTitle(title);
                    descriptionTv.setText(description);
                    loadCreatorInfo(dateTime, createdBy);
                    try {
                        Picasso.get().load(icon).placeholder(R.drawable.ic_image_black)
                                .into(groupIconIv);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadCreatorInfo(final String dateTime, String createdBy) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(createdBy).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String name = (String) dataSnapshot.child("name").getValue();
                    createdByTv.setText("Created by " + name + " on " + dateTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void mapper() {
        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        groupIconIv = findViewById(R.id.groupIconIv);
        descriptionTv = findViewById(R.id.descriptionTv);
        createdByTv = findViewById(R.id.createdByTv);
        editGroupTv = findViewById(R.id.editGroupTv);
        leaveGroupTv = findViewById(R.id.leaveGroupTv);
        addParticipantTv = findViewById(R.id.addParticipantTv);
        participantsTv = findViewById(R.id.participantsTv);
        recyclerView = findViewById(R.id.participantsRv);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}