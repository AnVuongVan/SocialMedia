package com.vietis.media.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.vietis.media.GroupChatsActivity;
import com.vietis.media.R;
import com.vietis.media.model.ModelGroupChatList;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterGroupChatList extends RecyclerView.Adapter<AdapterGroupChatList.MyHolder> {
    private Context context;
    private List<ModelGroupChatList> groupChatLists;

    public AdapterGroupChatList(Context context, List<ModelGroupChatList> groupChatLists) {
        this.context = context;
        this.groupChatLists = groupChatLists;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_group_chats_list, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        ModelGroupChatList model = groupChatLists.get(position);
        final String groupId = model.getGroupId();
        String groupIcon = model.getGroupIcon();
        String groupTitle = model.getGroupTitle();

        holder.nameTv.setText("");
        holder.timeTv.setText("");
        holder.messageTv.setText("");
        loadLastMessages(model, holder);

        holder.groupTitleTv.setText(groupTitle);
        try {
            Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_primary)
                    .into(holder.groupIconIv);
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, GroupChatsActivity.class);
                intent.putExtra("groupId", groupId);
                context.startActivity(intent);
            }
        });

    }

    private void loadLastMessages(ModelGroupChatList model, final MyHolder holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(model.getGroupId()).child("Messages").limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String message = (String) dataSnapshot.child("message").getValue();
                            String timestamp = (String) dataSnapshot.child("timestamp").getValue();
                            String sender = (String) dataSnapshot.child("sender").getValue();
                            String type = (String) dataSnapshot.child("type").getValue();

                            Calendar calendar = Calendar.getInstance(Locale.getDefault());
                            assert timestamp != null;
                            calendar.setTimeInMillis(Long.parseLong(timestamp));
                            String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                            assert type != null;
                            if (type.equals("image")) {
                                holder.messageTv.setText("Sent a photo");
                            } else {
                                holder.messageTv.setText(message);
                            }
                            holder.timeTv.setText(pTime);

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.orderByChild("uid").equalTo(sender)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot dataSnap : snapshot.getChildren()) {
                                                String name = (String) dataSnap.child("name").getValue();
                                                holder.nameTv.setText(name);
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
    public int getItemCount() {
        return groupChatLists.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder {
        private CircularImageView groupIconIv;
        private TextView groupTitleTv, nameTv, timeTv, messageTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            groupIconIv = itemView.findViewById(R.id.groupIconIv);
            groupTitleTv = itemView.findViewById(R.id.groupTitleTv);
            messageTv = itemView.findViewById(R.id.messageTv);
            nameTv = itemView.findViewById(R.id.nameTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }

}
