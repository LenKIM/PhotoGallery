package com.yyy.xxx.photogallery;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import android.net.Uri;
import android.util.Log;

import com.yyy.xxx.photogallery.model.GalleryItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by len on 2017. 1. 11..
 */

public class FlickrFetchr {

    private static final String TAG = FlickrFetchr.class.getName();
    private static final String API_KEY = "";

    public static int currentPageNo = 1;

    //Page.541
    private static final String FETCH_RECENTS_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";

    private static final Uri ENDPOINT = Uri.parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .build();

    public byte [] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
                    ": with " +
                urlSpec);
            }


            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchRecentPhotos(){
        String url = buildUrl(FETCH_RECENTS_METHOD, null);
        return downloadGalleryItems(url);
    }

    public List<GalleryItem> searchPhotos(String query){
        String url = buildUrl(SEARCH_METHOD, query);
        return downloadGalleryItems(url);
    }

    private List<GalleryItem> downloadGalleryItems(String url) {
//    public List<GalleryItem> fetchItems(Integer page) {

//        String pageNo = String.valueOf(page);
//    public List<GalleryItem> fetchItems() {

        List<GalleryItem> items = new ArrayList<>();
//        List<GalleryItem> items = new ArrayList<>();

        try{
//            String url = Uri.parse("https://api.flickr.com/services/rest/")
//                    .buildUpon()
//                    .appendQueryParameter("method", "flickr.photos.getRecent")
//                    .appendQueryParameter("api_key",API_KEY)
//                    .appendQueryParameter("format", "json")
//                    .appendQueryParameter("nojsoncallback","1")
//                    .appendQueryParameter("page", pageNo)
//                    .appendQueryParameter("extras","url_s")
//                    .build().toString();

            String jsonString = getUrlString(url);

            Log.i(TAG, "Received JSON : "+ jsonString);
            Log.i(TAG, "Received URL : "+ url);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);
//            parseItems2(items, jsonString);
//            items = parseItems3(jsonBody);
        }catch (IOException e){
            Log.e(TAG, "Failed to fetch items e",e);
        } catch (JSONException e1) {
            Log.e(TAG, "Failed to parse JSON", e1);
        }
        return items;
    }

    private String buildUrl(String method, String query){
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method", method);

        if (method.equals(SEARCH_METHOD)) {
            uriBuilder.appendQueryParameter("text", query);
        }
        return uriBuilder.build().toString();
    }

    private void parseItems(List<GalleryItem> items, JSONObject jsonBody)
        throws IOException, JSONException {

        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        for (int i = 0; i < photoJsonArray.length(); i++) {
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);

            GalleryItem item = new GalleryItem();

            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));

            if (!photoJsonObject.has("url_s")) {
                continue;
            }
            item.setUrl(photoJsonObject.getString("url_s"));
            item.setOwner(photoJsonObject.getString("owner"));
            items.add(item);
        }
    }

//    GSON 사용해서 만드는 첫번째 방법.
    private void parseItems2(List<GalleryItem> items, String jsonBody)
            throws IOException, JSONException {

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(GalleryItem[].class, new ChallengeDeserializer())
                .create();

        GalleryItem [] photoLists = gson.fromJson(jsonBody, GalleryItem[].class);

        //스캔 포토 리스트
        for (GalleryItem item: photoLists) {
            if (item.getUrl() != null){
                items.add(item);
            }
            
        }

    }

    private class ChallengeDeserializer implements JsonDeserializer<GalleryItem[]> {

        @Override
        public GalleryItem[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            //photo
            JsonElement photos = json.getAsJsonObject().get("photos");
            JsonElement photoArray = photos.getAsJsonObject().get("photo");
            //Deserialize it. You use a new instance of Gson to avoid infinite recursion
            // to this deserializer
            Gson gson = new GsonBuilder()
                    .setFieldNamingStrategy(new ChallengeNamingStrategy())
                    .create();
            return gson.fromJson(photoArray, GalleryItem[].class);
        }

        private class ChallengeNamingStrategy implements FieldNamingStrategy {

            @Override
            public String translateName(Field f) {
                switch (f.getName()){
                    case "mId":
                        return "id";
                    case "mCaption" :
                        return "title";
                    case "mUrl" :
                        return "url_s";
                    default:
                        return f.getName();
                }
            }
        }
    }

    private List<GalleryItem> parseItems3(JSONObject jsonBody)
            throws JSONException {
        Gson gson = new GsonBuilder().create();
        JSONObject photoJsonObject =jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photoJsonObject.getJSONArray("photo");
        return Arrays.asList(gson.fromJson(photoJsonArray.toString(), GalleryItem[].class));
        }
    }
//    private void gsonParseItems(List<GsonGalleryItem> items, String jsonString ){
//        Gson gson = new GsonBuilder().create();
//
//        GsonGalleryItem galleryItem = gson.fromJson(jsonString, GsonGalleryItem.class);
//
//        for (GsonGalleryItem ggg: items) {
//
//            ggg.setId(galleryItem.getId());
//            if (!galleryItem.getUrl_s().isEmpty()) {
//                ggg.setUrl_s(galleryItem.getUrl_s());
//            }
//            ggg.setTitle(galleryItem.getTitle());
//        }
//        items.add(galleryItem);
//    }
