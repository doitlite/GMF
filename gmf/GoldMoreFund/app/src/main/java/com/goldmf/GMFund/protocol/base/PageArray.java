package com.goldmf.GMFund.protocol.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cupide on 15/8/29.
 */
public class PageArray<T extends PageArray.PageItemIndex> {

    private ArrayList<T> sendBackup = new ArrayList<>();//temp
    private ArrayList<T> mutableArray = new ArrayList<>();

    private int pageNum;
    private int pageCount;
    private long firstTS;
    private long lastTS;
    private boolean haseMore;
    private boolean bSorted = true; //有序的

    public final long getLastTS(){
        return this.lastTS;
    }
    public final long getFirstTS(){
        return this.firstTS;
    }

    public final int getPageNum(){
        return this.pageNum;
    }
    public final int getPageCount(){
        return this.pageCount;
    }

    public final List<T> data(){
        return this.mutableArray;
    }

    public final int count(){
        return this.mutableArray.size();
    }

    public final boolean getMore(){
        return this.haseMore;
    }
    public final void setMore(boolean more){
        this.haseMore = more;
    }

    public final boolean getSorted(){
        return this.bSorted;
    }
    public final void setSorted(boolean b){
        this.bSorted = b;
    }

    public Boolean isEmpty() {
        return this.mutableArray.size()== 0;
    }

    public PageArray(){
    }

    public PageArray(List<T> a, int pageNum, int pageCount) {
        if (a != null) {
            this.mutableArray.addAll(a);
        }
        this.pageNum = pageNum;
        this.pageCount = pageCount;
        this.haseMore = pageCount > pageNum;
    }

    public PageArray(List<T> array, long firstTS, long lastTS, boolean more) {
        if (array != null) {
            this.mutableArray.addAll(array);
        }
        this.firstTS = firstTS;
        this.lastTS = lastTS;
        this.haseMore = more;
    }

    public void addArrayFromTop(PageArray<T> page){

        if(page == null || page.data() == null || page.count() == 0)
            return;

        if(this.isEmpty()){
            this._insertToEmptyPage(page);
        }
        else{

            if(this.pageCount == 0 || page.pageCount == 0)
            {
                if(page.haseMore && this.firstTS < page.firstTS)
                {
                    //新数据爆了
                    this.minimization();
                    this.lastTS = page.lastTS;
                }

                this._addArrayFromTop(page.data());

                if(this.firstTS > page.firstTS){
                    //新数据在下方
                    this.haseMore = page.haseMore;
                }
                this.firstTS = page.firstTS;
            }
            else
            {
                this.minimization();

                this._addArrayToBottom(page.data());

                this.pageNum = page.pageNum;
                this.pageCount = page.pageCount;
                this.haseMore = page.haseMore;
            }
        }
    }

    public void addArrayToBottom(PageArray<T> page) {

        if(page == null || page.data() == null || page.count() == 0)
            return;

        if(this.isEmpty()){
            this._insertToEmptyPage(page);
        }
        else{
            if(this.pageCount == 0 || page.pageCount == 0)
            {
                this._addArrayToBottom(page.data());

                if(this.lastTS > page.lastTS){
                    //新数据在上方
                    this.haseMore = page.haseMore;
                }
                this.lastTS = page.lastTS;
            }
            else
            {
                this._addArrayToBottom(page.data());

                this.pageNum = page.pageNum;
                this.pageCount = page.pageCount;
                this.haseMore = page.haseMore;
            }
        }
    }

    public void delList(List<PageProtocolDelItem> delList){

        for (PageProtocolDelItem delKey : delList)
        {
            ArrayList<PageItemIndex> oldArray = new ArrayList<>();
            oldArray.addAll(mutableArray);

            for (PageItemIndex del : oldArray)
            {
                if(del.same(delKey)){
                    this.mutableArray.remove(del);
                }
            }
        }
    }

    public void addFromeTop(T item)
    {
        if(item == null || this.sendBackup.contains(item))
            return;

        this.sendBackup.add(item);
        this.mutableArray.add(0, item);
    }

    public void addToBottom(T item)
    {
        if(item == null || this.sendBackup.contains(item))
            return;

        this.sendBackup.add(item);
        this.mutableArray.add(item);
    }

    public void del(T item)
    {
        if(item == null)
            return;

        if(this.sendBackup.contains(item))
        {
            this.mutableArray.remove(item);
            this.mutableArray.remove(item);
        }
        else
        {
            for (T temp : this.mutableArray) {
                if (temp.same(item)) {
                    this.mutableArray.remove(temp);
                    break;
                }
            }
        }
    }

    private void minimization(){
        int tempIndex = 0;

        while (tempIndex < this.mutableArray.size())
        {
            T temp = this.mutableArray.get(tempIndex);
            if(!this.findIteminBackup(temp))
            {
                this.mutableArray.remove(tempIndex);
            }
            else
            {
                tempIndex ++;
            }
        }
    }

    private boolean findIteminBackup(T item) {
        for (PageItemIndex temp : this.sendBackup) {
            if (temp.same(item)) {
                return true;
            }
        }
        return false;
    }

    private void  _insertToEmptyPage(PageArray page){
        if(this.isEmpty()){

            this.mutableArray.addAll(page.data());

            this.pageNum = page.pageNum;
            this.pageCount = page.pageCount;

            this.firstTS = page.firstTS;
            this.lastTS = page.lastTS;
            this.haseMore = page.haseMore;
        }
    }
    private void _addArrayFromTop(List<T> tempArray){

        if(this.bSorted)
        {
            this._addArrayFromTopSorted(tempArray);
        }
        else
        {
            this._addArrayFromTopUnsorted(tempArray);
        }
    }

    private void _addArrayFromTopUnsorted(List<T> tempArray){

        for (int end = tempArray.size()-1; end>=0 ; end--)
        {
            T oldTemp = tempArray.get(end);

            boolean bFind = false;
            for(int j = 0; j < this.mutableArray.size(); j++)
            {
                PageItemIndex temp = this.mutableArray.get(j);
                bFind = temp.same(oldTemp);
                if(bFind){
                    this.mutableArray.remove(j);
                    this.mutableArray.add(j, oldTemp);
                    break;
                }
            }

            if(!bFind)
            {
                this.mutableArray.add(0, oldTemp);
            }
        }
    }

    private void _addArrayFromTopSorted(List<T> tempArray){
//        //剔除掉我发送的数据
//        List<T> tempArray = new ArrayList<>();
//        for (T item : data) {
//            if(!this.findIteminBackup(item)){
//                tempArray.add(item);
//            }
//        }

        int end = tempArray.size()-1;
        for (; end>=0 ; end--)
        {
            T oldTemp = tempArray.get(end);

            boolean bFind = false;
            for(int j = 0; j < this.mutableArray.size(); j++)
            {
                PageItemIndex temp = this.mutableArray.get(j);
                bFind = temp.same(oldTemp);
                if(bFind){
                    this.mutableArray.remove(j);
                    this.mutableArray.add(j, oldTemp);
                    break;
                }
            }

            if(!bFind)
            {
                break;
            }
        }

        if(end >= 0){
            for (int i = end; i >= 0; i--) {
                this.mutableArray.add(0, tempArray.get(i));
            }
        }
    }

    private void _addArrayToBottom(List<T> tempArray){

        if(this.bSorted)
        {
            this._addArrayToBottomSorted(tempArray);
        }
        else
        {
            this._addArrayToBottomUnsorted(tempArray);
        }

    }

    private void _addArrayToBottomUnsorted(List<T> tempArray) {

        for (int begin = 0; begin < tempArray.size(); begin++) {
            T newTemp = tempArray.get(begin);

            boolean bFind = false;
            for (int j = 0; j < this.mutableArray.size(); j++) {
                T temp = this.mutableArray.get(j);
                bFind = temp.same(newTemp);
                if (bFind) {
                    this.mutableArray.remove(j);
                    this.mutableArray.add(j, newTemp);
                    break;
                }
            }

            if (!bFind) {
                this.mutableArray.add(newTemp);
            }
        }
    }

    private void _addArrayToBottomSorted(List<T> tempArray) {
//        //剔除掉我发送的数据
//        List<T> tempArray = new ArrayList<>();
//        for (T item : data) {
//            if (!this.findIteminBackup(item)) {
//                tempArray.add(item);
//            }
//        }

        int begin = 0;
        for (; begin < tempArray.size(); begin++) {
            T newTemp = tempArray.get(begin);

            boolean bFind = false;
            for (int j = 0; j < this.mutableArray.size(); j++) {
                T temp = this.mutableArray.get(j);
                bFind = temp.same(newTemp);
                if (bFind) {
                    this.mutableArray.remove(j);
                    this.mutableArray.add(j, newTemp);
                    break;
                }
            }

            if (!bFind) {
                break;
            }
        }

        if (begin < tempArray.size()) {
            for (int i = begin; i < tempArray.size(); i++) {
                this.mutableArray.add(tempArray.get(i));
            }
        }
    }


    public static abstract class PageItemIndex implements Serializable {

        public abstract Object getKey();
        public abstract long getTime();

        public boolean same(PageItemIndex index) {
            if (index == this)
                return true;

            if ((index.getKey() instanceof String) && (this.getKey() instanceof String)
                    && index.getKey().toString().equals((this.getKey()).toString()))
                return true;

            return (index.getKey() instanceof Number) && (this.getKey() instanceof Number)
                    && index.getKey().toString().equals((this.getKey()).toString());

        }
    }

    public static class PageProtocolDelItem extends PageItemIndex{

        private  String mKey;
        @Override
        public Object getKey() {
            return mKey;
        }

        @Override
        public long getTime() {
            return 0;
        }

        PageProtocolDelItem(String key){
            mKey = key;
        }

        @Override
        public boolean same(PageItemIndex index) {
            return this.getKey().equals(index.getKey().toString());
        }
    }
}
