package com.yyy.xxx.photogallery;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by len on 2017. 1. 11..
 */
public class PhotoGalleryFragment extends Fragment {


    private static final String TAG = PhotoGalleryFragment.class.getName();

    public static final int PERMISSION_GRANTED = PackageManager.PERMISSION_GRANTED;

    private RecyclerView mPhotoRecyclerView;

    public static PhotoGalleryFragment newInstance(){
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemsTask().execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

            mPhotoRecyclerView = (RecyclerView) view
                    .findViewById(R.id.fragment_photo_gallery);
            mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
            return view;

    }

    private class FetchItemsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
//            try {
//                String result = new FlickrFetchr()
//                        .getUrlString("http://thatsnothing.blog.me/220701695721");
//                Log.i(TAG, "Fetched contents of URL: " + result);
//            }catch (IOException ioe){
//                Log.e(TAG, "Failed to fetch URL : ", ioe);
//            }
            new FlickrFetchr().fetchItems();
        return null;
        }
    }
}
