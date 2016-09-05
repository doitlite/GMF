package com.goldmf.GMFund.extension;

import android.graphics.Bitmap;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by yale on 15/7/30.
 */
public class FileExtension {
    private FileExtension() {
    }

    public static String md5FromFile(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] data = readDataOrNilFromFile(file);
            if (data != null) {
                byte[] hash = digest.digest(data);
                StringBuilder hex = new StringBuilder(hash.length * 2);
                for (byte b : hash) {
                    if ((b & 0xFF) < 0x10) hex.append("0");
                    hex.append(Integer.toHexString(b & 0xFF));
                }
                return hex.toString();
            }
        } catch (NoSuchAlgorithmException ignored) {
        }
        return null;
    }

    public static File createFile(String path, boolean deleteIfExist) {
        File file = new File(path);
        if (deleteIfExist && file.exists()) {
            if (file.isDirectory()) {
                return null;
            }
            file.delete();
        }
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
            return file;
        } catch (IOException e) {
            return null;
        }
    }

    public static void writeDataToFile(File file, byte[] data, boolean overrideIfExist) {
        if (file == null || data == null) return;
        if (file.exists() && !overrideIfExist) return;
        if (file.exists() && file.isDirectory()) return;

        file.getParentFile().mkdirs();
        BufferedOutputStream output = null;
        try {
            output = new BufferedOutputStream(new FileOutputStream(file));
            output.write(data);
            output.flush();
        } catch (Exception ignored) {
        } finally {
            close(output);
        }
    }

    public static void writeDataToFile(File file, InputStream stream, boolean overrideIfExist) {
        if (file == null || stream == null) return;
        if (file.exists() && !overrideIfExist) return;
        if (file.exists() && file.isDirectory()) return;

        byte[] buffer = new byte[1024 * 10];
        int read_count = 0;

        file.getParentFile().mkdirs();
        BufferedOutputStream output = null;
        try {
            output = new BufferedOutputStream(new FileOutputStream(file));
            while ((read_count = stream.read(buffer)) != -1) {
                output.write(buffer, 0, read_count);
            }
            output.flush();
        } catch (Exception ignored) {
        } finally {
            close(output);
            close(stream);
        }
    }

    public static void writeDataToFile(File file, Bitmap bitmap, boolean overrideIfExist, Bitmap.CompressFormat format, int quality) {
        if (file == null || bitmap == null) return;
        if (file.exists() && !overrideIfExist) return;
        if (file.exists() && file.isDirectory()) return;

        file.getParentFile().mkdirs();
        BufferedOutputStream output = null;
        try {
            output = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(format, quality, output);
            output.flush();
        } catch (Exception ignored) {
        } finally {
            close(output);
        }
    }

    public static byte[] readDataOrNilFromFile(File file) {
        if (file == null || !file.exists() || file.isDirectory()) return null;

        BufferedInputStream input = null;
        try {
            input = new BufferedInputStream(new FileInputStream(file));
            int count = input.available();
            byte[] data = new byte[count];
            input.read(data, 0, count);
            input.close();
            input = null;
            return data;
        } catch (Exception ignored) {
        } finally {
            close(input);
        }

        return null;
    }

    public static void zip(File from, File to) {
        if (from == null || to == null || !from.exists() || (to.exists() && to.isDirectory()))
            return;

        byte[] buffer = new byte[1024];

        if (from.isDirectory()) {
            List<File> files = Stream.of(from.listFiles()).filter(it -> !it.isDirectory()).collect(Collectors.toList());
            if (!files.isEmpty()) {
                ZipOutputStream output = null;
                FileInputStream input = null;
                try {
                    output = new ZipOutputStream(new FileOutputStream(to));
                    int readCount;
                    for (File f : files) {
                        ZipEntry entry = new ZipEntry(f.getName());
                        entry.setSize(f.length());
                        entry.setTime(f.lastModified());
                        output.putNextEntry(entry);

                        input = new FileInputStream(f);
                        while ((readCount = input.read(buffer)) != -1) {
                            output.write(buffer, 0, readCount);
                        }
                        close(input);
                        output.flush();
                    }
                    output.flush();
                    close(output);
                } catch (IOException ignored) {
                } finally {
                    close(input);
                    close(output);
                }
            }
        } else {
            ZipOutputStream output = null;
            FileInputStream input = null;
            try {
                output = new ZipOutputStream(new FileOutputStream(to));
                int readCount;
                ZipEntry entry = new ZipEntry(from.getName());
                entry.setSize(from.length());
                entry.setTime(from.lastModified());
                output.putNextEntry(entry);

                input = new FileInputStream(from);
                while ((readCount = input.read(buffer)) != -1) {
                    output.write(buffer, 0, readCount);
                }
                output.flush();
                close(input);
                close(output);
            } catch (IOException ignored) {
            } finally {
                close(input);
                close(output);
            }
        }
    }

    private static void close(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignored) {
            }
        }
    }
}
