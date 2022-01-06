package com.test.smartbandage.model;

/**
 * Created by lipponep on 22.11.2017.
 */

import com.google.gson.annotations.SerializedName;

public class ECGResponse {

    @SerializedName("Body")
    public final Body body;

    public ECGResponse(Body body) {
        this.body = body;
    }

    public static class Body {
        @SerializedName("Samples")
        public final int samples[];
        public Body(int samples[]) {
            this.samples = samples;
        }
    }
}