package com.goldmf.GMFund.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.model.User.User_Type;
import com.orhanobut.logger.Logger;

import static com.goldmf.GMFund.extension.IntExtension.anyMatch;
import static com.goldmf.GMFund.extension.ObjectExtension.apply;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_getSizePreDraw;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageUri;
import static com.goldmf.GMFund.extension.ViewExtension.v_updateLayoutParams;

/**
 * Created by yalez on 2016/7/11.
 */
public class UserAvatarView extends RelativeLayout {
    private ImageView mFrameImage;
    private SimpleDraweeView mAvatarImage;

    public UserAvatarView(Context context) {
        this(context, null);
    }

    public UserAvatarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UserAvatarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        GenericDraweeHierarchy hierarchy = GenericDraweeHierarchyBuilder.newInstance(getResources())
                .setPlaceholderImage(R.mipmap.ic_avatar_placeholder, ScalingUtils.ScaleType.FIT_CENTER)
                .setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                .setRoundingParams(new RoundingParams().setRoundAsCircle(true))
                .build();
        mAvatarImage = new SimpleDraweeView(context, hierarchy);
        addView(mAvatarImage, apply(new LayoutParams(-2, -2), params -> {
            params.width = dp2px(this, 31);
            params.height = dp2px(this, 31);
            params.addRule(CENTER_IN_PARENT);
        }));

        mFrameImage = new ImageView(context);
        mFrameImage.setScaleType(ImageView.ScaleType.FIT_XY);
        addView(mFrameImage, apply(new LayoutParams(-2, -2), params -> {
            params.addRule(CENTER_IN_PARENT);
        }));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(dp2px(this, 50), MeasureSpec.EXACTLY);
        }

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(dp2px(this, 50), MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void updateView(String avatarURL, int userType) {
        v_setImageUri(mAvatarImage, avatarURL);

        if (anyMatch(userType, User_Type.Trader)) {
            mFrameImage.setImageResource(R.mipmap.ic_avatar_trader_bg);
            v_updateLayoutParams(mFrameImage, params -> {
                params.width = dp2px(50);
                params.height = dp2px(50);
            });
        } else if (anyMatch(userType, User_Type.Talent)) {
            mFrameImage.setImageResource(R.mipmap.ic_avatar_talent_bg);
            v_updateLayoutParams(mFrameImage, params -> {
                params.width = dp2px(50);
                params.height = dp2px(50);
            });
        } else {
            mFrameImage.setImageResource(0);
            v_updateLayoutParams(mFrameImage, params -> {
                params.width = -2;
                params.height = -2;
            });
        }
    }
}
