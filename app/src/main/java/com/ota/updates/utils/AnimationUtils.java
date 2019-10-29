package com.ota.updates.utils;

import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

public class AnimationUtils {

    public static void toggleViewAnimation(View view, boolean show, boolean shouldGone, String type, int delay) {
        Transition transition;
        switch (type){
            case "slide":
                transition = new Slide(Gravity.TOP);
                break;
            case "circle":
                transition = new CircualrRevealTransation();
                break;
            default:
                transition = new Fade();

        }
        transition.setDuration(delay);
        transition.addTarget(view.getId());
        try {
            TransitionManager.beginDelayedTransition((ViewGroup)view.getParent(), transition);
        } catch (UnsupportedOperationException e){
            e.printStackTrace();
        }
        if(shouldGone){
            view.setVisibility(show ? View.VISIBLE : View.GONE);
        } else {
            view.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        }
    }




}
