package com.example.chatkma.adapters;

import android.app.AlertDialog;
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
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatkma.R;
import com.example.chatkma.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    Context context;
    List<ModelChat> chatList;
    String imageUrl;
    FirebaseUser fUser;

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate layout: row_chat_left.xml de nhan tin nhan, row_chat_right de gui tin nhan
        if (i==MSG_TYPE_RIGHT){
            View view= LayoutInflater.from(context).inflate(R.layout.row_chat_right, viewGroup, false);
            return new MyHolder(view);
        }
        else {
            View view= LayoutInflater.from(context).inflate(R.layout.row_chat_left, viewGroup, false);
            return new MyHolder(view);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, final int i) {
        //get data
        String message = chatList.get(i).getMessage();
        String timeStamp = chatList.get(i).getTimeStamp();
        String type = chatList.get(i).getType();
        // chuyen timestamp qua dinh dang dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        try {
            calendar.setTimeInMillis(Long.parseLong(timeStamp));
        } catch (Exception e){
            e.getStackTrace();
        }
        String dateTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();
        if (type.equals("text")){
            //text message
            myHolder.messageTv.setVisibility(View.VISIBLE);
            myHolder.messageIv.setVisibility(View.GONE);
            myHolder.messageTv.setText(message);
        }
        else {
            //image message
            myHolder.messageTv.setVisibility(View.GONE);
            myHolder.messageIv.setVisibility(View.VISIBLE);
            Picasso.get().load(message).placeholder(R.drawable.ic_image).into(myHolder.messageIv);

        }
        int y =i;


        //set data

        myHolder.messageTv.setText(message);
        myHolder.timeTv.setText(dateTime);
        try {
            Picasso.get().load(imageUrl).into(myHolder.profileTv);
        }
        catch (Exception e){

        }
        //click vao linearLayout de xoa tin nhan
        myHolder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Thu h???i");
                builder.setMessage("B???n ch???c mu???n thu h???i tin nh???n n??y ?");
                // delete btn
                builder.setPositiveButton("X??a", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(y);
                    }
                });
                // cancel btn
                builder.setNegativeButton("Tho??t", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    //dismiss Dialog
                    dialog.dismiss();
                    }
                });
                // t???o v?? show dialog
                builder.create().show();

            }
        });



        //set tinh trang da nhan/xem tin nhan
        if (i==chatList.size()-1){
            if (chatList.get(i).isSeen()){
                myHolder.isSeenTv.setText("???? xem");
            }
            else {
                myHolder.isSeenTv.setText("???? g???i");
            }
        }
        else {
            myHolder.isSeenTv.setVisibility(View.GONE);

        }

    }

    private void deleteMessage(int position) {
        String myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();


        String msgTimeStamp = chatList.get(position).getTimeStamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    if (ds.child("sender").getValue().equals(myUID)){
                        //Tr?????ng h???p 1: x??a tin nh???n ??? trong c???t Chats database

                        //ds.getRef().removeValue();

                        // Tr?????ng h???p 2: Set l???i tin nh???n th??nh: "Tin nh???n ???? ???????c thu h???i"
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("message","Tin nh???n ???? ???????c thu h???i...");
                        ds.getRef().updateChildren(hashMap);

                        Toast.makeText(context, "???? thu h???i tin nh???n", Toast.LENGTH_SHORT).show();

                    }
                    else {
                        Toast.makeText(context, "B???n ch??? c?? th??? thu h???i tin nh???n c???a m??nh...", Toast.LENGTH_SHORT).show();
                    }




                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //get user dang dang nhap
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(fUser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }
    }

    class  MyHolder extends RecyclerView.ViewHolder{

        ImageView profileTv, messageIv;
        TextView messageTv, timeTv, isSeenTv;
        LinearLayout messageLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            profileTv = itemView.findViewById(R.id.profileTv);
            messageIv = itemView.findViewById(R.id.messageIv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            isSeenTv = itemView.findViewById(R.id.isSeenTv);
            messageLayout= itemView.findViewById(R.id.messageLayout);
        }
    }


}
