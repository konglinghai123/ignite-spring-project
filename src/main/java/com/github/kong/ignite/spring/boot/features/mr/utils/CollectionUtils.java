package com.github.kong.ignite.spring.boot.features.mr.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 集合拆分工具
 */
public class CollectionUtils {

    /**
     * 拆分集合
     * @param <T>
     * @return  返回拆分后的各个集合
     */
    public static  <T> List<List<T>> split(List<T> baseList, int size){
        if (baseList == null || baseList.size() == 0) {
            return null;
        }
        if (size <= 0) {
            size = 1000;
        }
        int arrSize = baseList.size() % size == 0 ? baseList.size() / size : baseList.size() / size + 1;
        List<List<T>> resultList = new ArrayList<>();
        for (int i = 0; i < arrSize; i++) {
            if (arrSize - 1 == i) {
                resultList.add((List<T>) new ArrayList<Object>(baseList.subList(i * size, baseList.size())));
            } else {
                resultList.add((List<T>) new ArrayList<Object>(baseList.subList(i * size, size * (i + 1))));
            }
        }

        return resultList;
    }
}
