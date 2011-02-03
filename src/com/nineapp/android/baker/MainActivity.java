package com.nineapp.android.baker;

import android.app.Activity;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private final int THRESHOLD_TEN = 10;
    private final double TWENTY_PERCENT = 0.2;
    private final double THIRTY_PERCENT = 0.3;
    private final double SEVENTY_PERCENT = 0.7;
    private final double EIGHTY_PERCENT = 0.8;


    private float downXValue;
    private float downYValue;

    private int numberOfPages;
    private int currentPage = -1;

    private WebView webView;
    List<String> htmlFiles;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        htmlFiles = loadBook();
        numberOfPages = htmlFiles.size();

        webView = (WebView) findViewById(R.id.webview);
        final WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);

        webView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return handleTouch(view, motionEvent);
            }
        });

        showNextPage();
    }

    private void showNextPage() {
        currentPage++;
        if (currentPage > numberOfPages - 1) {
            currentPage = numberOfPages - 1;
        }
        webView.loadUrl(String.format("file:///android_asset/book/%s", htmlFiles.get(currentPage)));
    }

    private void showPreviousPage() {
        currentPage--;
        if (currentPage < 0) {
            currentPage = 0;
        }
        webView.loadUrl(String.format("file:///android_asset/book/%s", htmlFiles.get(currentPage)));
    }

    private List<String> loadBook() {
        List<String> htmlFiles = new ArrayList<String>();
        try {
            String[] files = getAssets().list("book");
            for (String file : files) {
                if (file.endsWith(".html")) {
                    htmlFiles.add(file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return htmlFiles;

    }

    private boolean handleTouch(View v, MotionEvent evt) {
        switch (evt.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                downXValue = evt.getRawX();
                downYValue = evt.getRawY();
                Display display = getWindowManager().getDefaultDisplay();
                int width = display.getWidth();
                double xNavLowBound = width * TWENTY_PERCENT;
                double xNavMidLowBound = width * THIRTY_PERCENT;
                double xNavMidUpBound = width * SEVENTY_PERCENT;
                double xNavUpBound = width * EIGHTY_PERCENT;
                int height = display.getHeight();
                double yNavLowBound = height * TWENTY_PERCENT;
                double yNavMidLowBound = height * THIRTY_PERCENT;
                double yNavMidUpBound = height * SEVENTY_PERCENT;
                double yNavUpBound = height * EIGHTY_PERCENT;
                final boolean xIsToLeftOfScreen = downXValue < xNavLowBound;
                final boolean xIsToRightOfScreen = downXValue > xNavUpBound;
                final boolean yIsToTopOfScreen = downYValue < yNavMidLowBound;
                final boolean xIsMiddleOfScreen = isBetween(downXValue, xNavMidLowBound, xNavMidUpBound);
                final boolean yIsMiddleOfScreen = isBetween(downYValue, yNavMidLowBound, yNavMidUpBound);
                if (xIsToLeftOfScreen && yIsMiddleOfScreen) {
                    showPreviousPage();
                } else if (xIsToRightOfScreen && yIsMiddleOfScreen) {
                    showNextPage();
                }
                break;
            case MotionEvent.ACTION_UP:
                float currentX = evt.getRawX();
                float difference = Math.abs(downXValue - currentX);

                if ((downXValue < currentX) && (difference > THRESHOLD_TEN)) {
                    showPreviousPage();
                }

                if ((downXValue > currentX) && (difference > THRESHOLD_TEN)) {
                    showNextPage();
                }
                break;
        }
        return true;
    }

    private boolean isBetween(float xy, double lower, double upper) {
        return (xy > lower) && (xy < upper);
    }

}
