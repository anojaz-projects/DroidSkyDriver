package com.metropolia.skydrive.util;

/**
 * @author Anoja
 * Date: 20.04.13
 * Time: 23:18
 */

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;

/**
 * This class adds some functionality for web view.
 */
public class WebViewFixed extends WebView
{

    private static final String TAG = "WebView";

    public WebViewFixed(Context context)
    {
        super(context);
    }

    public WebViewFixed(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public WebViewFixed(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus)
    {
        try
        {
            super.onWindowFocusChanged(hasWindowFocus);
        } catch (NullPointerException ex)
        {
            Log.e(TAG, "WebView.onWindowFocusChanged", ex);
        }
    }
}