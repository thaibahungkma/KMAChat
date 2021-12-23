package com.example.chatkma.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatkma.R;
import com.example.chatkma.models.ModelGroupChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterGroupChat extends RecyclerView.Adapter<AdapterGroupChat.HolderGroupChat> {
    private static final int MSG_TYPE_LEFT=0;
    private static final int MSG_TYPE_RIGHT=1;

    private Context context;
    private ArrayList<ModelGroupChat> modelGroupChatList;

    private FirebaseAuth firebaseAuth;

    public AdapterGroupChat(Context context, ArrayList<ModelGroupChat> modelGroupChatList) {
        this.context = context;
        this.modelGroupChatList = modelGroupChatList;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderGroupChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        if (viewType==MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_right,parent,false);
            return new HolderGroupChat(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_left,parent,false);
            return new HolderGroupChat(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChat holder, int position) {
        //get data from model
        ModelGroupChat model = modelGroupChatList.get(position);
        String timestamp = model.getTimestamp();
        String message = model.getMessage();
        String senderUid= model.getSender();
        String messageType= model.getType();

        // chuyen timestamp qua dinh dang dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        try {
            calendar.setTimeInMillis(Long.parseLong(timestamp));
        } catch (Exception e){
            e.getStackTrace();
        }
        String dateTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();
        //set data
        if (messageType.equals("text")){
            //text message, hide image view, show messageTv
            holder.messageIv.setVisibility(View.GONE);
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageTv.setText(message);
        }
        else {
            //image message, hide messageTv, show image
            holder.messageIv.setVisibility(View.VISIBLE);
            holder.messageTv.setVisibility(View.GONE);
            try {
                Picasso.get().load(message).placeholder(R.drawable.ic_image).into(holder.messageIv);
            }
            catch (Exception e){
                holder.messageIv.setImageResource(R.drawable.ic_image);
            }
        }

        holder.timeTv.setText(dateTime);
        setUserName(model,holder);

    }

    private void setUserName(ModelGroupChat model, HolderGroupChat holder) {
        //get sender info from uid in model
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(model.getSender())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            String name= ""+ds.child("Hoten").getValue();
                            holder.nameTv.setText(name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return modelGroupChatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (modelGroupChatList.get(position).getSender().equals(firebaseAuth.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }
    }

    class HolderGroupChat extends RecyclerView.ViewHolder{
        private TextView nameTv, messageTv, timeTv;
        private ImageView messageIv;
        public HolderGroupChat(@NonNull View itemView) {
            super(itemView);
            nameTv= itemView.findViewById(R.id.nameTv);
            messageTv= itemView.findViewById(R.id.messageTv);
            timeTv= itemView.findViewById(R.id.timeTv);
            messageIv= itemView.findViewById(R.id.messageIv);

        }
    }

}
