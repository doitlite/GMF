package com.goldmf.GMFund.manager.stock;

import android.content.Context;
import android.text.TextUtils;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.extension.FileExtension;
import com.google.gson.Gson;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import static com.goldmf.GMFund.model.StockInfo.StockBrief;

/**
 * Created by yale on 15/7/30.
 * 股票搜索历史的缓存类
 * 用于获取、更新、删除搜索历史记录
 */
public class SearchHistoryStockStore {

    private static SearchHistoryStockStore sInstance;

    private LinkedList<StockBrief> mDataSet;
    private boolean mDataSetChanged;

    public  static SearchHistoryStockStore getInstance() {
        if (sInstance == null) {
            sInstance = new SearchHistoryStockStore();
            sInstance.unarchiveNow();
        }
        return sInstance;
    }

    SearchHistoryStockStore() {
        this.unarchiveNow();
    }

    /**
     * 获取所有搜索记录
     */
    public List<StockBrief> getAllItems() {
        return mDataSet;
    }

    /**
     * 添加搜索记录
     */
    public void appendItem(StockBrief item) {
        if (item != null) {
            if (mDataSet.contains(item)) {
                mDataSet.remove(item);
                mDataSet.add(0, item);
            } else {
                mDataSet.add(0, item);
            }
            mDataSetChanged = true;
        }
    }

    /**
     * 移除搜索记录
     */
    public void deleteItem(StockBrief item) {
        if (item != null && mDataSet.contains(item)) {
            mDataSet.remove(item);
            mDataSetChanged = true;
        }
    }

    /**
     * 保存缓存至本地
     */
    public void archiveNow() {
        if (mDataSetChanged) {
            String path = getSavePath();
            File f = new File(path);
            if (mDataSet == null || mDataSet.isEmpty()) {
                f.delete();
            } else {
                StockBrief[] items = new StockBrief[mDataSet.size()];
                mDataSet.toArray(items);
                byte[] data = new Gson().toJson(items, StockBrief[].class).getBytes();
                FileExtension.writeDataToFile(f, data, true);
            }
        }
    }

    /**
     * 从本地读取缓存
     */
    private void unarchiveNow() {
        String path = getSavePath();
        File f = new File(path);
        byte[] dataOrNil = FileExtension.readDataOrNilFromFile(f);
        if (dataOrNil == null) {
            mDataSet = new LinkedList<>();
        } else {
            StockBrief[] items = new Gson().fromJson(new String(dataOrNil), StockBrief[].class);
            if (items == null || items.length == 0) {
                mDataSet = new LinkedList<>();
            } else {
                List<StockBrief> list = Stream.of(items).filter(it -> !TextUtils.isEmpty(it.code)).collect(Collectors.toList());
                mDataSet = new LinkedList<>(list);
            }
        }
    }

    private static String getSavePath() {
        Context context = MyApplication.SHARE_INSTANCE;
        return context.getCacheDir().getAbsolutePath() + File.separator + "stock_search_history2.dat";
    }
}
