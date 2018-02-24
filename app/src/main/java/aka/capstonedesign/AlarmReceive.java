package aka.capstonedesign;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static android.content.ContentValues.TAG;

/**
 * Created by 예림 on 2017-12-13.
 */

public class AlarmReceive extends BroadcastReceiver {   //BroadcastReceiver 가필요함

        String INTENT_ACTION = Intent.ACTION_BOOT_COMPLETED;
        final String TAG = "BOOT_START_SERVICE";
        FirebaseStorage mFirebaseStorage;
        FirebaseAuth mFirebaseAuth;
        FirebaseDatabase mFirebaseDatabase;
        Bitmap bitmap;

        @Override
        public void onReceive(Context context, Intent intent) {//알람 시간이 되었을때 onReceive를 호출함

            /*mFirebaseStorage = FirebaseStorage.getInstance();
            mFirebaseAuth = FirebaseAuth.getInstance();
            ValueEventListener profileListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) { //초기에 한번 불러오고, 데이터가 바뀌면 다시 불러옴
                    StorageUri storageUri = dataSnapshot.getValue(StorageUri.class);
                    String profilename = storageUri.getUri();
                    if (mFirebaseAuth.getCurrentUser() != null) {
                        final StorageReference storageRef = mFirebaseStorage.getReferenceFromUrl("gs://capstonedesign-41e19.appspot.com").child(mFirebaseAuth.getCurrentUser().getUid()).child(profilename);
                        storageRef.getBytes(480*480).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            }
                        });
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                }
            };
            mFirebaseDatabase.getReference(mFirebaseAuth.getCurrentUser().getUid()).child("StorageUri").addValueEventListener(profileListener);*/


            Notification.Builder builder = new Notification.Builder(context);

            Drawable vectorDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.alarm, null); //drawble 폴더에 이미지 넣기
            Bitmap myLogo = ((BitmapDrawable) vectorDrawable).getBitmap();

            builder.setContentTitle("WalkingDoggy")
                    .setContentText("오늘은 산책 시켜주실거죠?")
                    .setTicker("..")
                    .setSmallIcon(R.mipmap.applogo) // 우리 어플 아이콘
                    .setLargeIcon(myLogo)
//                .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .setWhen(System.currentTimeMillis())
                    .setDefaults(Notification.DEFAULT_ALL);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                builder.setCategory(Notification.CATEGORY_MESSAGE)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setVisibility(Notification.VISIBILITY_PUBLIC);
            }

            NotificationManager nm = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            nm.notify(1234, builder.build());


        }
}
