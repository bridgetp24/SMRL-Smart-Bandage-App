package com.test.smartbandage.model;


import com.google.gson.annotations.SerializedName;

public class EcgModel {

    @SerializedName("Body")
    public final Body mBody;

    public EcgModel(Body body) {
        mBody = body;
    }

    public class Body {

        @SerializedName("Samples")
        public final int[] data;

        @SerializedName("Timestamp")
        public final Long timestamp;

        public Body(int[] data, Long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }

        public int[] getData() {
            return data;
        }
    }

    public Body getBody() {
        return mBody;
    }
}
