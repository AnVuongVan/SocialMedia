package com.vietis.media.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.vietis.media.ChatActivity;
import com.vietis.media.R;
import com.vietis.media.model.ModelUsers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.MyHolder> {
    private Context context;
    private List<ModelUsers> userList;
    private Map<String, String> lastMessageMap;

    public AdapterChatList(Context context, List<ModelUsers> userList) {
        this.context = context;
        this.userList = userList;
        this.lastMessageMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_chat_list, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        final String hisUid = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String userName = userList.get(position).getName();
        String lastMessage = lastMessageMap.get(hisUid);

        holder.nameTv.setText(userName);
        if (lastMessage == null || lastMessage.equals("dafault")) {
            holder.lastMessageTv.setVisibility(View.GONE);
        } else {
            holder.lastMessageTv.setVisibility(View.VISIBLE);
            holder.lastMessageTv.setText(lastMessage);
        }

        try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_img)
                    .into(holder.profileIv);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (userList.get(position).getOnlineStatus().equals("online")) {
            holder.onlineStatusIv.setImageResource(R.drawable.circle_online_status);
        } else {
            holder.onlineStatusIv.setImageResource(R.drawable.circle_offline_status);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                ref.child(hisUid).child("BlockedUsers").orderByChild("uid")
                        .equalTo(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    Toast.makeText(context, "You're blocked by this user, can't send message", Toast.LENGTH_SHORT).show();
                                } else {
                                    Intent intent = new Intent(context, ChatActivity.class);
                                    intent.putExtra("hisUid", hisUid);
                                    context.startActivity(intent);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        });

    }

    public void setLastMessageMap(String userId, String lastMessages) {
        lastMessageMap.put(userId, lastMessages);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder {
        private CircularImageView profileIv;
        private ImageView onlineStatusIv;
        private TextView nameTv, lastMessageTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profileIv = itemView.findViewById(R.id.profileIv);
            onlineStatusIv = itemView.findViewById(R.id.onlineStatusIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            lastMessageTv = itemView.findViewById(R.id.lastMessageTv);
        }
    }

}
