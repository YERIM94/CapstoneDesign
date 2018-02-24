package aka.capstonedesign;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TabFragment4 extends Fragment {

    public TabFragment4(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        View view= (View) inflater.inflate(R.layout.fragment_tab_fragment4, container, false);

        final int ITEM_SIZE = 4;


        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        List<Item> items = new ArrayList<>();
        Item[] item = new Item[ITEM_SIZE];
        item[0] = new Item(R.drawable.a, "산책 가기 전 준비물", "목줄 (훈련용 가슴줄X)\n리드 줄 (120cm 정도)\n배변봉투\n화장지\n계절별 견종에 따른 강아지 옷\n견명인식표\n물");
        item[1] = new Item(R.drawable.e, "산책 후 손질", "산책이나 운동을 한 후에는 먼지 같은 이물질이 반려견 몸에 묻게 되며 심한 운동을 한 후에는 침을 많이 흘립니다. " +
                "\n그러므로 먼저 젖은 수건으로 침과 먼지 등을 닦은 후 브러시를 이용하여 브러싱을 해줍니다.");
        item[2] = new Item(R.drawable.c, "산책 시 주의사항", "1. 반려견을 제어할 수 있어야 합니다." +
                "\n2. 여름철에는 폭염으로 뜨거워진 아스팔트 위를 걷는 것 만으로도 열사병의 위험이 있습니다." +
                "\n3. 비가 내릴 때는 산책 하지 않는 것이 좋습니다.");
        item[3] = new Item(R.drawable.f, "눈 오는 날 산책할 때 주의사항", "1. 염화칼슘을 조심해야 합니다. 염화칼슘을 맨 발바닥으로 밟거나 먹지 않도록 주의해주세요." +
                "\n2. 발바닥 털은 잘 정리해야 합니다." +
                "\n3. 추위나 동상에 주의해야 합니다.");

        for (int i = 0; i < ITEM_SIZE; i++) {
            items.add(item[i]);
        }

        recyclerView.setAdapter(new RecyclerAdapter(getActivity().getApplicationContext(), items, R.layout.fragment_tab_fragment4));

        return view;
    }
}
