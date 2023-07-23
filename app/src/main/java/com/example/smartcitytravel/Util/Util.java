package com.example.smartcitytravel.Util;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.smartcitytravel.Fragments.ErrorDialogFragment;
import com.example.smartcitytravel.R;

import org.apache.commons.lang3.StringUtils;


public class Util {
    public Util() {
    }

    public void hideKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusView = activity.getCurrentFocus();
        if (focusView != null) {
            inputManager.hideSoftInputFromWindow(focusView.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void makeScreenNotTouchable(Activity activity) {
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void makeScreenTouchable(Activity activity) {
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void createErrorDialog(FragmentActivity activity, String title, String message) {
        ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment(title, message);
        errorDialogFragment.show(activity.getSupportFragmentManager(), "error_dialog");
        errorDialogFragment.setCancelable(false);
    }

    public String capitalizedName(String full_name) {
        String capitalizedName = "";
        String[] split_full_name = full_name.split("\\s+");
        for (String name : split_full_name) {
            name = StringUtils.capitalize(name);

            capitalizedName = capitalizedName + name + " ";
        }
        return capitalizedName;
    }

    public void setStatusBarColor(Activity activity, int colorResId) {
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(activity, colorResId));
    }

    public void setStatusBarColorDrawable(Activity activity, int drawableResId) {
        Drawable drawableBackground = ResourcesCompat.getDrawable(activity.getResources(), drawableResId, null);

        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setNavigationBarColor(activity.getResources().getColor(android.R.color.transparent));
        window.setBackgroundDrawable(drawableBackground);

    }

    public void addToolbar(AppCompatActivity activity, Toolbar toolbar, String title) {
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle(title);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
    }

    public void addToolbarAndNoUpButton(AppCompatActivity activity, Toolbar toolbar, String title) {
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle(title);
    }

}
