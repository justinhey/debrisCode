package xdrj.com.debris;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.chad.library.adapter.base.BaseQuickAdapter;



import java.util.ArrayList;
import java.util.List;



@Route(path = "/main/home")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        RecyclerView  rcv_main = (RecyclerView) findViewById(R.id.rcv_main);

        rcv_main.setLayoutManager(new LinearLayoutManager(this));

        List<String> items = new ArrayList<>();

        items.add("test");

        HomeAdapter adapter = new HomeAdapter(R.layout.item_main, items);

        rcv_main.setAdapter(adapter);

        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

                ARouter.getInstance().build("/test/main").navigation();

            }
        });

    }
}
