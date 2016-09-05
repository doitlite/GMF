package com.goldmf.GMFund.controller;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.controller.StockChartFragments.StockChartDetailFragment;
import com.goldmf.GMFund.util.UmengUtil;

import static com.goldmf.GMFund.controller.QuotationFragments.StockDetailFragment.TYPE_TLINE_DAY;
import static com.goldmf.GMFund.controller.StockChartFragments.StockChartDetailFragment.sChartTypeChanged;
import static com.goldmf.GMFund.model.LineData.GMFStockAnswer_Authority_No;
import static com.goldmf.GMFund.model.LineData.KLineData.Spec_Type_NONE;

/**
 * Created by Evan on 16/3/5 下午4:34.
 */
public class StockChartActivity extends BaseActivity {
    public static String KEY_FRAGMENT_CLASS_NAME = "gmf_fragment_class_name";

    // 股票交易页专用
    public static final String KEY_STOCK_CHART_TYPE = "stock_chart_type";
    public static final String KEY_STOCK_AUTHORITY_TYPE = "stock_authority_type";
    public static final String KEY_STOCK_SPEC_TYPE = "stock_spec_type";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UmengUtil.stat_click_event(UmengUtil.eEVENTIDEnterFullScreen);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.act_stack_template_no_actionbar);

        if (savedInstanceState == null) {
            String fragmentClassName = getIntent().getStringExtra(KEY_FRAGMENT_CLASS_NAME);
            if (TextUtils.isEmpty(fragmentClassName)) {
                return;
            }

            if (fragmentClassName.equals(StockChartDetailFragment.class.getName())) {
                String stockId = getIntent().getStringExtra(CommonProxyActivity.KEY_STOCK_ID_STRING);
                int chartType = getIntent().getIntExtra(KEY_STOCK_CHART_TYPE, TYPE_TLINE_DAY);
                int authorityType = getIntent().getIntExtra(KEY_STOCK_AUTHORITY_TYPE, GMFStockAnswer_Authority_No);
                int specType = getIntent().getIntExtra(KEY_STOCK_SPEC_TYPE, Spec_Type_NONE);

                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.add(R.id.container_fragment, new StockChartDetailFragment().init(stockId, chartType, authorityType, specType), StockChartDetailFragment.class.getSimpleName());
                transaction.commit();
            } else {
                try {
                    Class clazz = Class.forName(fragmentClassName);
                    BaseFragment fragment = (BaseFragment) clazz.newInstance();

                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction transaction = fm.beginTransaction();
                    transaction.add(R.id.container_fragment, fragment);
                    transaction.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                    finish();
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            StockChartDetailFragment fragment = (StockChartDetailFragment) getSupportFragmentManager().findFragmentByTag(StockChartDetailFragment.class.getSimpleName());
            sChartTypeChanged.onNext(new StockChartFragments.StockTypeShare(fragment.currentAuthorityType, fragment.currentSpecType, fragment.currentSelectedTab));
            supportFinishAfterTransition();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
