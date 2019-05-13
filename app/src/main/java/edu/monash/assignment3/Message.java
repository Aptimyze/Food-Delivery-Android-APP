package edu.monash.assignment3;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

import edu.monash.assignment3.Model.Current;
import edu.monash.assignment3.Model.Order;
import edu.monash.assignment3.Model.Request;

public class Message extends Service implements ChildEventListener{

    FirebaseDatabase db;
    DatabaseReference requests;


    public Message() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        db = FirebaseDatabase.getInstance();
        requests = db.getReference("Requests");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        requests.addChildEventListener(this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

    }


    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        Request request = dataSnapshot.getValue(Request.class);
        if(request.getStatus().equals("1")){
            showMessage(dataSnapshot.getKey(),request);

        }

    }

    private void showMessage(String key, Request request) {

        Intent intent = new Intent(getBaseContext(),OrderStatus.class);
        intent.putExtra("userPhone",request.getPhone());
        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext(),"Hello");

        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                //.setWhen(System.currentTimeMillis())
                .setContentTitle("Take Out")
                .setTicker("TakeOut")
                .setContentInfo("Your order was updated")
                .setContentText("Order "+key+"was update to "+ Current.converCodeToStatus(request.getStatus()))
                .setContentIntent(contentIntent)
                .setContentInfo("Info").setSmallIcon(R.mipmap.ic_launcher);

        Notification notification = builder.build();

        NotificationManager notificationManager = (NotificationManager) getBaseContext().getSystemService(getBaseContext().NOTIFICATION_SERVICE);
        //NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(990,notification);
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
