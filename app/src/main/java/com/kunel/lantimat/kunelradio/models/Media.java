package com.kunel.lantimat.kunelradio.models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by GabdrakhmanovII on 11.08.2017.
 */

public class Media {

    String id;
    String code;
    Images images;
    ArrayList<Images> carousel_media;

    public Media(String id, String code, Images images) {
        this.id = id;
        this.code = code;
        this.images = images;
    }

    public Media(String id, Images images, ArrayList<Images> carousel_media) {
        this.id = id;
        this.images = images;
        this.carousel_media = carousel_media;
    }

    public String getCode() {
        return code;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Images getImages() {
        return images;
    }

    public void setImages(Images images) {
        this.images = images;
    }

    public ArrayList<Images> getCarousel_media() {
        return carousel_media;
    }

    public void setCarousel_media(ArrayList<Images> carousel_media) {
        this.carousel_media = carousel_media;
    }
}
