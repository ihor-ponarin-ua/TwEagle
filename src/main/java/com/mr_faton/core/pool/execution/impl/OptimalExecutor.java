package com.mr_faton.core.pool.execution.impl;

import com.mr_faton.core.pool.execution.ExecutionPool;
import com.mr_faton.core.task.Task;
import org.apache.log4j.Logger;

import java.util.concurrent.*;

/**
 * Created by Mr_Faton on 15.09.2015.
 */
public class OptimalExecutor implements ExecutionPool {
    private static final Logger logger = Logger.getLogger("" +
            "com.mr_faton.core.pool.execution.impl.OptimalExecutor");
    private ExecutorService pool;
    private BlockingQueue<Runnable> queue;
    private static final int QUEUE_SIZE = 2;
    private static final int MAX_POOL_SIZE = 10;
    private static final int ROOT_THREADS = 1;
    private static final long THREAD_LIFE_TIME = 2L;

    public OptimalExecutor() {
        logger.debug("constructor");
        queue = new ArrayBlockingQueue<>(QUEUE_SIZE, true);
        pool =
                new ThreadPoolExecutor(ROOT_THREADS, MAX_POOL_SIZE, THREAD_LIFE_TIME, TimeUnit.MINUTES, queue);
    }

    @Override
    public void execute(Task task) throws Exception {
        logger.debug("execute " + task);
        pool.execute(task);
    }

    @Override
    public void shutDown() {
        logger.debug("shut down OptimalExecutor");
        if (pool != null) {
            pool.shutdown();
        }
    }
}
