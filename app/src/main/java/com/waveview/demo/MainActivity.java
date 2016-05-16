package com.waveview.demo;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created by kai.wang on 6/17/14.
 */
public class MainActivity extends ListActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ListAdapter adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new String[]{"ware", "circle ware"}
        );
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        switch (position) {
            case 0:
                Intent intent0 = new Intent(this, WaveActivity.class);
                startActivity(intent0);
                break;
            case 1:
                Intent intent1 = new Intent(this, CircleWaveActivity.class);
                startActivity(intent1);
                break;
        }
    }
}