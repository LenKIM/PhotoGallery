package com.yyy.xxx.photogallery.model;

/**
 * Created by len on 2017. 1. 13..
 */

public class GalleryItem {

//    @SerializedName("title")
    private String mCaption;
//    @SerializedName("id")
    private String mId;
//    @SerializedName("url_s")
    private String mUrl;

    @Override
    public String toString() {
        return mCaption;
    }

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String mCaption) {
        this.mCaption = mCaption;
    }

    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }
}
