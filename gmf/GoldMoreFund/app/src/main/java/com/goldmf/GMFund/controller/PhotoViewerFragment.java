package com.goldmf.GMFund.controller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facebook.cache.common.CacheKey;
import com.facebook.common.references.CloseableReference;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.cache.CacheKeyFactory;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.cache.MemoryCache;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.base.KeepClassProtocol;
import com.goldmf.GMFund.controller.dialog.GMFBottomSheet;
import com.goldmf.GMFund.extension.BitmapExtension;
import com.goldmf.GMFund.extension.FileExtension;
import com.goldmf.GMFund.model.GMFMessage.ShortcutImagePair;
import com.goldmf.GMFund.util.FrescoHelper;
import com.goldmf.GMFund.widget.fresco.DefaultZoomableController;
import com.goldmf.GMFund.widget.fresco.ZoomableDraweeView;
import com.orhanobut.logger.Logger;
import com.yale.ui.framelayout.InterceptableFrameLayout;
import com.yale.ui.pager.AdvanceViewPager;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.RunnableFuture;

import static android.content.Context.ACCESSIBILITY_SERVICE;
import static android.content.Context.MODE_APPEND;
import static com.goldmf.GMFund.controller.FragmentStackActivity.goBack;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.BLACK_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.WHITE_COLOR;
import static com.goldmf.GMFund.extension.IntExtension.anyMatch;
import static com.goldmf.GMFund.extension.ObjectExtension.opt;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.UIControllerExtension.getScreenSize;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.showToast;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_setGone;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageUri;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisibility;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisible;
import static com.goldmf.GMFund.extension.ViewExtension.v_updateLayoutParams;

/**
 * Created by yale on 16/5/27.
 */

public class PhotoViewerFragment extends SimpleFragment {

    private int mIndex;
    private List<ShortcutImagePair> mImageURLs;
    private Rect[] mRectArray;
    private boolean mIsEntering = false;
    private boolean mIsExiting = false;

    private InterceptableFrameLayout mRootView;
    private View mBGLayer;
    private SimpleDraweeView mPreviewImage;
    private AdvanceViewPager mPager;
    private TextView mCountLabel;
    private Button mSaveButton;

    public PhotoViewerFragment init(List<ShortcutImagePair> imageURLs, int index) {
        Bundle arguments = new Bundle();
        if (imageURLs instanceof Serializable) {
            arguments.putSerializable(CommonProxyActivity.KEY_IMAGE_LIST, (Serializable) imageURLs);
        }
        arguments.putInt(CommonProxyActivity.KEY_TAB_IDX_INT, index);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mImageURLs = safeGet(() -> (List<ShortcutImagePair>) getArguments().getSerializable(CommonProxyActivity.KEY_IMAGE_LIST), Collections.<ShortcutImagePair>emptyList());
        mIndex = getArguments().getInt(CommonProxyActivity.KEY_TAB_IDX_INT);
        Parcelable[] parcelables = safeGet(() -> getArguments().getParcelableArray(CommonProxyActivity.KEY_RECT_LIST), new Parcelable[]{});
        mRectArray = new Rect[parcelables.length];
        System.arraycopy(parcelables, 0, mRectArray, 0, parcelables.length);
        return inflater.inflate(R.layout.frag_photo_viewer, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setStatusBarBackgroundColor(this, BLACK_COLOR);

        mRootView = (InterceptableFrameLayout) view;
        mBGLayer = v_findView(this, R.id.layer_bg);
        mPreviewImage = v_findView(this, R.id.img_preview);
        mCountLabel = v_findView(this, R.id.label_count);
        mPager = v_findView(this, R.id.pager);
        mSaveButton = v_findView(this, R.id.btn_save);

        mBGLayer.setAlpha(0.0f);

        runOnUIThread(() -> {
            ViewConfiguration configuration = ViewConfiguration.get(getContext());
            PointF mDownPoint = new PointF();
            PointF mCurrentPoint = new PointF();


            mRootView.setOnInterceptTouchEventDelegate(new InterceptableFrameLayout.OnInterceptTouchEventDelegate() {
                boolean ignoreEvent = false;
                long downTimeInMills = 0;


                @SuppressWarnings("ConstantConditions")
                @Override
                public Boolean onInterceptTouchEvent(MotionEvent ev) {
                    Boolean isScaling = ev.getPointerCount() > 1 || safeGet(() -> getCurrentChildFragment(mPager).mController.getScaleFactor() > 1.0f, false);
                    if (isScaling) {
                        ignoreEvent = true;
                    }

                    int action = ev.getAction();
                    if (action == MotionEvent.ACTION_DOWN) {
                        mDownPoint.set(ev.getRawX(), ev.getRawY());
                        downTimeInMills = System.currentTimeMillis();
                        ignoreEvent = false;
                    } else if (!ignoreEvent && action == MotionEvent.ACTION_MOVE) {
                        float absDX = Math.abs(ev.getRawX() - mCurrentPoint.x);
                        float absDY = Math.abs(ev.getRawY() - mCurrentPoint.y);
                        if (absDX > configuration.getScaledTouchSlop()) {
                            ignoreEvent = true;
                        }

                        if (!ignoreEvent && absDY > configuration.getScaledTouchSlop()) {
                            return true;
                        }
                    } else if (action == MotionEvent.ACTION_UP) {
                        long passedTimeInMills = System.currentTimeMillis() - downTimeInMills;
                        float absDx = Math.abs(ev.getRawX() - mDownPoint.x);
                        float absDy = Math.abs(ev.getRawY() - mDownPoint.y);
                        if (passedTimeInMills < ViewConfiguration.getTapTimeout() && absDx < configuration.getScaledTouchSlop() && absDy < configuration.getScaledTouchSlop()) {
                            exitWithAnimation();
                        }
                    }

                    mCurrentPoint.set(ev.getRawX(), ev.getRawY());

                    return false;
                }
            });
            mRootView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent ev) {
                    int action = ev.getAction();
                    if (action == MotionEvent.ACTION_MOVE) {
                        float dy = ev.getRawY() - mCurrentPoint.y;
                        offsetTranslationY(mPager, dy);
                        float alpha = 1.0f - (Math.abs(mPager.getTranslationY()) / mPager.getMeasuredHeight());
                        mBGLayer.setAlpha(alpha);
                    } else if (anyMatch(action, MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL)) {
                        float translationY = mPager.getTranslationY();
                        float absTranslationY = Math.abs(translationY);
                        int pagerHeight = mPager.getMeasuredHeight();

                        if (absTranslationY > pagerHeight / 4) {
                            exitWithAnimation();
                        } else {
                            AnimatorSet set = new AnimatorSet();
                            set.playTogether(ObjectAnimator.ofFloat(mPager, "translationY", 0), ObjectAnimator.ofFloat(mBGLayer, "alpha", 1.0f));
                            set.start();
                        }

                    }
                    mCurrentPoint.set(ev.getRawX(), ev.getRawY());
                    return true;
                }
            });
        });

        enterWithAnimation(view);
    }

    private static void offsetTranslationY(View view, float offsetY) {
        view.setTranslationY(view.getTranslationY() + offsetY);
    }

    @Override
    protected boolean onInterceptGoBack() {
        return mIsEntering || mIsExiting || super.onInterceptGoBack();
    }

    @Override
    protected boolean onInterceptKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitWithAnimation();
            return true;
        }
        return super.onInterceptKeyDown(keyCode, event);
    }

    private void enterWithAnimation(View view) {
        view.post(() -> {
            if (mRectArray.length > mIndex) {
                Rect initImageRect = mRectArray[mIndex];

                Rect parentRect = new Rect();
                view.getGlobalVisibleRect(parentRect);

                v_updateLayoutParams(mPreviewImage, FrameLayout.LayoutParams.class, params -> {
                    params.width = initImageRect.width();
                    params.height = initImageRect.height();
                    params.leftMargin = initImageRect.left + parentRect.left;
                    params.topMargin = initImageRect.top + parentRect.top;
                });

                ShortcutImagePair pair = mImageURLs.get(mIndex);
                v_setImageUri(mPreviewImage, pair.shortcutUrl);
                Float ratio = getImageRatio(pair);
                if (ratio == null) {
                    goBack(this);
                    return;
                }
                Rect dstRect = new Rect();
                // prepare rect
                {
                    dstRect.setEmpty();
                    float parentRatio = (float) parentRect.width() / parentRect.height();
                    if (ratio == 1.0f) {
                        dstRect.right = parentRect.width();
                        dstRect.bottom = parentRect.width();
                    } else if (ratio > parentRatio) {
                        dstRect.right = parentRect.width();
                        dstRect.bottom = (int) ((float) parentRect.width() / ratio);
                    } else {
                        dstRect.right = (int) ((float) parentRect.height() * ratio);
                        dstRect.bottom = parentRect.height();
                    }
                    dstRect.offset((parentRect.width() - dstRect.width()) >> 1, 0);
                    dstRect.offset(0, (parentRect.height() - dstRect.height()) >> 1);
                }

                FrameLayout.LayoutParams mParams = (FrameLayout.LayoutParams) mPreviewImage.getLayoutParams();
                PropertyValuesHolder setLeft = PropertyValuesHolder.ofInt("left", mParams.leftMargin, dstRect.left);
                PropertyValuesHolder setTop = PropertyValuesHolder.ofInt("top", mParams.topMargin, dstRect.top);
                PropertyValuesHolder setWidth = PropertyValuesHolder.ofInt("width", mParams.width, dstRect.width());
                PropertyValuesHolder setHeight = PropertyValuesHolder.ofInt("height", mParams.height, dstRect.height());
                KeepClassProtocol proxy = new KeepClassProtocol() {

                    void setLeft(int left) {
                        mParams.leftMargin = left;
                        mPreviewImage.requestLayout();
                    }

                    void setTop(int top) {
                        mParams.topMargin = top;
                        mPreviewImage.requestLayout();
                    }

                    void setWidth(int width) {
                        mParams.width = width;
                        mPreviewImage.requestLayout();
                    }

                    void setHeight(int height) {
                        mParams.height = height;
                        mPreviewImage.requestLayout();
                    }

                };
                mIsEntering = true;
                ObjectAnimator previewImageAnimator = ObjectAnimator.ofPropertyValuesHolder(proxy, setLeft, setTop, setWidth, setHeight);
                previewImageAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        resetViewPager(mImageURLs, mIndex);
                        runOnUIThreadDelayed(() -> v_setGone(mPreviewImage), 200L);
                        mIsEntering = false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        super.onAnimationCancel(animation);
                        resetViewPager(mImageURLs, mIndex);
                        runOnUIThreadDelayed(() -> v_setGone(mPreviewImage), 200L);
                        mIsEntering = false;
                    }

                });
                ObjectAnimator bgLayerAnimator = ObjectAnimator.ofFloat(mBGLayer, "alpha", 1.0f);
                AnimatorSet set = new AnimatorSet();
                set.play(previewImageAnimator).with(bgLayerAnimator);
                set.start();
            }
        });
    }


    private void exitWithAnimation() {
        if (!mIsEntering && !mIsExiting) {
            mIsExiting = true;
            int currentIdx = mPager.getCurrentItem();
            if (currentIdx >= 0) {
                ShortcutImagePair pair = mImageURLs.get(currentIdx);
                Float ratio = getImageRatio(pair);
                View parent = getView();
                if (ratio == null || parent == null) {
                    mIsExiting = false;
                    goBack(this);
                    return;
                }


                v_setImageUri(mPreviewImage, pair.shortcutUrl);

                Rect parentRect = new Rect();
                parent.getGlobalVisibleRect(parentRect);

                Rect startRect = new Rect();
                // prepare rect
                {
                    startRect.setEmpty();
                    float parentRatio = (float) parentRect.width() / parent.getHeight();
                    if (ratio == 1.0f) {
                        startRect.right = parentRect.width();
                        startRect.bottom = parentRect.width();
                    } else if (ratio > parentRatio) {
                        startRect.right = parentRect.width();
                        startRect.bottom = (int) ((float) parentRect.width() / ratio);
                    } else {
                        startRect.right = (int) ((float) parentRect.height() * ratio);
                        startRect.bottom = parentRect.height();
                    }
                    startRect.offset((parentRect.width() - startRect.width()) >> 1, 0);
                    startRect.offset(0, (parentRect.height() - startRect.height()) >> 1);
                    startRect.offset(0, (int) mPager.getTranslationY());
                }

                Rect endRect = mRectArray[currentIdx];
                FrameLayout.LayoutParams mParams = (FrameLayout.LayoutParams) mPreviewImage.getLayoutParams();
                PropertyValuesHolder setLeft = PropertyValuesHolder.ofInt("left", startRect.left, endRect.left);
                PropertyValuesHolder setTop = PropertyValuesHolder.ofInt("top", startRect.top, endRect.top);
                PropertyValuesHolder setWidth = PropertyValuesHolder.ofInt("width", startRect.width(), endRect.width());
                PropertyValuesHolder setHeight = PropertyValuesHolder.ofInt("height", startRect.height(), endRect.height());
                KeepClassProtocol proxy = new KeepClassProtocol() {

                    void setLeft(int left) {
                        mParams.leftMargin = left;
                        mPreviewImage.requestLayout();
                    }

                    void setTop(int top) {
                        mParams.topMargin = top;
                        mPreviewImage.requestLayout();
                    }

                    void setWidth(int width) {
                        mParams.width = width;
                        mPreviewImage.requestLayout();
                    }

                    void setHeight(int height) {
                        mParams.height = height;
                        mPreviewImage.requestLayout();
                    }

                };

                v_setGone(mPager);
                v_setVisible(mPreviewImage);
                ObjectAnimator previewAnimator = ObjectAnimator.ofPropertyValuesHolder(proxy, setLeft, setTop, setWidth, setHeight);
                previewAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mIsExiting = false;
                        goBack(PhotoViewerFragment.this);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        super.onAnimationCancel(animation);
                        mIsExiting = false;
                        goBack(PhotoViewerFragment.this);
                    }

                });
                ObjectAnimator bgLayerAnimator = ObjectAnimator.ofFloat(mBGLayer, "alpha", 0.0f);
                AnimatorSet set = new AnimatorSet();
                set.play(previewAnimator).with(bgLayerAnimator);
                set.start();
            }
        }
    }

    private void resetViewPager(List<ShortcutImagePair> imageURLs, int initIdx) {
        Context ctx = getActivity();
        v_setVisibility(mCountLabel, imageURLs.size() > 1 ? View.VISIBLE : View.GONE);

        ViewPager.OnPageChangeListener listener = new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCountLabel.setText(String.format(Locale.getDefault(), "%d/%d", position + 1, imageURLs.size()));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        };
        mPager.addOnPageChangeListener(listener);

        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mPager.setOnPreInterceptTouchEventDelegate(new AdvanceViewPager.OnPreInterceptTouchEventDelegate() {

            @Override
            public Boolean shouldDisallowInterceptTouchEvent(MotionEvent event) {
                if (event.getPointerCount() > 1) {
                    return true;
                }

                float scaleFactor = 1.0f;
                PhotoViewerPageFragment currentFragment = PhotoViewerFragment.this.getCurrentChildFragment(mPager);
                if (currentFragment != null) {
                    DefaultZoomableController controller = currentFragment.mController;
                    scaleFactor = controller.getScaleFactor();
                }

                if (scaleFactor > 1.0f) {
                    return true;
                }

                return null;
            }
        });

        mPager.setAdapter(new MyPagerAdapter(getChildFragmentManager(), imageURLs));
        mPager.setCurrentItem(initIdx);
        listener.onPageSelected(initIdx);
    }

    private PhotoViewerPageFragment getCurrentChildFragment(ViewPager pager) {
        if (pager.getAdapter() != null) {
            MyPagerAdapter adapter = (MyPagerAdapter) pager.getAdapter();
            PhotoViewerPageFragment currentFragment = adapter.liveItems[pager.getCurrentItem()];
            if (currentFragment != null) {
                return currentFragment;
            }
        }
        return null;
    }

    private Float getImageRatio(ShortcutImagePair pair) {
        ImagePipeline pipeline = Fresco.getImagePipeline();
        CacheKeyFactory factory = DefaultCacheKeyFactory.getInstance();
        MemoryCache<CacheKey, CloseableImage> memoryCache = Fresco.getImagePipelineFactory().getBitmapMemoryCache();

        ImageRequest lowRequest = ImageRequest.fromUri(pair.shortcutUrl);
        ImageRequest request = ImageRequest.fromUri(pair.sourceUrl);

        if (pipeline.isInBitmapMemoryCache(request)) {
            CacheKey key = factory.getBitmapCacheKey(request, null);
            if (key != null) {
                CloseableReference<CloseableImage> imageRef = memoryCache.get(key);
                if (imageRef != null) {
                    CloseableImage image = imageRef.get();
                    if (image != null && image.getWidth() > 0 && image.getHeight() > 0) {
                        return (float) image.getWidth() / image.getHeight();
                    }
                    imageRef.close();
                }
            }
        }
        if (pipeline.isInBitmapMemoryCache(lowRequest)) {
            CacheKey key = factory.getBitmapCacheKey(lowRequest, null);
            if (key != null) {
                CloseableReference<CloseableImage> imageRef = memoryCache.get(key);
                if (imageRef != null) {
                    CloseableImage image = imageRef.get();
                    if (image != null && image.getWidth() > 0 && image.getHeight() > 0) {
                        return (float) image.getWidth() / image.getHeight();
                    }
                    imageRef.close();
                }
            }
        }

        return null;
    }

    private static class MyPagerAdapter extends FragmentPagerAdapter {
        private List<ShortcutImagePair> imageURLs;
        private PhotoViewerPageFragment[] liveItems;

        public MyPagerAdapter(FragmentManager manager, List<ShortcutImagePair> imageURLs) {
            super(manager);
            this.imageURLs = imageURLs;
            this.liveItems = new PhotoViewerPageFragment[imageURLs.size()];
        }


        @Override
        public Fragment getItem(int position) {
            return new PhotoViewerPageFragment().init(position);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Object item = super.instantiateItem(container, position);
            liveItems[position] = (PhotoViewerPageFragment) item;
            return item;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
            liveItems[position] = null;
        }

        @Override
        public int getCount() {
            return imageURLs.size();
        }
    }

    public static class PhotoViewerPageFragment extends Fragment {

        private int mIndex;
        private DefaultZoomableController mController;

        public PhotoViewerPageFragment init(int index) {
            Bundle arguments = new Bundle();
            arguments.putInt(CommonProxyActivity.KEY_TAB_IDX_INT, index);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container2, @Nullable Bundle savedInstanceState) {
            mIndex = getArguments().getInt(CommonProxyActivity.KEY_TAB_IDX_INT);

            Context ctx = inflater.getContext();
            Resources res = ctx.getResources();
            FrameLayout container = new FrameLayout(getActivity());

            GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(res)
                    .setFadeDuration(0)
                    .setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                    .build();
            ZoomableDraweeView draweeView = new ZoomableDraweeView(ctx);
            draweeView.setId(R.id.img_cover);
            draweeView.setHierarchy(hierarchy);
            mController = DefaultZoomableController.newInstance();
            draweeView.setZoomableController(mController);
            container.addView(draweeView, new FrameLayout.LayoutParams(-1, -1));

            container.setLayoutParams(new ViewPager.LayoutParams());
            return container;
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            ZoomableDraweeView draweeView = v_findView(this, R.id.img_cover);
            ShortcutImagePair pair = PhotoViewerFragment.class.cast(getParentFragment()).mImageURLs.get(mIndex);
            ImageRequest lowResImageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(pair.shortcutUrl))
                    .setPostprocessor(new BasePostprocessor() {
                        @Override
                        public void process(Bitmap bitmap) {
                            super.process(bitmap);
                            draweeView.getHierarchy().setProgressBarImage(null);
                        }
                    })
                    .build();
            ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(pair.sourceUrl)).build();


            ImagePipeline pipeline = Fresco.getImagePipeline();
            boolean hasMemoryCache = pipeline.isInBitmapMemoryCache(lowResImageRequest) || pipeline.isInBitmapMemoryCache(imageRequest);
            draweeView.getHierarchy().setProgressBarImage(hasMemoryCache ? null : newProgressBarDrawable());
            PipelineDraweeControllerBuilder controllerBuilder = Fresco.newDraweeControllerBuilder()
                    .setOldController(draweeView.getController())
                    .setLowResImageRequest(lowResImageRequest)
                    .setImageRequest(imageRequest)
                    .setControllerListener(new BaseControllerListener<ImageInfo>() {
                        @Override
                        public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                            super.onFinalImageSet(id, imageInfo, animatable);
                            mController.setMaxScaleFactor(computeMaxScale(imageInfo.getWidth(), imageInfo.getHeight()));
                            mController.setMinScaleFactor(1.0f);
                            draweeView.setZoomableController(mController);
                        }
                    });
            draweeView.setController(controllerBuilder.build());
            draweeView.setOnTouchListener(new View.OnTouchListener() {
                private Handler handler = new Handler();
                private Runnable mLastTask = null;
                private PointF mDownPoint = new PointF();
                private ViewConfiguration mConfiguration = ViewConfiguration.get(getActivity());

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        mDownPoint.set(event.getRawX(), event.getRawY());
                        handler.removeCallbacks(mLastTask);
                        mLastTask = () -> {
                            showSavePhotoSheet(pair);
                        };
                        handler.postDelayed(mLastTask, 1000L);
                    } else if (anyMatch(event.getAction(), MotionEvent.ACTION_MOVE)) {
                        float dx = Math.abs(event.getRawX() - mDownPoint.x);
                        float dy = Math.abs(event.getRawY() - mDownPoint.y);
                        if (dx > mConfiguration.getScaledTouchSlop() || dy > mConfiguration.getScaledTouchSlop()) {
                            handler.removeCallbacks(mLastTask);
                            mLastTask = null;
                        }
                    } else if (anyMatch(event.getAction(), MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL) ||
                            event.getPointerCount() > 1) {
                        handler.removeCallbacks(mLastTask);
                        mLastTask = null;
                    }
                    return false;
                }
            });
        }

        private float computeMaxScale(int imageWidth, int imageHeight) {
            if (imageWidth > 0 && imageHeight > 0) {
                Rect screenSize = getScreenSize(this);
                float ratio = (float) imageWidth / imageHeight;
                float screenRatio = (float) screenSize.width() / screenSize.height();
                if (ratio >= screenRatio) {
                    float actualHeight = (float) screenSize.width() / ratio;
                    return Math.max((float) screenSize.height() / actualHeight, 2);
                } else {
                    float actualWidth = (float) screenSize.height() * ratio;
                    return Math.max((float) screenSize.width() / actualWidth, 2);
                }
            }

            return 2;
        }

        private void showSavePhotoSheet(ShortcutImagePair pair) {
            if (FrescoHelper.isImageInDiskCache(pair.sourceUrl)) {
                GMFBottomSheet bottomSheet = new GMFBottomSheet.Builder(getActivity())
                        .addContentItem(new GMFBottomSheet.BottomSheetItem("save", "保存图片", 0))
                        .create();
                bottomSheet.setOnItemClickListener((sheet, item) -> {
                    sheet.dismiss();
                    Bitmap bitmap = FrescoHelper.getBitmapFromDisk(pair.sourceUrl);
                    if (bitmap != null) {
                        if (new File("/sdcard/DCIM/").exists()) {
                            new File("/sdcard/DCIM/Camera").mkdirs();
                            File savePath = new File("/sdcard/DCIM/Camera", System.currentTimeMillis() + ".jpg");
                            FileExtension.writeDataToFile(savePath, bitmap, true, Bitmap.CompressFormat.JPEG, 100);
                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            mediaScanIntent.setData(Uri.fromFile(savePath));
                            getActivity().sendBroadcast(mediaScanIntent);
                            showToast(getActivity(), "保存成功");
                        } else {
                            showToast(this, "请插入SD卡");
                        }
                        bitmap.recycle();
                    } else {
                        showToast(this, "未知错误，请稍候重试");
                    }
                });
                bottomSheet.show();
            }
        }

    }

    @NonNull
    private static Drawable newProgressBarDrawable() {
        return new Drawable() {
            Paint mPaint = new Paint();
            int size = dp2px(80);
            int radius = size >> 1;
            int borderWidth = dp2px(1);
            int inset = dp2px(2);
            int borderColor = WHITE_COLOR;
            int bgColor = 0x33000000;
            int fgColor = WHITE_COLOR;
            int level = 0;
            RectF rect = new RectF();

            @Override
            public void draw(Canvas canvas) {

                int startX = (canvas.getWidth() - size) >> 1;
                int startY = (canvas.getHeight() - size) >> 1;

                mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(bgColor);
                mPaint.setAlpha(51);
                canvas.drawCircle(startX + radius, startY + radius, radius, mPaint);

                mPaint.setAlpha(255);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(borderWidth);
                mPaint.setColor(borderColor);
                canvas.drawCircle(startX + radius, startY + radius, radius, mPaint);

                rect.set(startX, startY, startX + size, startY + size);
                rect.inset(inset, inset);
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(fgColor);
                float sweepAngle = (float) level * 360 / 10000;

                canvas.drawArc(rect, -90f, sweepAngle, true, mPaint);

            }

            @Override
            public boolean setVisible(boolean visible, boolean restart) {
                return super.setVisible(visible, restart);
            }

            @Override
            public void setAlpha(int alpha) {
                mPaint.setAlpha(alpha);
            }

            @Override
            public void setColorFilter(ColorFilter colorFilter) {
                mPaint.setColorFilter(colorFilter);
            }

            @Override
            public int getOpacity() {
                return mPaint.getAlpha();
            }

            @Override
            protected boolean onLevelChange(int level) {
                this.level = level;
                invalidateSelf();
                return true;
            }
        };
    }
}
