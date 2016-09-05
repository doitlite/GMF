package com.goldmf.GMFund.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by yale on 16/6/28.
 */
public class GzipUtil {
    private GzipUtil() {
    }

    @SuppressWarnings("ConstantConditions")
    public static byte[] compress(String text) {
        byte[] data = new byte[0];

        ByteArrayOutputStream byteOutput = null;
        GZIPOutputStream output = null;
        try {
            byteOutput = new ByteArrayOutputStream();
            output = new GZIPOutputStream(byteOutput);
            output.write(text.getBytes("UTF-8"));
            output.flush();
            output.finish();
            data = byteOutput.toByteArray();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        } finally {
            close(output);
            close(byteOutput);
        }

        return data;
    }

    @SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
    public static String decompress(byte[] data) {
        String ret = "";

        GZIPInputStream input = null;
        ByteArrayOutputStream output = null;
        try {
            output = new ByteArrayOutputStream();
            input = new GZIPInputStream(new ByteArrayInputStream(data));
            byte[] buffer = new byte[1024];
            int read_count;
            while ((read_count = input.read(buffer)) > 0) {
                output.write(buffer, 0, read_count);
            }
            output.flush();
            ret = new String(output.toByteArray(), "UTF-8");
        } catch (Exception ignored) {
            ignored.printStackTrace();
        } finally {
            close(input);
            close(output);
        }

        return ret;
    }

    private static void close(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception ignored) {
            }
        }
    }
}
