package com.goldmf.GMFund.controller;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.extension.ViewExtension;

/**
 * Created by yale on 15/9/7.
 */
public class RecyclerViewFragment extends SimpleFragment {

    protected RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_recyclerview, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = ViewExtension.v_findView(this, R.id.recyclerView);
    }

}
