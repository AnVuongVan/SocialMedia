package com.vietis.media.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vietis.media.GroupCreateActivity;
import com.vietis.media.MainActivity;
import com.vietis.media.R;
import com.vietis.media.SettingsActivity;
import com.vietis.media.adapter.AdapterChatList;
import com.vietis.media.model.ModelChat;
import com.vietis.media.model.ModelChatList;
import com.vietis.media.model.ModelUsers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatListFragment extends Fragment {
    private List<ModelChatList> chatLists;
    private List<ModelUsers> usersList;
    private AdapterChatList adapterChatList;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference reference;
    private FirebaseUser currentUser;
    private RecyclerView recyclerView;

    public ChatListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("ChatList").child(currentUser.getUid());

        recyclerView = view.findViewById(R.id.recyclerView);
        chatLists = new ArrayList<>();

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatLists.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ModelChatList chatList = dataSnapshot.getValue(ModelChatList.class);
                    chatLists.add(chatList);
                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }

    private void loadChats() {
        usersList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ModelUsers users = dataSnapshot.getValue(ModelUsers.class);
                    for (ModelChatList chatList : chatLists) {
                        assert users != null;
                        if (users.getUid() != null && users.getUid().equals(chatList.getId())) {
                            usersList.add(users);
                            break;
                        }
                    }
                    adapterChatList = new AdapterChatList(getContext(), usersList);
                    recyclerView.setAdapter(adapterChatList);

                    for (int i = 0; i < usersList.size(); i++) {
                        lastMessages(usersList.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void lastMessages(final String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String theLastMessage = "default";
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ModelChat chat = dataSnapshot.getValue(ModelChat.class);
                    if (chat == null) {
                        continue;
                    }
                    String sender = chat.getSender();
                    String receiver = chat.getReceiver();
                    if (sender == null || receiver == null) {
                        continue;
                    }

                    if (receiver.equals(currentUser.getUid()) && sender.equals(userId)
                            || receiver.equals(userId) && sender.equals(currentUser.getUid())) {
                        if (chat.getType().equals("image")) {
                            theLastMessage = "Sent a photo";
                        } else {
                            theLastMessage = chat.getMessage();
                        }
                    }
                }
                adapterChatList.setLastMessageMap(userId, theLastMessage);
                adapterChatList.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); // to show menu option in fragment
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_group_info).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                firebaseAuth.signOut();
                checkUserStatus();
                break;
            case R.id.action_settings:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                break;
            case R.id.action_create_group:
                startActivity(new Intent(getActivity(), GroupCreateActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(getActivity(), MainActivity.class));
            Objects.requireNonNull(getActivity()).finish();
        }
    }

}