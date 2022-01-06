package com.test.smartbandage.model;

/**
 * Created by lipponep on 22.11.2017.
 */

import com.google.gson.annotations.SerializedName;

public class HRResponse {

    @SerializedName("Body")
    public final Body body;

    public HRResponse(Body body) {
        this.body = body;
    }

    public static class Body {
        @SerializedName("average")
        public final float average;

        @SerializedName("rrData")
        public final int rrData[];
        public Body(float average, int rrData[]) {
            this.rrData = rrData; this.average = average;
        }
    }
}
