package com.goldmf.GMFund.controller;

import android.animation.LayoutTransition;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.facebook.drawee.view.SimpleDraweeView;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.controller.business.StockController;
import com.goldmf.GMFund.controller.business.UserController;
import com.goldmf.GMFund.manager.mine.Mine;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.model.CommonDefine.PlaceHolder;
import com.goldmf.GMFund.model.GMFRankUser;
import com.goldmf.GMFund.model.GMFRankUser.UserAction;
import com.goldmf.GMFund.model.Rank;
import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.widget.UserAvatarView;
import com.yale.ui.support.AdvanceSwipeRefreshLayout;

import java.util.Iterator;
import java.util.List;

import yale.extension.system.SimpleRecyclerViewAdapter;

import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_RANK_ID_STRING;
import static com.goldmf.GMFund.controller.FragmentStackActivity.goBack;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_UserDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.RISE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.getIncomeTextColor;
import static com.goldmf.GMFund.extension.IntExtension.anyMatch;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.addContentInset;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.addHorizontalSepLine;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.getSimpleAdapter;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concat;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concatNoBreak;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setFontSize;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.UIControllerExtension.showToast;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_setBehavior;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setGone;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageUri;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisible;
import static com.goldmf.GMFund.util.FormatUtil.formatRatio;
import static com.goldmf.GMFund.util.FormatUtil.formatSecond;

/**
 * Created by yale on 15/10/14.
 */
public class CommonUserFragments {
    private CommonUserFragments() {
    }

    public static class ChooseNameAndAvatarFragment extends SimpleFragment {
        private UmengUtil.WXLoginInfo mLoginInfo;

        public ChooseNameAndAvatarFragment init(UmengUtil.WXLoginInfo loginInfo) {
            Bundle arguments = new Bundle();
            arguments.putSerializable(UmengUtil.WXLoginInfo.class.getName(), loginInfo);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mLoginInfo = (UmengUtil.WXLoginInfo) getArguments().getSerializable(UmengUtil.WXLoginInfo.class.getName());
            return inflater.inflate(R.layout.frag_choose_name_and_avatar, container, false);
        }

        @Override
        public void onViewCreated(View rootView, Bundle savedInstanceState) {
            super.onViewCreated(rootView, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));


            if (mLoginInfo == null || !MineManager.getInstance().isLoginOK()) {
                showToast(this, "参数异常");
                goBack(this);
                return;
            }

            v_setClick(rootView, R.id.btn_save, v -> performSaveInfo());


            Mine mine = MineManager.getInstance().getmMe();
            UmengUtil.WXLoginInfo wechatInfo = mLoginInfo;
            Stream.of(R.id.cell_current_name, R.id.cell_current_avatar)
                    .map(it -> rootView.findViewById(it))
                    .forEach(it -> it.setSelected(true));

            Iterator<? extends TextView> labelStream = Stream.of(R.id.label_current_name, R.id.label_wechat_name).map(it -> (TextView) rootView.findViewById(it)).getIterator();
            Iterator<? extends String> nameStream = Stream.of(mine.getName(), wechatInfo.nickName).getIterator();
            while (labelStream.hasNext() && nameStream.hasNext()) {
                labelStream.next().setText(nameStream.next());
            }

            Iterator<? extends SimpleDraweeView> draweeViewStream = Stream.of(R.id.img_current_avatar, R.id.img_wechat_avatar).map(it -> (SimpleDraweeView) rootView.findViewById(it)).getIterator();
            Iterator<? extends String> avatarURLStream = Stream.of(mine.getPhotoUrl(), wechatInfo.avatarURL).getIterator();
            while (draweeViewStream.hasNext() && avatarURLStream.hasNext()) {
                SimpleDraweeView draweeView = draweeViewStream.next();
                String avatarURL = avatarURLStream.next();
                v_setImageUri(draweeView, avatarURL);
                draweeView.setTag(avatarURL);
            }


            Iterator<? extends View> targetViewStream = Stream.of(R.id.cell_wechat_name, R.id.cell_current_name, R.id.cell_wechat_avatar, R.id.cell_current_avatar).map(it -> rootView.findViewById(it)).getIterator();
            Iterator<? extends View> unselectedViewStream = Stream.of(R.id.cell_current_name, R.id.cell_wechat_name, R.id.cell_current_avatar, R.id.cell_wechat_avatar).map(it -> rootView.findViewById(it)).getIterator();
            while (targetViewStream.hasNext() && unselectedViewStream.hasNext()) {
                View targetView = targetViewStream.next();
                View unselectedView = unselectedViewStream.next();
                targetView.setOnClickListener(v -> {
                    targetView.setSelected(true);
                    unselectedView.setSelected(false);
                });
            }
        }

        private void performSaveInfo() {
            View parent = getView();
            if (parent != null) {

                String name = Stream.of(R.id.label_wechat_name, R.id.label_current_name).map(it -> (TextView) parent.findViewById(it)).filter(it -> it.isSelected()).findFirst().map(it -> it.getText().toString()).get();
                String avatarURL = Stream.of(R.id.img_wechat_avatar, R.id.img_current_avatar).map(it -> (SimpleDraweeView) parent.findViewById(it)).filter(it -> it.isSelected()).findFirst().map(it -> it.getTag().toString()).get();

                UserController.modifyName(name).subscribe();
                UserController.modifyAvatar(avatarURL, true).subscribe();
                NotificationCenter.userInfoChangedSubject.onNext(null);
                goBack(this);
            }
        }
    }

    public static class UserLeaderBoardFragment extends SimpleFragment {
        private String mRankID;

        private AdvanceSwipeRefreshLayout mRefreshLayout;
        private RecyclerView mRecyclerView;

        public UserLeaderBoardFragment init(String rankID) {
            Bundle arguments = new Bundle();
            arguments.putString(KEY_RANK_ID_STRING, rankID);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mRankID = getArguments().getString(CommonProxyActivity.KEY_RANK_ID_STRING, "");
            return inflater.inflate(R.layout.frag_user_leaderboard, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, RED_COLOR);
            setupBackButton(this, findToolbar(this));

            mRefreshLayout = (AdvanceSwipeRefreshLayout) mContentSection;
            mRecyclerView = v_findView(mContentSection, R.id.recyclerView);

            mRefreshLayout.setOnRefreshListener(() -> fetchData(false));
            v_setClick(mReloadSection, v -> fetchData(true));
            View appbarLayout = v_findView(mContentSection, R.id.appBarLayout);
            mRefreshLayout.setOnPreInterceptTouchEventDelegate(ev -> appbarLayout.getTop() < 0);

            {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
                addHorizontalSepLine(mRecyclerView);
                addContentInset(mRecyclerView, new Rect(0, 0, 0, dp2px(48)));
            }

            {
                AppBarLayout appBarLayout = v_findView(mContentSection, R.id.appBarLayout);
                TextView appBarTitleLabel = v_findView(appBarLayout, R.id.label_title);
                Toolbar toolbar = findToolbar(this);
                toolbar.setLayoutTransition(new LayoutTransition());
                TextView toolbarTitleLabel = v_findView(toolbar, R.id.toolbarTitle);
                v_setBehavior(appBarLayout, new AppBarLayout.Behavior() {
                    @Override
                    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, int dx, int dy, int[] consumed) {
                        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
                        updateTitle();
                    }

                    @Override
                    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target) {
                        super.onStopNestedScroll(coordinatorLayout, child, target);
                        updateTitle();
                    }

                    private void updateTitle() {
                        if (appBarLayout.getTop() < -appBarTitleLabel.getBottom()) {
                            v_setText(toolbarTitleLabel, appBarTitleLabel.getText());
                            v_setVisible(toolbarTitleLabel);
                        } else {
                            v_setGone(toolbarTitleLabel);
                        }
                    }
                });
            }

            fetchData(true);
        }

        private void fetchData(boolean reload) {
            consumeEventMRUpdateUI(StockController.fetchUserLeaderBoard(mRankID), reload)
                    .setTag("reload_data")
                    .onNextSuccess(response -> {
                        UserLeaderBoardHeaderVM intro = new UserLeaderBoardHeaderVM(response.data);
                        List<UserLeaderBoardCellVM> items = Stream.of(response.data.userList).map(it -> new UserLeaderBoardCellVM(it)).collect(Collectors.toList());
                        updateLeaderBoardHeader(intro);
                        updateUserListSection(items);
                    })
                    .done();
        }

        private void updateLeaderBoardHeader(UserLeaderBoardHeaderVM vm) {
            View header = v_findView(mContentSection, R.id.header_leaderboard);
            v_setText(header, R.id.label_title, vm.name);
            v_setText(header, R.id.label_desc, vm.desc);
            v_setText(header, R.id.label_time, vm.updateTime);

            View listHeader = v_findView(mContentSection, R.id.header_list);
            v_setText(listHeader, R.id.text3, vm.listHeaderThirdTabText);
        }

        private void updateUserListSection(List<UserLeaderBoardCellVM> items) {
            if (mRecyclerView.getAdapter() != null) {
                SimpleRecyclerViewAdapter<UserLeaderBoardCellVM> adapter = getSimpleAdapter(mRecyclerView);
                adapter.resetItems(items);
            } else {
                SimpleRecyclerViewAdapter<UserLeaderBoardCellVM> adapter = new SimpleRecyclerViewAdapter.Builder<>(items)
                        .onCreateItemView(R.layout.cell_user_leaderboard)
                        .onCreateViewHolder(builder -> {
                            builder.bindChildWithTag("avatar", R.id.img_avatar)
                                    .bindChildWithTag("rankImage", R.id.img_rank)
                                    .bindChildWithTag("user_name_and_feed", R.id.label_user_name_and_feed)
                                    .bindChildWithTag("point_and_desc", R.id.label_point_and_desc)
                                    .bindChildWithTag("position", R.id.label_position)
                                    .configureView((item, position) -> {
                                        UserAvatarView avatar = builder.getChildWithTag("avatar");
                                        avatar.updateView(item.userAvatarURL, item.userType);
                                        v_setText(builder.getChildWithTag("user_name_and_feed"), item.userNameAndFeed);
                                        v_setText(builder.getChildWithTag("point_and_desc"), item.pointAndDesc);

                                        TextView rankLabel = builder.getChildWithTag("position");
                                        ImageView rankImage = builder.getChildWithTag("rankImage");
                                        if (anyMatch(item.positionInt, 1, 2, 3)) {
                                            rankLabel.setText("");
                                            int backgroundRes = R.mipmap.ic_rank_1;
                                            if (anyMatch(item.positionInt, 2))
                                                backgroundRes = R.mipmap.ic_rank_2;
                                            if (anyMatch(item.positionInt, 3))
                                                backgroundRes = R.mipmap.ic_rank_3;
                                            rankImage.setImageResource(backgroundRes);
                                        } else {
                                            rankLabel.setText("" + item.positionInt);
                                            rankImage.setImageResource(0);
                                        }
                                    });
                            return builder.create();
                        })
                        .onViewHolderCreated((ad, holder) -> {
                            v_setClick(holder.itemView, v -> {
                                UserLeaderBoardCellVM item = ad.getItem(holder.getAdapterPosition());
                                showActivity(this, an_UserDetailPage(item.user));
                            });
                        })
                        .create();
                mRecyclerView.setAdapter(adapter);
            }
        }
    }

    private static class UserLeaderBoardHeaderVM {
        public CharSequence name;
        public CharSequence desc;
        public CharSequence updateTime;
        public CharSequence listHeaderThirdTabText;

        public UserLeaderBoardHeaderVM(Rank raw) {
            this.name = raw.title;
            this.desc = raw.context;
            this.updateTime = "最后更新：" + formatSecond(raw.updateTime, "yyyy-MM-dd HH:mm");
            this.listHeaderThirdTabText = safeGet(() -> raw.userList.get(0).point.desc, "");
            if (TextUtils.isEmpty(this.listHeaderThirdTabText)) {
                this.listHeaderThirdTabText = "收益";
            }
        }
    }

    private static class UserLeaderBoardCellVM {
        public User user;
        public int userType;
        public String userID;
        public String userName;
        public int positionInt;
        public String userAvatarURL;
        public CharSequence userNameAndFeed;
        public CharSequence pointAndDesc;

        public UserLeaderBoardCellVM(GMFRankUser raw) {
            this.user = safeGet(() -> raw.getUser(), null);
            this.userType = safeGet(() -> raw.getUser().type, User.User_Type.Custom);
            this.userID = String.valueOf(safeGet(() -> raw.getUser().index, 0));
            this.userName = safeGet(() -> raw.getUser().getName(), PlaceHolder.NULL_VALUE);
            this.positionInt = safeGet(() -> raw.position, 0);
            this.userAvatarURL = safeGet(() -> raw.getUser().getPhotoUrl(), "");
            CharSequence userName = safeGet(() -> raw.getUser().getName(), PlaceHolder.NULL_VALUE);
            CharSequence defaultFeed = setFontSize("第" + positionInt + "名", sp2px(10));
            CharSequence feed = safeGet(() -> {
                if (TextUtils.isEmpty(raw.lastAction.desc)) {
                    return defaultFeed;
                }
                return setFontSize(concatNoBreak(setColor(formatSecond(raw.lastAction.time, "yyyy/MM/dd"), TEXT_GREY_COLOR), getUserActionDesc(raw.lastAction.type), setColor(raw.lastAction.desc, TEXT_GREY_COLOR)), sp2px(10));
            })
                    .def(defaultFeed)
                    .get();
            this.userNameAndFeed = concat(userName, feed);
            this.pointAndDesc = safeGet(() -> isMultiLinePointAndDesc(raw.point) ?
                    concat(setFontSize(setColor(formatRatio(raw.point.value, false, 2), getIncomeTextColor(raw.point.value, RISE_COLOR)), sp2px(18)), setFontSize(setColor(raw.point.desc, TEXT_GREY_COLOR), sp2px(10))) :
                    setFontSize(setColor(formatRatio(raw.point.value, true, 2), getIncomeTextColor(raw.point.value, RISE_COLOR)), sp2px(20)))
                    .def(setFontSize(PlaceHolder.NULL_VALUE, sp2px(20)))
                    .get();
        }

        private static boolean isMultiLinePointAndDesc(GMFRankUser.UserPoint point) {
            return !TextUtils.isEmpty(point.desc);
        }

        private static CharSequence getUserActionDesc(int userAction) {
            if (userAction == UserAction.TYPE_BUY) {
                return setColor(" 买入 ", 0xFF3498DB);
            } else if (userAction == UserAction.TYPE_SELL) {
                return setColor(" 卖出 ", 0xFFF5A623);
            }
            return " ";
        }
    }
}
