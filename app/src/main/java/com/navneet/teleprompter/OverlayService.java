package com.navneet.teleprompter;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.view.*;
import android.widget.*;

public class OverlayService extends Service {
    WindowManager wm; View panel; ScrollView scroll; TextView text, speedLabel; Handler h=new Handler(Looper.getMainLooper());
    int speed=150; boolean running=false; Runnable tick;

    int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    public IBinder onBind(Intent i){return null;}

    public int onStartCommand(Intent i,int f,int id){
        if(i!=null){
            speed=i.getIntExtra("wpm",150);
            String s=i.getStringExtra("script");
            show(s==null?"":s,i.getIntExtra("opacity",90),i.getIntExtra("fontSize",30));
        }
        return START_NOT_STICKY;
    }

    void show(String script,int op,int font){
        createChannel();
        startForeground(77,new Notification.Builder(this,"teleprompter")
            .setContentTitle("Teleprompter Pro").setContentText("Floating teleprompter is active")
            .setSmallIcon(android.R.drawable.ic_media_play).build());

        wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg=new GradientDrawable(); bg.setColor(Color.argb(Math.max(90,op*255/100),8,8,10)); bg.setCornerRadius(dp(16));
        box.setBackground(bg); box.setPadding(dp(8),dp(6),dp(8),dp(8));

        LinearLayout bar=new LinearLayout(this); bar.setGravity(Gravity.CENTER_VERTICAL);
        Button close=btn("×"), play=btn("▶"), reset=btn("↺"), slower=btn("−"), faster=btn("+");
        speedLabel=label(speed+" WPM");
        bar.addView(play); bar.addView(reset); bar.addView(slower);
        bar.addView(speedLabel,new LinearLayout.LayoutParams(0,dp(44),1));
        bar.addView(faster); bar.addView(close); box.addView(bar);

        scroll=new ScrollView(this); scroll.setFillViewport(true); scroll.setSmoothScrollingEnabled(true);
        text=new TextView(this); text.setText(script); text.setTextColor(Color.WHITE); text.setTextSize(font);
        text.setGravity(Gravity.CENTER_HORIZONTAL); text.setLineSpacing(0,1.12f);
        text.setPadding(dp(24),dp(70),dp(24),dp(260)); scroll.addView(text);
        box.addView(scroll,new LinearLayout.LayoutParams(-1,0,1)); panel=box;

        int flags=WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        WindowManager.LayoutParams lp=new WindowManager.LayoutParams(dp(360),dp(300),
            Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE,
            flags,PixelFormat.TRANSLUCENT);
        lp.gravity=Gravity.TOP|Gravity.CENTER_HORIZONTAL; lp.y=dp(100); wm.addView(box,lp);

        View.OnTouchListener drag=new View.OnTouchListener(){
            float dx,dy;
            public boolean onTouch(View v,MotionEvent e){
                if(e.getAction()==MotionEvent.ACTION_DOWN){dx=e.getRawX()-lp.x;dy=e.getRawY()-lp.y;return true;}
                if(e.getAction()==MotionEvent.ACTION_MOVE){lp.x=(int)(e.getRawX()-dx);lp.y=(int)(e.getRawY()-dy);wm.updateViewLayout(panel,lp);return true;}
                return true;
            }
        };
        bar.setOnTouchListener(drag);

        close.setOnClickListener(v->stopSelf());
        play.setOnClickListener(v->{running=!running;play.setText(running?"Ⅱ":"▶");if(running)startScroll();});
        reset.setOnClickListener(v->{running=false;play.setText("▶");scroll.smoothScrollTo(0,0);});
        slower.setOnClickListener(v->{speed=Math.max(20,speed-10);speedLabel.setText(speed+" WPM");});
        faster.setOnClickListener(v->{speed=Math.min(370,speed+10);speedLabel.setText(speed+" WPM");});
    }

    Button btn(String s){Button b=new Button(this);b.setText(s);b.setTextSize(12);return b;}
    TextView label(String s){TextView t=new TextView(this);t.setText(s);t.setTextColor(Color.WHITE);t.setGravity(Gravity.CENTER);t.setTextSize(13);return t;}

    void startScroll(){
        if(tick!=null)h.removeCallbacks(tick);
        tick=new Runnable(){public void run(){
            if(!running || scroll==null)return;
            int px=Math.max(1,Math.round(speed/60f*1.5f));
            scroll.smoothScrollBy(0,px);
            h.postDelayed(this,50);
        }};
        h.post(tick);
    }

    void createChannel(){
        if(Build.VERSION.SDK_INT>=26)
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(
                new NotificationChannel("teleprompter","Teleprompter",NotificationManager.IMPORTANCE_LOW));
    }
    public void onDestroy(){
        running=false;if(tick!=null)h.removeCallbacks(tick);
        if(wm!=null&&panel!=null)try{wm.removeView(panel);}catch(Exception ignored){}
        super.onDestroy();
    }
}
