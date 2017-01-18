package com.yyy.xxx.photogallery;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yyy.xxx.photogallery.model.GalleryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by len on 2017. 1. 11..
 */
public class PhotoGalleryFragment extends Fragment  {


    private static final String TAG = PhotoGalleryFragment.class.getName();

    private RecyclerView mPhotoRecyclerView;
    private int lastFetchPage = 1;
    private List<GalleryItem> mItems = new ArrayList<>();
//    private List<GsonGalleryItem> mItems = new ArrayList<>();

    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

    public static PhotoGalleryFragment newInstance(){
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
//        new FetchItemsTask().execute(FlickrFetchr.currentPageNo);
//        new FetchItemsTask().execute();
        updateItems();

//        Intent i = PollService.newIntent(getActivity());
//        getActivity().startService(i);
//        테스트 코드임
//        PollService.setServiceAlarm(getActivity(), true);
        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setmThumbnailDownloadListener(new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail) {
                Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                target.bindDrawable(drawable);
            }
        });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

            mPhotoRecyclerView = (RecyclerView) view
                    .findViewById(R.id.fragment_photo_gallery);
            mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        setupAdapter();
            mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                    Log.d(TAG, "전체 아이템 갯수" + String.valueOf(totalItemCount));

                    PhotoAdapter adapter = (PhotoAdapter) mPhotoRecyclerView.getAdapter();
                    int lastpostion = adapter.getLastboundPostion();
                    GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();

                    Log.d(TAG,"getSpanCount()" + layoutManager.getSpanCount());
                    //아.. 3개씩 빠지는구나.....
                    if (lastpostion >= totalItemCount - 1){

                        String query = QueryPreferences.getStoredQuery(getActivity());

                        if (query != null){
                            new FetchItemsTask(query).execute();
                        } else {
                            new FetchItemsTask(null).execute();

                        }
                    }


                    /**
                     *     PhotoAdapter adapter = (PhotoAdapter)recyclerView.getAdapter(); // must be cast to Photoadapter, as I use a non-inherited method
                     int lastPosition = adapter.getLastBoundPosition();
                     GridLayoutManager layoutManager = (GridLayoutManager)recyclerView.getLayoutManager();
                     int loadBufferPosition = 1;
                     if(lastPosition >= adapter.getItemCount() - layoutManager.getSpanCount()- loadBufferPosition){
                     new FetchItemsTask().execute(lastPosition + 1);
                     * */
                }
            });
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        mThumbnailDownloader.clearQueue();
        Log.i(TAG, "Background thread destroyed");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "QueryTextSubmit: " + query);
                QueryPreferences.setStoredQuery(getActivity(), query);
                updateItems();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "QueryTextChange: " + newText);
                return false;
            }
        });
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn(getActivity())) {
            toggleItem.setTitle(R.string.stop_polling);
        } else {
            toggleItem.setTitle(R.string.start_polling);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(), null);
                updateItems();
                return true;

            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                    return super.onOptionsItemSelected(item);
        }
    }

    private void updateItems() {
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemsTask(query).execute();
    }

    private void setupAdapter() {
        /**
         * isAdded()
         * Return true if the fragment is currently added to its activity.
         * 프래그먼트가 액티비티에 연결되었는지 확인하는 것
         */
        if (isAdded()){
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {

//        private TextView mTitleTextView;
//        이제 진짜 사진을 넣을 차례
        private ImageView mItemImageView;
        public PhotoHolder(View itemView){
            super(itemView);
            mItemImageView = (ImageView) itemView.findViewById(R.id.fragment_photo_gallery_image_view);
        }

//        public void bindGalleryItem(GalleryItem item){

//        public void bindGalleryItem(GsonGalleryItem item){
//            mTitleTextView.setText(item.toString());
//        }
        public void bindDrawable(Drawable drawable){
            mItemImageView.setImageDrawable(drawable);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        //처음에 Adapter에 리스트넣음
        private List<GalleryItem> mGalleryItems;
//        private List<GsonGalleryItem> mGalleryItems;

        public int lastboundPostion;

        public int getLastboundPostion(){
            return lastboundPostion;
        }

        public PhotoAdapter(List<GalleryItem> galleryItems) {
//        public PhotoAdapter(List<GsonGalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            TextView textView = new TextView(getActivity());
//            return new PhotoHolder(textView);
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
//            GsonGalleryItem galleryItem = mGalleryItems.get(position);
            GalleryItem galleryItem = mGalleryItems.get(position);
//            holder.bindGalleryItem(galleryItem);
            Drawable placeholder = getResources().getDrawable(R.drawable.bill_up_close);
            holder.bindDrawable(placeholder);
            mThumbnailDownloader.queueThumbnail(holder, galleryItem.getUrl());
            lastboundPostion = position;
            Log.i(TAG,"마지막 lastboundPostion는" + lastboundPostion);


        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }


    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {

        private String mQuery;

        public FetchItemsTask(String query) {
            this.mQuery = query;
        }


        //        protected List<GsonGalleryItem> doInBackground(Void... params) {
//            try {
//                String result = new FlickrFetchr()
//                        .getUrlString("http://thatsnothing.blog.me/220701695721");
//                Log.i(TAG, "Fetched contents of URL: " + result);
//            }catch (IOException ioe){
//                Log.e(TAG, "Failed to fetch URL : ", ioe);
//            }

//        @Override
//        protected List<GalleryItem> doInBackground(Integer... params) {
//            return new FlickrFetchr().fetchItems(params[0]);
//        }


        @Override
        protected List<GalleryItem> doInBackground(Void... params) {

//            String query = "robot"; // 테스트를 위해 임시로 지정

            if (mQuery == null) {
                return new FlickrFetchr().fetchRecentPhotos();
            } else {
                return new FlickrFetchr().searchPhotos(mQuery);
            }
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {

//            if (lastFetchPage > 1){
//                mItems.addAll(galleryItems);
//                Log.d(TAG, "lastFetchPage" + lastFetchPage);
//                mPhotoRecyclerView.getAdapter().notifyDataSetChanged();
//            } else {
                mItems = galleryItems;
                setupAdapter();
            }
//            lastFetchPage++;
//            mItems.addAll()
        }
    }
