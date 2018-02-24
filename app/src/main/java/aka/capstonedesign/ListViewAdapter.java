package aka.capstonedesign;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by 예림 on 2017-12-07.
 */

public class ListViewAdapter extends BaseAdapter {
    //생성자 만들기
    private Context context;
    private List<Map> list;

    public ListViewAdapter(Context context, List<Map> list) {
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

        convertView = li.inflate(R.layout.lay_walk, null);

        final Map map = list.get(position);

        TextView txtmemo = (TextView) convertView.findViewById(R.id.tv_memo);
        TextView txtDate = (TextView) convertView.findViewById(R.id.tv_realdate); // 산책날짜
        TextView txtTime = (TextView) convertView.findViewById(R.id.textView_time); // 산책시간
        TextView txtDistan = (TextView) convertView.findViewById(R.id.textView_distance); // 산책거리
        ImageView imgview = (ImageView) convertView.findViewById(R.id.imageView);
        /*ImageButton close = (ImageButton) convertView.findViewById(R.id.imageButton_x);*/

        //-----------------지도 띄우기
        try {
            String imgpath = "/sdcard/Android/data/aka.capstonedesign/files/screenshots/walk_" + map.getWalkdate() + "_2.png";
            Bitmap bm = BitmapFactory.decodeFile(imgpath);
            imgview.setScaleType(ImageView.ScaleType.CENTER);
            imgview.setImageBitmap(bm);
            //Toast.makeText(getApplicationContext(), "load ok", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            //Toast.makeText(getApplicationContext(), "load error", Toast.LENGTH_SHORT).show();
            }

            txtDistan.setText(map.getDistance().toString());
            txtDate.setText(map.getWalkdate());
            txtTime.setText(map.getWalktime());
            txtmemo.setText(map.getMemo());

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            return convertView;
        }
    }
