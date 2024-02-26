package com.sucy.skill.utils;

import java.io.Serializable;

/**
 * GeekCollectLimit
 * me.geek.collect.api
 *
 * @author 老廖
 * @since 2023/10/3 9:09
 */
public class Pair<K, V> implements Serializable {

    public K key;
    public V value;

    public Pair(K first, V second) {
        this.key = first;
        this.value = second;
    }

    @Override
    public String toString() {
        return "("+ key +", "+ value + ")";
    }
}
