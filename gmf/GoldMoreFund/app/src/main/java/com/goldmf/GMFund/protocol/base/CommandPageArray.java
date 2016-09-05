package com.goldmf.GMFund.protocol.base;


import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.model.RemaindFeed;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by cupide on 16/3/11.
 */
public class CommandPageArray<T extends PageArray.PageItemIndex> {

    public interface CommandPage<T extends PageArray.PageItemIndex>{

        void onNext(PageProtocol<T> protocol, PageArray<T> page);

        void onPre(PageProtocol<T> protocol, PageArray<T> page);
    }

    protected PageProtocol<T> protocol;
    protected PageArray<T> pageArray = new PageArray<>();
    protected CommandPage command = null;
    protected boolean bSupportDel = false;
    private boolean lock = false;

    private List<MResults<CommandPageArray<T>>> getPageCallbacks = new ArrayList<>();

    public PageArray<T> getPage(){
        return this.pageArray;
    }

    public List<T> data(){
        if(this.pageArray != null)
            return this.pageArray.data();
        return null;
    }
    public final boolean getMore(){
        return this.pageArray.getMore();
    }

    public CommandPageArray(){
    }

    public CommandPageArray(Builder builder){

        if(builder.protocol == null) {
            protocol = new PageProtocol<>(null);
        }else {
            protocol = builder.protocol;
        }
        protocol.classOfT = builder.pageClassOfT;
        protocol.pageCgiUrl = builder.pageCgiUrl;
        protocol.pageCgiParam = builder.pageCgiParam;
        protocol.parseMoreData  = builder.parseMoreData;
        protocol.translateItem = builder.translateItem;
        bSupportDel = builder.bSupportDel;
        command = builder.command;
        this.pageArray.setSorted(builder.bSorted);
    }

    public void build(){}


    private void safeInfoCallbackRet(MResults.MResultsInfo<CommandPageArray<T>> info){
        for (MResults<CommandPageArray<T>> result : this.getPageCallbacks){

            MResults.MResultsInfo.SafeOnResult(result, info);
        }

        this.getPageCallbacks.clear();
    }

    /**
     * 获取下一页，下一页的定义是：按照顺序先后，在后的为下一页
     * @param results
     */
    public void getNextPage(final MResults<CommandPageArray<T>> results) {
        if(this.command == null || this.protocol == null || this.pageArray == null){
            MResults.MResultsInfo.SafeOnResult(results,
                    new MResults.MResultsInfo<CommandPageArray<T>>().setData(this));
            return;
        }

        this.getPageCallbacks.add(results);
        if(this.lock){
            return;
        }
        this.lock = true;

        this.command.onNext(this.protocol, this.pageArray);

        this.protocol.mCallback = new ProtocolCallback() {
            @Override
            public void onFailure(ProtocolBase protocol, int errCode) {

                CommandPageArray.this.lock = false;
                CommandPageArray.this.safeInfoCallbackRet(protocol.buildRet());
            }

            @Override
            public void onSuccess(ProtocolBase protocol) {

                if (protocol instanceof PageProtocol) {
                    PageProtocol p = (PageProtocol) protocol;
                    CommandPageArray.this.pageArray.addArrayToBottom(p.page);
                    if(p.delList.size() > 0){
                        CommandPageArray.this.pageArray.delList(p.delList);
                    }
                }
                CommandPageArray.this.lock = false;

                MResults.MResultsInfo<CommandPageArray<T>> info = protocol.buildRet();
                info.data = CommandPageArray.this;
                CommandPageArray.this.safeInfoCallbackRet(info);
            }
        };

        if(this.bSupportDel){
            this.protocol.begin = this.pageArray.getFirstTS();
            this.protocol.end = this.pageArray.getLastTS();
        }

        this.protocol.startWork();
    }


    /**
     * 获取上一页，上一页的定义是：按照时间先后，在先的为上一页
     * @param results
     */
    public void getPrePage(final MResults<CommandPageArray<T>> results){

        if(this.command == null || this.protocol == null || this.pageArray == null){
            MResults.MResultsInfo.SafeOnResult(results,
                    new MResults.MResultsInfo<CommandPageArray<T>>().setData(this));
            return;
        }

        this.getPageCallbacks.add(results);
        if(this.lock){
            return;
        }
        this.lock = true;

        this.command.onPre(this.protocol, this.pageArray);

        this.protocol.mCallback = new ProtocolCallback() {
            @Override
            public void onFailure(ProtocolBase protocol, int errCode) {
                CommandPageArray.this.lock = false;
                CommandPageArray.this.safeInfoCallbackRet(protocol.buildRet());
            }

            @Override
            public void onSuccess(ProtocolBase protocol) {

                if(protocol instanceof PageProtocol){
                    PageProtocol p = (PageProtocol)protocol;
                    CommandPageArray.this.pageArray.addArrayFromTop(p.page);
                    if(p.delList.size() > 0){
                        CommandPageArray.this.pageArray.delList(p.delList);
                    }
                }
                CommandPageArray.this.lock = false;

                MResults.MResultsInfo<CommandPageArray<T>> info = protocol.buildRet();
                info.data = CommandPageArray.this;
                CommandPageArray.this.safeInfoCallbackRet(info);
            }
        };

        if(this.bSupportDel){
            this.protocol.begin = this.pageArray.getFirstTS();
            this.protocol.end = this.pageArray.getLastTS();
        }
        this.protocol.startWork();
    }


    public void addFromeTop(T item){
        this.pageArray.addFromeTop(item);
    }

    public void addToBottom(T item){
        this.pageArray.addToBottom(item);

    }
    public void del(T item){
        this.pageArray.del(item);
    }

    public void addArrayFromTop(PageArray<T> page){
        pageArray.addArrayFromTop(page);
    }

    public void addArrayToBottom(PageArray<T> page){
        pageArray.addArrayToBottom(page);
    }

    public static class Builder<T extends PageArray.PageItemIndex>{

        private String pageCgiUrl;
        private Map<String, String> pageCgiParam;
        private Class<T> pageClassOfT;
        private boolean bSupportDel = false;
        private boolean bSorted = true;
        private PageProtocol<T> protocol = null;
        private CommandPage<T> command;
        private PageProtocol.ParseMoreData parseMoreData;
        private PageProtocol.TranslateItem translateItem;

        public Builder() {
        }

        public Builder<T> cgiUrl(String url) {
            if (url == null) throw new IllegalArgumentException("url == null");
            pageCgiUrl = url;
            return this;
        }

        public Builder<T> classOfT(Class<T> classOfT) {
            if (classOfT == null) throw new IllegalArgumentException("classOfT == null");
            pageClassOfT = classOfT;
            return this;
        }

        public Builder<T> supportDel(boolean supportDel){
            bSupportDel = supportDel;
            return this;
        }

        public Builder<T> cgiParam(Map<String, String> cgiParam) {
            if (cgiParam == null) throw new IllegalArgumentException("cgiParam == null");
            pageCgiParam = cgiParam;
            return this;
        }

        public Builder<T> cgiParam(ComonProtocol.ParamParse cgiParam) {
            if (cgiParam == null) throw new IllegalArgumentException("cgiParam == null");
            pageCgiParam = cgiParam.getParams();
            return this;
        }

        public Builder<T> cgiParam(ComonProtocol.ParamParse.ParamBuilder cgiParamBuilder) {
            if (cgiParamBuilder == null) throw new IllegalArgumentException("cgiParamBuilder == null");
            pageCgiParam = new ComonProtocol.ParamParse(cgiParamBuilder).getParams();
            return this;
        }

        public Builder<T> command(CommandPage<T> cmd) {
            if (cmd == null) throw new IllegalArgumentException("command == null");
            command = cmd;
            return this;
        }

        public Builder<T> pageProtocl(PageProtocol<T> pageProtocol){
            if(pageProtocol == null)  throw new IllegalArgumentException("pageProtocol == null");
            protocol = pageProtocol;
            return this;
        }

        public Builder<T> bSorted(boolean sorted){
            bSorted = sorted;
            return this;
        }

        public Builder<T> parseMoreData(PageProtocol.ParseMoreData moreData){
            if(moreData == null)  throw new IllegalArgumentException("parseMoreData == null");
            parseMoreData = moreData;
            return this;
        }

        public Builder<T> translateItem(PageProtocol.TranslateItem item){
            if(item == null)  throw new IllegalArgumentException("translateItem == null");
            translateItem = item;
            return this;
        }


        public Builder<T> commandTS() {
            command = new CommandPage<T>() {
                @Override
                public void onNext(PageProtocol<T> protocol, PageArray<T> array) {

                    if(array.isEmpty())
                    {
                        //第一次
                        protocol.lastTS = 0;
                    }
                    else
                    {
                        protocol.lastTS = array.getLastTS();
                    }

                    protocol.firstTS = 0;
                }

                @Override
                public void onPre(PageProtocol<T> protocol, PageArray<T> array) {

                    if(array.isEmpty())
                    {
                        //第一次
                        protocol.firstTS = 0;
                    }
                    else
                    {
                        protocol.firstTS = array.getFirstTS();
                    }

                    protocol.lastTS = 0;
                }
            };
            return this;
        }

        public Builder<T> commandPage(int size) {
            command = new CommandPage<T>() {
                @Override
                public void onNext(PageProtocol<T> protocol, PageArray<T> array) {

                    if(array.isEmpty())
                    {
                        //第一次
                        protocol.pageNumber = 1;
                    }
                    else
                    {
                        protocol.pageNumber = array.getPageNum()+1;
                    }
                    protocol.pageSize = size;
                }

                @Override
                public void onPre(PageProtocol<T> protocol, PageArray<T> array) {

                    protocol.pageNumber = 1;
                    protocol.pageSize = size;
                }
            };
            return this;
        }

        public CommandPageArray<T> build() {
            if (pageCgiUrl == null) throw new IllegalArgumentException("pageCgiUrl == null");
            if (command == null) throw new IllegalArgumentException("command == null");

            return new CommandPageArray<>(this);
        }
    }
}
