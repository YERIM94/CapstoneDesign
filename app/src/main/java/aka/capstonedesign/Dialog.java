package aka.capstonedesign;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class Dialog extends Activity {

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseUser mFirebaseUser;

    private String walkTime;
    private Double walkDistance;
    private String walkDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        final ListView listView = (ListView) findViewById(R.id.listView);

        //TabFragment2에서 intent로 map데이터 받아옴
        Intent intent = getIntent();
        String getdate = intent.getStringExtra("clickdate");


        //프로필정보 불러와서 기본화면 세팅
        ValueEventListener calendarListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { //초기에 한번 불러오고, 데이터가 바뀌면 다시 불러옴
                if(dataSnapshot.exists()){
                    List<Map> list = new ArrayList<Map>();
                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                        Map map = postSnapshot.getValue(Map.class);
                        list.add(map);
                    }
                    ListViewAdapter adapter = new ListViewAdapter(Dialog.this, list);
                    listView.setAdapter(adapter);
                }
                else{
                    finish();
                    Toast.makeText(Dialog.this,"기록이 없는 날입니다.",Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        mFirebaseDatabase.getReference(mFirebaseUser.getUid()).child("Map").orderByChild("walkday").equalTo(getdate).addValueEventListener(calendarListener);
    }
}
