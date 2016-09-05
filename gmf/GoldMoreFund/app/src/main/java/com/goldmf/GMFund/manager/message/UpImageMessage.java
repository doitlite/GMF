package com.goldmf.GMFund.manager.message;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.manager.common.CommonManager;
import com.goldmf.GMFund.protocol.BookFundProtocol;
import com.goldmf.GMFund.util.UploadFileUtil;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rx.functions.Action1;
import rx.functions.Action2;

/**
 * Created by cupide on 15/11/30.
 */
public class UpImageMessage extends SendMessage {
    private String imageFile;

    public double percent;

    public List<String> imageFileList= new ArrayList<>();
    public List<String> contentList = new ArrayList<>();

    public List<String> imageUrlList = new ArrayList<>();

    public String getImageFile(){
        return imageFile;
    }

    public UpImageMessage(String imageFile, int imageWidth, int imageHeight){
        super("");

        super.messagType = 2;
        super.templateType = Message_Image;
        this.imageFile = imageFile;
        super.imageUrl = imageFile;
        super.imageWidth = imageWidth;
        super.imageHeight = imageHeight;
    }

    //创建一个topic话题
    public UpImageMessage(String imageFile, int imageWidth, int imageHeight, String content, String title){
        super(content);

        super.messagType = 2;
        super.templateType = Message_Topic;
        this.imageFile = imageFile;
        super.imageWidth = imageWidth;
        super.imageHeight = imageHeight;
        super.title = title;
    }

    //创建一个带图片的 BarMessage
    public UpImageMessage(String content, List<String> imageFileList){
        super(content);

        super.messagType = 2;
        super.templateType = Message_Image_2;
        this.imageUrlList.addAll(imageFileList);
    }

    protected void uploadFile(@Nullable Action1<Double> progressListener,
                              @Nullable Action1<Boolean> completeListener) {

        if (completeListener == null)
            return;
        UpImageMessage.this.imageUrlList.clear();


        if (this.templateType == Message_Image) {

            Action1<Double> progressAction = (percent) -> {
                UpImageMessage.this.percent = percent;
                if (progressListener != null) {
                    progressListener.call(percent);
                }
            };

            Action2<Boolean, String> completionAction = (success, url) -> {
                if (success) {
                    UpImageMessage.this.imageUrl = url;
                    UpImageMessage.this.percent = 1.0f;
                }


                completeListener.call(success);
            };

            UploadFileUtil.uploadFile(CommonManager.ChatImg,
                    new File(UpImageMessage.this.getImageFile()),
                    progressAction,
                    completionAction);
        } else {
            this.loading = false;

            if(getImageFile() != null && getImageFile().length() > 0)
            {
                UploadFileUtil.uploadFile(CommonManager.ChatImg,
                        new File(UpImageMessage.this.getImageFile()),
                        null,
                        (success, url) -> {
                            if (success) {
                                UpImageMessage.this.imageUrl = url;

                                ArrayList<String> pathList = new ArrayList<>(UpImageMessage.this.imageFileList);
                                UpImageMessage.this.uploadFileList(pathList, completeListener);

                            } else {
                                //finish
                                completeListener.call(false);
                            }
                        });
            } else {

                ArrayList<String> pathList = new ArrayList<>(UpImageMessage.this.imageFileList);
                UpImageMessage.this.uploadFileList(pathList, completeListener);
            }
        }

    }

    private void uploadFileList(List<String> pathList,
                                @Nullable Action1<Boolean> completeListener) {

        if (completeListener == null)
            return;

        if(pathList != null && pathList.size() > 0) {
            String tempImageFile = pathList.get(0);

            MyApplication.SHARE_INSTANCE.mHandler.post(() -> {

                UploadFileUtil.uploadFile(CommonManager.ChatImg,
                        new File(tempImageFile),
                        null,
                        (success, url) -> {
                            if (success) {
                                pathList.remove(0);
                                UpImageMessage.this.imageUrlList.add(url);

                                uploadFileList(pathList, completeListener);

                            } else {
                                //finish
                                completeListener.call(false);
                            }
                        });
            });
        } else {
            //finish
            completeListener.call(true);
        }
    }

}
