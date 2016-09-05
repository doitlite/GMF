package com.goldmf.GMFund.controller.internal;

import android.util.SparseArray;
import android.view.View;

import com.goldmf.GMFund.controller.circle.CircleHelper;

import yale.extension.system.SimpleViewHolder;

/**
 * Created by yalez on 2016/7/29.
 */
public interface ChildBinder {

    View itemView();

    ChildBinder bindChildWithTag(String tag, int... layoutIDs);

    <T extends View> T getChildWithTag(String tag);
}
