package com.d1mq.tiger.util;

import java.util.HashSet;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xzh
 * @since 2021/7/8 4:45 下午
 */
public class TigerExecutorV5 implements Executor {
    // 工作线程的数量
    private AtomicInteger workCount = new AtomicInteger(0);
    // 存放工作线程 Worker 的引用
    private final HashSet<Worker> workers = new HashSet<>();

    // 核心线程数量
    private volatile int corePoolSize;
    // 最大线程数
    private volatile int maximumPoolSize;
    // 空闲时间
    private volatile long keepAliveTime;
    // 空闲时间
    private TimeUnit timeUnit;
    // 由调用者提供的阻塞队列，核心线程数满了之后往这里放
    private final BlockingQueue<Runnable> workQueue;
    // 拒绝策略
    private volatile RejectedExecutionHandler handler;
    // 线程工厂
    private volatile ThreadFactory threadFactory;

    public TigerExecutorV5(
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            TimeUnit timeUnit,
            BlockingQueue<Runnable> workQueue,
            RejectedExecutionHandler handler,
            ThreadFactory threadFactory) {
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.workQueue = workQueue;
        this.handler = handler;
        this.threadFactory = threadFactory;
    }

    @Override
    public void execute(Runnable command) {
        if (workCount.get() < corePoolSize) {
            addWorker(command);
            return;
        }
        if (!workQueue.offer(command)) {
            if (workCount.get() < maximumPoolSize) {
                addWorker(command);
                return;
            }
            handler.rejectedExecution(command, this);
        }
    }

    private void addWorker(Runnable command) {
        // 工作线程数 <= 核心线程时，新建工作线程
        Worker w = new Worker(command);
        // 增加工作线程数
        workCount.getAndIncrement();
        workers.add(w);
        // 并且把它启动
        w.thread.start();
    }

    private final class Worker implements Runnable {

        final Thread thread;
        private Runnable task;

        Worker(Runnable firstTask) {
            this.task = firstTask;
            this.thread = threadFactory.newThread(this);
        }

        // 死循环从队列里读任务，然后运行任务
        @Override
        public void run() {
            while (task != null || (task = getTask()) != null) {
                task.run();
                task = null;
            }
            workCount.getAndDecrement();
        }

        // 阻塞地从队列里获取一个任务
        private Runnable getTask() {
            boolean timed = workCount.get() > corePoolSize;
            try {
                return timed ? workQueue.poll(keepAliveTime, timeUnit) : workQueue.take();
            } catch (InterruptedException e) {
                return null;
            }
        }

    }
}
