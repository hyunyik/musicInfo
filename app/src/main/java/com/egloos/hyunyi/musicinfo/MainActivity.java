package com.egloos.hyunyi.musicinfo;

//import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

    private SharedPreferences prefs;
    private boolean isServiceOn;
    private boolean isAutoStartOn;
    private boolean isSimilarOn;
    private boolean isTickerOn;
    private int transparency;
    private int tickTime;
    private int location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        isServiceOn = prefs.getBoolean("IS_SERVICE_ON", false);
        isAutoStartOn = prefs.getBoolean("AUTO_START", false);
        isSimilarOn = prefs.getBoolean("SimilarOn", false);
        isTickerOn = prefs.getBoolean("TickerOn", false);
        transparency = prefs.getInt("TRANSPARENCY", 100);
        tickTime = prefs.getInt("TickTime", 5);
        location = prefs.getInt("Location", 0);

        TextView Trans = (TextView) findViewById(R.id.trans);
        Trans.setText(String.format("Transparency: %3d", transparency) + "%");

        TextView tTickTime = (TextView) findViewById(R.id.ticktime);
        tTickTime.setText(String.format("Tick Time: %3d sec", tickTime));

        Switch ServiceOnOff = (Switch) findViewById(R.id.service_on_off);
        ServiceOnOff.setChecked(isServiceOn);

        Switch AutoStart = (Switch) findViewById(R.id.auto_start);
        AutoStart.setChecked(isAutoStartOn);

        Switch SimilarArt = (Switch) findViewById(R.id.sw_similar_artist);
        SimilarArt.setChecked(isSimilarOn);

        Switch NotiTicker = (Switch) findViewById(R.id.notification_ticker);
        NotiTicker.setChecked(isTickerOn);

        final SeekBar TransSeekBar = (SeekBar) findViewById(R.id.trans_seekbar);
        TransSeekBar.setMax(100);
        TransSeekBar.setProgress(transparency);

        final SeekBar TickTimeSeekBar = (SeekBar) findViewById(R.id.tick_time_seekBar);
        TickTimeSeekBar.setMax(10);
        TickTimeSeekBar.setProgress(tickTime);

        final SharedPreferences.Editor editor = prefs.edit();

        Spinner LocationSpinner = (Spinner) findViewById(R.id.location_spinner);
        ArrayAdapter<CharSequence> LocationAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.location_data, R.layout.location_spinner_item);
        LocationSpinner.setAdapter(LocationAdapter);
        LocationSpinner.setSelection(location);

        ServiceOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startService(new Intent(getApplicationContext(), MainService.class));
                    editor.putBoolean("IS_SERVICE_ON", true);
                    editor.commit();
                } else {
                    stopService(new Intent(getApplicationContext(), MainService.class));
                    editor.putBoolean("IS_SERVICE_ON", false);
                    editor.commit();
                }
            }
        });

        AutoStart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editor.putBoolean("AUTO_START", true);
                    editor.commit();
                } else {
                    editor.putBoolean("AUTO_START", false);
                    editor.commit();
                }
            }
        });

        SimilarArt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editor.putBoolean("SimilarOn", true);
                    editor.commit();
                } else {
                    editor.putBoolean("SimilarOn", false);
                    editor.commit();
                }
            }
        });

        NotiTicker.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editor.putBoolean("TickerOn", true);
                    editor.commit();
                } else {
                    editor.putBoolean("TickerOn", false);
                    editor.commit();
                }
            }
        });

        TransSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView Trans = (TextView) findViewById(R.id.trans);
                Trans.setText(String.format("Transparency: %3d", progress) + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                editor.putInt("TRANSPARENCY", seekBar.getProgress());
                editor.commit();

                if (prefs.getBoolean("IS_SERVICE_ON", false)) {
                    Intent i = new Intent(getApplicationContext(), MainService.class);
                    i.setAction("com.egloos.hyunyi.musicinfo.SET_TRANSPARENCY");
                    i.putExtra("transparency", seekBar.getProgress());
                    startService(i);
                }
            }
        });

        TickTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView tTickTime = (TextView) findViewById(R.id.ticktime);
                tTickTime.setText(String.format("Tick Time: %3d sec", progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                editor.putInt("TickTime", seekBar.getProgress());
                editor.commit();
            }
        });

        LocationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editor.putInt("Location", position);
                editor.commit();

                if (prefs.getBoolean("IS_SERVICE_ON",false)) {
                    Intent i = new Intent(getApplicationContext(), MainService.class);
                    stopService(i);
                    startService(i);
                }

                /*
                String selected = parent.getItemAtPosition(position).toString();
                switch (selected) {
                    case "Left":

                        break;
                    case "Right":
                        break;
                }
                */
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //stopService(new Intent(this, MainService.class));
        //unregisterReceiver(myBroadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
