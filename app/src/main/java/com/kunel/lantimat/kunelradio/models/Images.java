package com.kunel.lantimat.kunelradio.models;


import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by GabdrakhmanovII on 11.08.2017.
 */

public class Images {
    private String thumbnail;
    private String low_resolution;
    private String standard_resolution;

    public Images(JSONObject jsonObject) throws JSONException {
        this.thumbnail = jsonObject.getJSONObject("thumbnail").getString("url");
        this.low_resolution = jsonObject.getJSONObject("low_resolution").getString("url");
        this.standard_resolution = jsonObject.getJSONObject("standard_resolution").getString("url");
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getLow_resolution() {
        return low_resolution;
    }

    public String getStandard_resolution() {
        return standard_resolution;
    }
}
