package com.test.smartbandage.model;

import com.google.gson.annotations.SerializedName;


public class InfoResponse {

    @SerializedName("Content")
    public final Content content;

    public InfoResponse(Content content) {
        this.content = content;
    }

    public static class Content {

        @SerializedName("LED Mode")
        public String[] LEDMode;

        @SerializedName("Pulse Width")
        public int[] pulseWidth;

        @SerializedName("LED Brightness")
        public String[] brightness;

        @SerializedName("SampleRates")
        public final int[] sampleRates;

        @SerializedName("Average Reading")
        public int[] avgReading;

        @SerializedName("Ranges")
        public final int ranges[];

        /**
         * Default Setting for the Pulse Oximeter Activity
         */
        public Content() {
            this.sampleRates = new int[]{50, 100, 200, 400, 800, 1000, 1600, 3200};
            this.ranges = new int[]{2048, 4096, 8192, 16384};
            this.LEDMode = new String[]{"Red LED Only", "Red + IR LED", "Red + IR + Green"};
            this.pulseWidth = new int[]{69, 118, 215, 411};
            this.brightness = new String[]{"Low", "Medium", "High", "Full"};
            this.avgReading = new int[]{1, 2, 4, 8, 16, 32};

        }

        /**
         * Default content params
         * @param sampleRates
         * @param ranges
         */
        public Content(int[] sampleRates, int [] ranges) {
             this.sampleRates = sampleRates;
             this.ranges = ranges;

        }


        /**
         * Constructor to manually assign Spinner settings
         *
         * @param sampleRates
         * @param ranges
         * @param LEDMode
         * @param pulseWidth
         * @param brightness
         * @param avgReading
         */
        public Content(int[] sampleRates, int [] ranges, String[] LEDMode, int[] pulseWidth, String [] brightness, int[] avgReading) {
            this.sampleRates = sampleRates;
            this.ranges = ranges;
            this.LEDMode = LEDMode;
            this.pulseWidth = pulseWidth;
            this.brightness = brightness;
            this.avgReading = avgReading;


        }

    }
}
