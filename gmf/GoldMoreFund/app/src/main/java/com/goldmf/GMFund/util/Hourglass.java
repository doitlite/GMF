package com.goldmf.GMFund.util;

/**
 * Created by cupide on 15/7/25.
 */
public class Hourglass {

    private long time = 0; //时长
    private long lastTime = 0; //上一次操作时间

    public Hourglass(){}

    public void start(){

        if(lastTime == 0)
        {
            //pause后 或者第一次 开启
            lastTime = current();
        }
        else {
            //正在运行
        }
    }

    public long stop(){

        if(lastTime > 0)
        {
            //当前处于非pause状态
            time = time + (current() - lastTime);
        }
        else {
            //当前处于pause状态
        }

        long retTime = time;
        time = 0;
        lastTime = 0;
        return retTime;

    }

    public long pause(){

        if(lastTime > 0)
        {
            //当前处于非pause状态
            time = time + (current() - lastTime);
            lastTime = 0;
        }
        else {
            //当前处于pause状态
        }

        return time;
    }

    public long peek(){

        if(lastTime > 0)
        {
            //当前处于非pause状态
            return time + (current() - lastTime);
        }
        else {
            //当前处于pause状态
            return time;
        }
    }

    public static long current(){
        return  System.currentTimeMillis();
    }
}
