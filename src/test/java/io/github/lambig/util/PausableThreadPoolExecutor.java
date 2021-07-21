package io.github.lambig.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PausableThreadPoolExecutor extends ThreadPoolExecutor {
  private boolean isPaused;
  private ReentrantLock pauseLock = new ReentrantLock();
  private Condition unPaused = pauseLock.newCondition();

  public PausableThreadPoolExecutor() {
    super(5, 10, 10000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(20));
    setThreadFactory(r -> new Thread(r, "DeferredTest"));
    this.pause();
  }

  protected void beforeExecute(Thread t, Runnable r) {
    super.beforeExecute(t, r);
    pauseLock.lock();
    try {
      while (isPaused) {
        unPaused.await();
      }
    } catch (InterruptedException ie) {
      t.interrupt();
    } finally {
      pauseLock.unlock();
    }
  }

  public void pause() {
    pauseLock.lock();
    try {
      isPaused = true;
    } finally {
      pauseLock.unlock();
    }
  }

  public void resume() {
    pauseLock.lock();
    try {
      isPaused = false;
      unPaused.signalAll();
    } finally {
      pauseLock.unlock();
    }
  }
}