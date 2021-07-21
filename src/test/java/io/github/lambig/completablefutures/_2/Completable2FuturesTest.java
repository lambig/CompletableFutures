package io.github.lambig.completablefutures._2;

import static io.github.lambig.tuplite._2.Tuple2._2testAllWith;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;


import io.github.lambig.util.PausableThreadPoolExecutor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class Completable2FuturesTest {
  @Nested
  class allTest {
    @Test
    void test() throws InterruptedException {
      // SetUp
      var executor = new PausableThreadPoolExecutor();
      var target =
          Completable2Futures.of(
              delayedValue(123, 500, executor),
              delayedValue("abc", 200, executor));
      // Exercise
      var actual = target.get();
      // Verify
      Thread.sleep(1000);
      assertThat(actual).isNotCompleted();
      executor.resume();
      var start = System.nanoTime();
      var values = actual.join();
      var elapsed = (System.nanoTime() - start) / 1000 / 1000;
      assertThat(elapsed)
          .isGreaterThanOrEqualTo(500)
          .isLessThan(600);
      assertThat(values)
          .matches(
              _2testAllWith(
                  Integer.valueOf(123)::equals,
                  "abc"::equals
              ));
    }
  }

  private static <E> CompletableFuture<E> delayedValue(E value, int milliseconds, Executor executor) {
    return
        supplyAsync(
            () -> {
              try {
                Thread.sleep(milliseconds);
              } catch (InterruptedException e) {
                fail(e);
              }
              return value;
            }, executor);
  }

}