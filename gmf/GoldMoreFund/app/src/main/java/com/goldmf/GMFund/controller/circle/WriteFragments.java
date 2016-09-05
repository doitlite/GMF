package com.goldmf.GMFund.controller.circle;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.base.KeepClassProtocol;
import com.goldmf.GMFund.cmd.CMDParser;
import com.goldmf.GMFund.controller.CommonProxyActivity;
import com.goldmf.GMFund.controller.SimpleFragment;
import com.goldmf.GMFund.controller.StockTradeFragments;
import com.goldmf.GMFund.controller.business.ChatController;
import com.goldmf.GMFund.controller.business.FundController;
import com.goldmf.GMFund.controller.dialog.GMFBottomSheet;
import com.goldmf.GMFund.controller.protocol.VCStateDataProtocol;
import com.goldmf.GMFund.extension.ObjectExtension;
import com.goldmf.GMFund.manager.common.CommonManager;
import com.goldmf.GMFund.manager.common.ShareInfo;
import com.goldmf.GMFund.manager.message.MessageManager;
import com.goldmf.GMFund.manager.message.MessageSession;
import com.goldmf.GMFund.manager.message.SendMessage;
import com.goldmf.GMFund.manager.message.UpImageMessage;
import com.goldmf.GMFund.model.CommonDefine;
import com.goldmf.GMFund.model.CommonDefine.PlaceHolder;
import com.goldmf.GMFund.model.Fund;
import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.model.FundBrief.Fund_Status;
import com.goldmf.GMFund.model.FundBrief.Money_Type;
import com.goldmf.GMFund.model.GMFMessage;
import com.goldmf.GMFund.model.StockInfo.StockBrief;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.util.UploadFileUtil;
import com.goldmf.GMFund.widget.GMFProgressDialog;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Action2;
import rx.functions.Func0;
import rx.subjects.PublishSubject;
import yale.extension.common.shape.RoundCornerShape;
import yale.extension.system.SimpleRecyclerViewAdapter;

import static com.goldmf.GMFund.controller.FragmentStackActivity.goBack;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_SelectFundPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_SelectStockPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.PickImageHelper.createPickImageFromCameraIntent;
import static com.goldmf.GMFund.controller.internal.PickImageHelper.createPickImageFromGalleryIntent;
import static com.goldmf.GMFund.controller.internal.PickImageHelper.handlePickImageFromCameraActivityResult;
import static com.goldmf.GMFund.controller.internal.PickImageHelper.handlePickImageFromGalleryActivityResult;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.LINE_BORDER_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.LINE_SEP_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLACK_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.WHITE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.YELLOW_COLOR;
import static com.goldmf.GMFund.extension.IntExtension.anyMatch;
import static com.goldmf.GMFund.extension.MResultExtension.getErrorMessage;
import static com.goldmf.GMFund.extension.ObjectExtension.apply;
import static com.goldmf.GMFund.extension.ObjectExtension.opt;
import static com.goldmf.GMFund.extension.ObjectExtension.safeCall;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.addCellVerticalSpacing;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.addContentInset;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.getSimpleAdapter;
import static com.goldmf.GMFund.extension.SpannableStringExtension.appendImageText;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concat;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concatNoBreak;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setFontSize;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.getScreenSize;
import static com.goldmf.GMFund.extension.UIControllerExtension.hideKeyboardFromWindow;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.UIControllerExtension.showKeyboardFromWindow;
import static com.goldmf.GMFund.extension.UIControllerExtension.showToast;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_addTextChangedListener;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_reviseTouchArea;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageResource;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageUri;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisibility;
import static com.goldmf.GMFund.extension.ViewExtension.v_updateLayoutParams;
import static com.goldmf.GMFund.extension.ViewGroupExtension.v_forEach;
import static com.goldmf.GMFund.model.FundBrief.Fund_Type;
import static com.goldmf.GMFund.util.FormatUtil.formatMoney;
import static com.goldmf.GMFund.util.FormatUtil.formatRemainingTimeOverMonth;
import static java.util.Collections.emptyList;

/**
 * Created by yale on 16/5/18.
 */
public class WriteFragments {
    private static final int REQUEST_CODE_CAMERA = 1024;
    private static final int REQUEST_CODE_GALLERY = 1025;

    private WriteFragments() {
    }

    private static void smoothScrollToView(View view, int scrollX, int scrollY, int minHeight) {
        smoothScrollToView(view, scrollX, scrollY, minHeight, null);
    }

    private static void smoothScrollToView(View view, int scrollX, int scrollY, int minHeight, Action0 finishCallback) {
        if (view.getScrollX() != scrollX || view.getScrollY() != scrollY && (view.getAnimation() == null || view.getAnimation().hasEnded())) {

            Object delegate = new KeepClassProtocol() {
                ViewGroup.LayoutParams params = view.getLayoutParams();

                public void setScrollX(int x) {
                    view.setScrollX(x);
                }

                public int getScrollX() {
                    return view.getScrollX();
                }

                public void setScrollY(int y) {
                    view.setScrollY(y);
                }

                public int getScrollY() {
                    return view.getScrollY();
                }

                public void setHeight(int height) {
                    if (params.height != height) {
                        params.height = height;
                        view.requestLayout();
                    }
                }

                public int getHeight() {
                    return params.height;
                }

            };

            PropertyValuesHolder scrollXHolder = PropertyValuesHolder.ofInt("scrollX", view.getScrollX(), scrollX);
            PropertyValuesHolder scrollYHolder = PropertyValuesHolder.ofInt("scrollY", view.getScrollY(), scrollY);
            PropertyValuesHolder heightHolder = PropertyValuesHolder.ofInt("height", view.getHeight(), minHeight);

            ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(delegate, scrollXHolder, scrollYHolder, heightHolder);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (finishCallback != null) {
                        finishCallback.call();
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    if (finishCallback != null) {
                        finishCallback.call();
                    }
                }
            });
            animator.start();
        } else {
            if (finishCallback != null) {
                finishCallback.call();
            }
        }
    }

    private static void setupExtraContainerWithImage(int maxCount, int destX, File rawImageFile, Bitmap thumbnail, ViewGroup bottomBar, ViewGroup extraContainer,
                                                     Action0 requestMoreImageAction,
                                                     Action1<File> onImageRemoveCallback, boolean scrollToEnd) {
        smoothScrollToView(bottomBar, destX, 0, dp2px(80));

        Context ctx = bottomBar.getContext();

        Action2<LinearLayout, View> onAddImageIndicatorClick = (container, child) -> {
            opt(requestMoreImageAction).consume(it -> it.call());
        };

        Action2<LinearLayout, View> onRemoveImageClick = (container, child) -> {
            container.removeView(child);
            opt(onImageRemoveCallback).consume(it -> it.call(rawImageFile));

            boolean[] notContainImage = new boolean[]{true};
            boolean[] notContainIndicator = new boolean[]{true};
            v_forEach(container, (idx, directChild) -> {
                if (directChild.getTag() != null) {
                    String tag = directChild.getTag().toString();
                    if (tag.equals("indicator")) {
                        notContainIndicator[0] = false;
                    } else if (tag.equals("image")) {
                        notContainImage[0] = false;
                    }
                }
            });

            if (notContainImage[0]) {
                extraContainer.removeAllViewsInLayout();
                smoothScrollToView(bottomBar, 0, 0, dp2px(60));
            } else if (notContainIndicator[0] && container.getChildCount() < maxCount) {
                appendAddImageIndicatorToContainer(container, onAddImageIndicatorClick);
            }
        };

        if (extraContainer.getChildCount() == 0 || extraContainer.getChildAt(0).getTag() == null || !extraContainer.getChildAt(0).getTag().toString().equals("image_section_list")) {
            extraContainer.removeAllViewsInLayout();

            HorizontalScrollView scrollView = new HorizontalScrollView(ctx);
            scrollView.setTag("image_section_list");
            scrollView.setHorizontalScrollBarEnabled(false);

            LinearLayout wrapper = new LinearLayout(ctx);
            wrapper.setOrientation(LinearLayout.HORIZONTAL);
            scrollView.addView(wrapper, new ScrollView.LayoutParams(-1, -1));

            appendImageToContainer(thumbnail, wrapper, onRemoveImageClick);
            if (wrapper.getChildCount() < maxCount) {
                appendAddImageIndicatorToContainer(wrapper, onAddImageIndicatorClick);
            }

            extraContainer.addView(scrollView, new FrameLayout.LayoutParams(-1, -1));
        } else {
            HorizontalScrollView scrollView = (HorizontalScrollView) extraContainer.getChildAt(0);
            LinearLayout wrapper = (LinearLayout) scrollView.getChildAt(0);
            wrapper.removeViewAt(wrapper.getChildCount() - 1);
            appendImageToContainer(thumbnail, wrapper, onRemoveImageClick);

            if (wrapper.getChildCount() < maxCount) {
                appendAddImageIndicatorToContainer(wrapper, onAddImageIndicatorClick);
            }
            scrollView.post(() -> {
//                scrollView.smoothScrollTo(Integer.MAX_VALUE, 0);
                scrollView.fullScroll(ScrollView.FOCUS_RIGHT);
            });
        }
    }

    private static void appendImageToContainer(Bitmap thumbnail, LinearLayout container, Action2<LinearLayout, View> onRemoveImageClick) {
        Context ctx = container.getContext();
        FrameLayout imageSection = new FrameLayout(ctx);
        imageSection.setTag("image");

        ImageView imageView = new ImageView(ctx);
        imageView.setImageBitmap(thumbnail);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageSection.addView(imageView, apply(new FrameLayout.LayoutParams(-1, -1), params -> {
            params.setMargins(dp2px(10), dp2px(10), dp2px(10), dp2px(10));
        }));

        ImageView removeImage = new ImageView(ctx);
        removeImage.setImageResource(R.drawable.ic_remove_dark);
        v_reviseTouchArea(removeImage);
        imageSection.addView(removeImage, apply(new FrameLayout.LayoutParams(-2, -2), params -> params.gravity = Gravity.RIGHT | Gravity.TOP));
        v_setClick(removeImage, v -> {
            opt(onRemoveImageClick).consume(it -> it.call(container, imageSection));
        });

        container.addView(imageSection, new LinearLayout.LayoutParams(dp2px(80), dp2px(80)));
    }

    private static void appendAddImageIndicatorToContainer(LinearLayout container, Action2<LinearLayout, View> onAddImageIndicatorClick) {
        Context ctx = container.getContext();
        FrameLayout addImageSection = new FrameLayout(ctx);
        addImageSection.setTag("indicator");

        ImageView imageView = new ImageView(ctx);
        imageView.setImageResource(R.drawable.ic_circle_add_image);
        addImageSection.addView(imageView, apply(new FrameLayout.LayoutParams(-1, -1), params -> {
            params.setMargins(dp2px(10), dp2px(10), dp2px(10), dp2px(10));
        }));

        container.addView(addImageSection, new LinearLayout.LayoutParams(dp2px(80), dp2px(80)));

        v_setClick(addImageSection, v -> {
            opt(onAddImageIndicatorClick).consume(it -> it.call(container, addImageSection));
        });
    }


    private static void setupExtraContainerWithFund(FundBrief fund, ViewGroup bottomBar, ViewGroup extraContainer, Action0 onRemoveCallback) {
        SelectFundFragment.VM vm = new SelectFundFragment.VM(fund);

        smoothScrollToView(bottomBar, extraContainer.getLeft(), 0, dp2px(80));

        Context ctx = extraContainer.getContext();
        extraContainer.removeAllViewsInLayout();

        FrameLayout wrapper = new FrameLayout(ctx);

        RelativeLayout fundSection = new RelativeLayout(ctx);
        fundSection.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(WHITE_COLOR, 0).border(LINE_BORDER_COLOR, dp2px(0.5f))));
        wrapper.addView(fundSection, apply(new FrameLayout.LayoutParams(-1, -1), params -> params.setMargins(dp2px(10), dp2px(10), dp2px(10), dp2px(10))));

        {
            GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(ctx.getResources())
                    .setPlaceholderImage(ctx.getResources().getDrawable(R.mipmap.ic_award_placeholder))
                    .build();
            SimpleDraweeView fundImage = new SimpleDraweeView(ctx, hierarchy);
            v_setImageUri(fundImage, vm.coverImageURL);
            fundSection.addView(fundImage, apply(new RelativeLayout.LayoutParams(dp2px(40), dp2px(40)), params -> {
                params.leftMargin = dp2px(10);
                params.addRule(RelativeLayout.CENTER_VERTICAL);
            }));

            LinearLayout textSection = new LinearLayout(ctx);
            textSection.setOrientation(LinearLayout.VERTICAL);
            fundSection.addView(textSection, apply(new RelativeLayout.LayoutParams(-1, -2), parmas -> {
                parmas.leftMargin = dp2px(60);
                parmas.rightMargin = dp2px(10);
                parmas.addRule(RelativeLayout.CENTER_VERTICAL);
            }));


            {
                TextView nameLabel = new TextView(ctx);
                nameLabel.setTextSize(16);
                nameLabel.setTextColor(TEXT_BLACK_COLOR);
                nameLabel.setLines(1);
                nameLabel.setEllipsize(TextUtils.TruncateAt.END);
                nameLabel.setText(vm.name);
                textSection.addView(nameLabel, new LinearLayout.LayoutParams(-1, -2));

                Space space = new Space(ctx);
                textSection.addView(space, new LinearLayout.LayoutParams(0, dp2px(4)));

                TextView descLabel = new TextView(ctx);
                descLabel.setTextSize(12);
                descLabel.setTextColor(TEXT_GREY_COLOR);
                descLabel.setLines(1);
                descLabel.setEllipsize(TextUtils.TruncateAt.END);
                descLabel.setText(vm.desc);
                textSection.addView(descLabel, new LinearLayout.LayoutParams(-1, -2));
            }
        }

        ImageView delImage = new ImageView(ctx);
        delImage.setImageResource(R.drawable.ic_remove_dark);
        wrapper.addView(delImage, apply(new FrameLayout.LayoutParams(-2, -2), params -> params.gravity = Gravity.TOP | Gravity.RIGHT));
        v_setClick(delImage, v -> {
            extraContainer.removeAllViewsInLayout();
            smoothScrollToView(bottomBar, 0, 0, dp2px(60));
            onRemoveCallback.call();
        });

        extraContainer.addView(wrapper, new FrameLayout.LayoutParams(-1, -1));

    }

    @SuppressWarnings("deprecation")
    private static void setupExtraContainerWithStock(StockBrief stock, ViewGroup bottomBar, ViewGroup extraContainer, Action0 onRemoveCallback) {
        smoothScrollToView(bottomBar, extraContainer.getLeft(), 0, dp2px(80));

        Context ctx = extraContainer.getContext();
        extraContainer.removeAllViewsInLayout();

        FrameLayout wrapper = new FrameLayout(ctx);

        RelativeLayout stockSection = new RelativeLayout(ctx);
        stockSection.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(WHITE_COLOR, 0).border(LINE_BORDER_COLOR, dp2px(0.5f))));
        wrapper.addView(stockSection, apply(new FrameLayout.LayoutParams(-1, -1), params -> params.setMargins(dp2px(10), dp2px(10), dp2px(10), dp2px(10))));

        {
            ImageView stockImage = new ImageView(ctx);
            stockImage.setImageResource(R.mipmap.ic_stock_default);
            stockSection.addView(stockImage, apply(new RelativeLayout.LayoutParams(dp2px(40), dp2px(40)), params -> {
                params.leftMargin = dp2px(10);
                params.addRule(RelativeLayout.CENTER_VERTICAL);
            }));

            TextView stockNameAndCodeLabel = new TextView(ctx);
            CharSequence text = safeGet(() -> concat(setFontSize(setColor(stock.name, TEXT_BLACK_COLOR), sp2px(16)), setFontSize(setColor(stock.index, TEXT_GREY_COLOR), sp2px(12))), "");
            stockNameAndCodeLabel.setText(text);
            stockSection.addView(stockNameAndCodeLabel, apply(new RelativeLayout.LayoutParams(-1, -2), params -> {
                params.leftMargin = dp2px(60);
                params.rightMargin = dp2px(10);
                params.addRule(RelativeLayout.CENTER_VERTICAL);
            }));
        }

        ImageView delImage = new ImageView(ctx);
        delImage.setImageResource(R.drawable.ic_remove_dark);
        wrapper.addView(delImage, apply(new FrameLayout.LayoutParams(-2, -2), params -> params.gravity = Gravity.TOP | Gravity.RIGHT));
        v_setClick(delImage, v -> {
            extraContainer.removeAllViewsInLayout();
            smoothScrollToView(bottomBar, 0, 0, dp2px(60));
            onRemoveCallback.call();
        });

        extraContainer.addView(wrapper, new FrameLayout.LayoutParams(-1, -1));
    }

    private static void createBottomBarCell(Context ctx, LinearLayout bottomBar, int iconResID, CharSequence name, View.OnClickListener onClickListener) {
        LinearLayout cell = new LinearLayout(ctx);
        cell.setOrientation(LinearLayout.HORIZONTAL);
        cell.setGravity(Gravity.CENTER_HORIZONTAL);

        ImageView iconImage = new ImageView(ctx);
        iconImage.setImageResource(iconResID);
        cell.addView(iconImage, apply(new LinearLayout.LayoutParams(-2, -2), params -> {
            params.gravity = Gravity.CENTER_VERTICAL;
        }));

        TextView nameLabel = new TextView(ctx);
        nameLabel.setTextSize(16);
        nameLabel.setDuplicateParentStateEnabled(true);
        int[][] states = {{android.R.attr.state_enabled}, {}};
        int[] colors = {TEXT_BLACK_COLOR, TEXT_GREY_COLOR};
        ColorStateList stateList = new ColorStateList(states, colors);
        nameLabel.setTextColor(stateList);
        nameLabel.setText(name);
        cell.addView(nameLabel, apply(new LinearLayout.LayoutParams(-2, -2), params -> {
            params.gravity = Gravity.CENTER_VERTICAL;
            params.leftMargin = dp2px(4);
        }));

        bottomBar.addView(cell, apply(new LinearLayout.LayoutParams(0, -1), parmas -> {
            parmas.weight = 1;
        }));
        v_setClick(cell, onClickListener);
    }


    public static class WriteMoodFragment extends SimpleFragment {

        private static class VCStateData implements VCStateDataProtocol<WriteMoodFragment> {
            String filledContent;
            String[] pickedImages;
            Bitmap[] thumbnails;
            ShareInfo attachment;
            String takenPicPath;

            public VCStateData() {
            }

            @Override
            public VCStateData init(WriteMoodFragment fragment) {
                filledContent = safeGet(() -> fragment.mContentField.getText().toString(), "");
                pickedImages = Stream.of(safeGet(() -> fragment.mImages, emptyList())).map(it -> it.getAbsolutePath()).toArray(count -> new String[count]);
                thumbnails = Stream.of(safeGet(() -> fragment.mThumbnails, emptyList())).toArray(count -> new Bitmap[count]);
                attachment = fragment.mAttachmentOrNil;
                takenPicPath = fragment.mTakingPicPath;
                return this;
            }

            @Override
            public void restore(WriteMoodFragment fragment) {
                safeCall(() -> {
                    fragment.mContentField.setText(filledContent);
                    fragment.mImages = Stream.of(pickedImages).map(path -> new File(path)).collect(Collectors.toList());
                    fragment.mThumbnails = Stream.of(thumbnails).collect(Collectors.toList());
                    fragment.mAttachmentOrNil = attachment;
                    fragment.mTakingPicPath = takenPicPath;
                });
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(this.filledContent);
                dest.writeStringArray(this.pickedImages);
                dest.writeTypedArray(this.thumbnails, flags);
                dest.writeParcelable(this.attachment, flags);
                dest.writeString(this.takenPicPath);
            }

            protected VCStateData(Parcel in) {
                this.filledContent = in.readString();
                this.pickedImages = in.createStringArray();
                this.thumbnails = in.createTypedArray(Bitmap.CREATOR);
                this.attachment = in.readParcelable(ShareInfo.class.getClassLoader());
                this.takenPicPath = in.readString();
            }

            public static final Creator<VCStateData> CREATOR = new Creator<VCStateData>() {
                @Override
                public VCStateData createFromParcel(Parcel source) {
                    return new VCStateData(source);
                }

                @Override
                public VCStateData[] newArray(int size) {
                    return new VCStateData[size];
                }
            };
        }


        private int mMessageType;
        private String mLinkID;
        private String mSessionID;

        private EditText mContentField;
        private TextView mPublishBtn;
        private ViewGroup mBottomBar;
        private List<File> mImages = new LinkedList<>();
        private List<Bitmap> mThumbnails = new LinkedList<>();
        private ShareInfo mAttachmentOrNil;
        private PublishSubject<Void> mOnContentChangedSubject = PublishSubject.create();
        private String mTakingPicPath;
        private int mBottomBarWidth = 0;

        public WriteMoodFragment init(int messageType, String linkID, String sessionID) {
            Bundle arguments = new Bundle();
            arguments.putInt(CommonProxyActivity.KEY_MESSAGE_TYPE_INT, messageType);
            arguments.putString(CommonProxyActivity.KEY_LINK_ID_STRING, linkID);
            arguments.putString(CommonProxyActivity.KEY_SESSION_ID_STRING, sessionID);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mMessageType = getArguments().getInt(CommonProxyActivity.KEY_MESSAGE_TYPE_INT);
            mLinkID = getArguments().getString(CommonProxyActivity.KEY_LINK_ID_STRING);
            mSessionID = getArguments().getString(CommonProxyActivity.KEY_SESSION_ID_STRING);
            return inflater.inflate(R.layout.frag_write_mood, container, false);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putParcelable("vc_data", new VCStateData().init(this));
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setupBackButton(this, findToolbar(this), R.drawable.ic_close_light);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);

            mPublishBtn = v_findView(this, R.id.btn_publish);
            mContentField = v_findView(mContentSection, R.id.field_content);
            mBottomBar = v_findView(mContentSection, R.id.bar_bottom);

            if (savedInstanceState != null) {
                VCStateData data = savedInstanceState.getParcelable("vc_data");
                if (data != null) {
                    data.restore(this);
                }
            }

            apply(mPublishBtn, it -> {
                int[][] states = {new int[]{android.R.attr.state_enabled}, new int[]{}};
                int[] colors = {YELLOW_COLOR, 0x66F8E71C};
                ColorStateList drawable = new ColorStateList(states, colors);
                it.setTextColor(drawable);
                it.setEnabled(false);
            });

            mBottomBar.post(() -> {
                v_updateLayoutParams(v_findView(mBottomBar, R.id.container_extra), ViewGroup.MarginLayoutParams.class, params -> {
                    params.width = mBottomBar.getWidth();
                    mBottomBarWidth = mBottomBar.getWidth();
                });
                Iterator<File> imageIter = mImages.iterator();
                Iterator<Bitmap> thumbnailIter = mThumbnails.iterator();
                while (imageIter.hasNext() && thumbnailIter.hasNext()) {
                    File image = imageIter.next();
                    Bitmap thumbnail = thumbnailIter.next();
                    boolean scrollToEnd = !imageIter.hasNext();
                    appendImageToBottomBar(image, thumbnail, scrollToEnd);
                }
            });

            v_addTextChangedListener(mContentField, editable -> mOnContentChangedSubject.onNext(null));

            v_setClick(mPublishBtn, v -> {
                sendMessage();
            });

            consumeEventMR(ChatController.getMessageManager(mMessageType, mLinkID, mSessionID))
                    .setTag("fetch_message_manager")
                    .onNextSuccess(response -> {
                        MessageManager manager = response.data;

                        updateTitle(safeGet(() -> manager.getSession().addButtonText, "写心情"));
                        resetBottomBar(manager.getSession());

                        boolean hasLimitWord = safeGet(() -> manager.getSession().limitWord > 0, false);
                        int limitWordCount = safeGet(() -> manager.getSession().limitWord, Integer.MAX_VALUE);

                        TextView limitLabel = v_findView(this, R.id.label_limit);
                        v_setVisibility(limitLabel, hasLimitWord ? View.VISIBLE : View.GONE);

                        consumeEvent(mOnContentChangedSubject)
                                .onNextFinish(ignored -> {
                                    String content = mContentField.getText().toString();
                                    int contentLength = content.length();
                                    boolean hasText = !TextUtils.isEmpty(content);
                                    boolean hasImage = !mImages.isEmpty();
                                    boolean hasAttachment = mAttachmentOrNil != null;
                                    boolean underLimit = !hasLimitWord || (contentLength <= limitWordCount);
                                    if (hasLimitWord) {
                                        v_setText(limitLabel, concatNoBreak(setColor(String.valueOf(contentLength), underLimit ? TEXT_GREY_COLOR : TEXT_RED_COLOR), "/", String.valueOf(limitWordCount)));
                                    }
                                    mPublishBtn.setEnabled((hasText || hasImage || hasAttachment) && underLimit);
                                })
                                .done();
                        mOnContentChangedSubject.onNext(null);

                        if (savedInstanceState == null) {
                            runOnUIThreadDelayed(() -> showKeyboardFromWindow(this), 200L);
                        }
                    })
                    .done();
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (anyMatch(requestCode, REQUEST_CODE_CAMERA, REQUEST_CODE_GALLERY)) {
                File lastImageFile = TextUtils.isEmpty(mTakingPicPath) ? null : new File(mTakingPicPath);
                mTakingPicPath = null;

                if (lastImageFile == null)
                    return;

                boolean isSuccess = false;
                Bitmap[] thumbnail = new Bitmap[1];
                Rect thumbnailSize = new Rect(0, 0, dp2px(40), dp2px(40));
                if (requestCode == REQUEST_CODE_CAMERA) {
                    isSuccess = handlePickImageFromCameraActivityResult(requestCode, resultCode, data, lastImageFile, thumbnail, thumbnailSize);
                } else if (requestCode == REQUEST_CODE_GALLERY) {
                    isSuccess = handlePickImageFromGalleryActivityResult(requestCode, resultCode, data, lastImageFile, thumbnail, thumbnailSize);
                }

                if (isSuccess) {
                    appendImageToBottomBar(lastImageFile, thumbnail[0], true);
                }
            }
        }

        private void appendImageToBottomBar(File imageFile, Bitmap thumbnail, boolean scrollToEnd) {
            ViewGroup extraContainer = v_findView(mBottomBar, R.id.container_extra);
            Action0 requestMoreImageAction = () -> {
                pickImage();
            };
            Action1<File> onImageRemoveCallback = file -> {
                int index = mImages.indexOf(file);
                if (index >= 0) {
                    mImages.remove(index);
                    mThumbnails.remove(index);
                    mOnContentChangedSubject.onNext(null);
                }
            };
            mBottomBar.post(() -> {
                setupExtraContainerWithImage(9, mBottomBarWidth, imageFile, thumbnail, mBottomBar, extraContainer, requestMoreImageAction, onImageRemoveCallback, scrollToEnd);
                mOnContentChangedSubject.onNext(null);
                if (!mImages.contains(imageFile)) {
                    mImages.add(imageFile);
                }
                if (!mThumbnails.contains(thumbnail)) {
                    mThumbnails.add(thumbnail);
                }
            });
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
//            PickImageHelper.clearPublicDir();
        }

        private void resetBottomBar(MessageSession session) {
            Context ctx = getActivity();
            LinearLayout bottomBar = v_findView(this, R.id.bar_bottom);
            LinearLayout cellContainer = v_findView(bottomBar, R.id.container_cell);
            cellContainer.removeAllViewsInLayout();
            Stream.of(safeGet(() -> session.actionList, emptyList()))
                    .forEach(action -> {
                        int iconResID = 0;
                        CharSequence name = null;
                        View.OnClickListener listener = null;

                        if (action == MessageSession.Session_New_Action_image) {
                            iconResID = R.mipmap.ic_circle_image;
                            name = "图片";
                            listener = v -> pickImage();
                        } else if (action == MessageSession.Session_New_Action_fund) {
                            iconResID = R.mipmap.ic_circle_portfolio;
                            name = "组合";
                            listener = v -> pickFund();
                        } else if (action == MessageSession.Session_New_Action_stock) {
                            iconResID = R.mipmap.ic_circle_stock;
                            name = "股票";
                            listener = v -> pickStock();
                        }

                        if (iconResID != 0 && !TextUtils.isEmpty(name) && listener != null) {
                            createBottomBarCell(ctx, cellContainer, iconResID, name, listener);
                        }
                    });
            v_updateLayoutParams(bottomBar, params -> {
                params.height = cellContainer.getChildCount() > 0 ? dp2px(60) : 0;
            });
        }

        private void pickFund() {
            consumeEvent(SelectFundFragment.SELECT_FUND_SUBJECT)
                    .onNextFinish(selectedFund -> {
                        ViewGroup extraContainer = v_findView(mBottomBar, R.id.container_extra);
                        SelectFundFragment.VM fundVM = new SelectFundFragment.VM(selectedFund);
                        ShareInfo shareInfo = new ShareInfo();
                        shareInfo.imageUrl = fundVM.coverImageURL;
                        shareInfo.title = fundVM.name.toString();
                        shareInfo.msg = fundVM.desc.toString();
                        shareInfo.url = CMDParser.create_portfolioCommand(safeGet(() -> fundVM.raw.index, 0));
                        mAttachmentOrNil = shareInfo;
                        mImages.clear();
                        mThumbnails.clear();
                        mOnContentChangedSubject.onNext(null);
                        setupExtraContainerWithFund(selectedFund, mBottomBar, extraContainer, () -> {
                            mAttachmentOrNil = null;
                            mOnContentChangedSubject.onNext(null);
                        });
                    })
                    .done();
            showActivity(this, an_SelectFundPage());
        }

        private void pickStock() {
            consumeEvent(StockTradeFragments.SelectStockFragment.SELECT_STOCK_SUBJECT.limit(1))
                    .setTag("select_stock")
                    .onNextFinish(selectedStock -> {
                        mAttachmentOrNil = new ShareInfo();
                        mAttachmentOrNil.imageUrl = CommonDefine.URL_FUND_PLACEHOLDER;
                        mAttachmentOrNil.title = selectedStock.name;
                        mAttachmentOrNil.msg = selectedStock.index;
                        mAttachmentOrNil.url = CMDParser.create_stockCommand(selectedStock.index);
                        mImages.clear();
                        mThumbnails.clear();
                        mOnContentChangedSubject.onNext(null);
                        ViewGroup extraContainer = v_findView(mBottomBar, R.id.container_extra);
                        setupExtraContainerWithStock(selectedStock, mBottomBar, extraContainer, () -> {
                            mAttachmentOrNil = null;
                            mOnContentChangedSubject.onNext(null);
                        });
                    })
                    .done();
            showActivity(this, an_SelectStockPage(""));
        }

        private void pickImage() {
            mTakingPicPath = null;
            mAttachmentOrNil = null;
            GMFBottomSheet bottomSheet = new GMFBottomSheet.Builder(getActivity())
                    .addContentItem(new GMFBottomSheet.BottomSheetItem("camera", "拍照", R.mipmap.ic_bottomsheet_camera))
                    .addContentItem(new GMFBottomSheet.BottomSheetItem("gallery", "从手机相册选择图片", R.mipmap.ic_bottomsheet_gallery))
                    .create();
            bottomSheet.setOnItemClickListener((sheet, item) -> {
                sheet.dismiss();
                String tag = opt(item.tag).let(it -> it.toString()).or("");
                Intent[] intents = new Intent[1];
                File[] files = new File[1];
                int requestCode = 0;
                boolean isSuccess = false;
                if (tag.equals("camera")) {
                    requestCode = REQUEST_CODE_CAMERA;
                    isSuccess = createPickImageFromCameraIntent(files, intents, String.format("%d.jpg", System.currentTimeMillis()));
                } else if (tag.equals("gallery")) {
                    requestCode = REQUEST_CODE_GALLERY;
                    isSuccess = createPickImageFromGalleryIntent(files, intents, String.format("%d.jpg", System.currentTimeMillis()));
                }
                if (isSuccess) {
                    mTakingPicPath = files[0].getAbsolutePath();
                    startActivityForResult(intents[0], requestCode);
                }
            });
            bottomSheet.show();
        }

        private void sendMessage() {

            GMFProgressDialog progressDialog = new GMFProgressDialog(getActivity(), "发送中...");
            progressDialog.show();

            mPublishBtn.setEnabled(false);
            String content = mContentField.getText().toString();

            if (!mImages.isEmpty()) {
                consumeEventMR(UploadFileUtil.uploadFile(CommonManager.ChatImg, mImages))
                        .onNextSuccess(uploadResponse -> {
                            List<String> imageList = uploadResponse.data;
                            UpImageMessage sendMessage = new UpImageMessage(content, imageList);
                            sendMessage.templateType = GMFMessage.Message_Image_2;
                            consumeEventMR(ChatController.sendBarMessage(mMessageType, mLinkID, mSessionID, sendMessage))
                                    .onNextStart(response -> progressDialog.dismiss())
                                    .onNextSuccess(response -> {
                                        UmengUtil.stat_click_event(UmengUtil.eEVENTIDNewTopicSuccessMood);
                                        NotificationCenter.onWriteNewArticleSubject.onNext(sendMessage.messageID);
                                        showToast(this, "发送成功");
                                        hideKeyboardFromWindow(this);
                                        goBack(this);
                                    })
                                    .onNextFail(response -> {
                                        mPublishBtn.setEnabled(true);
                                        changeVisibleSection(TYPE_CONTENT);
                                        showToast(this, getErrorMessage(response));
                                    })
                                    .done();
                        })
                        .onNextFail(response -> {
                            progressDialog.dismiss();
                            mPublishBtn.setEnabled(true);
                            changeVisibleSection(TYPE_CONTENT);
                            showToast(this, getErrorMessage(response));
                        })
                        .done();
            } else {
                SendMessage sendMessage = new SendMessage(content);
                if (mAttachmentOrNil != null) {
                    sendMessage.templateType = GMFMessage.Message_TarLink_2;
                    sendMessage.attachInfo = mAttachmentOrNil;
                } else {
                    sendMessage.templateType = GMFMessage.Message_Text_2;
                }

                consumeEventMR(ChatController.sendBarMessage(mMessageType, mLinkID, mSessionID, sendMessage))
                        .onNextStart(response -> progressDialog.dismiss())
                        .onNextSuccess(response -> {
                            NotificationCenter.onWriteNewArticleSubject.onNext(sendMessage.messageID);
                            showToast(this, "发送成功");
                            hideKeyboardFromWindow(this);
                            goBack(this);
                        })
                        .onNextFail(response -> {
                            mPublishBtn.setEnabled(true);
                            changeVisibleSection(TYPE_CONTENT);
                            showToast(this, getErrorMessage(response));
                        })
                        .done();
            }
        }

    }

    public static class WriteIntelligenceFragment extends SimpleFragment {

        private static class VCStateData implements VCStateDataProtocol<WriteIntelligenceFragment> {
            String filledContent;
            String pickedImages;
            Bitmap thumbnails;
            ShareInfo attachment;
            String takenPicPath;
            int intelligencePrice;

            public VCStateData() {
            }

            @Override
            public VCStateData init(WriteIntelligenceFragment fragment) {
                filledContent = safeGet(() -> fragment.mContentField.getText().toString(), "");
                pickedImages = safeGet(() -> fragment.mImage.getAbsolutePath(), "");
                thumbnails = fragment.mThumbnail;
                attachment = fragment.mAttachmentOrNil;
                takenPicPath = fragment.mTakingPicPath;
                intelligencePrice = fragment.mIntelligencePrice;
                return this;
            }

            @Override
            public void restore(WriteIntelligenceFragment fragment) {
                safeCall(() -> {
                    fragment.mContentField.setText(filledContent);
                    fragment.mImage = TextUtils.isEmpty(pickedImages) ? null : new File(pickedImages);
                    fragment.mThumbnail = thumbnails;
                    fragment.mAttachmentOrNil = attachment;
                    fragment.mTakingPicPath = takenPicPath;
                    fragment.mIntelligencePrice = intelligencePrice;
                });
            }


            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(this.filledContent);
                dest.writeString(this.pickedImages);
                dest.writeParcelable(this.thumbnails, flags);
                dest.writeParcelable(this.attachment, flags);
                dest.writeString(this.takenPicPath);
                dest.writeInt(this.intelligencePrice);
            }

            protected VCStateData(Parcel in) {
                this.filledContent = in.readString();
                this.pickedImages = in.readString();
                this.thumbnails = in.readParcelable(Bitmap.class.getClassLoader());
                this.attachment = in.readParcelable(ShareInfo.class.getClassLoader());
                this.takenPicPath = in.readString();
                this.intelligencePrice = in.readInt();
            }

            public static final Creator<VCStateData> CREATOR = new Creator<VCStateData>() {
                @Override
                public VCStateData createFromParcel(Parcel source) {
                    return new VCStateData(source);
                }

                @Override
                public VCStateData[] newArray(int size) {
                    return new VCStateData[size];
                }
            };
        }


        private int mMessageType;
        private String mLinkID;
        private String mSessionID;

        private PopupWindow mVisiblePopupWindow;
        private PublishSubject<Void> onContentChangedSubject = PublishSubject.create();
        private boolean mHasSetMosaicStock = false;
        private boolean mHasSetMosaicImage = false;
        private File mImage;
        private Bitmap mThumbnail;
        private ShareInfo mAttachmentOrNil;
        private int mIntelligencePrice = 0;
        private String mTakingPicPath;

        private TextView mPublishButton;
        private EditText mContentField;
        private ViewGroup mBottomBar;
        private int mBottomBarWidth = 0;

        public WriteIntelligenceFragment init(int messageType, String linkID, String sessionID) {
            Bundle arguments = new Bundle();
            arguments.putInt(CommonProxyActivity.KEY_MESSAGE_TYPE_INT, messageType);
            arguments.putString(CommonProxyActivity.KEY_LINK_ID_STRING, linkID);
            arguments.putString(CommonProxyActivity.KEY_SESSION_ID_STRING, sessionID);
            setArguments(arguments);
            return this;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putParcelable("vc_data", new VCStateData().init(this));
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mMessageType = getArguments().getInt(CommonProxyActivity.KEY_MESSAGE_TYPE_INT);
            mLinkID = getArguments().getString(CommonProxyActivity.KEY_LINK_ID_STRING);
            mSessionID = getArguments().getString(CommonProxyActivity.KEY_SESSION_ID_STRING);
            return inflater.inflate(R.layout.frag_write_intelligence, container, false);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setupBackButton(this, findToolbar(this), R.drawable.ic_close_light);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);

            changeVisibleSection(TYPE_LOADING);

            mPublishButton = v_findView(this, R.id.btn_publish);
            mContentField = v_findView(view, R.id.field_content);
            mBottomBar = v_findView(view, R.id.bar_bottom);

            if (savedInstanceState != null) {
                VCStateData data = savedInstanceState.getParcelable("vc_data");
                if (data != null) {
                    data.restore(this);
                }
            }

            apply(mPublishButton, it -> {
                int[][] states = {new int[]{android.R.attr.state_enabled}, new int[]{}};
                int[] colors = {YELLOW_COLOR, 0x66F8E71C};

                ColorStateList drawable = new ColorStateList(states, colors);
                it.setTextColor(drawable);
                it.setEnabled(false);
            });

            v_addTextChangedListener(mContentField, editable -> onContentChangedSubject.onNext(null));

            v_setClick(mPublishButton, v -> {
                hideKeyboardFromWindow(this);
                sendMessage();
            });

            consumeEventMR(ChatController.getMessageManager(mMessageType, mLinkID, mSessionID))
                    .onNextSuccess(response -> {
                        MessageManager manager = response.data;

                        updateTitle(safeGet(() -> manager.getSession().addButtonText, "写情报"));
                        resetBottomBar(manager.getSession());

                        List<Integer> scores = safeGet(() -> manager.getSession().scoreList, Collections.<Integer>emptyList());
                        if (scores.isEmpty()) {
                            showToast(this, getErrorMessage(response));
                            goBack(this);
                        } else {

                            mBottomBar.post(() -> {
                                View extraContainer = v_findView(mBottomBar, R.id.container_extra);
                                v_updateLayoutParams(extraContainer, params -> {
                                    params.width = mBottomBar.getWidth();
                                    mBottomBarWidth = mBottomBar.getWidth();
                                });

                                if (mImage != null && mThumbnail != null) {
                                    appendImageToBottomBar(mImage, mThumbnail, true);
                                }
                            });

                            boolean hasLimitWord = safeGet(() -> manager.getSession().limitWord > 0, false);
                            int limitWordCount = safeGet(() -> manager.getSession().limitWord, Integer.MAX_VALUE);
                            TextView limitLabel = v_findView(this, R.id.label_limit);
                            v_setVisibility(limitLabel, hasLimitWord ? View.VISIBLE : View.GONE);

                            consumeEvent(onContentChangedSubject)
                                    .setTag("onContentChanged")
                                    .onNextFinish(ignored -> {
                                        String content = mContentField.getText().toString();
                                        int contentLength = content.length();
                                        Func0<Boolean> isContainMosaicText = () -> CircleHelper.REGEX.CONTAIN_MOSAIC_TEXT.matcher(content).find();
                                        boolean containMosaic = mHasSetMosaicImage || mHasSetMosaicStock || isContainMosaicText.call();
                                        boolean hasText = !TextUtils.isEmpty(mContentField.getText().toString());
                                        boolean underLimit = !hasLimitWord || contentLength <= limitWordCount;

                                        if (hasLimitWord) {
                                            v_setText(limitLabel, concatNoBreak(setColor(String.valueOf(contentLength), underLimit ? TEXT_GREY_COLOR : TEXT_RED_COLOR), "/", String.valueOf(limitWordCount)));
                                        }

                                        v_forEach((ViewGroup) mBottomBar.getChildAt(0), (pos, child) -> child.setEnabled(!containMosaic));
                                        mPublishButton.setEnabled(containMosaic && hasText && underLimit);
                                    })
                                    .done();

                            resetScoreGroup(scores);
                            changeVisibleSection(TYPE_CONTENT);
                            if (savedInstanceState == null) {
                                runOnUIThreadDelayed(() -> showKeyboardFromWindow(this), 200L);
                            }
                        }
                    })
                    .onNextFail(response -> {
                        showToast(this, getErrorMessage(response));
                        goBack(this);
                    })
                    .done();
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (getActivity() != null && getView() != null) {
                if (requestCode == REQUEST_CODE_CAMERA || requestCode == REQUEST_CODE_GALLERY) {

                    File lastPictureFile = TextUtils.isEmpty(mTakingPicPath) ? null : new File(mTakingPicPath);

                    if (lastPictureFile == null) {
                        return;
                    }

                    Bitmap[] thumbnail = new Bitmap[1];
                    Rect thumbnailSize = new Rect(0, 0, dp2px(60), dp2px(60));
                    boolean isSuccess = false;
                    if (requestCode == REQUEST_CODE_CAMERA) {
                        isSuccess = handlePickImageFromCameraActivityResult(requestCode, resultCode, data, lastPictureFile, thumbnail, thumbnailSize);
                    }
                    if (requestCode == REQUEST_CODE_GALLERY) {
                        isSuccess = handlePickImageFromGalleryActivityResult(requestCode, resultCode, data, lastPictureFile, thumbnail, thumbnailSize);
                    }
                    if (isSuccess) {
                        appendImageToBottomBar(lastPictureFile, thumbnail[0], true);
                    }
                }
            }
        }

        private void appendImageToBottomBar(File imageFile, Bitmap thumbnail, boolean scrollToEnd) {
            mHasSetMosaicImage = true;
            ViewGroup extraContainer = v_findView(mBottomBar, R.id.container_extra);
            Action0 requestMoreImageAction = () -> takePhotoFromSystem();
            Action1<File> onImageRemoveCallback = file -> {
                mHasSetMosaicImage = false;
                onContentChangedSubject.onNext(null);
            };
            mBottomBar.post(() -> {
                setupExtraContainerWithImage(1, mBottomBarWidth, imageFile, thumbnail, mBottomBar, extraContainer, requestMoreImageAction, onImageRemoveCallback, scrollToEnd);
                onContentChangedSubject.onNext(null);
                mImage = imageFile;
                mThumbnail = thumbnail;
            });
        }

        private void resetBottomBar(MessageSession session) {
            Context ctx = getActivity();
            LinearLayout bottomBar = v_findView(this, R.id.bar_bottom);
            LinearLayout cellContainer = v_findView(bottomBar, R.id.container_cell);
            cellContainer.removeAllViewsInLayout();
            Stream.of(safeGet(() -> session.actionList, emptyList()))
                    .forEach(action -> {
                        int iconResID = 0;
                        CharSequence name = null;
                        View.OnClickListener listener = null;

                        if (action == MessageSession.Session_New_Action_text) {
                            iconResID = R.mipmap.ic_circle_mosaic_text;
                            name = "打码文字";
                            listener = v -> appendHiddenText();
                        } else if (action == MessageSession.Session_New_Action_image) {
                            iconResID = R.mipmap.ic_circle_mosaic_image;
                            name = "打码图片";
                            listener = v -> takePhotoFromSystem();
                        } else if (action == MessageSession.Session_New_Action_stock) {
                            iconResID = R.mipmap.ic_circle_mosaic_stock;
                            name = "打码股票";
                            listener = v -> pickStock();
                        }

                        if (iconResID != 0 && !TextUtils.isEmpty(name) && listener != null) {
                            createBottomBarCell(ctx, cellContainer, iconResID, name, listener);
                        }
                    });
            v_updateLayoutParams(bottomBar, params -> {
                params.height = cellContainer.getChildCount() > 0 ? dp2px(60) : 0;
            });
        }

        private void appendHiddenText() {
            mContentField.requestFocus();
            showKeyboardFromWindow(this);
            String appendText = "#在这里输入你想隐藏的文字#";
            int startOffset = "#".length();
            int endOffset = -"#".length();
            mContentField.append(appendText);
            mContentField.setSelection(mContentField.length() - appendText.length() + startOffset, mContentField.length() + endOffset);
        }


        private void takePhotoFromSystem() {
            mTakingPicPath = null;
            mAttachmentOrNil = null;
            GMFBottomSheet bottomSheet = new GMFBottomSheet.Builder(getActivity())
                    .addContentItem(new GMFBottomSheet.BottomSheetItem("camera", "拍照", R.mipmap.ic_bottomsheet_camera))
                    .addContentItem(new GMFBottomSheet.BottomSheetItem("gallery", "从手机相册选择图片", R.mipmap.ic_bottomsheet_gallery))
                    .create();
            bottomSheet.setOnItemClickListener((sheet, item) -> {
                sheet.dismiss();
                String tag = opt(item.tag).let(it -> it.toString()).or("");
                Intent[] intents = new Intent[1];
                File[] files = new File[1];
                int requestCode = 0;
                boolean isSuccess = false;
                if (tag.equals("camera")) {
                    requestCode = REQUEST_CODE_CAMERA;
                    isSuccess = createPickImageFromCameraIntent(files, intents, null);
                } else if (tag.equals("gallery")) {
                    requestCode = REQUEST_CODE_GALLERY;
                    isSuccess = createPickImageFromGalleryIntent(files, intents, null);
                }
                if (isSuccess) {
                    mTakingPicPath = files[0].getAbsolutePath();
                    startActivityForResult(intents[0], requestCode);
                }
            });
            bottomSheet.show();
        }

        private void pickStock() {
            consumeEvent(StockTradeFragments.SelectStockFragment.SELECT_STOCK_SUBJECT.limit(1))
                    .setTag("pick_stock")
                    .onNextFinish(selectedStock -> {
                        mHasSetMosaicStock = true;
                        mHasSetMosaicImage = false;
                        mAttachmentOrNil = new ShareInfo();
                        mAttachmentOrNil.imageUrl = CommonDefine.URL_FUND_PLACEHOLDER;
                        mAttachmentOrNil.title = selectedStock.name;
                        mAttachmentOrNil.msg = selectedStock.index;
                        mAttachmentOrNil.url = CMDParser.create_stockCommand(selectedStock.index);
                        ViewGroup extraContainer = v_findView(mBottomBar, R.id.container_extra);
                        setupExtraContainerWithStock(selectedStock, mBottomBar, extraContainer, () -> {
                            mAttachmentOrNil = null;
                            mHasSetMosaicStock = false;
                            onContentChangedSubject.onNext(null);
                        });
                        onContentChangedSubject.onNext(null);
                    })
                    .done();

            showActivity(this, an_SelectStockPage(""));
        }

        private void sendMessage() {
            String content = mContentField.getText().toString();
            int score = mIntelligencePrice;

            mIsOperation = true;
            mPublishButton.setEnabled(false);

            GMFProgressDialog progressDialog = new GMFProgressDialog(getActivity(), "发送中...");
            progressDialog.show();

            if (mHasSetMosaicImage && mImage != null) {
                consumeEventMR(UploadFileUtil.uploadFile(CommonManager.ChatImg, mImage))
                        .onNextSuccess(uploadResponse -> {
                            String imageURL = uploadResponse.data;
                            UpImageMessage sendMessage = new UpImageMessage(content, Collections.singletonList(imageURL));
                            sendMessage.templateType = SendMessage.Message_Image_2;
                            sendMessage.intelligence = GMFMessage.Intelligence.build(score);

                            consumeEventMR(ChatController.sendBarMessage(mMessageType, mLinkID, mSessionID, sendMessage))
                                    .onNextStart(response -> {
                                        mIsOperation = false;
                                        mPublishButton.setEnabled(true);
                                        progressDialog.dismiss();
                                    })
                                    .onNextSuccess(response -> {
                                        NotificationCenter.onWriteNewArticleSubject.onNext(sendMessage.messageID);
                                        UmengUtil.stat_click_event(UmengUtil.eEVENTIDNewTopicSuccessIntelligence);
                                        showToast(this, "发送成功");
                                        goBack(this);
                                    })
                                    .onNextFail(response -> {
                                        mPublishButton.setEnabled(true);
                                        showToast(this, getErrorMessage(response));
                                    })
                                    .done();
                        })
                        .onNextFail(uploadResponse -> {
                            mIsOperation = false;
                            mPublishButton.setEnabled(true);
                            progressDialog.dismiss();
                            showToast(this, getErrorMessage(uploadResponse));
                        })
                        .done();
            } else {
                SendMessage sendMessage = new SendMessage(content);
                sendMessage.intelligence = GMFMessage.Intelligence.build(score);
                if (mAttachmentOrNil == null) {
                    sendMessage.templateType = SendMessage.Message_Text_2;
                } else {
                    sendMessage.attachInfo = mAttachmentOrNil;
                    sendMessage.templateType = SendMessage.Message_TarLink_2;
                }

                consumeEventMR(ChatController.sendBarMessage(mMessageType, mLinkID, mSessionID, sendMessage))
                        .onNextStart(response -> {
                            mIsOperation = false;
                            mPublishButton.setEnabled(true);
                            progressDialog.dismiss();
                        })
                        .onNextSuccess(response -> {
                            NotificationCenter.onWriteNewArticleSubject.onNext(sendMessage.messageID);
                            showToast(this, "发送成功");
                            goBack(this);
                        })
                        .onNextFail(response -> {
                            showToast(this, getErrorMessage(response));
                        })
                        .done();
            }
        }

        @Override
        protected boolean onInterceptGoBack() {
            boolean isConsumed = dismissCurrentVisiblePopupWindow();
            return isConsumed || super.onInterceptGoBack();
        }

        private boolean dismissCurrentVisiblePopupWindow() {
            if (mVisiblePopupWindow != null) {
                mVisiblePopupWindow.dismiss();
                mVisiblePopupWindow = null;
                return true;
            }

            return false;
        }

        @SuppressWarnings("deprecation")
        private void resetScoreGroup(List<Integer> scores) {
            Context ctx = getActivity();
            LinearLayout scoreGroup = v_findView(this, R.id.group_score);

            scoreGroup.post(() -> {
                int count = scores.size();
                scoreGroup.removeAllViewsInLayout();

                if (count > 0) {
                    int usedSpace = dp2px(4) * 4;
                    float unitNum = (count <= 5) ? 5 : 5.3f;
                    float widthUnit = ((float) (scoreGroup.getMeasuredWidth() - usedSpace)) / unitNum;

                    Stream.of(scores).limit(4)
                            .forEach(score -> {
                                addScoreLabel(scoreGroup, String.valueOf(score), (int) widthUnit, label -> {
                                            v_setClick(label, v -> {
                                                v_forEach(scoreGroup, (pos, child) -> child.setSelected(child == label));
                                                mIntelligencePrice = score;
                                            });
                                        }
                                );
                            });

                    if (count > 4) {
                        if (count == 5) {
                            addScoreLabel(scoreGroup, String.valueOf(scores.get(4)), (int) widthUnit, label -> {
                                label.setPadding(dp2px(10), 0, dp2px(10), 0);
                                v_setClick(label, v -> {
                                    v_forEach(scoreGroup, (pos, child) -> child.setSelected(child == label));
                                    mIntelligencePrice = scores.get(4);
                                });
                            });
                        } else {
                            addScoreLabel(scoreGroup, String.valueOf("其它"), (int) (widthUnit * 1.3), label -> {
                                label.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down_dark, 0);
                                label.setPadding(dp2px(10), 0, dp2px(10), 0);
                                v_setClick(label, v -> {
                                    hideKeyboardFromWindow(this);
                                    dismissCurrentVisiblePopupWindow();
                                    FrameLayout wrapper = new FrameLayout(ctx);
                                    int borderLength = dp2px(0.5f);
                                    wrapper.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(0, 0).border(LINE_SEP_COLOR, borderLength)));
                                    ListView listView = new ListView(ctx);
                                    ArrayAdapter<Integer> adapter = new ArrayAdapter<>(ctx, android.support.design.R.layout.support_simple_spinner_dropdown_item, scores.subList(4, scores.size()));
                                    listView.setAdapter(adapter);
                                    listView.setOnItemClickListener((parent, view, position, id) -> {
                                        dismissCurrentVisiblePopupWindow();
                                        Integer score = (Integer) parent.getAdapter().getItem(position);
                                        String text = score.toString();
                                        label.setText(text);
                                        v_forEach(scoreGroup, (pos, child) -> child.setSelected(child == label));
                                        mIntelligencePrice = score;
                                    });
                                    wrapper.addView(listView, apply(new FrameLayout.LayoutParams(-2, -2), params -> {
                                        params.setMargins(borderLength, borderLength, borderLength, borderLength);
                                    }));
                                    PopupWindow window = new PopupWindow(ctx);
                                    window.setContentView(wrapper);
                                    window.setOutsideTouchable(true);
                                    window.setFocusable(true);
                                    window.setWidth((int) (widthUnit * 1.3));

                                    Rect screenSize = getScreenSize(this);
                                    Rect visibleRect = new Rect();
                                    label.getGlobalVisibleRect(visibleRect);
                                    int remainingSpace = screenSize.height() - visibleRect.bottom - dp2px(4);
                                    int maxHeight = remainingSpace / 2;
                                    window.setHeight(maxHeight);

                                    window.setBackgroundDrawable(new ColorDrawable(WHITE_COLOR));
                                    window.showAsDropDown(v, 0, dp2px(4));
                                    mVisiblePopupWindow = window;
                                });
                            });
                        }
                    }

                    scoreGroup.removeViewAt(scoreGroup.getChildCount() - 1);

                    int selectedIdx = Math.min(Stream.of(scores).filter(it -> it == mIntelligencePrice).findFirst().orElse(0), 4);
                    scoreGroup.getChildAt(selectedIdx).performClick();
                }

                v_setVisibility(scoreGroup, count == 0 ? View.GONE : View.VISIBLE);
            });
        }

        @SuppressWarnings("deprecation")
        private void addScoreLabel(LinearLayout scoreGroup, String text, int width, Action1<TextView> finishCallback) {
            TextView scoreLabel = new TextView(scoreGroup.getContext());
            scoreLabel.setGravity(Gravity.CENTER);
            scoreLabel.setText(String.valueOf(text));

            StateListDrawable drawable = new StateListDrawable();
            drawable.addState(new int[]{android.R.attr.state_pressed}, new ShapeDrawable(new RoundCornerShape(RED_COLOR, dp2px(15))));
            drawable.addState(new int[]{android.R.attr.state_selected}, new ShapeDrawable(new RoundCornerShape(RED_COLOR, dp2px(15))));
            drawable.addState(new int[0], new ShapeDrawable(new RoundCornerShape(0, dp2px(15)).border(0xFFCCCCCC, dp2px(1))));
            scoreLabel.setBackgroundDrawable(drawable);
            scoreGroup.addView(scoreLabel, new LinearLayout.LayoutParams(width, -1));

            scoreGroup.addView(new Space(scoreGroup.getContext()), new LinearLayout.LayoutParams(dp2px(4), -1));

            if (finishCallback != null) {
                finishCallback.call(scoreLabel);
            }
        }
    }

    public static class SelectFundFragment extends SimpleFragment {
        public static PublishSubject<FundBrief> SELECT_FUND_SUBJECT = PublishSubject.create();

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_select_fund, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this), R.drawable.ic_close_light);

            setOnSwipeRefreshListener(() -> fetchData(false));
            v_setClick(mReloadSection, v -> fetchData(true));

            changeVisibleSection(TYPE_LOADING);

            fetchData(true);
        }

        private void fetchData(boolean reload) {
            consumeEventMRUpdateUI(FundController.fetchRecommendFundList(reload), reload)
                    .onNextSuccess(response -> {
                        List<VM> items = Stream.of(response.data).map(it -> new VM(it)).collect(Collectors.toList());
                        updateContentSection(items);
                    })
                    .done();
        }

        @SuppressWarnings("deprecation")
        private void updateContentSection(List<VM> items) {
            RecyclerView recyclerView = v_findView(mContentSection, R.id.recyclerView);
            if (recyclerView.getAdapter() != null) {
                SimpleRecyclerViewAdapter<VM> adapter = getSimpleAdapter(recyclerView);
                adapter.resetItems(items);
            } else {
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
                addCellVerticalSpacing(recyclerView, dp2px(16));
                addContentInset(recyclerView, new Rect(dp2px(10), 0, dp2px(10), 0));

                SimpleRecyclerViewAdapter<VM> adapter = new SimpleRecyclerViewAdapter.Builder<>(items)
                        .onCreateItemView(R.layout.cell_select_fund)
                        .onCreateViewHolder(builder -> {
                            builder.bindChildWithTag("coverImage", R.id.img_cover)
                                    .bindChildWithTag("nameLabel", R.id.label_name)
                                    .bindChildWithTag("descLabel", R.id.label_desc)
                                    .bindChildWithTag("tagImage", R.id.img_tag)
                                    .configureView((item, pos) -> {
                                        v_setImageUri(builder.getChildWithTag("coverImage"), item.coverImageURL);
                                        v_setText(builder.getChildWithTag("nameLabel"), item.name);
                                        v_setText(builder.getChildWithTag("descLabel"), item.desc);
                                        v_setImageResource(builder.getChildWithTag("tagImage"), item.tagImageResID);
                                    });
                            return builder.create();
                        })
                        .onViewHolderCreated((ad, holder) -> {
                            holder.itemView.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(WHITE_COLOR, dp2px(4)).border(LINE_BORDER_COLOR, dp2px(0.5f))));
                            v_setClick(holder.itemView, v -> {
                                VM item = ad.getItem(holder.getAdapterPosition());
                                SELECT_FUND_SUBJECT.onNext(item.raw);
                                goBack(this);
                            });
                        })
                        .create();
                recyclerView.setAdapter(adapter);
            }
        }

        private static class VM {
            public String coverImageURL;
            public CharSequence name;
            public CharSequence desc;
            public FundBrief raw;
            public int tagImageResID;

            public VM(FundBrief raw) {
                this.raw = raw;
                this.coverImageURL = safeGet(() -> raw.fundIcon, "");
                this.name = safeGet(() -> raw.name, PlaceHolder.NULL_VALUE);

                this.desc = safeGet(() -> {
                    String raisedAmount = formatMoney(raw.targetCapital, false, 0) + Money_Type.getUnit(raw.moneyType);
                    String duration;
                    if (anyMatch(raw.innerType, Fund_Type.Porfolio, Fund_Type.WuYo, Fund_Type.WenJian)) {
                        duration = "T+" + raw.tradingDay;
                    } else {
                        duration = ObjectExtension.toString("", formatRemainingTimeOverMonth(raw.stopTime - raw.startTime));
                    }
                    return raisedAmount + " · " + duration;
                }, PlaceHolder.NULL_VALUE);

                boolean isStop = safeGet(() -> raw.status == Fund_Status.Stop, false);
                int innerType = safeGet(() -> raw.innerType, -1);
                if (isStop) {
                    this.tagImageResID = R.mipmap.ic_tag_fund_stop;
                } else if (anyMatch(innerType, Fund_Type.Porfolio)) {
                    this.tagImageResID = R.mipmap.ic_tag_fund_jinqu;
                } else if (anyMatch(innerType, Fund_Type.WuYo)) {
                    this.tagImageResID = R.mipmap.ic_tag_fund_wuyou;
                } else if (anyMatch(innerType, Fund_Type.WenJian)) {
                    this.tagImageResID = R.mipmap.ic_tag_fund_wenyin;
                } else {
                    this.tagImageResID = 0;
                }
            }
        }
    }
}
