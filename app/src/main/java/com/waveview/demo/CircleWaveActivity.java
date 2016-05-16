package com.waveview.demo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.SeekBar;

import com.john.waveview.CircleWaveView;

/**
 * Created by qinchong on 5/16/16.
 */
public class CircleWaveActivity extends Activity {

    CircleWaveView circleWaveView;
    private SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_ware);
        circleWaveView = (CircleWaveView) findViewById(R.id.circle_wave_view);
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                circleWaveView.setProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
