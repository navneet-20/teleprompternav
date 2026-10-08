package com.navneet.teleprompter;
import android.app.*; import android.content.*; import android.graphics.Color; import android.graphics.PixelFormat; import android.os.*; import android.view.*; import android.widget.*; import android.text.TextUtils;
public class OverlayService extends Service{
 WindowManager wm; View panel; TextView text; Handler h=new Handler(Looper.getMainLooper()); int y=0; int speed=150; boolean running=false; Runnable tick;
 int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);} 
 public IBinder onBind(Intent i){return null;}
 public int onStartCommand(Intent i,int f,int id){ if(i!=null){speed=i.getIntExtra("wpm",150); String s=i.getStringExtra("script"); show(s==null?"":s,i.getIntExtra("opacity",90));} return START_NOT_STICKY; }
 void show(String script,int op){
  createChannel(); startForeground(77, new Notification.Builder(this,"teleprompter").setContentTitle("Teleprompter running").setContentText("Floating teleprompter is active").setSmallIcon(android.R.drawable.ic_media_play).build());
  wm=(WindowManager)getSystemService(WINDOW_SERVICE); LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(10),dp(8),dp(10),dp(8)); box.setBackgroundColor(Color.argb(Math.max(80,op*255/100),0,0,0));
  LinearLayout bar=new LinearLayout(this); Button close=btn("×"), play=btn("▶"), slower=btn("−"), faster=btn("+"); TextView sp=label(speed+" WPM"); bar.addView(play);bar.addView(slower);bar.addView(sp,new LinearLayout.LayoutParams(0,dp(44),1));bar.addView(faster);bar.addView(close); box.addView(bar);
  ScrollView sv=new ScrollView(this); text=new TextView(this); text.setText(script); text.setTextColor(Color.WHITE); text.setTextSize(30); text.setGravity(Gravity.CENTER); text.setPadding(dp(25),dp(25),dp(25),dp(25)); sv.addView(text); box.addView(sv,new LinearLayout.LayoutParams(-1,0,1)); panel=box;
  int flags=WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS; WindowManager.LayoutParams lp=new WindowManager.LayoutParams(dp(340),dp(250),Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE,flags,PixelFormat.TRANSLUCENT); lp.gravity=Gravity.TOP|Gravity.CENTER_HORIZONTAL; lp.y=dp(100); wm.addView(box,lp);
  View.OnTouchListener drag=new View.OnTouchListener(){float dx,dy; public boolean onTouch(View v,android.view.MotionEvent e){if(e.getAction()==0){dx=e.getRawX()-lp.x;dy=e.getRawY()-lp.y;return true;} if(e.getAction()==2){lp.x=(int)(e.getRawX()-dx);lp.y=(int)(e.getRawY()-dy);wm.updateViewLayout(panel,lp);return true;}return true;}}; bar.setOnTouchListener(drag);
  close.setOnClickListener(v->stopSelf()); play.setOnClickListener(v->{running=!running;play.setText(running?"Ⅱ":"▶");if(running)startScroll();}); slower.setOnClickListener(v->{speed=Math.max(20,speed-10);sp.setText(speed+" WPM");}); faster.setOnClickListener(v->{speed=Math.min(370,speed+10);sp.setText(speed+" WPM");});
 }
 Button btn(String s){Button b=new Button(this);b.setText(s);return b;} TextView label(String s){TextView t=new TextView(this);t.setText(s);t.setTextColor(Color.WHITE);t.setGravity(Gravity.CENTER);t.setTextSize(14);return t;}
 void startScroll(){tick=new Runnable(){public void run(){if(!running||text==null)return; int px=Math.max(1,Math.round(speed/60f*1.6f)); text.scrollBy(0,px); h.postDelayed(this,1000/15);}};h.post(tick);}
 void createChannel(){if(Build.VERSION.SDK_INT>=26)((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(new NotificationChannel("teleprompter","Teleprompter",NotificationManager.IMPORTANCE_LOW));}
 public void onDestroy(){running=false;if(tick!=null)h.removeCallbacks(tick);if(wm!=null&&panel!=null)try{wm.removeView(panel);}catch(Exception e){}super.onDestroy();}
}
