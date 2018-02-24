package aka.capstonedesign;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;


public class TabFragment2 extends Fragment implements View.OnClickListener {

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseUser mFirebaseUser;
    String clickday;
    String firstdayOfMonth;
    public TabFragment2(){

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        View view= (View) inflater.inflate(R.layout.fragment_tab_fragment2, container, false);
        MaterialCalendarView materialCalendarView = (MaterialCalendarView) view.findViewById( R.id.calendarView );

        final ListView listView = (ListView) view.findViewById(R.id.WalkListView);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-");
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
        firstdayOfMonth = simpleDateFormat1.format(calendar.getTime())+"01";
        clickday = simpleDateFormat2.format(calendar.getTime());

        ValueEventListener calendarListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { //초기에 한번 불러오고, 데이터가 바뀌면 다시 불러옴
                List<String> list = new ArrayList<>();
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Map map = postSnapshot.getValue(Map.class);
                    if(!list.contains(map.getWalkday()))
                        list.add(map.getWalkday());
                }
                WalkListViewAdapter adapter = new WalkListViewAdapter(getContext(), list);
                listView.setAdapter(adapter);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        mFirebaseDatabase.getReference(mFirebaseUser.getUid()).child("Map").orderByChild("walkday").startAt(firstdayOfMonth).endAt(clickday).addValueEventListener(calendarListener);

        materialCalendarView.state().edit()
                .setFirstDayOfWeek( Calendar.SUNDAY )
                .setMinimumDate( CalendarDay.from( 1970, 1, 1 ) )
                .setMaximumDate( CalendarDay.from( 2100, 12, 31 ) )
                .setCalendarDisplayMode( CalendarMode.MONTHS )
                .commit();

        materialCalendarView.setOnDateChangedListener( new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                Date clickdate = date.getDate();

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                clickday = simpleDateFormat.format(clickdate);

                Intent intent = new Intent(getActivity(),Dialog.class);
                intent.putExtra("clickdate",clickday);
                startActivity(intent);
            }
        } );

        materialCalendarView.addDecorators(
                new SundayDecorator(),
                new SaturdayDecorator(),
                new OneDayDecorator()
        );
        return view;
    }

    @Override
    public void onClick(View v) {
    }

}
