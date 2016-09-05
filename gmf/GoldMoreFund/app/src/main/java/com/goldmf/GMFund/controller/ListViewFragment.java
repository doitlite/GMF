package com.goldmf.GMFund.controller;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.extension.ViewExtension;
import com.goldmf.GMFund.widget.EmbedProgressView;

/**
 * Created by yale on 15/9/7.
 */
public class ListViewFragment extends SimpleFragment {

    protected ListView mListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_listview, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListView = (ListView) view.findViewById(android.R.id.list);
    }

}
