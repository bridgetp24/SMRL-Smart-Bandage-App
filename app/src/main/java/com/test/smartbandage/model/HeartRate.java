package com.test.smartbandage.model;


import com.google.gson.annotations.SerializedName;

public class HeartRate {

    @SerializedName("Body")
    public final Body body;

    public HeartRate(Body body) {
        this.body = body;
    }

    public static class Body {

        @SerializedName("average")
        public final float average;

        @SerializedName("rrData")
        public final int rrData[];

        public Body(float average, int[] rrData) {
            this.average = average;
            this.rrData = rrData;
        }
    }
}
