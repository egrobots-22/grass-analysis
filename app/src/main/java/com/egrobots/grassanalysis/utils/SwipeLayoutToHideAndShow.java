package com.egrobots.grassanalysis.utils;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import java.util.List;

import androidx.media3.ui.PlayerView;

public class SwipeLayoutToHideAndShow implements View.OnTouchListener {

    public enum SwipeDirection {
        topToBottom, bottomToTop, leftToRight, rightToLeft
    }

    private ViewGroup rootLayout;
    private ViewGroup layoutToShowHide;
    private GestureDetector gestureDetector;
    private List<SwipeDirection> swipeDirections;

    public void initialize(ViewGroup rootLayout, ViewGroup layoutToShowHide, List<SwipeDirection> swipeDirections, int maxSwipeDistance) {
        GestureListener gestureListener = new GestureListener();
        gestureDetector = new GestureDetector(rootLayout.getContext(), gestureListener);
        this.rootLayout = rootLayout;
        this.layoutToShowHide = layoutToShowHide;
        this.swipeDirections = swipeDirections;
        gestureListener.MAX_SWIPE_DISTANCE = maxSwipeDistance;

        this.layoutToShowHide.setOnTouchListener(this);
        this.rootLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    public void initialize(ViewGroup rootLayout, ViewGroup layoutToShowHide, List<SwipeDirection> swipeDirections) {
        initialize(rootLayout, layoutToShowHide, swipeDirections, 1);
    }

    public void cancel() {
        rootLayout.setOnTouchListener(null);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
//        rootLayout.showController();
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private int MAX_SWIPE_DISTANCE = 1;
        private final int SWIPE_VELOCITY_THRESHOLD = 1;


        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > MAX_SWIPE_DISTANCE && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeLeftToRight();
                        } else {
                            onSwipeRightToLeft();
                        }
                    }
                    result = true;
                } else if (Math.abs(diffY) > MAX_SWIPE_DISTANCE && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeTopToBottom();
                    } else {
                        onSwipeBottomToTop();
                    }
                }
                result = true;

            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    public void onSwipeLeftToRight() {
        boolean isVisible = layoutToShowHide.getVisibility() == View.VISIBLE;
        if ((swipeDirections.contains(SwipeDirection.leftToRight) && isVisible) || (swipeDirections.contains(SwipeDirection.rightToLeft) && !isVisible))
            toggleViewVisibilityWithAnimation(SwipeDirection.leftToRight);
    }

    public void onSwipeRightToLeft() {
        boolean isVisible = layoutToShowHide.getVisibility() == View.VISIBLE;
        if ((swipeDirections.contains(SwipeDirection.rightToLeft) && isVisible) || (swipeDirections.contains(SwipeDirection.leftToRight) && !isVisible))
            toggleViewVisibilityWithAnimation(SwipeDirection.rightToLeft);
    }

    public void onSwipeBottomToTop() {
        boolean isVisible = layoutToShowHide.getVisibility() == View.VISIBLE;
        if ((swipeDirections.contains(SwipeDirection.bottomToTop) && isVisible) || (swipeDirections.contains(SwipeDirection.topToBottom) && !isVisible))
            toggleViewVisibilityWithAnimation(SwipeDirection.bottomToTop);
    }

    public void onSwipeTopToBottom() {
        boolean isVisible = layoutToShowHide.getVisibility() == View.VISIBLE;
        if ((swipeDirections.contains(SwipeDirection.topToBottom) && isVisible) || (swipeDirections.contains(SwipeDirection.bottomToTop) && !isVisible))
            toggleViewVisibilityWithAnimation(SwipeDirection.topToBottom);
    }

    void toggleViewVisibilityWithAnimation(SwipeDirection swipeDirection) {

        int currenVisibility = layoutToShowHide.getVisibility();
        int deltaVal = swipeDirection == SwipeDirection.leftToRight || swipeDirection == SwipeDirection.topToBottom ? 1000 : -1000;
        if (currenVisibility == View.GONE) {
            deltaVal = -deltaVal;
        }

        int fromXDelta = currenVisibility == View.VISIBLE || swipeDirection == SwipeDirection.topToBottom || swipeDirection == SwipeDirection.bottomToTop ? 0 : deltaVal;
        int toXDelta = currenVisibility == View.GONE || swipeDirection == SwipeDirection.topToBottom || swipeDirection == SwipeDirection.bottomToTop ? 0 : deltaVal;
        int fromYDelta = currenVisibility == View.VISIBLE || swipeDirection == SwipeDirection.leftToRight || swipeDirection == SwipeDirection.rightToLeft ? 0 : deltaVal;
        int toYDelta = currenVisibility == View.GONE || swipeDirection == SwipeDirection.leftToRight || swipeDirection == SwipeDirection.rightToLeft ? 0 : deltaVal;

        Animation animation = new TranslateAnimation(fromXDelta, toXDelta,fromYDelta, toYDelta);
        animation.setDuration(500);
        layoutToShowHide.startAnimation(animation);
        layoutToShowHide.setVisibility(toXDelta == 0 && toYDelta == 0 ? View.VISIBLE : View.GONE);
    }
}