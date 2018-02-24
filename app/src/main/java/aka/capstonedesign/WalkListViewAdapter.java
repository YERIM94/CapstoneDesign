package aka.capstonedesign;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by 예림 on 2017-12-12.
 */

    public class WalkListViewAdapter extends BaseAdapter {
        //생성자 만들기
        private Context context;
        private List<String> list;

        public WalkListViewAdapter(Context context, List<String> list) {
            this.context = context;
            this.list = list;
        }

        public int getCount() {  //몇개인지 나타내는 것.
            return list.size();
        }

        public Object getItem(int position) {
            return list.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater li = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = li.inflate(R.layout.walkday, null);
            TextView walkday = (TextView) convertView.findViewById(R.id.walkday);

            final String day = list.get(position);

            String Day = day.substring(8);
            walkday.setText(Day);

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            return convertView;
        }
}
