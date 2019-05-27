package com.example.nochances.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.nochances.R;

public class ImageTouchSlider extends RelativeLayout implements
                            View.OnTouchListener{

    /** FIELDS
     * 1. mContext: the context (activity or fragment) in which this slider will appear
     * 2. mImage: the image we use for this slider (@drawable/answer_call)
     * 3. mScreenWidthInPixel: the width of the screen in pixels
     * 4. mDesnity: the pixel density of the display (for converting from pixels to dp and vice versa)
     * NOTE: dp width of screen = mScreenWidthInPixel / mDensity (in int)
     * 5. mPaddingInDp: the padding we add to the left and right of the slider (in dp)
     * NOTE: length of slider = mScreenWidthInDp - mPaddingInDp * 2;
     */
    private Context mContext;
    private ImageView mImage;

    private int mScreenWidthInPixel;
    private float mDensity;
    private int mPaddingInDp = 15; // padding to the slider

    public interface OnImageSliderChangedListener{
        void onChanged();
    }

    private OnImageSliderChangedListener mOnImageSliderChangedListener;

    /**
     * Constructor from  specific context. Assigns the context and creates the view
     */
    public ImageTouchSlider(Context context) {
        super(context);
        mContext = context;
        createView(); // create the view
    }

    /**
     * Constructor with an additional set of attributes
     */
    public ImageTouchSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        createView(); // create the view
    }

    /**
     * Constructor with attributes and a style definition
     */
    public ImageTouchSlider(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        createView(); // create the view
    }

    /**
     * Creates our actual slider view
     */
    @SuppressLint("ClickableViewAccessibility")
    public void createView() {
        // first, inflate the xml view, which is just an image in a relative layout
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(inflater != null) {
            inflater.inflate(R.layout.image_touch_slider, this, true);
            // get the image which we will slide
            mImage = findViewById(R.id.slider);
            // add a listener to that image!
            mImage.setOnTouchListener(this);

            WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            if(manager != null) {
                Display display = manager.getDefaultDisplay();
                DisplayMetrics outMetrics = new DisplayMetrics();
                display.getMetrics(outMetrics);

                // get the screen width in pixels
                mScreenWidthInPixel = outMetrics.widthPixels;
                // get the pixel density of the display
                mDensity = getResources().getDisplayMetrics().density;
            }
        }
    }

    /**
     * Callback to when we touch the image on the slider
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        MarginLayoutParams layoutParams = (MarginLayoutParams) v.getLayoutParams();
        int width = v.getWidth();
        // get absolute coordinates of event, relative to the device screen!
        float xPos = event.getRawX();

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // when we just press on the slider, nothing happens!
                // TODO: should we do something here?
                break;
            case MotionEvent.ACTION_MOVE:
                if(xPos < (mScreenWidthInPixel - (width/2) - mPaddingInDp*mDensity) && xPos > mPaddingInDp*mDensity+(width/2)) {
                    mOnImageSliderChangedListener.onChanged();
                    layoutParams.leftMargin = (int)(xPos - width/2);
                    mImage.setLayoutParams(layoutParams);
                }
                break;
            case MotionEvent.ACTION_UP:
                // when we release the slider, also nothing happens
                // TODO: should we do something here?
                break;
            default:
                break;
        }

        return true;
    }

    public void setOnImageSliderChangedListener(OnImageSliderChangedListener listener) {
        mOnImageSliderChangedListener = listener;
    }

}
