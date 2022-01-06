package com.test.smartbandage.utils;

/**
 * Helper class for formatting Contract and Path
 */
public class FormatHelper {

    private FormatHelper() {}

    public static String formatContractToJson(String serial, String uri) {
        StringBuilder sb = new StringBuilder();
        return sb.append("{\"Uri\": \"").append(serial).append("/").append(uri).append("\"}").toString();
    }

    public static String pathFormatHelper(String serial, String path) {
        final StringBuilder sb = new StringBuilder();
        // TODO: fix this because different than movesense
        return sb.append(serial).append("/").append(path).toString();
    }
}
