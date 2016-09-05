package com.goldmf.GMFund.manager.dev;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.goldmf.GMFund.util.FormatUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by yale on 15/10/26.
 */
public class AccessLoggerWriter {
    private BufferedWriter mOutput;
    private boolean mIsClosed = true;
    private final Handler mHandler;

    public AccessLoggerWriter(OutputStream output) {
        if (output != null) {
            mOutput = new BufferedWriter(new OutputStreamWriter(output));
            mIsClosed = false;
        }
        HandlerThread handlerThread = new HandlerThread("AccessLog", Thread.NORM_PRIORITY);
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }

    public void write(int level, String tag, String message) {
        if (mOutput != null && !mIsClosed) {
            mHandler.post(() -> {

                try {
                    if (!mIsClosed) {
                        mOutput.write(FormatUtil.formatSecond("MM-dd HH:mm:ss:SSS ") + toString(level) + "/" + tag + ": " + message + "\n");
                        mOutput.flush();
                    }
                } catch (Exception e) {
                    close();
                }
            });
        }
    }

    public void reset(OutputStream output) {
        close();
        if (output != null) {
            mHandler.post(() -> {
                mOutput = new BufferedWriter(new OutputStreamWriter(output));
                mIsClosed = false;
            });
        }
    }

    public void close() {
        mHandler.post(() -> {
            mIsClosed = true;
            if (mOutput != null) {
                try {
                    mOutput.close();
                } catch (IOException ignored) {
                }
            }
        });
    }

    private static String toString(int level) {
        if (level == Log.DEBUG) return "D";
        else if (level == Log.VERBOSE) return "V";
        else if (level == Log.INFO) return "I";
        else if (level == Log.WARN) return "W";
        else if (level == Log.ASSERT) return "A";
        else if (level == Log.ERROR) return "E";
        else return "U";
    }
}
