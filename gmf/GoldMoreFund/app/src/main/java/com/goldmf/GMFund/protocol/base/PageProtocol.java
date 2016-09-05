package com.goldmf.GMFund.protocol.base;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.protocol.base.PageArray.PageItemIndex;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yale.extension.common.Optional;

import static com.goldmf.GMFund.protocol.base.PageArray.*;

/**
 * Created by cupide on 16/3/11.
 */
public class PageProtocol<T extends PageItemIndex> extends ProtocolBase {

    public interface ParseMoreData<T extends PageArray.PageItemIndex> {

        void onParse(JsonElement data);
    }

    public interface TranslateItem<T extends PageArray.PageItemIndex> {

        T onTranslate(JsonObject data);
    }

    public long firstTS;
    public long lastTS;

    public int pageNumber;
    public int pageSize;

    public long begin;
    public long end;

    public ArrayList<PageProtocolDelItem> delList = new ArrayList<>();

    public String pageCgiUrl;
    public Map<String, String> pageCgiParam;
    public Class<T> classOfT;
    public PageArray<T> page;

    public PageProtocol(ProtocolCallback callback) {
        super(callback);
    }

    public ParseMoreData parseMoreData = null;
    public TranslateItem<T> translateItem = null;

    @Override
    protected boolean parseData(JsonElement dic) {
        if (super.returnCode == 0) {
            page = ParsePageArray(dic, classOfT, this.byPage(), delList, this.translateItem);

            if (parseMoreData != null) {
                parseMoreData.onParse(dic);
            }
        }
        return (super.returnCode == 0);
    }

    @Override
    protected String getUrl() {
        return pageCgiUrl;
    }

    @Override
    protected Map<String, Object> getPostData() {
        return null;
    }

    @Override
    protected Map<String, String> getParam() {
        HashMap<String, String> param = new HashMap<>();

        param.putAll(Optional.of(this.pageCgiParam).or(Collections.emptyMap()));

        if (this.byPage()) {
            param.put("page_no", String.valueOf(this.pageNumber));
            param.put("page_size", String.valueOf(this.pageSize));
        } else {
            param.put("first_ts", String.valueOf(this.firstTS));
            param.put("last_ts", String.valueOf(this.lastTS));

            if (begin != 0 && end != 0) {
                param.put("begin", String.valueOf(this.begin));
                param.put("end", String.valueOf(this.end));
            }
        }

        return param;
    }

    private boolean byPage() {
        return this.pageSize != 0;
    }


    public static <T extends PageItemIndex> PageArray<T> ParsePageArray(JsonElement dic,
                                                                        Class<T> classOfT,
                                                                        boolean byPage,
                                                                        ArrayList<PageProtocolDelItem> delList,
                                                                        TranslateItem<T> translateItem) {

        if (dic.isJsonObject()) {

            JsonArray listJson = new JsonArray();
            if (GsonUtil.has(dic, "list"))
                listJson = GsonUtil.getAsJsonArray(dic, "list");
            else if (GsonUtil.has(dic, "message_list")) {
                listJson = GsonUtil.getAsJsonArray(dic, "message_list");
            }

            List<T> list = null;
            if(translateItem == null) {
                list = getListArray(listJson, classOfT);
            }
            else{
                list = getListArray(listJson, translateItem);
            }


            if (byPage) {
                //有page的
                int pageNumber = GsonUtil.getAsInt(dic.getAsJsonObject(), "cur_page_no");
                int pageCount = GsonUtil.getAsInt(dic.getAsJsonObject(), "total_page_count");
                if (pageCount == 0 && pageNumber == 0) {
                    pageCount = 1;
                    pageNumber = 1;
                }
                return new PageArray<>(list, pageNumber, pageCount);

            } else {
                long fTS = GsonUtil.getAsLong(dic, "meta", "first_ts");
                long lTS = GsonUtil.getAsLong(dic, "meta", "last_ts");
                boolean more = GsonUtil.getAsInt(dic, "meta", "more") == 1;

                //获取delete_list
                JsonArray delete_list = GsonUtil.getAsJsonArray(dic, "meta", "delete_list");
                if (delete_list != null && delete_list.size() > 0 && delList != null) {
                    for (JsonElement temp : delete_list) {
                        String text = GsonUtil.getAsString(temp);
                        delList.add(new PageProtocolDelItem(text));
                    }
                }

                if (list != null && list.size() > 0) {
                    return new PageArray<>(list, fTS, lTS, more);
                } else {
                    //处理count == 0 的情况
                    return new PageArray<>(list, 0, 0, false);
                }
            }
        }
        return new PageArray<>();
    }

    public static <T extends PageItemIndex> List<T> getListArray(JsonArray array, TranslateItem<T> translateItem) {

        if (array == null)
            return new ArrayList<>();

        try {
            if (translateItem != null) {

                return Stream.of(Optional.of(array).or(new JsonArray()))
                        .map(it -> GsonUtil.getAsJsonObject(it))
                        .map(it -> translateItem.onTranslate(it))
                        .filter(it -> it != null)
                        .collect(Collectors.<T>toList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public static <T extends PageItemIndex> List<T> getListArray(JsonArray array, Class<T> classOfT) {

        if (array == null)
            return new ArrayList<>();

        try {
            Method method = classOfT.getMethod("translate", JsonArray.class);

            Object invoke = method.invoke(null, array);
            List<T> list = new ArrayList<>((List<T>) invoke);

            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}
