// All rights reserved
package com.wuin.wi_mega.common.enums;


import com.wuin.wi_mega.common.util.CompareUtils;

/**
 * @version 1.0
 * @created 2023/3/2 下午3:14
 **/
public interface IEnumItem<K,V> {
    K getCode();
    V getMessage();

    default boolean equalByCode(K code) {
        return CompareUtils.equals(getCode(), code);
    }
}