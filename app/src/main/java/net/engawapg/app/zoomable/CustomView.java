package net.engawapg.app.zoomable;

import static java.sql.DriverManager.println;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;


public class CustomView extends RelativeLayout {

    LayoutInflater mInflater;
    public CustomView(Context context) {
        super(context);
        mInflater = LayoutInflater.from(context);
        init();

    }
    public CustomView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mInflater = LayoutInflater.from(context);
        init();
    }
    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = LayoutInflater.from(context);
        init();
    }
    public void init()
    {
        View v = mInflater.inflate(R.layout.custom_view, this, true);
        ImageView iv = (ImageView) v.findViewById(R.id.imageView1);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("CustomView", "***onTouchEvent");
        return super.onTouchEvent(event);
    }

}
