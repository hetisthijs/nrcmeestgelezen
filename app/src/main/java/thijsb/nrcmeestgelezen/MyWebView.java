package thijsb.nrcmeestgelezen;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.webkit.WebView;

public class MyWebView extends WebView {
    private boolean flinged;

    private static final int SWIPE_MIN_DISTANCE = 320;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    GestureDetector gd;

    public MyWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        gd = new GestureDetector(context, sogl);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gd.onTouchEvent(event);
        if (flinged) {
            flinged = false;
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    GestureDetector.SimpleOnGestureListener sogl = new GestureDetector.SimpleOnGestureListener() {
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            /*if (event1.getX() < 1200 && event1.getX() > 80) {
                return false;
            }
            if (Math.abs(event1.getY() - event1.getY()) > SWIPE_MAX_OFF_PATH)
                return false;*/
            if(event1.getX() - event2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                loadUrl("javascript:Android.swipeLeft()");
                Log.i("Swiped","swipe left");
                flinged = true;
            } else if (event2.getX() - event1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                loadUrl("javascript:Android.swipeRight()");
                Log.i("Swiped","swipe right");
                flinged = true;
            }
            return true;
        }
    };
}