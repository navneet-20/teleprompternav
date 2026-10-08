package com.navneet.teleprompter;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.text.Editable; import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    EditText script, minutes, seconds;
    TextView count, wpmLabel, modeHint;
    SeekBar wpm, opacity, fontSize;
    RadioButton manualMode, timeMode;

    int dp(float x){ return (int)(x * getResources().getDisplayMetrics().density + .5f); }
    TextView tv(String s,float size){
        TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(Color.WHITE);
        t.setPadding(dp(2),dp(2),dp(2),dp(2)); return t;
    }
    Button action(String s){ Button b=new Button(this); b.setText(s); b.setTextSize(12); return b; }

    @Override public void onCreate(Bundle b){ super.onCreate(b); build(); }

    void build(){
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16),dp(14),dp(16),dp(10)); root.setBackgroundColor(Color.rgb(14,16,24));

        LinearLayout header=new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL);
        TextView logo=new TextView(this); logo.setText("◉"); logo.setTextSize(30); logo.setTextColor(Color.rgb(170,145,255));
        header.addView(logo,new LinearLayout.LayoutParams(dp(42),dp(50)));
        LinearLayout titles=new LinearLayout(this); titles.setOrientation(LinearLayout.VERTICAL);
        TextView title=tv("Teleprompter Pro",22); title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        TextView sub=tv("Write • Time • Read • Record",12); sub.setTextColor(Color.rgb(185,180,205));
        titles.addView(title); titles.addView(sub); header.addView(titles,new LinearLayout.LayoutParams(0,dp(52),1));
        root.addView(header);

        LinearLayout tools=new LinearLayout(this);
        Button paste=action("PASTE"), copy=action("COPY"), clear=action("CLEAR");
        tools.addView(paste,new LinearLayout.LayoutParams(0,dp(46),1));
        tools.addView(copy,new LinearLayout.LayoutParams(0,dp(46),1));
        tools.addView(clear,new LinearLayout.LayoutParams(0,dp(46),1));
        root.addView(tools);

        script=new EditText(this); script.setHint("Paste or type your script here…"); script.setGravity(Gravity.TOP|Gravity.START);
        script.setTextColor(Color.WHITE); script.setHintTextColor(Color.rgb(130,130,135)); script.setTextSize(18);
        GradientDrawable scriptBg=new GradientDrawable(); scriptBg.setColor(Color.rgb(25,27,38)); scriptBg.setCornerRadius(dp(16)); script.setBackground(scriptBg); script.setPadding(dp(14),dp(14),dp(14),dp(14));
        root.addView(script,new LinearLayout.LayoutParams(-1,0,1));

        count=tv("0 words",12); count.setTextColor(Color.LTGRAY); root.addView(count,new LinearLayout.LayoutParams(-1,dp(28)));
        script.addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence s,int a,int c,int d){}
            public void onTextChanged(CharSequence s,int a,int b,int c){ count.setText(words()+" words"); updateCalculatedWpm(); }
            public void afterTextChanged(Editable e){}
        });

        root.addView(tv("Playback speed",14));
        LinearLayout modes=new LinearLayout(this);
        manualMode=new RadioButton(this); manualMode.setText("Manual WPM"); manualMode.setTextColor(Color.WHITE); manualMode.setChecked(true);
        timeMode=new RadioButton(this); timeMode.setText("Target time"); timeMode.setTextColor(Color.WHITE);
        modes.addView(manualMode,new LinearLayout.LayoutParams(0,dp(48),1)); modes.addView(timeMode,new LinearLayout.LayoutParams(0,dp(48),1));
        root.addView(modes);

        wpmLabel=tv("Speed: 150 WPM",13); root.addView(wpmLabel);
        wpm=new SeekBar(this); wpm.setMax(350); wpm.setProgress(130); root.addView(wpm,new LinearLayout.LayoutParams(-1,dp(42)));
        wpm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar s,int p,boolean f){ if(manualMode.isChecked()) wpmLabel.setText("Speed: "+(p+20)+" WPM"); }
            public void onStartTrackingTouch(SeekBar s){} public void onStopTrackingTouch(SeekBar s){}
        });

        modeHint=tv("Choose Manual WPM or let the target time calculate it.",11); modeHint.setTextColor(Color.LTGRAY); root.addView(modeHint);
        LinearLayout time=new LinearLayout(this);
        minutes=new EditText(this); seconds=new EditText(this);
        minutes.setInputType(2); seconds.setInputType(2); minutes.setHint("Minutes"); seconds.setHint("Seconds");
        minutes.setTextColor(Color.WHITE); seconds.setTextColor(Color.WHITE); minutes.setHintTextColor(Color.GRAY); seconds.setHintTextColor(Color.GRAY);
        time.addView(minutes,new LinearLayout.LayoutParams(0,dp(48),1)); time.addView(seconds,new LinearLayout.LayoutParams(0,dp(48),1));
        root.addView(time);

        root.addView(tv("Overlay opacity",13));
        opacity=new SeekBar(this); opacity.setMax(100); opacity.setProgress(90); root.addView(opacity,new LinearLayout.LayoutParams(-1,dp(38)));

        root.addView(tv("Text size",13));
        fontSize=new SeekBar(this); fontSize.setMax(24); fontSize.setProgress(10); root.addView(fontSize,new LinearLayout.LayoutParams(-1,dp(38)));

        Button launch=action("▶  Launch Floating Teleprompter");
        launch.setTextSize(14); launch.setTextColor(Color.WHITE); GradientDrawable launchBg=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{Color.rgb(120,92,220),Color.rgb(72,142,220)}); launchBg.setCornerRadius(dp(18)); launch.setBackground(launchBg); root.addView(launch,new LinearLayout.LayoutParams(-1,dp(56)));
        TextView hint=tv("Tip: Target time automatically calculates WPM. You can adjust speed while reading.",11);
        hint.setTextColor(Color.LTGRAY); root.addView(hint);

        CompoundButton.OnCheckedChangeListener mode=(v,checked)->{
            if(!checked) return;
            if(v==manualMode) timeMode.setChecked(false); else manualMode.setChecked(false);
            boolean timed=timeMode.isChecked();
            wpm.setEnabled(!timed);
            minutes.setEnabled(timed); seconds.setEnabled(timed);
            modeHint.setText(timed ? "WPM is calculated from your script and target time." : "Set your reading speed manually.");
            updateCalculatedWpm();
        };
        manualMode.setOnCheckedChangeListener(mode); timeMode.setOnCheckedChangeListener(mode);
        minutes.setEnabled(false); seconds.setEnabled(false);

        minutes.addTextChangedListener(simpleWatcher()); seconds.addTextChangedListener(simpleWatcher());

        paste.setOnClickListener(v->{ ClipboardManager cm=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE); if(cm.hasPrimaryClip()) script.setText(cm.getPrimaryClip().getItemAt(0).coerceToText(this)); });
        copy.setOnClickListener(v->{ ClipboardManager cm=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE); cm.setPrimaryClip(ClipData.newPlainText("Teleprompter script",script.getText().toString())); Toast.makeText(this,"Script copied",Toast.LENGTH_SHORT).show(); });
        clear.setOnClickListener(v->script.setText(""));
        launch.setOnClickListener(v->launchOverlay());

        setContentView(root);
        if(Build.VERSION.SDK_INT>=33) requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},42);
    }

    TextWatcher simpleWatcher(){ return new TextWatcher(){ public void beforeTextChanged(CharSequence s,int a,int c,int d){} public void onTextChanged(CharSequence s,int a,int b,int c){updateCalculatedWpm();} public void afterTextChanged(Editable e){} }; }

    void updateCalculatedWpm(){
        if(timeMode==null || !timeMode.isChecked()) return;
        int total=parse(minutes)*60+parse(seconds);
        if(total>0 && words()>0){
            int value=Math.max(20,Math.min(370,(int)Math.round(words()*60.0/total)));
            wpmLabel.setText("Calculated: "+value+" WPM");
        } else wpmLabel.setText("Calculated: — WPM");
    }

    void launchOverlay(){
        if(script.getText().toString().trim().isEmpty()){ Toast.makeText(this,"Please enter a script first.",Toast.LENGTH_SHORT).show(); return; }
        if(!Settings.canDrawOverlays(this)){
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName())));
            Toast.makeText(this,"Allow display over other apps, then press Launch again.",Toast.LENGTH_LONG).show(); return;
        }
        int speed=manualMode.isChecked()?wpm.getProgress()+20:calcTimedSpeed();
        Intent i=new Intent(this,OverlayService.class);
        i.putExtra("script",script.getText().toString());
        i.putExtra("wpm",speed);
        i.putExtra("opacity",opacity.getProgress());
        i.putExtra("fontSize",fontSize.getProgress()+20);
        if(Build.VERSION.SDK_INT>=26) startForegroundService(i); else startService(i);
    }

    int calcTimedSpeed(){
        int total=parse(minutes)*60+parse(seconds);
        if(total<=0 || words()==0){ Toast.makeText(this,"Enter a valid target time.",Toast.LENGTH_SHORT).show(); return wpm.getProgress()+20; }
        return Math.max(20,Math.min(370,(int)Math.round(words()*60.0/total)));
    }
    int words(){ String s=script.getText().toString().trim(); return s.isEmpty()?0:s.split("\\s+").length; }
    int parse(EditText e){ try{return Integer.parseInt(e.getText().toString());}catch(Exception x){return 0;} }
}
