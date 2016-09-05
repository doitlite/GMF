package com.goldmf.GMFund.controller.internal;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;

import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.controller.SimpleFragment;
import com.goldmf.GMFund.controller.UserDetailFragments;
import com.goldmf.GMFund.controller.business.UserController;
import com.goldmf.GMFund.controller.chat.ChatCacheManager;
import com.goldmf.GMFund.controller.dialog.GMFBottomSheet;
import com.goldmf.GMFund.extension.UIControllerExtension;
import com.goldmf.GMFund.manager.common.CommonManager;
import com.goldmf.GMFund.manager.mine.Mine;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.util.UploadFileUtil;
import com.goldmf.GMFund.widget.GMFProgressDialog;
import com.soundcloud.android.crop.Crop;

import java.io.File;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import static com.goldmf.GMFund.extension.MResultExtension.getErrorMessage;
import static com.goldmf.GMFund.extension.MResultExtension.isSuccess;
import static com.goldmf.GMFund.extension.MResultExtension.map;
import static com.goldmf.GMFund.extension.ObjectExtension.safeCall;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.UIControllerExtension.showToast;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;

/**
 * Created by yale on 16/6/3.
 */
public class PickAvatarHelper {
    private static final int REQUEST_CODE_IMAGE_FROM_CAMERA = 283;
    private static final int REQUEST_CODE_IMAGE_FROM_GALLERY = 284;

    public static void showPickPhotoBottomSheet(SimpleFragment fragment, File[] outPictureFile) {
        GMFBottomSheet.Builder builder = new GMFBottomSheet.Builder(fragment.getActivity());
        builder.addContentItem(new GMFBottomSheet.BottomSheetItem("camera", "拍照", 0));
        builder.addContentItem(new GMFBottomSheet.BottomSheetItem("gallery", "从手机相册选取图片", 0));
        boolean isBindWX = safeGet(() -> MineManager.getInstance().getmMe().isBindWX, false);
        if (!isBindWX) {
            builder.addContentItem(new GMFBottomSheet.BottomSheetItem("wechat", "使用我的微信头像", 0));
        }
        GMFBottomSheet sheet = builder.create();

        sheet.setOnItemClickListener((bottomSheet, item) -> {
            bottomSheet.dismiss();
            if (item.tag != null) {
                boolean stat = fragment instanceof UserDetailFragments.UserDetailFragment;


                String id = item.tag.toString();
                if (id.equals("camera") || id.equals("gallery")) {
                    safeCall(() -> {
                        Intent[] intents = new Intent[1];
                        if (id.equals("camera")) {
                            PickImageHelper.createPickImageFromCameraIntent(outPictureFile, intents, null);
                        } else {
                            PickImageHelper.createPickImageFromGalleryIntent(outPictureFile, intents, null);
                        }
                        if (intents[0] != null) {
                            fragment.startActivityForResult(intents[0], id.equals("camera") ? REQUEST_CODE_IMAGE_FROM_CAMERA : REQUEST_CODE_IMAGE_FROM_GALLERY);
                        }
                    });
                } else if (id.equals("wechat")) {
                    if (stat) {
                        UmengUtil.stat_click_event(UmengUtil.eEVENTIDUserHomeAlertChooseWX);
                    }

                    GMFProgressDialog progressDialog = new GMFProgressDialog(fragment.getActivity(), "正在获取微信信息...");
                    progressDialog.show();
                    Observable<MResults.MResultsInfo<UmengUtil.WXLoginInfo>> observable = UmengUtil.fetchWXAccessToken(fragment.getActivity())
                            .observeOn(AndroidSchedulers.mainThread())
                            .flatMap(response -> isSuccess(response) ?
                                    UserController.bindWXAccount(response.data).map(it -> map(it, response.data)) : Observable.just(response));
                    fragment.consumeEventMR(observable)
                            .setTag("fetch_wx_info")
                            .onNextSuccess(wxResponse -> {
                                UmengUtil.WXLoginInfo loginInfo = wxResponse.data;
                                Mine me = MineManager.getInstance().getmMe();
                                if (me != null) {
                                    me.isBindWX = true;
                                    me.wxNickName = loginInfo.nickName;
                                }
                                fragment.consumeEventMR(UserController.modifyAvatar(loginInfo.avatarURL, true))
                                        .onNextStart(response -> progressDialog.dismiss())
                                        .onNextSuccess(response -> {
                                            NotificationCenter.userInfoChangedSubject.onNext(null);
                                            showToast(fragment, "修改成功");
                                        })
                                        .onNextFail(response -> {
                                            if (response.errCode == 22001) {
                                                NotificationCenter.userInfoChangedSubject.onNext(null);
                                                showToast(fragment, "修改成功");
                                            } else {
                                                showToast(fragment, getErrorMessage(response));
                                            }
                                        })
                                        .done();
                            })
                            .onNextFail(response -> {
                                progressDialog.dismiss();
                                showToast(fragment, getErrorMessage(response));
                            })
                            .done();
                }
            }
        });
        sheet.show();
    }

    public static void handlePickAvatarCallback(SimpleFragment fragment, int requestCode, int resultCode, Intent data, File[] outPictureFiles) {
        if (requestCode == REQUEST_CODE_IMAGE_FROM_CAMERA || requestCode == REQUEST_CODE_IMAGE_FROM_GALLERY) {
            if (outPictureFiles != null && outPictureFiles.length > 0) {
                Bitmap[] thumbnails = new Bitmap[1];
                Rect thumbnailSize = new Rect(0, 0, dp2px(30), dp2px(30));
                boolean isSuccess;
                if (requestCode == REQUEST_CODE_IMAGE_FROM_CAMERA) {
                    isSuccess = PickImageHelper.handlePickImageFromCameraActivityResult(requestCode, resultCode, data, outPictureFiles[0], thumbnails, thumbnailSize);
                } else {
                    isSuccess = PickImageHelper.handlePickImageFromGalleryActivityResult(requestCode, resultCode, data, outPictureFiles[0], thumbnails, thumbnailSize);
                }

                if (isSuccess) {
                    beginCrop(fragment, outPictureFiles, Uri.fromFile(outPictureFiles[0]));
                }
            }
        } else if (requestCode == Crop.REQUEST_CROP) {
            if (resultCode == Activity.RESULT_OK) {
                if (outPictureFiles != null && outPictureFiles.length > 0) {
                    performModifyAvatar(fragment, outPictureFiles[0]);
                }
            }
        }
    }

    private static void beginCrop(SimpleFragment fragment, File[] outPictureFiles, Uri source) {
        String destinationPath = ChatCacheManager.shareManager().getNewCacheImagePath();
        outPictureFiles[0] = new File(destinationPath);
        Uri destination = Uri.fromFile(new File(destinationPath));
        Crop.of(source, destination)
                .asSquare()
                .withMaxSize(256, 256)
                .start(fragment.getActivity(), fragment, Crop.REQUEST_CROP);
    }

    private static void performModifyAvatar(SimpleFragment fragment, File avatarFile) {
        GMFProgressDialog progressView = new GMFProgressDialog(fragment.getActivity());
        progressView.setMessage("正在提交，请稍后");
        progressView.show();

        UploadFileUtil.uploadFile(CommonManager.Avator, avatarFile, null, (result, imagePath) -> {
            if (result) {
                fragment.consumeEventMR(UserController.modifyAvatar(imagePath, false))
                        .onNextStart(response -> progressView.dismiss())
                        .onNextSuccess(response -> {
                            NotificationCenter.userInfoChangedSubject.onNext(null);
                            showToast(fragment, "修改成功");
                        })
                        .onNextFail(response -> {
                            UIControllerExtension.createAlertDialog(fragment, getErrorMessage(response)).show();
                        })
                        .done();
            } else {
                progressView.dismiss();
                showToast(fragment, "上传失败");
            }
        });
    }
}
