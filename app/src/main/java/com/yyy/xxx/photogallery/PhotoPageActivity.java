package com.yyy.xxx.photogallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

/**
 * Created by len on 2017. 1. 19..
 */

public class PhotoPageActivity extends SingleFragmentActivity {

    private PhotoPageFragment fragment;

    public static Intent newIntent(Context context, Uri photoPageUri) {
        Intent i = new Intent(context, PhotoPageActivity.class);
        i.setData(photoPageUri);
        return i;
    }


    @Override
    protected Fragment createFragment() {
        return PhotoPageFragment.newInstance(getIntent().getData());
    }

    @Override
    public void onBackPressed() {
        if (fragment.webViewCanGoBack()) {
            fragment.webViewGoBack();
        } else {
            super.onBackPressed();
        }
    }
}
