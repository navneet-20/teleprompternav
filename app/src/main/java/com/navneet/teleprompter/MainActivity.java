package com.navneet.teleprompter;

import android.app.*; import android.content.*; import android.graphics.Color; import android.net.Uri; import android.os.*; import android.provider.Settings; import android.view.*; import android.view.inputmethod.InputMethodManager; import android.widget.*; import java.util.*;

public class MainActivity extends Activity {
 EditText script; TextView count; SeekBar opacity; EditText minutes; EditText seconds; SeekBar wpm; TextView wpmLabel;
 int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);} 
 @Override public void onCreate(Bundle b){super.onCreate(b); build();}
 TextView tv(String s,int size){TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(Color.WHITE); t.setPadding(dp(4),dp(4),dp(4),dp(4)); return t;}
 void build(){
  LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(16),dp(18),dp(16),dp(12)); root.setBackgroundColor(Color.rgb(15,15,18));
  TextView title=tv("Teleprompter Pro",24); root.addView(title,new LinearLayout.LayoutParams(-1,dp(45)));
  LinearLayout row=new LinearLayout(this); Button paste=new Button(this); paste.setText("PASTE"); Button clear=new Button(this); clear.setText("CLEAR"); row.addView(paste,new LinearLayout.LayoutParams(0,dp(48),1)); row.addView(clear,new LinearLayout.LayoutParams(0,dp(48),1)); root.addView(row);
  script=new EditText(this); script.setHint("Paste or type your script here…"); script.setGravity(Gravity.TOP); script.setTextColor(Color.WHITE); script.setHintTextColor(Color.GRAY); script.setTextSize(18); script.setBackgroundColor(Color.rgb(28,28,32)); script.setPadding(dp(12),dp(12),dp(12),dp(12)); root.addView(script,new LinearLayout.LayoutParams(-1,0,1));
  count=tv("0 words",14); root.addView(count,new LinearLayout.LayoutParams(-1,dp(30))); script.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int d){} public void onTextChanged(CharSequence s,int a,int b,int c){count.setText(words()+" words");} public void afterTextChanged(android.text.Editable e){}});
  root.addView(tv("Target time (minutes / seconds)",14)); LinearLayout time=new LinearLayout(this); minutes=new EditText(this); minutes.setInputType(2); minutes.setHint("min"); seconds=new EditText(this); seconds.setInputType(2); seconds.setHint("sec"); time.addView(minutes,new LinearLayout.LayoutParams(0,dp(52),1)); time.addView(seconds,new LinearLayout.LayoutParams(0,dp(52),1)); root.addView(time);
  wpmLabel=tv("Speed: 150 WPM",14); root.addView(wpmLabel); wpm=new SeekBar(this); wpm.setMax(350); wpm.setProgress(130); root.addView(wpm,new LinearLayout.LayoutParams(-1,dp(45))); wpm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int p,boolean f){wpmLabel.setText("Speed: "+(p+20)+" WPM");} public void onStartTrackingTouch(SeekBar s){} public void onStopTrackingTouch(SeekBar s){}});
  root.addView(tv("Overlay opacity",14)); opacity=new SeekBar(this); opacity.setMax(100); opacity.setProgress(90); root.addView(opacity,new LinearLayout.LayoutParams(-1,dp(45)));
  LinearLayout buttons=new LinearLayout(this); Button launch=new Button(this); launch.setText("LAUNCH FLOATING TELEPROMPTER"); buttons.addView(launch,new LinearLayout.LayoutParams(0,dp(55),1)); root.addView(buttons);
  TextView hint=tv("Tip: set target time to automatically calculate the required WPM. You can also control speed inside the floating window.",12); hint.setTextColor(Color.LTGRAY); root.addView(hint);
  setContentView(root);
  paste.setOnClickListener(v->{ClipboardManager cm=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE); if(cm.hasPrimaryClip()) script.setText(cm.getPrimaryClip().getItemAt(0).coerceToText(this));}); clear.setOnClickListener(v->script.setText("")); launch.setOnClickListener(v->{if(!Settings.canDrawOverlays(this)){startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()))); Toast.makeText(this,"Allow display over other apps, then press Launch again.",Toast.LENGTH_LONG).show(); return;} int speed=calcSpeed(); Intent i=new Intent(this,OverlayService.class); i.putExtra("script",script.getText().toString()); i.putExtra("wpm",speed); i.putExtra("opacity",opacity.getProgress()); if(Build.VERSION.SDK_INT>=26) startForegroundService(i); else startService(i);});
 }
 int words(){String s=script.getText().toString().trim(); return s.isEmpty()?0:s.split("\\s+").length;}
 int calcSpeed(){int m=parse(minutes),s=parse(seconds); if(m>0||s>0){int total=m*60+s; if(total>0&&words()>0)return Math.max(20,Math.min(370,(int)Math.round(words()*60.0/total)));} return wpm.getProgress()+20;}
 int parse(EditText e){try{return Integer.parseInt(e.getText().toString());}catch(Exception x){return 0;}}
}
