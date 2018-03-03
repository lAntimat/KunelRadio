package com.kunel.lantimat.kunelradio.models;

/**
 * Created by GabdrakhmanovII on 10.08.2017.
 */

public class Feed {
    String title;
    String date;
    String imgUrl;
    String feedUrl;
    String views;

    public Feed(String title, String date, String imgUrl, String feedUrl, String views) {
        this.title = title;
        this.date = date;
        this.imgUrl = imgUrl;
        this.feedUrl = feedUrl;
        this.views = views;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }

    public String getViews() {
        return views;
    }

    public void setViews(String views) {
        this.views = views;
    }
}
