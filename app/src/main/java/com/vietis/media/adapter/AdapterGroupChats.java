package com.vietis.media.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.vietis.media.R;
import com.vietis.media.model.ModelGroupChat;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterGroupChats extends RecyclerView.Adapter<AdapterGroupChats.MyHolder> {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private List<ModelGroupChat> groupChatList;
    private FirebaseAuth firebaseAuth;

    public AdapterGroupChats(Context context, List<ModelGroupChat> groupChatList) {
        this.context = context;
        this.groupChatList = groupChatList;
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(context).inflate(R.layout.row_group_chat_right, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.row_group_chat_left, parent, false);
        }
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        ModelGroupChat model = groupChatList.get(position);
        String message = model.getMessage();
        String timestamp = model.getTimestamp();
        String type = model.getType();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        if (type.equals("text")) {
            holder.messageIv.setVisibility(View.GONE);
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageTv.setText(message);
        } else {
            holder.messageIv.setVisibility(View.VISIBLE);
            holder.messageTv.setVisibility(View.GONE);
            try {
                Picasso.get().load(message).placeholder(R.drawable.ic_image_black)
                        .into(holder.messageIv);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        holder.timeTv.setText(pTime);
        if (!model.getSender().equals(firebaseAuth.getUid())) {
            setUserProperty(model, holder);
        }

    }

    private void setUserProperty(ModelGroupChat model, final MyHolder holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(model.getSender())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String name = (String) dataSnapshot.child("name").getValue();
                            String image = (String) dataSnapshot.child("image").getValue();

                            holder.nameTv.setText(name);
                            try {
                                Picasso.get().load(image).placeholder(R.drawable.ic_default_img)
                                        .into(holder.profileIv);
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

    @Override
    public int getItemViewType(int position) {
        if (groupChatList.get(position).getSender().equals(firebaseAuth.getUid())) {
            return MSG_TYPE_RIGHT;
        }
        return MSG_TYPE_LEFT;
    }

    @Override
    public int getItemCount() {
        return groupChatList.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder {
        private TextView nameTv, messageTv, timeTv;
        private CircularImageView profileIv;
        private ImageView messageIv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.nameTv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            profileIv = itemView.findViewById(R.id.profileIv);
            messageIv = itemView.findViewById(R.id.messageIv);
        }
    }

}
