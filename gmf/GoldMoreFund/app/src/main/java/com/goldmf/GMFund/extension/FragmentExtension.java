package com.goldmf.GMFund.extension;

import android.support.v4.app.Fragment;

import com.goldmf.GMFund.controller.BaseFragment;
import com.goldmf.GMFund.controller.SimpleFragment;

import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;

/**
 * Created by yale on 16/4/7.
 */
public class FragmentExtension {
    private FragmentExtension() {
    }

    public static SimpleFragment getParentSimpleFragment(Fragment fragment) {
        return safeGet(() -> (SimpleFragment) fragment.getParentFragment(), null);
    }

    public static BaseFragment getParentBaseFragment(Fragment fragment) {
        return safeGet(() -> (BaseFragment) fragment.getParentFragment(), null);
    }
}
