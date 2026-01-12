package com.wuin.wi_mega.common.util;

public abstract class SimpleSnowflake {

    private final static long START_STAMP = 1676131200L; // 2023-02-12 00:00:00 - 到现在一年时间

    private static long sequence = 0L;

    private static long lastStamp = -1L;

    private static long DEF_POD_ID = 0;


    /**
     * 该方法单秒最大生成 8191 个ID
     *
     * @return 趋势增ID
     */
    public static synchronized long nextId() {
//        if (DEF_POD_ID == 0) {
//            CasCache casCache = SpringContext.getBean(CasCache.class);
//            DEF_POD_ID = casCache.increaseAndGet("SERVER_ID");
//            if (DEF_POD_ID > 255) { //最大8位，不能超过255
//                casCache.set("SERVER_ID", 1L);
//                DEF_POD_ID = 1;
//            }
//        }
        //暂时设置使用秒数来进行衡量
        long currStmp = System.currentTimeMillis() / 1000;
        if (currStmp < lastStamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }
        if (currStmp == lastStamp) {
            sequence++;
        } else {
            sequence = 0L;
        }
        lastStamp = currStmp;

        return (currStmp - START_STAMP) << 22 | (sequence << 9) | DEF_POD_ID;
    }

}
