package com.goldmf.GMFund.util;

import android.graphics.Bitmap;

import com.goldmf.GMFund.controller.internal.SignalColorHolder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;

import static com.goldmf.GMFund.controller.internal.SignalColorHolder.*;

/**
 * Created by yalez on 2016/8/5.
 */
public class ZxingUtil {
    private ZxingUtil() {
    }

    public static Bitmap generateQRCode(String content, int width, int height) {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 0);

        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    pixels[x + y * width] = matrix.get(x, y) ? BLACK_COLOR : WHITE_COLOR;
                }
            }
            return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.RGB_565);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return null;
    }
}
