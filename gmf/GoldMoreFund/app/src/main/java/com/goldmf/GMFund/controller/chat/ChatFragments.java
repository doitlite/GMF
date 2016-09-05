package com.goldmf.GMFund.controller.chat;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.goldmf.GMFund.MyConfig;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.cmd.CMDParser;
import com.goldmf.GMFund.controller.BaseFragment;
import com.goldmf.GMFund.controller.CommonFragments.ViewPictureFragment;
import com.goldmf.GMFund.controller.FragmentStackActivity;
import com.goldmf.GMFund.controller.chat.ChatViewModels.PlainImageMsgCellVM;
import com.goldmf.GMFund.controller.chat.ChatViewModels.PlainTextMsgCellVM;
import com.goldmf.GMFund.controller.chat.ChatViewModels.SystemImageMsgCellVM;
import com.goldmf.GMFund.controller.chat.ChatViewModels.WelcomeTipsMsgCellVM;
import com.goldmf.GMFund.controller.dialog.GMFBottomSheet;
import com.goldmf.GMFund.controller.dialog.GMFDialog;
import com.goldmf.GMFund.controller.protocol.VCStateDataProtocol;
import com.goldmf.GMFund.extension.BitmapExtension;
import com.goldmf.GMFund.manager.message.MessageManager;
import com.goldmf.GMFund.manager.message.MessageSession;
import com.goldmf.GMFund.manager.message.SendMessage;
import com.goldmf.GMFund.manager.message.UpImageMessage;
import com.goldmf.GMFund.model.GMFMessage;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.widget.EmbedProgressView;
import com.goldmf.GMFund.widget.SimpleOnItemTouchListener;
import com.goldmf.GMFund.widget.UploadLayer;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action2;
import rx.functions.Func2;
import rx.subjects.PublishSubject;
import yale.extension.common.Optional;
import yale.extension.common.shape.RoundCornerShape;

import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_LINK_ID_STRING;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_MESSAGE_TYPE_INT;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_SESSION_ID_STRING;
import static com.goldmf.GMFund.controller.FragmentStackActivity.CLIPBOARD_SERVICE;
import static com.goldmf.GMFund.controller.FragmentStackActivity.goBack;
import static com.goldmf.GMFund.controller.FragmentStackActivity.pushFragment;
import static com.goldmf.GMFund.controller.chat.ChatViewModels.BaseMsgCellVM.STATE_FAIL;
import static com.goldmf.GMFund.controller.chat.ChatViewModels.BaseMsgCellVM.STATE_OK;
import static com.goldmf.GMFund.controller.chat.ChatViewModels.BaseMsgCellVM.TYPE_IMAGE_NOTIFICATION;
import static com.goldmf.GMFund.controller.chat.ChatViewModels.BaseMsgCellVM.TYPE_PLAIN_IMAGE_LEFT;
import static com.goldmf.GMFund.controller.chat.ChatViewModels.BaseMsgCellVM.TYPE_PLAIN_IMAGE_RIGHT;
import static com.goldmf.GMFund.controller.chat.ChatViewModels.BaseMsgCellVM.TYPE_PLAIN_TEXT_LEFT;
import static com.goldmf.GMFund.controller.chat.ChatViewModels.BaseMsgCellVM.TYPE_PLAIN_TEXT_RIGHT;
import static com.goldmf.GMFund.controller.chat.ChatViewModels.BaseMsgCellVM.TYPE_WELCOME_TIPS;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.BLUE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.extension.BitmapExtension.getBitmapSize;
import static com.goldmf.GMFund.extension.BitmapExtension.newScreenScaleFactor;
import static com.goldmf.GMFund.extension.ObjectExtension.safeCall;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.hideKeyboardFromWindow;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.UIControllerExtension.showKeyboardFromWindow;
import static com.goldmf.GMFund.extension.UIControllerExtension.showToast;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_isVisible;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setGone;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageUri;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisible;
import static com.goldmf.GMFund.util.FormatUtil.formatTimeByNow;
import static java.lang.Math.max;

/**
 * Created by yale on 15/11/20.
 */
public class ChatFragments {
    public static PublishSubject<Void> sReceivedNewMessageSubject = PublishSubject.create();

    public static class ConversationDetailFragment extends BaseFragment implements ChatMessageEventHandler.Delegate {


        private static class VCStateData implements VCStateDataProtocol<ConversationDetailFragment> {

            String photoPath;

            public VCStateData() {

            }

            @Override
            public VCStateData init(ConversationDetailFragment fragment) {
                this.photoPath = fragment.mCurrentPhotoPath;
                return this;
            }

            @Override
            public void restore(ConversationDetailFragment fragment) {
                safeCall(() -> fragment.mCurrentPhotoPath = photoPath);
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(this.photoPath);
            }

            protected VCStateData(Parcel in) {
                photoPath = in.readString();
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


        private static final int REQUEST_CODE_IMAGE_FROM_CAMERA = 283;
        private static final int REQUEST_CODE_IMAGE_FROM_GALLERY = 284;

        private int mMessageType;
        private String mLinkId;
        private String mSessionId;
        private String mCurrentPhotoPath;
        private EditText mContentField;
        private RecyclerView mRecyclerView;
        private MessageManager mManagerOrNil;
        private ConversationDetailAdapter mAdapter;
        private ChatMessageEventHandler mMessageEventHandler = new ChatMessageEventHandler();
        private boolean hasEnableDevModeAbility = false;

        public ConversationDetailFragment init(int messageType, String linkId, String sessionId) {
            Bundle arguments = new Bundle();
            arguments.putInt(KEY_MESSAGE_TYPE_INT, messageType);
            arguments.putString(KEY_LINK_ID_STRING, linkId);
            arguments.putString(KEY_SESSION_ID_STRING, sessionId);
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
            mMessageType = getArguments().getInt(KEY_MESSAGE_TYPE_INT);
            mLinkId = getArguments().getString(KEY_LINK_ID_STRING, "");
            mSessionId = getArguments().getString(KEY_SESSION_ID_STRING, "");
            return inflater.inflate(R.layout.frag_conversation_detail, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));
            updateTitle("");

            if (savedInstanceState != null) {
                VCStateData data = savedInstanceState.getParcelable("vc_data");
                if (data != null) {
                    data.restore(this);
                    mMessageEventHandler.onEnterConversation(mMessageType, mLinkId, mSessionId);
                }
            }

            // bind child views
            mContentField = v_findView(this, R.id.field_content);
            mRecyclerView = v_findView(this, R.id.recyclerView);

            // init child views
            mRecyclerView.setItemAnimator(null);
            v_setGone(this, R.id.btn_send_to_all);
            v_setGone(this, R.id.section_input);
            v_setClick(this, R.id.btn_send, this::performSendMessage);
            v_setClick(this, R.id.btn_camera, this::showPickPhotoBottomSheet);
            v_setClick(this, R.id.btn_send_to_all, this::showSendToAllBottomSheet);

            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            Func2<View, Integer, Boolean> itemClickHandler = (child, position) -> {
                ChatViewModels.BaseMsgCellVM item = mAdapter.getItems().get(position);
                if (item.type == TYPE_PLAIN_IMAGE_LEFT || item.type == TYPE_PLAIN_IMAGE_RIGHT) {
                    if (item.state == STATE_OK) {
                        PlainImageMsgCellVM cast = (PlainImageMsgCellVM) item;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            String transactionName = UUID.randomUUID().toString();
                            ((FragmentStackActivity) getActivity()).pushFragmentWithShareElement(new ViewPictureFragment().initWithRemoteImage(cast.imageUri, cast.imageSize.width(), cast.imageSize.height(), transactionName), child.findViewById(R.id.img_cover), transactionName);
                        } else {
                            pushFragment(this, new ViewPictureFragment().initWithRemoteImage(cast.imageUri, cast.imageSize.width(), cast.imageSize.height(), ""));
                        }
                        return true;
                    } else if (item.state == STATE_FAIL) {
                        showResendBottomSheet(item);
                        return true;
                    }
                } else if (item.type == TYPE_PLAIN_TEXT_RIGHT) {
                    if (item.state == STATE_FAIL) {
                        showResendBottomSheet(item);
                        return true;
                    }
                } else if (item.type == TYPE_IMAGE_NOTIFICATION) {
                    SystemImageMsgCellVM cast = (SystemImageMsgCellVM) item;
                    CMDParser.parse(Optional.of(cast.linkOrNil).let(link -> link.url).or("")).call(getActivity());
                    UmengUtil.stat_open_detail_event(getActivity());
                }
                return false;
            };

            Action2<View, Integer> itemLongClickHandler = (child, position) -> {
                ChatViewModels.BaseMsgCellVM item = mAdapter.getItems().get(position);
                showCopyBottomSheet(item);
            };
            mRecyclerView.addOnItemTouchListener(new SimpleOnItemTouchListener(mRecyclerView, itemClickHandler, itemLongClickHandler));
            mAdapter = new ConversationDetailAdapter(new LinkedList<>());
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);

            consumeEvent(ChatService.getSessionManager(mMessageType, mLinkId, mSessionId))
                    .onNextFinish(manager -> {
                        mManagerOrNil = manager;
                        MessageSession session = Optional.of(manager).let(MessageManager::getSession).orNull();

                        String conversationTitle = "会话详情页";
                        boolean isShowSendGroupMessageBtn = false;
                        boolean isShowBottomBar = false;
                        Optional<String> draftOrNil = Optional.empty();

                        if (session != null) {
                            hasEnableDevModeAbility = session.linkID.equalsIgnoreCase("10000") && session.messageType == 2;
                            conversationTitle = session.title;
                            isShowSendGroupMessageBtn = session.isOwner;
                            isShowBottomBar = session.enablePost;
                            draftOrNil = Optional.of(session)
                                    .let(MessageSession::subText)
                                    .reserveIf(text -> text.type == MessageSession.SessionText.Local)
                                    .let(text -> text.str);
                        }
                        updateTitle(conversationTitle);

                        draftOrNil.apply(draft -> {
                            mContentField.setText(draft);
                            mContentField.setSelection(mContentField.length(), mContentField.length());
                        });

                        if (isShowSendGroupMessageBtn)
                            v_setVisible(this, R.id.btn_send_to_all);
                        if (isShowBottomBar)
                            v_setVisible(this, R.id.section_input);

                        mMessageEventHandler.setDelegate(this, true);
                    })
                    .done();

            consumeEvent(Observable.
                    create(sub -> mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                            super.onScrollStateChanged(recyclerView, newState);
                            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                                if (!sub.isUnsubscribed()) {
                                    sub.onNext(null);
                                } else {
                                    mRecyclerView.removeOnScrollListener(this);
                                }
                            }
                        }

                        @Override
                        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);
                            if (!sub.isUnsubscribed()) {
                                sub.onNext(null);
                            } else {
                                mRecyclerView.removeOnScrollListener(this);
                            }
                        }
                    }))
                    .debounce(250, TimeUnit.MILLISECONDS))
                    .onNextFinish(nil -> {
                        View child = mRecyclerView.findChildViewUnder(0, 0);
                        if (child != null) {
                            int childPosition = mRecyclerView.getChildAdapterPosition(child);
                            if (childPosition == 0)
                                mMessageEventHandler.onRequestOlderMessages();
                        }
                    })
                    .done();

            UmengUtil.stat_enter_conversation_item(getActivity(), Optional.of(this));
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            ChatCacheManager.shareManager().cleanOutOfLimitCache();
        }

        @Override
        protected boolean onInterceptGoBack() {
            if (mManagerOrNil != null && v_isVisible(getView(), R.id.section_input)) {
                MessageSession session = mManagerOrNil.getSession();
                if (session != null) {
                    String draft = mContentField.getText().toString();
                    if (!TextUtils.isEmpty(draft)) {
                        mManagerOrNil.saveLocalMessage(new SendMessage(draft));
                    } else {
                        mManagerOrNil.saveLocalMessage(null);
                    }
                }
            }
            ChatCacheManager.shareManager().cleanOutOfLimitCache();
            mMessageEventHandler.onExitConversation();
            return super.onInterceptGoBack();
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (getView() != null) {
                if (isVisibleToUser) {
                    mMessageEventHandler.onEnterConversation(mMessageType, mLinkId, mSessionId);
                } else {
                    mMessageEventHandler.onExitConversation();
                }
            }
        }

        @Override
        public ConversationDetailAdapter getAdapter() {
            return mAdapter;
        }

        @Override
        public RecyclerView getRecyclerView() {
            return mRecyclerView;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_CODE_IMAGE_FROM_CAMERA) {
                // on finish image capture
                if (resultCode == Activity.RESULT_OK) {
                    // on success capture image
                    String imagePath = mCurrentPhotoPath;
                    if (TextUtils.isEmpty(imagePath))
                        return;
                    Rect bitmapSize = getBitmapSize(imagePath, true);
                    if (bitmapSize.width() > 0 && bitmapSize.height() > 0) {
                        runOnUIThreadDelayed(() -> {
                            if (mMessageEventHandler.mCurrentMessageManagerOrNil != null)
                                mMessageEventHandler.onSendPlainImageMessage(Uri.fromFile(new File(imagePath)), bitmapSize.width(), bitmapSize.height());
                        },1000);
                    }
                }
            } else if (requestCode == REQUEST_CODE_IMAGE_FROM_GALLERY) {
                if (resultCode == Activity.RESULT_OK) {
                    Uri imageUri = data.getData();
                    String[] columns = new String[]{MediaStore.Images.Media.DATA};
                    Cursor cursor = getActivity().getContentResolver().query(imageUri, columns, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int columnIdx = cursor.getColumnIndex(columns[0]);
                        if (columnIdx >= 0) {
                            String imagePath = cursor.getString(columnIdx);
                            Rect bitmapSize = getBitmapSize(imagePath, true);
                            if (bitmapSize.width() > 0 && bitmapSize.height() > 0) {
                                mMessageEventHandler.onSendPlainImageMessage(Uri.fromFile(new File(imagePath)), bitmapSize.width(), bitmapSize.height());
                            }
                        }
                        cursor.close();
                    }
                }
            }
        }

        private void showSendToAllBottomSheet() {

            boolean isShowTopicItem = false;
            GMFBottomSheet sheet;
            MessageSession session = Optional.of(mManagerOrNil).let(MessageManager::getSession).orNull();
            if (session != null) {
                isShowTopicItem = session.enableTopic;
            }

            if (isShowTopicItem) {
                sheet = new GMFBottomSheet.Builder(getActivity())
                        .setTitle("如需发送其他格式消息，请在消息中联系客服")
                        .addContentItem(new GMFBottomSheet.BottomSheetItem(0, "发文字", R.mipmap.ic_bottomsheet_text))
                        .addContentItem(new GMFBottomSheet.BottomSheetItem(1, "拍照", R.mipmap.ic_bottomsheet_camera))
                        .addContentItem(new GMFBottomSheet.BottomSheetItem(2, "从手机相册选择图片", R.mipmap.ic_bottomsheet_gallery))
                        .addContentItem(new GMFBottomSheet.BottomSheetItem(3, "图文话题", R.mipmap.ic_bottomsheet_topic))
                        .create();

            } else {
                sheet = new GMFBottomSheet.Builder(getActivity())
                        .setTitle("如需发送其他格式消息，请在消息中联系客服")
                        .addContentItem(new GMFBottomSheet.BottomSheetItem(0, "发文字", R.mipmap.ic_bottomsheet_text))
                        .addContentItem(new GMFBottomSheet.BottomSheetItem(1, "拍照", R.mipmap.ic_bottomsheet_camera))
                        .addContentItem(new GMFBottomSheet.BottomSheetItem(2, "从手机相册选择图片", R.mipmap.ic_bottomsheet_gallery))
                        .create();
            }

            sheet.setOnItemClickListener((bottomSheet, item) -> {
                bottomSheet.dismiss();
                int tag = (int) item.tag;
                if (tag == 0) {
                    if (session != null) {
                        pushFragment(this, new SendGroupMessageFragment().init(mMessageType, mLinkId, mSessionId, "组合【" + session.title + "】"));
                        UmengUtil.stat_send_to_all_text_message(getActivity());
                    }
                } else if (tag == 1) {
                    performTakePhoto();
                    UmengUtil.stat_send_to_all_image_message(getActivity());
                } else if (tag == 2) {
                    performPickPhotoFromGallery();
                    UmengUtil.stat_send_to_all_image_message(getActivity());
                } else if (tag == 3) {
                    if (session != null) {
                        pushFragment(this, new SendTopicFragment().init(mMessageType, mLinkId, mSessionId, "组合【" + session.title + "】"));
                    }
                }
            });
            sheet.show();
        }

        private void showCopyBottomSheet(ChatViewModels.BaseMsgCellVM message) {
            int type = message.type;
            if (type == TYPE_PLAIN_TEXT_LEFT || type == TYPE_PLAIN_TEXT_RIGHT) {
                PlainTextMsgCellVM textMessage = (PlainTextMsgCellVM) message;
                GMFBottomSheet sheet = new GMFBottomSheet.Builder(getActivity())
                        .setTitle(textMessage.raw.content)
                        .addContentItem(new GMFBottomSheet.BottomSheetItem(0, "复制", R.mipmap.ic_bottomsheet_copy))
                        .create();
                sheet.setOnItemClickListener((bottomSheet, item) -> {
                    bottomSheet.dismiss();
                    ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(ClipData.newPlainText(null, textMessage.raw.content));
                });
                sheet.show();
            }
        }

        private void showResendBottomSheet(ChatViewModels.BaseMsgCellVM message) {
            GMFBottomSheet sheet = new GMFBottomSheet.Builder(getActivity())
                    .setTitle("提示")
                    .addContentItem(new GMFBottomSheet.BottomSheetItem(0, "重新发送", R.mipmap.ic_bottomsheet_resend))
                    .addContentItem(new GMFBottomSheet.BottomSheetItem(1, "放弃发送", R.mipmap.ic_bottomsheet_delete))
                    .create();
            sheet.setOnItemClickListener((bottomSheet, item) -> {
                bottomSheet.dismiss();
                int tag = (int) item.tag;
                if (tag == 0) {
                    if (message.type == TYPE_PLAIN_TEXT_RIGHT) {
                        mMessageEventHandler.onResendPlainTextMessage((PlainTextMsgCellVM) message);
                    } else if (message.type == TYPE_PLAIN_IMAGE_RIGHT) {
                        mMessageEventHandler.onResendPlainImageMessage((PlainImageMsgCellVM) message);
                    }
                } else if (tag == 1) {
                    mMessageEventHandler.onDeleteMessage(message);
                }
            });
            sheet.show();
        }


        private void showPickPhotoBottomSheet() {

            GMFBottomSheet.Builder builder = new GMFBottomSheet.Builder(getActivity());
            builder.setTitle("上传图片");
            builder.addContentItem(new GMFBottomSheet.BottomSheetItem("camera", "拍照", R.mipmap.ic_bottomsheet_camera));
            builder.addContentItem(new GMFBottomSheet.BottomSheetItem("gallery", "从手机相册选取图片", R.mipmap.ic_bottomsheet_gallery));
            GMFBottomSheet sheet = builder.create();

            sheet.setOnItemClickListener((bottomSheet, item) -> {
                bottomSheet.dismiss();
                if (item.tag != null) {
                    String id = item.tag.toString();
                    if (id.equals("camera")) {
                        performTakePhoto();
                        UmengUtil.stat_send_image_message(getActivity());
                    } else if (id.equals("gallery")) {
                        performPickPhotoFromGallery();
                        UmengUtil.stat_send_image_message(getActivity());
                    }
                }
            });
            sheet.show();
        }

        private void performTakePhoto() {
            safeCall(() -> {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    mCurrentPhotoPath = ChatCacheManager.shareManager().getNewCacheImagePath();
                    File photoFile = new File(mCurrentPhotoPath);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    startActivityForResult(takePictureIntent, REQUEST_CODE_IMAGE_FROM_CAMERA);
                }
            });
        }

        private void performPickPhotoFromGallery() {
            safeCall(() -> {
                Intent takePictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_CODE_IMAGE_FROM_GALLERY);
                }
            });
        }

        void performSendMessage() {
            hideKeyboardFromWindow(getActivity());
            String content = mContentField.getText().toString();
            if (!TextUtils.isEmpty(content)) {
                mContentField.setText("");

                if (hasEnableDevModeAbility && content.equalsIgnoreCase("GoldMoreFund520")) {
                    showToast(this, "Hello World!");
                    MyConfig.setDevModeEnable(true);
                    goBack(this);
                    return;
                }

                mMessageEventHandler.onSendPlainTextMessage(content);
                UmengUtil.stat_send_text_message(getActivity());
            }
        }
    }

    static class ConversationDetailAdapter extends RecyclerView.Adapter<ConversationDetailViewHolder> {
        private List<ChatViewModels.BaseMsgCellVM> mItems;

        public ConversationDetailAdapter(List<ChatViewModels.BaseMsgCellVM> items) {
            mItems = items;
        }

        @Override
        public ConversationDetailViewHolder onCreateViewHolder(ViewGroup parent, int type) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View itemView;
            if (type == TYPE_PLAIN_TEXT_LEFT)
                itemView = inflater.inflate(R.layout.cell_chat_plain_text_left, parent, false);
            else if (type == TYPE_PLAIN_TEXT_RIGHT)
                itemView = inflater.inflate(R.layout.cell_chat_plain_text_right, parent, false);
            else if (type == TYPE_PLAIN_IMAGE_LEFT)
                itemView = inflater.inflate(R.layout.cell_chat_plain_image_left, parent, false);
            else if (type == TYPE_PLAIN_IMAGE_RIGHT)
                itemView = inflater.inflate(R.layout.cell_chat_plain_image_right, parent, false);
            else if (type == TYPE_IMAGE_NOTIFICATION)
                itemView = inflater.inflate(R.layout.cell_chat_image_notification, parent, false);
            else if (type == TYPE_WELCOME_TIPS)
                itemView = inflater.inflate(R.layout.cell_chat_welcome_tips, parent, false);
            else
                itemView = new View(parent.getContext());
            return new ConversationDetailViewHolder(itemView, type);
        }

        public List<ChatViewModels.BaseMsgCellVM> getItems() {
            return mItems;
        }

        public void resetItems(List<ChatViewModels.BaseMsgCellVM> items) {
            mItems = items;
            notifyDataSetChanged();
        }

        public void addItem(int position, ChatViewModels.BaseMsgCellVM item) {
            if (position >= 0 && position <= mItems.size()) {
                mItems.add(position, item);
                notifyItemInserted(position);
                notifyItemRangeChanged(max(0, position - 1), 3);
            }
        }

        public void removeItem(int position) {
            if (position >= 0 && position < mItems.size()) {
                mItems.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(max(0, position - 1), 2);
            }
        }

        @Override
        public void onBindViewHolder(ConversationDetailViewHolder holder, int position) {
            holder.configureCell(mItems.get(position));
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        @Override
        public int getItemViewType(int position) {
            return mItems.get(position).type;
        }
    }

    private static class ConversationDetailViewHolder extends RecyclerView.ViewHolder {
        // url pattern (https?://)?[-\w;/?:@&=+$|\_.!~*|'()\[\]%#,☺]+[\w/#](\(\))?
        private int mType;
        private SimpleDraweeView mAvatarImage;
        private SimpleDraweeView mCoverImage;
        private View mCoverWrapper;
        private TextView mTitleLabel;
        private TextView mContentLabel;
        private View mLinkSection;
        private TextView mLinkLabel;
        private TextView mTimeLabel;
        private TextView mProgressLabel;
        private UploadLayer mUploadLayer;
        private View mSendingLayer;
        private View mReloadImage;

        public ConversationDetailViewHolder(View itemView, int type) {
            super(itemView);
            mType = type;
            if (type == TYPE_PLAIN_TEXT_LEFT || type == TYPE_PLAIN_TEXT_RIGHT) {
                mAvatarImage = v_findView(itemView, R.id.img_avatar);
                mContentLabel = v_findView(itemView, R.id.label_content);
                mContentLabel.setMovementMethod(new LinkMovementMethod());
                mTimeLabel = v_findView(itemView, R.id.label_time);
                if (type == TYPE_PLAIN_TEXT_RIGHT) {
                    mSendingLayer = v_findView(itemView, R.id.layer_sending);
                    mReloadImage = v_findView(itemView, R.id.img_reload);
                }
                mContentLabel.setLinkTextColor(BLUE_COLOR);
            } else if (type == TYPE_PLAIN_IMAGE_LEFT || type == TYPE_PLAIN_IMAGE_RIGHT) {
                mAvatarImage = v_findView(itemView, R.id.img_avatar);
                mCoverImage = v_findView(itemView, R.id.img_cover);
                mTimeLabel = v_findView(itemView, R.id.label_time);
                mCoverWrapper = v_findView(itemView, R.id.cover_wrapper);
                if (type == TYPE_PLAIN_IMAGE_RIGHT) {
                    mProgressLabel = v_findView(itemView, R.id.label_progress);
                    mUploadLayer = v_findView(itemView, R.id.layer_upload);
                    mReloadImage = v_findView(itemView, R.id.img_reload);
                }
            } else if (type == TYPE_IMAGE_NOTIFICATION) {
                mTitleLabel = v_findView(itemView, R.id.label_title);
                mCoverImage = v_findView(itemView, R.id.img_cover);
                mContentLabel = v_findView(itemView, R.id.label_content);
                mLinkSection = v_findView(itemView, R.id.section_link);
                mLinkLabel = v_findView(itemView, R.id.label_link);
                mTimeLabel = v_findView(itemView, R.id.label_time);
            } else if (type == TYPE_WELCOME_TIPS) {
                itemView.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(0x1A000000, dp2px(4))));
                mContentLabel = v_findView(itemView, R.id.label_content);
            }
        }

        public void configureCell(ChatViewModels.BaseMsgCellVM vm) {
            if (mType == TYPE_PLAIN_TEXT_LEFT || mType == TYPE_PLAIN_TEXT_RIGHT) {
                configureCellImpl((PlainTextMsgCellVM) vm);
            } else if (mType == TYPE_PLAIN_IMAGE_LEFT || mType == TYPE_PLAIN_IMAGE_RIGHT) {
                configureCellImpl((PlainImageMsgCellVM) vm);
            } else if (mType == TYPE_IMAGE_NOTIFICATION) {
                configureCellImpl((SystemImageMsgCellVM) vm);
            } else if (mType == TYPE_WELCOME_TIPS) {
                configureCellImpl((WelcomeTipsMsgCellVM) vm);
            }
            if (mTimeLabel != null) {
                if (vm.showTime) {
                    mTimeLabel.setText("- " + formatTimeByNow(vm.raw.createTime) + " -");
                    v_setVisible(mTimeLabel);
                } else {
                    v_setGone(mTimeLabel);
                }
            }
        }

        private void configureCellImpl(WelcomeTipsMsgCellVM vm) {
            v_setText(mContentLabel, vm.content);
        }

        private void configureCellImpl(PlainTextMsgCellVM item) {
            v_setImageUri(mAvatarImage, item.senderAvatarURL);
            mContentLabel.setText(item.parseMessage((Activity) itemView.getContext()));

            if (item.type == TYPE_PLAIN_TEXT_RIGHT) {
                if (item.state == PlainImageMsgCellVM.STATE_OK) {
                    v_setGone(mSendingLayer);
                    v_setGone(mReloadImage);
                } else if (item.state == PlainImageMsgCellVM.STATE_SENDING) {
                    v_setVisible(mSendingLayer);
                    v_setGone(mReloadImage);
                } else if (item.state == PlainImageMsgCellVM.STATE_FAIL) {
                    v_setGone(mSendingLayer);
                    v_setVisible(mReloadImage);
                }
            }
        }

        private void configureCellImpl(PlainImageMsgCellVM item) {
            v_setImageUri(mAvatarImage, item.senderAvatarURL);
            v_setImageUri(mCoverImage, item.imageUri);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mCoverWrapper.getLayoutParams();
            float ratio = (float) item.imageSize.width() / item.imageSize.height();
            final int maxSize = dp2px(120);
            if (ratio >= 2) {
                params.width = maxSize;
                params.height = dp2px(60);
            } else if (ratio > 1) {
                params.width = maxSize;
                params.height = (int) (maxSize / ratio);
            } else if (ratio == 1) {
                params.width = maxSize;
                params.height = maxSize;
            } else if (ratio > 0.5) {
                params.width = (int) (maxSize * ratio);
                params.height = maxSize;
            } else {
                params.width = dp2px(60);
                params.height = dp2px(120);
            }
            mCoverWrapper.setLayoutParams(params);

            if (item.type == TYPE_PLAIN_IMAGE_RIGHT) {
                if (item.state == PlainImageMsgCellVM.STATE_SENDING) {
                    mProgressLabel.setText(item.uploadProgress + "%");
                    mUploadLayer.setProgress(item.uploadProgress);
                    mProgressLabel.setVisibility(View.VISIBLE);
                    mUploadLayer.setVisibility(View.VISIBLE);
                    mReloadImage.setVisibility(View.GONE);
                } else if (item.state == PlainImageMsgCellVM.STATE_FAIL) {
                    mProgressLabel.setVisibility(View.GONE);
                    mUploadLayer.setVisibility(View.GONE);
                    mReloadImage.setVisibility(View.VISIBLE);
                } else if (item.state == PlainImageMsgCellVM.STATE_OK) {
                    mProgressLabel.setVisibility(View.GONE);
                    mUploadLayer.setVisibility(View.GONE);
                    mReloadImage.setVisibility(View.GONE);
                }
            }


        }

        private void configureCellImpl(SystemImageMsgCellVM item) {
            if (TextUtils.isEmpty(item.coverURL)) {
                v_setGone(mCoverImage);
            } else {
                v_setImageUri(mCoverImage, item.coverURL);
                v_setVisible(mCoverImage);
            }
            mTitleLabel.setText(item.title);

            if (TextUtils.isEmpty(item.content)) {
                v_setGone(mContentLabel);
            } else {
                mContentLabel.setText(item.content);
                v_setVisible(mContentLabel);
            }

            if (item.linkOrNil != null) {
                mLinkLabel.setText(item.linkOrNil.name);
                v_setVisible(mLinkSection);
            } else {
                v_setGone(mLinkSection);
            }
        }
    }

    public static class SendGroupMessageFragment extends BaseFragment {
        private int mMessageType;
        private String mLinkId;
        private String mSessionId;
        private String mReceiverName;
        private EditText mContentField;

        public SendGroupMessageFragment init(int messageType, String linkId, String sessionId, String receiverName) {
            Bundle arguments = new Bundle();
            arguments.putInt("message_type", messageType);
            arguments.putString("link_id", linkId);
            arguments.putString("session_id", sessionId);
            arguments.putString("receiver_name", receiverName);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mMessageType = getArguments().getInt("message_type");
            mLinkId = getArguments().getString("link_id", "");
            mSessionId = getArguments().getString("session_id", "");
            mReceiverName = getArguments().getString("receiver_name", "未知");
            return inflater.inflate(R.layout.frag_send_group_msg, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this), R.drawable.ic_close_light);

            v_setClick(this, R.id.btn_send, this::showConfirmDialog);
            mContentField = v_findView(view, R.id.field_content);
            mContentField.requestFocus();

            consumeEvent(ChatService.getSessionManager(mMessageType, mLinkId, mSessionId))
                    .onNextFinish(manager -> {
                        SendMessage message = manager.getLocalMessage();
                        if (message.templateType == GMFMessage.Message_Text) {
                            mContentField.setText(message.content);
                        }
                    })
                    .done();

            runOnUIThreadDelayed(() -> showKeyboardFromWindow(this), 200L);
        }

        @Override
        protected boolean onInterceptGoBack() {
            String draft = mContentField.getText().toString();
            updateDraft(mMessageType, mLinkId, mSessionId, draft);

            return super.onInterceptGoBack();
        }

        private void showConfirmDialog() {
            String content = mContentField.getText().toString().trim();
            if (TextUtils.isEmpty(content)) {
                return;
            }

            GMFDialog confirmDialog = new GMFDialog.Builder(getActivity())
                    .setMessage("确定要发送这段文字到" + mReceiverName + "？")
                    .setPositiveButton("确认发送", (dialog, i) -> {
                        dialog.dismiss();
                        mContentField.setText("");
                        sendMessage(mMessageType, mLinkId, mSessionId, content);
                        goBack(this);
                    })
                    .setNegativeButton("取消")
                    .create();
            confirmDialog.show();
        }

        private static void sendMessage(int messageType, String linkId, String sessionId, String content) {
            ChatService.getSessionManager(messageType, linkId, sessionId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(manager -> {
                        if (manager != null) {
                            manager.sendMessage(new SendMessage(content));
                        }
                    });
        }

        private static void updateDraft(int messageType, String linkId, String sessionId, String draft) {
            ChatService.getSessionManager(messageType, linkId, sessionId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(managerOrNil -> Optional.of(managerOrNil).apply(manager -> manager.saveLocalMessage(TextUtils.isEmpty(draft) ? null : new SendMessage(draft))));
        }
    }

    public static class SendTopicFragment extends BaseFragment {

        private static final int REQUEST_CODE_IMAGE_FROM_CAMERA = 283;
        private static final int REQUEST_CODE_IMAGE_FROM_GALLERY = 284;
        private static final int TYPE_IMAGE_FROM_COVER = 285;
        private static final int TYPE_IMAGE_FROM_INSERT = 286;
        private static final int TYPE_REPLACE_INSERT_IMAGE = 287;
        private int mImageType = 0;

        private int mMessageType;
        private String mLinkId;
        private String mSessionId;
        private String mReceiverName;
        private SimpleDraweeView mCoverImage;
        private EditText mTitleLabel;
        private EditText mContentLabel;
        private ImageView mDeleteCoverImage;
        private ImageView mUpdatePhotoImage;
        private LinearLayout mContainer;
        private Bitmap mUploadBitmap;
        private String mCurrentPhotoPath;
        private String mCoverPhotoPath;
        private String mUpdateBeforePhotoPath;
        private List<String> imageFileList = new ArrayList<>();
        private List<String> contentList = new ArrayList<>();
        private MessageManager mManagerOrNil;
        private EmbedProgressView mProgressView;
        private LinearLayout mContentSection;

        public SendTopicFragment init(int messageType, String linkId, String sessionId, String receiverName) {
            Bundle arguments = new Bundle();
            arguments.putInt("message_type", messageType);
            arguments.putString("link_id", linkId);
            arguments.putString("session_id", sessionId);
            arguments.putString("receiver_name", receiverName);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mMessageType = getArguments().getInt("message_type");
            mLinkId = getArguments().getString("link_id", "");
            mSessionId = getArguments().getString("session_id", "");
            mReceiverName = getArguments().getString("receiver_name", "未知");
            return inflater.inflate(R.layout.frag_send_topic, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this), R.drawable.ic_close_light);

            mContentSection = v_findView(this, R.id.section_content);
            mCoverImage = v_findView(this, R.id.img_cover);
            mCoverImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mTitleLabel = v_findView(this, R.id.label_title);
            mContentLabel = v_findView(this, R.id.label_content);
            mContainer = v_findView(this, R.id.container);
            mDeleteCoverImage = v_findView(this, R.id.img_delete_cover);
            mProgressView = v_findView(this, R.id.section_sending);
            v_setClick(mCoverImage, v -> showPickPhotoBottomSheet(TYPE_IMAGE_FROM_COVER));
            v_setClick(mDeleteCoverImage, v -> showDeletePhotoBottomSheet(v, TYPE_IMAGE_FROM_COVER, null));
            v_setClick(this, R.id.cell_insert_picture, v -> showPickPhotoBottomSheet(TYPE_IMAGE_FROM_INSERT));
            v_setClick(this, R.id.btn_send, this::showConfirmDialog);
            mTitleLabel.requestFocus();

            consumeEvent(ChatService.getSessionManager(mMessageType, mLinkId, mSessionId))
                    .onNextFinish(manager -> mManagerOrNil = manager)
                    .done();

            v_setGone(mProgressView);
            v_setGone(mDeleteCoverImage);
            v_setVisible(mContentSection);
        }

        @Override
        protected boolean onInterceptGoBack() {
            String title = mTitleLabel.getText().toString().trim();

            if (title.length() > 0) {
                String content = mContentLabel.getText().toString().trim();

                String imagePath = mCoverPhotoPath;
                Rect bitmapSize = getBitmapSize(imagePath, true);
                UpImageMessage message = ChatService.buildTopicMessage(imagePath, bitmapSize.width(), bitmapSize.height(), content, title, imageFileList, contentList);
                ChatService.saveLocalMessage(mManagerOrNil, message);
            } else {
                ChatService.saveLocalMessage(mManagerOrNil, null);
            }
            return super.onInterceptGoBack();
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_CODE_IMAGE_FROM_CAMERA) {
                // on finish image capture
                if (resultCode == Activity.RESULT_OK) {
                    // on success capture image
                    mUploadBitmap = BitmapExtension.decodeBitmap(mCurrentPhotoPath, newScreenScaleFactor(getActivity()));
                    if (mImageType == TYPE_IMAGE_FROM_COVER) {
                        mCoverImage.setImageBitmap(mUploadBitmap);
                        v_setVisible(mDeleteCoverImage);
                        mCoverPhotoPath = mCurrentPhotoPath;
                        Rect bitmapSize = getBitmapSize(mCurrentPhotoPath, true);
                        v_setClick(mCoverImage, v -> pushFragment(this, new ViewPictureFragment().initWithRemoteImage(Uri.fromFile(new File(mCurrentPhotoPath)), bitmapSize.width(), bitmapSize.height(), "")));
                    } else if (mImageType == TYPE_IMAGE_FROM_INSERT) {
                        View cell = createCell(getActivity(), mContainer, mUploadBitmap, mCurrentPhotoPath);
                        mContainer.addView(cell);
                        imageFileList.add(mCurrentPhotoPath);
                    } else if (mImageType == TYPE_REPLACE_INSERT_IMAGE) {
                        int index = imageFileList.indexOf(mUpdateBeforePhotoPath);
                        imageFileList.remove(mUpdateBeforePhotoPath);
                        mUpdatePhotoImage.setImageBitmap(mUploadBitmap);
                        imageFileList.add(index, mCurrentPhotoPath);
                    }
                    mUpdateBeforePhotoPath = mCurrentPhotoPath;
                }
            } else if (requestCode == REQUEST_CODE_IMAGE_FROM_GALLERY) {
                if (resultCode == Activity.RESULT_OK) {
                    Uri imageUri = data.getData();
                    String[] columns = new String[]{MediaStore.Images.Media.DATA};
                    Cursor cursor = getActivity().getContentResolver().query(imageUri, columns, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int columnIdx = cursor.getColumnIndex(columns[0]);
                        if (columnIdx >= 0) {
                            String imagePath = cursor.getString(columnIdx);
                            mUploadBitmap = BitmapExtension.decodeBitmap(imagePath, newScreenScaleFactor(getActivity()));
                            if (mImageType == TYPE_IMAGE_FROM_COVER) {
                                mCoverImage.setImageBitmap(mUploadBitmap);
                                v_setVisible(mDeleteCoverImage);
                                mCoverPhotoPath = imagePath;
                                Rect bitmapSize = getBitmapSize(imagePath, true);
                                v_setClick(mCoverImage, v -> pushFragment(this, new ViewPictureFragment().initWithRemoteImage(Uri.fromFile(new File(imagePath)), bitmapSize.width(), bitmapSize.height(), "")));
                            } else if (mImageType == TYPE_IMAGE_FROM_INSERT) {
                                View cell = createCell(getActivity(), mContainer, mUploadBitmap, imagePath);
                                mContainer.addView(cell);
                                imageFileList.add(imagePath);
                            } else if (mImageType == TYPE_REPLACE_INSERT_IMAGE) {
                                int index = imageFileList.indexOf(mUpdateBeforePhotoPath);
                                imageFileList.remove(mUpdateBeforePhotoPath);
                                mUpdatePhotoImage.setImageBitmap(mUploadBitmap);
                                imageFileList.add(index, imagePath);
                            }
                            mUpdateBeforePhotoPath = imagePath;
                        }
                        cursor.close();
                    }
                }
            }
        }

        private View createCell(Context ctx, ViewGroup parent, Bitmap bitmap, String imagePath) {
            View cell = LayoutInflater.from(ctx).inflate(R.layout.cell_photo_and_content, parent, false);
            ImageView insertImage = v_findView(cell, R.id.img_insert);
            Rect bitmapSize = getBitmapSize(imagePath, true);
            insertImage.setAdjustViewBounds(true);
            insertImage.setMaxHeight(2 * bitmapSize.width());
            insertImage.setImageBitmap(bitmap);
            v_setClick(insertImage, v -> pushFragment(this, new ViewPictureFragment().initWithRemoteImage(Uri.fromFile(new File(imagePath)), bitmapSize.width(), bitmapSize.height(), "")));
            v_setClick(cell, R.id.img_delete_insert, v -> showDeletePhotoBottomSheet(v, TYPE_REPLACE_INSERT_IMAGE, imagePath));
            v_setVisible(cell, R.id.img_delete_insert);
            return cell;
        }

        private void showDeletePhotoBottomSheet(View view, int type, String imagePath) {

            GMFBottomSheet.Builder builder = new GMFBottomSheet.Builder(getActivity());
            builder.addContentItem(new GMFBottomSheet.BottomSheetItem("delete", "删除图片", R.mipmap.ic_bottomsheet_delete));
            builder.addContentItem(new GMFBottomSheet.BottomSheetItem("camera", "拍照", R.mipmap.ic_bottomsheet_camera));
            builder.addContentItem(new GMFBottomSheet.BottomSheetItem("gallery", "从手机相册选取图片", R.mipmap.ic_bottomsheet_gallery));
            GMFBottomSheet sheet = builder.create();

            sheet.setOnItemClickListener((bottomSheet, item) -> {
                bottomSheet.dismiss();
                if (item.tag != null) {
                    String id = item.tag.toString();
                    if (id.equals("delete")) {
                        if (type == TYPE_IMAGE_FROM_COVER) {
                            performDeleteCover();
                        } else if (type == TYPE_REPLACE_INSERT_IMAGE) {
                            performDeletePhoto(view, imagePath);
                        }
                    } else if (id.equals("camera")) {
                        if (type == TYPE_REPLACE_INSERT_IMAGE) {
                            updatePhoto(view);
                        }
                        performTakePhoto(type);
                        UmengUtil.stat_send_image_message(getActivity());
                    } else if (id.equals("gallery")) {
                        if (type == TYPE_REPLACE_INSERT_IMAGE) {
                            updatePhoto(view);
                        }
                        performPickPhotoFromGallery(type);
                        UmengUtil.stat_send_image_message(getActivity());
                    }
                }
            });
            sheet.show();
        }

        private void updatePhoto(View view) {
            for (int i = 0; i < ((RelativeLayout) view.getParent()).getChildCount(); i++) {
                if (((RelativeLayout) view.getParent()).getChildAt(i) instanceof ImageView && ((RelativeLayout) view.getParent()).getChildAt(i) != view) {
                    mUpdatePhotoImage = (ImageView) ((RelativeLayout) view.getParent()).getChildAt(i);
                }
            }
        }

        private void performDeleteCover() {
            mCoverImage.setImageURI(Uri.EMPTY);
            v_setClick(mCoverImage, v -> showPickPhotoBottomSheet(TYPE_IMAGE_FROM_COVER));
            v_setGone(mDeleteCoverImage);
            mCoverPhotoPath = null;
        }

        private void performDeletePhoto(View view, String imagePath) {

            for (int i = 0; i < mContainer.getChildCount(); i++) {
                if (mContainer.getChildAt(i) == view.getParent()) {
                    mContainer.removeView(mContainer.getChildAt(i));
                    imageFileList.remove(imagePath);
                    String photoContent = "";
                    for (int j = 0; j < ((RelativeLayout) view.getParent()).getChildCount(); j++) {
                        if (((RelativeLayout) view.getParent()).getChildAt(j) instanceof EditText) {
                            EditText interceptedContent = (EditText) ((RelativeLayout) view.getParent()).getChildAt(j);
                            photoContent = interceptedContent.getText().toString();
                        }
                    }
                    if (i != 0) {
                        for (int j = 0; j < ((RelativeLayout) mContainer.getChildAt(i - 1)).getChildCount(); j++) {
                            if (((RelativeLayout) mContainer.getChildAt(i - 1)).getChildAt(j) instanceof EditText) {
                                EditText interceptedContent = (EditText) ((RelativeLayout) mContainer.getChildAt(i - 1)).getChildAt(j);
                                interceptedContent.setText(interceptedContent.getText().toString() + "\r\n" + photoContent);
                            }
                        }
                    } else {
                        mContentLabel.setText(mContentLabel.getText().toString() + "\r\n" + photoContent);
                    }
                }
            }
        }

        private void showPickPhotoBottomSheet(int type) {

            GMFBottomSheet.Builder builder = new GMFBottomSheet.Builder(getActivity());
            if (type == TYPE_IMAGE_FROM_COVER) {
                builder.setTitle("上传封面");
            }
            builder.addContentItem(new GMFBottomSheet.BottomSheetItem("camera", "拍照", R.mipmap.ic_bottomsheet_camera));
            builder.addContentItem(new GMFBottomSheet.BottomSheetItem("gallery", "从手机相册选取图片", R.mipmap.ic_bottomsheet_gallery));
            GMFBottomSheet sheet = builder.create();

            sheet.setOnItemClickListener((bottomSheet, item) -> {
                bottomSheet.dismiss();
                if (item.tag != null) {
                    String id = item.tag.toString();
                    if (id.equals("camera")) {
                        performTakePhoto(type);
                        UmengUtil.stat_send_image_message(getActivity());
                    } else if (id.equals("gallery")) {
                        performPickPhotoFromGallery(type);
                        UmengUtil.stat_send_image_message(getActivity());
                    }
                }
            });
            sheet.show();
        }

        private void performTakePhoto(int type) {
            safeCall(() -> {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    mCurrentPhotoPath = ChatCacheManager.shareManager().getNewCacheImagePath();
                    File photoFile = new File(mCurrentPhotoPath);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    startActivityForResult(takePictureIntent, REQUEST_CODE_IMAGE_FROM_CAMERA);
                    mImageType = type;
                }
            });
        }

        private void performPickPhotoFromGallery(int type) {
            safeCall(() -> {
                Intent takePictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_CODE_IMAGE_FROM_GALLERY);
                    mImageType = type;
                }
            });
        }

        private void showConfirmDialog() {
            String title = mTitleLabel.getText().toString().trim();
            String content = mContentLabel.getText().toString().trim();
            for (int i = 0; i < mContainer.getChildCount(); i++) {
                for (int j = 0; j < ((RelativeLayout) mContainer.getChildAt(i)).getChildCount(); j++) {
                    if (((RelativeLayout) mContainer.getChildAt(i)).getChildAt(j) instanceof EditText) {
                        EditText interceptContent = (EditText) ((RelativeLayout) mContainer.getChildAt(i)).getChildAt(j);
                        String photoContent = interceptContent.getText().toString();
                        contentList.add(photoContent);
                    }
                }
            }

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content) || TextUtils.isEmpty(mCoverPhotoPath)) {
                new GMFDialog.Builder(getActivity())
                        .setMessage("封面，标题及内容不能为空")
                        .setPositiveButton("确定", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .create()
                        .show();
                return;
            }

            GMFDialog confirmDialog = new GMFDialog.Builder(getActivity())
                    .setMessage("确定要发送这段图文话题到" + mReceiverName + "？")
                    .setPositiveButton("确认发送", (dialog, i) -> {
                        dialog.dismiss();
                        performSendTopicMessage(content, title);
                    })
                    .setNegativeButton("取消")
                    .create();
            confirmDialog.show();
        }

        private void performSendTopicMessage(String content, String title) {
            String imagePath = mCoverPhotoPath;
            Rect bitmapSize = getBitmapSize(imagePath, true);
            if (bitmapSize.width() > 0 && bitmapSize.height() > 0) {
                mProgressView.setMessage("正在发送");
                v_setVisible(mProgressView);
                v_setGone(mContentSection);

                UpImageMessage message = ChatService.buildTopicMessage(imagePath, bitmapSize.width(), bitmapSize.height(), content, title, imageFileList, contentList);
                ChatService.sendPlainTopicMessage(mManagerOrNil, message)
                        .doOnNext(result -> {
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> {
                            v_setGone(mProgressView);
                            v_setVisible(mContentSection);
                            if (result != null) {
                                if (result.isSuccess) {
                                    imageFileList.clear();
                                    contentList.clear();
                                    goBack(this);
                                } else {
                                    new GMFDialog.Builder(getActivity())
                                            .setMessage(result.msg)
                                            .setPositiveButton("确定", (dialog, which) -> {
                                                dialog.dismiss();
                                            })
                                            .create()
                                            .show();
                                }
                            } else {
                                new GMFDialog.Builder(getActivity())
                                        .setMessage("未知错误")
                                        .setPositiveButton("确定", (dialog, which) -> {
                                            dialog.dismiss();
                                        })
                                        .create()
                                        .show();
                            }
                        });
            }
        }
    }


}
