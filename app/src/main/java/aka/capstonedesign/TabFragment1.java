package aka.capstonedesign;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.reflect.Array;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.ContentValues.TAG;


public class TabFragment1 extends Fragment {

    public TabFragment1() {

    }

    private FirebaseStorage mFirebaseStorage;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseDatabase;

    CircleImageView profile_image;
    String lastday;

    int walking;
    double percent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = (ViewGroup) inflater.inflate(R.layout.fragment_tab_fragment1, container, false);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        ImageButton profile_edit = (ImageButton) view.findViewById(R.id.profile_edit);
        profile_image = (CircleImageView) view.findViewById(R.id.profile);
        final TextView profile_name = (TextView) view.findViewById(R.id.profile_name);
        final TextView profile_age = (TextView) view.findViewById(R.id.profile_age);
        final TextView profile_varieties = (TextView) view.findViewById(R.id.profile_varieties);
        final TextView profile_weight = (TextView) view.findViewById(R.id.profile_weight);
        final TextView howManyWalk = (TextView) view.findViewById(R.id.howManyWalk);
        final TextView recentdate = (TextView) view.findViewById(R.id.lastdate);
        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar2);
        final TextView walkcheck = (TextView) view.findViewById(R.id.walking_check);
        final ImageView dot = (ImageView) view.findViewById(R.id.dot);
        final TextView percentgauge = (TextView)view.findViewById(R.id.percent);

        //프로필정보 불러와서 기본화면 세팅
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { //초기에 한번 불러오고, 데이터가 바뀌면 다시 불러옴
                Profile profile = dataSnapshot.getValue(Profile.class);

                String varieties = profile.getVarieties();
                String weight = profile.getWeight();

                profile_name.setText(profile.getName());
                profile_age.setText(Integer.toString(profile.getAge()));
                profile_varieties.setText(varieties);
                profile_weight.setText(weight);

                walking = howManyWalk(varieties, profile.getAge() * 12, weight);
                howManyWalk.setText("매일 " + walking + "분");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());

            }
        };
        mFirebaseDatabase.getReference(mFirebaseUser.getUid()).child("Profile").addValueEventListener(postListener);

        //액티비티 실행할 때 프로필 이미지 설정
        ValueEventListener profileListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { //초기에 한번 불러오고, 데이터가 바뀌면 다시 불러옴
                StorageUri storageUri = dataSnapshot.getValue(StorageUri.class);
                String profilename = storageUri.getUri();
                setProfileImage(profile_image, profilename);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        mFirebaseDatabase.getReference(mFirebaseUser.getUid()).child("StorageUri").addValueEventListener(profileListener);


        //최근기록
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    Map map = dataSnapshot.getValue(Map.class);
                    String lastdate = map.getWalkdate();
                    recentdate.setText(lastdate);
                    lastday = map.getWalkday();
                } else
                    recentdate.setText("기록이 없습니다.");
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

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
        };
        mFirebaseDatabase.getReference(mFirebaseUser.getUid()).child("Map").orderByKey().limitToLast(1).addChildEventListener(childEventListener);

        //프로필 수정버튼 클릭
        profile_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ProfileActivity.class));
            }
        });

        //오늘 날짜
        Calendar cal = Calendar.getInstance();
        final Date date = cal.getTime();

        //이번주 일요일 날짜
        Calendar sunday = Calendar.getInstance();
        sunday.setTime(getSunday());

        //일요일,오늘 날짜 포맷 맞추기
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String day1 = simpleDateFormat.format(sunday.getTime());
        String day2 = simpleDateFormat.format(date);

        //이번 주 일요일부터 오늘까지의 산책 데이터 가져와서 날짜별로 배열에 넣어 누적시키기(하루에 여러번 한 것도 반영되도록)
        ValueEventListener calendarListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //달성도 체크
                    double[] array={0,0,0,0,0,0,0};
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        try{
                            Map map = postSnapshot.getValue(Map.class);
                            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss");
                            Date parsedate = simpleDateFormat1.parse(map.getWalkdate());
                            int i = getDateDay(parsedate)-1;
                            int index = map.getWalktime().indexOf(':');
                            int daytime = Integer.parseInt(map.getWalktime().substring(1, index));
                            array[i] += (double)daytime;
                        } catch (java.text.ParseException ex) {
                            ex.printStackTrace();}
                    }
                    Log.d("array",String.valueOf(array[0]));
                    for(int i=0;i<getDateDay(date);i++){
                        double sum = array[i] / (double)walking;
                        if(sum>=1)
                            sum = 1;
                        percent += sum;
                    }
                        Log.d("percent",String.valueOf(percent));
                    double result = percent/getDateDay(date);
                    progressBar.setProgress((int)(result*100));
                    percentgauge.setText(String.valueOf((int)(result*100)));
                    if(result >= 1) {
                        walkcheck.setText("충분");
                        dot.setImageResource(R.drawable.dot_green);
                    }
                    else{
                        if(result<1 && result>=0.5) {
                            walkcheck.setText("부족");
                            dot.setImageResource(R.drawable.dot_yellow);
                        }
                        else{
                            walkcheck.setText("부족");
                            dot.setImageResource(R.drawable.dot_red);
                        }
                    }
                    percent=0;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        mFirebaseDatabase.getReference(mFirebaseUser.getUid()).child("Map").orderByChild("walkday").startAt(day1).endAt(day2).addListenerForSingleValueEvent(calendarListener);

        return view;
    }

    //액티비티 실행할때 기본 프로필 이미지 셋팅
    private void setProfileImage(final CircleImageView circleImageView, String string) {
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        if (mFirebaseAuth.getCurrentUser() != null) {
            final StorageReference storageRef = mFirebaseStorage.getReferenceFromUrl("gs://capstonedesign-41e19.appspot.com").child(mFirebaseAuth.getCurrentUser().getUid()).child(string);
            Glide.with(this)
                    .using(new FirebaseImageLoader())
                    .load(storageRef)
                    .into(circleImageView);
        }
    }

    public Date getSunday() {
        Calendar calendar1 = Calendar.getInstance();

        int dayNum = calendar1.get(Calendar.DAY_OF_WEEK); //오늘 날짜가 숫자로 리턴

        int x = dayNum - 1;

        calendar1.add(Calendar.DATE, -x);
        Date StartDate = calendar1.getTime(); // 시작일(일요일)

        return StartDate;
    }

    //날짜에서 요일 가져오기
    public static int getDateDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int dayNum = cal.get(Calendar.DAY_OF_WEEK);

        return dayNum;
    }

    //권장산책량
    public static int howManyWalk(String size, int months, String isObesity) {
        if (size != null && (size.equals("말티즈") || size.equals("요크셔테리어") || size.equals("시츄")
                || size.equals("비숑") || size.equals("포메라니안") || size.equals("치와와")
                || size.equals("웰시코기") || size.equals("슈나우저") || size.equals("닥스훈트")
                || size.equals("퍼그") || size.equals("페키니즈") || size.equals("미니핀")
                || size.equals("빠삐용") || size.equals("화이트테리어") || size.equals("토이푸들")
                || size.equals("미니어처푸들") || size.equals("프렌치 불독"))) {
            if (132 >= months && months > 12) {
                if (isObesity != null && isObesity.equals("fat")) {
                    return 35;
                } else {
                    return 30;
                }
            } else if (months <= 12) {
                return 15;
            } else {
                return 20;
            }
        } else {
            if (132 >= months && months > 12) {
                if (isObesity.equals("fat")) {
                    return 65;
                } else {
                    return 60;
                }
            } else if (months <= 12) {
                return 30;
            } else {
                return 35;
            }
        }
    }
}

