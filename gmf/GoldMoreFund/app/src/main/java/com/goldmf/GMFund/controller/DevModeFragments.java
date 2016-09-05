package com.goldmf.GMFund.controller;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.goldmf.GMFund.MyConfig;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.controller.dialog.SigningDialog;
import com.goldmf.GMFund.extension.FileExtension;
import com.goldmf.GMFund.manager.dev.AccessLogManager;
import com.goldmf.GMFund.manager.dev.RequestLogManager;
import com.goldmf.GMFund.manager.dev.RequestLogManager.RequestLog;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.widget.QACell;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashSet;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

import static com.goldmf.GMFund.controller.FragmentStackActivity.goBack;
import static com.goldmf.GMFund.controller.FragmentStackActivity.pushFragment;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_FunctionIntroductionPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.BLUE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLACK_COLOR;
import static com.goldmf.GMFund.extension.UIControllerExtension.createErrorDialog;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setGone;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisible;

/**
 * Created by yale on 15/10/26.
 */
public class DevModeFragments {
    private DevModeFragments() {
    }

    public static class DevModeHomeFragment extends BaseFragment {

        private LinearLayout mList;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_list_template, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, BLUE_COLOR);
            findToolbar(this).setBackgroundColor(BLUE_COLOR);
            setupBackButton(this, findToolbar(this));
            updateTitle("开发者模式");

            // bind child views
            mList = v_findView(this, R.id.list);
            updateContentView();
        }

        @SuppressWarnings("unchecked")
        private void updateContentView() {
            final Func2<CharSequence, Action1<View>, HashMap<String, Object>> createItem = (title, clickEvent) -> {
                HashMap<String, Object> ret = new HashMap<>();
                ret.put("title", title);
                ret.put("clickEvent", clickEvent);
                return ret;
            };

            final HashMap<String, Object>[] items = new HashMap[]{
                    createItem.call("Yale的主页", v -> pushFragment(this, new WebViewFragments.WebViewFragment().init("http://188.166.76.220/index.html"))),
                    createItem.call("Rango的主页", v -> pushFragment(this, new WebViewFragments.WebViewFragment().init("http://192.168.0.47:8600"))),
                    createItem.call("设置服务器", v -> pushFragment(this, new SelectHostNameFragment())),
                    createItem.call("请求日志", v -> pushFragment(this, new SelectRequestLogFragment())),
                    createItem.call("显示日志和上传日志", v -> pushFragment(this, new SelectAccessLogFragment())),
                    createItem.call("幻灯片", v -> showActivity(this, an_FunctionIntroductionPage(false))),
                    createItem.call("关闭开发者模式", v -> {
                        MyConfig.setDevModeEnable(false);
                        goBack(this);
                    }),
                    createItem.call("清除图片缓存", v -> {
                        Fresco.getImagePipeline().clearCaches();
                    }),
                    createItem.call("弹出签到对话框", v -> {
                        new SigningDialog(getActivity()).show();
                    }),
                    createItem.call("设置止盈止损", v -> pushFragment(this, new StockTradeFragments.StockSettingFragment()))
            };

            mList.removeAllViewsInLayout();
            Stream.of(items)
                    .map(map -> createBasicCell(mList, (CharSequence) map.get("title"), (Action1<View>) map.get("clickEvent")))
                    .forEach(cell -> {
                        mList.addView(cell);
                        mList.addView(createLine(getActivity(), true));
                    });
            mList.addView(createLine(getActivity(), false), 0);
            mList.removeViewAt(mList.getChildCount() - 1);
            mList.addView(createLine(getActivity(), false));
        }
    }

    public static class SelectRequestLogFragment extends BaseFragment {

        private LinearLayout mList;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_list_template, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, BLUE_COLOR);
            findToolbar(this).setBackgroundColor(BLUE_COLOR);
            setupBackButton(this, findToolbar(this));
            updateTitle("选择请求日志");

            // bind child views
            mList = v_findView(this, R.id.list);
            updateContentView();
        }

        @SuppressWarnings("unchecked")
        private void updateContentView() {

            mList.removeAllViewsInLayout();
            Stream.of(RequestLogManager.getLogs())
                    .map(log -> createBasicCell(mList, log.mMethod + "    " + log.mURL.encodedPath(), view -> pushFragment(this, new RequestLogDetailFragment().init(log))))
                    .forEach(cell -> {
                        mList.addView(cell);
                        mList.addView(createLine(getActivity(), true));
                    });
            mList.addView(createLine(getActivity(), false), 0);
            mList.removeViewAt(mList.getChildCount() - 1);
            mList.addView(createLine(getActivity(), false));
        }
    }

    public static class RequestLogDetailFragment extends BaseFragment {
        private RequestLog mRequestLog;

        public RequestLogDetailFragment init(RequestLog requestLog) {
            mRequestLog = requestLog;
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_empty, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, BLUE_COLOR);
            findToolbar(this).setBackgroundColor(BLUE_COLOR);
            setupBackButton(this, findToolbar(this));
            updateTitle("日志详情");

            updateContentView(v_findView(this, R.id.section_content));
        }

        private void updateContentView(FrameLayout rootView) {
            Context ctx = rootView.getContext();

            rootView.removeAllViewsInLayout();
            ScrollView scrollView = new ScrollView(ctx);
            rootView.addView(scrollView, -1, -2);

            LinearLayout parent = new LinearLayout(ctx);
            parent.setOrientation(LinearLayout.VERTICAL);
            scrollView.addView(parent, -1, -2);

            parent.addView(new View(ctx), -1, dp2px(20));

            QACell requestCell = new QACell(ctx);
            requestCell.setQuestion("Request Message");
            requestCell.setAnswer(mRequestLog.requestMessage());
            parent.addView(requestCell, -1, -2);

            parent.addView(new View(ctx), -1, dp2px(20));
            QACell responseCodeCell = new QACell(ctx);
            responseCodeCell.setQuestion("Response Code");
            responseCodeCell.setAnswer("" + mRequestLog.mResponseCode);
            parent.addView(responseCodeCell, -1, -2);

            parent.addView(new View(ctx), -1, dp2px(20));
            QACell answerCell = new QACell(ctx);
            answerCell.setQuestion("Response Message");

            String responseMessage = mRequestLog.responseMessage();
            try {
                JSONObject jsonObj = new JSONObject(responseMessage);
                responseMessage = jsonObj.toString(4);
            } catch (Exception ignored) {
            }
            answerCell.setAnswer(responseMessage);
            parent.addView(answerCell, -1, -2);
            parent.addView(new View(ctx), -1, dp2px(20));
        }
    }

    public static class SelectAccessLogFragment extends BaseFragment {

        private LinearLayout mList;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_list_template, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, BLUE_COLOR);
            findToolbar(this).setBackgroundColor(BLUE_COLOR);
            setupBackButton(this, findToolbar(this));
            updateTitle("选择请求日志");

            // bind child views
            mList = v_findView(this, R.id.list);
            updateContentView();
        }

        @SuppressWarnings("unchecked")
        private void updateContentView() {
            File[] accessLogList = getAccessLogList();
            if (accessLogList != null) {
                mList.removeAllViewsInLayout();
                Stream.of(accessLogList)
                        .sorted((lhs, rhs) -> (int) (rhs.lastModified() - lhs.lastModified()))
                        .map(file -> createBasicCell(mList, file.getName(), v -> pushFragment(this, new AccessLogDetailFragment().init(file.getPath()))))
                        .forEach(cell -> {
                            mList.addView(cell);
                            mList.addView(createLine(getActivity(), true));
                        });
                mList.addView(createLine(getActivity(), false), 0);
                mList.removeViewAt(mList.getChildCount() - 1);
                mList.addView(createLine(getActivity(), false));
            }
        }

        private static File[] getAccessLogList() {
            File accessLogDir = AccessLogManager.getDefaultLogFileParentDir();
            if (accessLogDir.exists()) {
                return accessLogDir.listFiles();
            } else {
                return null;
            }
        }
    }

    public static class AccessLogDetailFragment extends SimpleFragment {
        private String mLogFilePath;

        public AccessLogDetailFragment init(String logFilePath) {
            mLogFilePath = logFilePath;
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_empty, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            updateTitle("日志详情");
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            findToolbar(this).setBackgroundColor(STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));

            File logFile = new File(mLogFilePath);
            if (logFile.exists()) {

                v_setGone(mContentSection);
                v_setVisible(mLoadingSection);

                //TODO 改成分段加载，现在一次全部加载出来太慢了
                Observable.just(new String(FileExtension.readDataOrNilFromFile(logFile)))
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(content -> {
                            ScrollView scrollView = new ScrollView(getActivity());
                            ((ViewGroup) mContentSection).addView(scrollView, -1, -2);

                            TextView contentLabel = new TextView(getActivity());
                            contentLabel.setTextSize(12);
                            contentLabel.setTextColor(TEXT_BLACK_COLOR);
                            contentLabel.setText(content);

                            scrollView.addView(contentLabel, -1, -2);
                            v_setGone(mLoadingSection);
                            v_setVisible(mContentSection);
                        });

            } else {
                createErrorDialog(this, "文件不存在").show();
            }
        }
    }

    public static class SelectHostNameFragment extends BaseFragment {

        private static final String KEY_PUBLIC_HOST = "public";
        private static final String KEY_DEVELOP1_HOST = "develop1";
        private static final String KEY_DEVELOP2_HOST = "develop2";
        private static final String KEY_DEVELOP3_HOST = "develop3";
        private static final String KEY_PRE_PUBLIC_HOST = "pre_public";
        private static final String KEY_CONSIS_HOST = "consis";
        private static final String KEY_AUSTIN_HOST = "austin";
        private static final String KEY_RANGO_HOST = "rango";
        private static final String KEY_LAIR_HOST = "lair";
        private static final String KEY_BUTY_HOST = "buty";
        private static final String KEY_BUTY_REAL_HOST = "buty_real";
        private static HashMap<String, Integer> sHostIdxMap = new HashMap<>();
        private static HashMap<Integer, String> sReverseHostIdxMap = new HashMap<>();

                                                static {
            sHostIdxMap.put(KEY_DEVELOP1_HOST, 1);
            sHostIdxMap.put(KEY_PRE_PUBLIC_HOST, 2);
            sHostIdxMap.put(KEY_PUBLIC_HOST, 3);
            sHostIdxMap.put(KEY_RANGO_HOST, 4);
            sHostIdxMap.put(KEY_CONSIS_HOST, 5);
            sHostIdxMap.put(KEY_AUSTIN_HOST, 6);
            sHostIdxMap.put(KEY_LAIR_HOST, 7);
            sHostIdxMap.put(KEY_BUTY_HOST, 8);
            sHostIdxMap.put(KEY_DEVELOP2_HOST, 9);
            sHostIdxMap.put(KEY_DEVELOP3_HOST, 10);

            sReverseHostIdxMap.put(1, KEY_DEVELOP1_HOST);
            sReverseHostIdxMap.put(2, KEY_PRE_PUBLIC_HOST);
            sReverseHostIdxMap.put(3, KEY_PUBLIC_HOST);
            sReverseHostIdxMap.put(4, KEY_RANGO_HOST);
            sReverseHostIdxMap.put(5, KEY_CONSIS_HOST);
            sReverseHostIdxMap.put(6, KEY_AUSTIN_HOST);
            sReverseHostIdxMap.put(7, KEY_LAIR_HOST);
            sReverseHostIdxMap.put(8, KEY_BUTY_HOST);
            sReverseHostIdxMap.put(9, KEY_DEVELOP2_HOST);
            sReverseHostIdxMap.put(10, KEY_DEVELOP3_HOST);
        }

        private LinearLayout mList;
        private LinkedHashSet<Pair<String, CheckBox>> mCheckBoxSet = new LinkedHashSet<>();

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_list_template, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, BLUE_COLOR);
            findToolbar(this).setBackgroundColor(BLUE_COLOR);
            setupBackButton(this, findToolbar(this));
            updateTitle("设置服务器");

            // bind child views
            mList = v_findView(this, R.id.list);

            updateContentView();

            String tag = sReverseHostIdxMap.get(MyConfig.CURRENT_HOST_NAME_IDX);
            if (!TextUtils.isEmpty(tag)) {
                setCheck(tag);
            }
        }

        @SuppressWarnings("unchecked")
        private void updateContentView() {
            final Func2<String, CharSequence, HashMap<String, Object>> createItem = (tag, title) -> {
                HashMap<String, Object> ret = new HashMap<>();
                ret.put("tag", tag);
                ret.put("title", title);
                ret.put("clickEvent", (Action1<CheckBox>) checkBox -> {
                    setCheck(tag);
                    if (MineManager.getInstance().isLoginOK()) {
                        MineManager.getInstance().logout();
                    }
                    int hostIndex = sHostIdxMap.get(tag);
                    MyConfig.setCurrentHostName(hostIndex);
                });
                return ret;
            };

            HashMap<String, Object>[] items = new HashMap[]{
                    createItem.call(KEY_PUBLIC_HOST, "正式服务器"),
                    createItem.call(KEY_PRE_PUBLIC_HOST, "预发布服务器"),
                    createItem.call(KEY_DEVELOP1_HOST, "测试服务器32080"),
                    createItem.call(KEY_DEVELOP2_HOST, "测试服务器42081"),
                    createItem.call(KEY_DEVELOP3_HOST, "测试服务器29080"),
                    createItem.call(KEY_CONSIS_HOST, "consis的服务器"),
                    createItem.call(KEY_AUSTIN_HOST, "austin的服务器"),
                    createItem.call(KEY_RANGO_HOST, "rango的服务器"),
                    createItem.call(KEY_LAIR_HOST, "lair的服务器"),
                    createItem.call(KEY_BUTY_HOST, "buty的服务器"),
                    createItem.call(KEY_BUTY_REAL_HOST, "buty的实盘交易服务器")
            };


            mList.removeAllViewsInLayout();
            mCheckBoxSet.clear();
            Stream.of(items)
                    .map(map -> {
                        Pair<View, CheckBox> pair = createCellWithCheckableStyle(mList, (CharSequence) map.get("title"), (Action1<CheckBox>) map.get("clickEvent"));
                        View cell = pair.first;
                        CheckBox checkBox = pair.second;
                        mCheckBoxSet.add(Pair.create((String) map.get("tag"), checkBox));
                        return cell;
                    })
                    .forEach(it -> mList.addView(it));
        }

        private void setCheck(String tag) {
            for (Pair<String, CheckBox> pair : mCheckBoxSet) {
                pair.second.setChecked(pair.first.equals(tag));
            }

        }
    }

    private static View createBasicCell(LinearLayout parent, CharSequence title, Action1<View> clickEvent) {
        Context ctx = parent.getContext();
        RelativeLayout cell = new RelativeLayout(ctx);
        cell.setBackgroundResource(R.drawable.sel_cell_bg_default);
        {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp2px(48));
            cell.setLayoutParams(params);
        }

        {
            TextView titleLabel = new TextView(ctx);
            titleLabel.setTextSize(16);
            titleLabel.setTextColor(TEXT_BLACK_COLOR);
            titleLabel.setText(title);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -2);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            params.leftMargin = dp2px(16);
            titleLabel.setLayoutParams(params);
            cell.addView(titleLabel);
        }

        {
            ImageView arrowImage = new ImageView(ctx);
            arrowImage.setImageResource(R.mipmap.ic_arrow_right_dark);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
            params.rightMargin = dp2px(16);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            arrowImage.setLayoutParams(params);
            cell.addView(arrowImage);
        }

        if (clickEvent != null)
            v_setClick(cell, clickEvent::call);
        return cell;
    }

    private static View createLine(Context ctx, boolean hasMargin) {
        View line = new View(ctx);
        line.setBackgroundColor(ctx.getResources().getColor(R.color.gmf_sep_Line));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, 1);
        if (hasMargin)
            params.leftMargin = dp2px(16);
        line.setLayoutParams(params);
        return line;
    }

    private static Pair<View, CheckBox> createCellWithCheckableStyle(LinearLayout parent, CharSequence title, Action1<CheckBox> clickEvent) {
        Context ctx = parent.getContext();
        RelativeLayout cell = new RelativeLayout(ctx);
        cell.setBackgroundResource(R.drawable.sel_cell_bg_default);
        {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp2px(48));
            cell.setLayoutParams(params);
        }

        {
            TextView titleLabel = new TextView(ctx);
            titleLabel.setTextSize(16);
            titleLabel.setTextColor(TEXT_BLACK_COLOR);
            titleLabel.setText(title);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -2);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            params.leftMargin = dp2px(16);
            titleLabel.setLayoutParams(params);
            cell.addView(titleLabel);
        }

        CheckBox checkBox = new CheckBox(ctx);
        checkBox.setFocusableInTouchMode(false);
        checkBox.setClickable(false);
        {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
            params.rightMargin = dp2px(16);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            checkBox.setLayoutParams(params);
            cell.addView(checkBox);
        }

        if (clickEvent != null)
            v_setClick(cell, v -> clickEvent.call(checkBox));

        return Pair.create(cell, checkBox);
    }


    public static class TempFragment extends SimpleFragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_temp, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
        }
    }
}
