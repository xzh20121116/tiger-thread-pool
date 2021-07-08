package com.d1mq.tiger.util;

import java.util.concurrent.Executor;

/**
 * 第一版
 * 最简单的线程工具
 *
 * @author xzh
 * @since 2021/7/8 4:19 下午
 */
public class TigerExecutorV1 implements Executor {

    @Override
    public void execute(Runnable command) {
        new Thread(command).start();
    }
}
