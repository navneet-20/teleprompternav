package com.navneet.teleprompter;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Typeface;
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

        LinearLayout bar=new LinearLayout(this); bar.setGravity(Gravity.CENTER_VERTICAL); bar.setPadding(dp(4),dp(2),dp(4),dp(2));
        TextView dragHandle=control("⠿",13,false), close=control("✕",18,true), play=control("▶",16,false), reset=control("↺",16,false), slower=control("−",18,false), faster=control("+",18,false);
        speedLabel=label(speed+" WPM");
        bar.addView(dragHandle,new LinearLayout.LayoutParams(dp(34),dp(44)));
        bar.addView(play,new LinearLayout.LayoutParams(dp(44),dp(44)));
        bar.addView(reset,new LinearLayout.LayoutParams(dp(44),dp(44)));
        bar.addView(slower,new LinearLayout.LayoutParams(dp(44),dp(44)));
        bar.addView(speedLabel,new LinearLayout.LayoutParams(0,dp(44),1));
        bar.addView(faster,new LinearLayout.LayoutParams(dp(44),dp(44)));
        bar.addView(close,new LinearLayout.LayoutParams(dp(46),dp(44))); box.addView(bar);

        scroll=new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(Color.TRANSPARENT); scroll.setSmoothScrollingEnabled(true);
        text=new TextView(this); text.setText(script); text.setTextColor(Color.WHITE); text.setTextSize(font);
        text.setGravity(Gravity.CENTER_HORIZONTAL); text.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL)); text.setLineSpacing(0,1.12f);
        text.setPadding(dp(24),dp(70),dp(24),dp(260)); scroll.addView(text);
        box.addView(scroll,new LinearLayout.LayoutParams(-1,0,1)); panel=box;

        int flags=WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        WindowManager.LayoutParams lp=new WindowManager.LayoutParams(dp(360),dp(300),
            Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE,
            flags,PixelFormat.TRANSLUCENT);
        lp.gravity=Gravity.TOP|Gravity.CENTER_HORIZONTAL; lp.y=dp(110); wm.addView(box,lp);

        View.OnTouchListener drag=new View.OnTouchListener(){
            float dx,dy;
            public boolean onTouch(View v,MotionEvent e){
                if(e.getAction()==MotionEvent.ACTION_DOWN){dx=e.getRawX()-lp.x;dy=e.getRawY()-lp.y;return true;}
                if(e.getAction()==MotionEvent.ACTION_MOVE){lp.x=(int)(e.getRawX()-dx);lp.y=(int)(e.getRawY()-dy);wm.updateViewLayout(panel,lp);return true;}
                return true;
            }
        };
        dragHandle.setOnTouchListener(drag);

        close.setOnClickListener(v->stopSelf());
        play.setOnClickListener(v->{running=!running;play.setText(running?"Ⅱ":"▶");if(running)startScroll();});
        reset.setOnClickListener(v->{running=false;play.setText("▶");scroll.smoothScrollTo(0,0);});
        slower.setOnClickListener(v->{speed=Math.max(20,speed-10);speedLabel.setText(speed+" WPM");});
        faster.setOnClickListener(v->{speed=Math.min(370,speed+10);speedLabel.setText(speed+" WPM");});
    }

    TextView control(String s,float size,boolean danger){ TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(danger?Color.rgb(255,170,185):Color.WHITE); t.setGravity(Gravity.CENTER); t.setTypeface(Typeface.DEFAULT,Typeface.BOLD); GradientDrawable g=new GradientDrawable(); g.setColor(danger?Color.rgb(72,35,48):Color.rgb(38,40,54)); g.setCornerRadius(dp(12)); t.setBackground(g); t.setPadding(0,0,0,0); return t; }
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
