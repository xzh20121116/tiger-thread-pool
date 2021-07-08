package com.d1mq.tiger.util;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Executor;

/**
 * 第二版
 * 把任务丢到一个 task 队列中,然后只启动一个线程,就叫他 workerx线程吧，不断从tasksd队列中取任务，执行任务
 *
 * @author xzh
 * @since 2021/7/8 4:20 下午
 */
public class TigerExecutorV2 implements Executor {
    //由调用者提供的阻塞队列
    private final BlockingDeque<Runnable> workQueue;

    public TigerExecutorV2(BlockingDeque<Runnable> workQueue) {
        this.workQueue = workQueue;
        new Thread(new Worker()).start();
    }

    @Override
    public void execute(Runnable command) {
        //直接往队列里放，等着被工作线程们抢
        if (!workQueue.offer(command)) {
            //如果队列满了,直接抛弃
            System.out.println("队列满了，直接抛弃");
        }
    }

    public final class Worker implements Runnable {
        //阻塞的从队列获取一个任务
        private Runnable getTask() {
            try {
                return workQueue.take();
            } catch (InterruptedException e) {
                return null;
            }
        }

        //死循环从队列里读取任务，然后运行任务
        @Override
        public void run() {
            Runnable task;
            while (true) {
                if ((task = getTask()) != null) {
                    task.run();
                }
            }
        }
    }
}
