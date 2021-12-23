package com.example.chatkma.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatkma.GroupChatActivity;
import com.example.chatkma.R;
import com.example.chatkma.models.ModelGroupChatList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterGroupChatList extends RecyclerView.Adapter<AdapterGroupChatList.HolderGroupChatList> {

    private Context context;
    private ArrayList<ModelGroupChatList> groupChatLists;

    public AdapterGroupChatList(Context context, ArrayList<ModelGroupChatList> groupChatLists) {
        this.context = context;
        this.groupChatLists = groupChatLists;
    }

    @NonNull
    @Override
    public HolderGroupChatList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_groupchats,parent,false);
        return new HolderGroupChatList(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChatList holder, int position) {
        //get data
        ModelGroupChatList model =groupChatLists.get(position);
        final String groupID = model.getGroupID();
        String groupIcon = model.getGroupIcon();
        String groupTitle = model.getGroupTitle();

        holder.nameTv.setText("");
        holder.timeTv.setText("");
        holder.messageTv.setText("");

        //load last message
        loadLastMessage(model, holder);

        //set data
        holder.groupTitleTv.setText(groupTitle);
        try {
            Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group).into(holder.groupIconIv);
        }
        catch (Exception e){
            holder.groupIconIv.setImageResource(R.drawable.ic_group);
        }
        //handle group click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            // open group chat
                Intent intent = new Intent(context, GroupChatActivity.class);
                intent.putExtra("groupID",groupID);
                context.startActivity(intent);
            }
        });


    }

    private void loadLastMessage(ModelGroupChatList model, HolderGroupChatList holder) {
        //get last message from group
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(model.getGroupID()).child("Messages").limitToLast(1)//get last item(message) from that child
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            //get data
                            String message =""+ds.child("message").getValue();
                            String timestamp=""+ds.child("timestamp").getValue();
                            String sender=""+ds.child("sender").getValue();
                            String messageType=""+ds.child("type").getValue();

                            //convert time
                            // chuyen timestamp qua dinh dang dd/mm/yyyy hh:mm am/pm
                            Calendar calendar = Calendar.getInstance(Locale.getDefault());
                            try {
                                calendar.setTimeInMillis(Long.parseLong(timestamp));
                            } catch (Exception e){
                                e.getStackTrace();
                            }
                            String dateTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

                            if (messageType.equals("image")){
                                holder.messageTv.setText("Gửi ảnh");
                            }
                            else {
                                holder.messageTv.setText(message);
                            }
                            holder.timeTv.setText(dateTime);
                            //get info of last sender of last message
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                            ref.orderByChild("uid").equalTo(sender)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot ds:dataSnapshot.getChildren()){
                                                String name=""+ds.child("Hoten").getValue();
                                                holder.nameTv.setText(name);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

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

    //view holder class
    class HolderGroupChatList extends RecyclerView.ViewHolder{

        //ui view
        private ImageView groupIconIv;
        private TextView groupTitleTv, nameTv, messageTv, timeTv;


        public HolderGroupChatList(@NonNull View itemView) {
            super(itemView);

            groupIconIv= itemView.findViewById(R.id.groupIconIv);
            groupTitleTv= itemView.findViewById(R.id.groupTitleTv);
            nameTv= itemView.findViewById(R.id.nameTv);
            messageTv= itemView.findViewById(R.id.messageTv);
            timeTv= itemView.findViewById(R.id.timeTv);

        }
    }
}
