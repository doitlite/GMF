package com.goldmf.GMFund.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cupide on 16/4/8.
 */
public class GMutableArrayUtil<K,V> {

    private int num;

    private HashMap<K, V> keys = new HashMap<>();
    private ArrayList<V> data = new ArrayList<>();

    public GMutableArrayUtil(int num){
        this.num = num;

    }

    public V put(K key, V value){

        if(key == null || value == null || num == 0)
            return value;

        if(this.keys.containsKey(key) && this.keys.get(key) == value)
            return value;

        while(this.data.size() >= this.num){
            //删除最早的一个
            V remove = this.data.get(0);
            this.removeValue(remove);
        }

        if(this.keys.containsKey(key)){
            this.data.remove(this.keys.get(key));
        }

        this.data.add(value);
        this.keys.put(key, value);

        return value;
    }

    public V get(K key){
        return this.keys.get(key);
    }

    public void clear (){
        this.data.clear();
        this.keys.clear();
    }

    public void remove(K key){
        if(!this.keys.containsKey(key))
            return;

        V value = this.keys.remove(key);
        this.data.remove(value);
    }

    private void removeValue(V value){

        K key = null;
        for (Map.Entry<K, V> entry : this.keys.entrySet()) {
            if(entry.getValue() == value){
                key = entry.getKey();
            }
        }

        if(key != null){
            this.keys.remove(key);
            this.data.remove(value);
        }
    }
}
