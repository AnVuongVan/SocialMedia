package com.vietis.media.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.vietis.media.R;
import com.vietis.media.model.ModelChat;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private List<ModelChat> chatList;
    private String imageUrl;

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
            return new MyHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
            return new MyHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {
        String message = chatList.get(position).getMessage();
        String timeStamp = chatList.get(position).getTimestamp();
        String type = chatList.get(position).getType();

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        try {
            calendar.setTimeInMillis(Long.parseLong(timeStamp));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        if (type.equals("text")) {
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageIv.setVisibility(View.GONE);
            holder.messageTv.setText(message);
        } else {
            holder.messageTv.setVisibility(View.GONE);
            holder.messageIv.setVisibility(View.VISIBLE);
            Picasso.get().load(message).placeholder(R.drawable.ic_image_black)
                    .into(holder.messageIv);
        }

        holder.timeTv.setText(dateTime);
        try {
            Picasso.get().load(imageUrl)
                    .placeholder(R.drawable.ic_default_img).into(holder.profileIv);
        } catch (Exception e) {
            Picasso.get().load(R.drawable.ic_default_img).into(holder.profileIv);
        }

        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Remove");
                builder.setMessage("Are you sure to remove this message?");

                builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteMessage(position);
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
            }
        });

        if (position == (chatList.size() - 1)) {
            if (chatList.get(position).isChecked()) {
                holder.isSeenTv.setText("seen");
            } else {
                holder.isSeenTv.setText("delivered");
            }
        } else {
            holder.isSeenTv.setVisibility(View.GONE);
        }
    }

    private void deleteMessage(int i) {
        final String myUID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        String msgTimeStamp = chatList.get(i).getTimestamp();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (Objects.equals(dataSnapshot.child("sender").getValue(), myUID)) {
                        //dataSnapshot.getRef().removeValue();
                        Map<String, Object> hashMap = new HashMap<>();
                        hashMap.put("message", "This message was removed");
                        dataSnapshot.getRef().updateChildren(hashMap);
                        Toast.makeText(context, "Message removed", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "You must delete only your message", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        // Get currently signed in user
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        assert fUser != null;
        if (chatList.get(position).getSender().equals(fUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    static class MyHolder extends RecyclerView.ViewHolder {
        private ImageView profileIv, messageIv;
        private TextView messageTv, timeTv, isSeenTv;
        private LinearLayout messageLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            profileIv = itemView.findViewById(R.id.profile_row_chat);
            messageIv = itemView.findViewById(R.id.messageIv_row_chat);
            messageTv = itemView.findViewById(R.id.message_row_chat);
            timeTv = itemView.findViewById(R.id.time_row_chat);
            isSeenTv = itemView.findViewById(R.id.seen_row_chat);
            messageLayout = itemView.findViewById(R.id.message_layout_row_chat);
        }
    }

}
