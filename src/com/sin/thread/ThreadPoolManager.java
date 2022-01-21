package com.sin.thread;

import com.sin.entity.DatabaseEntity;
import com.sin.entity.TableEntity;
import com.sin.service.DBConnection;
import com.sin.service.DBManager;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolManager {
    // 线程池的参数
    private static final int CORE_POOL_SIZE = 8;
    private static final int MAX_POOL_SIZE = 8192;
    private static final int QUEUE_CAPACITY = 8192;
    private static final long KEEP_ALIVE_TIME = 100 * 60;

    // 由主线程传入的database的相关信息
    private DBManager dbManager;
    // 新建的连接池
    DBConnection dbconn;

    public ThreadPoolManager(DBManager dbManager, DBConnection dbconn) {
        this.dbManager = dbManager;
        this.dbconn = dbconn;
        assert dbconn != null;
    }

    // 利用线程池去管理多线程任务
    public void runInsertTask() throws InterruptedException {
        // 多线程启动
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        int runTasks = 0;
        Future<Boolean>[] result = new Future[CORE_POOL_SIZE];
        for (DatabaseEntity databaseEntity : dbManager.dbList) {
            for (TableEntity tableEntity : databaseEntity.tableEntityMap.values()) {
                if (runTasks < CORE_POOL_SIZE) {
                    result[runTasks++] = poolExecutor.submit(new MultithreadDatabaseInsert(tableEntity, databaseEntity.name, dbconn));
                } else {
                    boolean finish = false;
                    while (!finish) {
                        for (int i = 0; i < CORE_POOL_SIZE; i++) {
                            if (result[i].isCancelled() || result[i].isDone()) {
                                result[i] = poolExecutor.submit(new MultithreadDatabaseInsert(tableEntity, databaseEntity.name, dbconn));
                                finish = true;
                            }
                        }
                        // 休眠一会 避免一直循环，占用CPU
                        Thread.sleep(1000);
                    }
                }
            }
        }
        poolExecutor.shutdown();
        while (!poolExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
            System.out.println("Wait for poolExecutor finished");
        }
    }
}
