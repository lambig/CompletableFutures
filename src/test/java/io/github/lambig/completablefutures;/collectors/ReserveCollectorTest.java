package io.github.lambig.completablefutures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


import io.github.lambig.completablefutures.collectors.ReserveCollector;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collector;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ReserveCollectorTest {
  @Nested
  class supplierのテスト {
    @Test
    void colistの新規インスタンスを供給していること() {
      //SetUp
      var target = new ReserveCollector<Integer>().supplier();
      //Exercise
      var actual1 = target.get();
      var actual2 = target.get();
      //Verify
      assertThat(actual1)
          .isInstanceOf(List.class)
          .isEmpty();
      assertThat(actual1).isNotSameAs(actual2);
    }
  }

  @Nested
  class accumulatorのテスト {
    @Test
    void reserveをリストに格納していること() {
      //SetUp
      var target = new ReserveCollector<Integer>().accumulator();
      var list = new ArrayList<CompletableFuture<Integer>>();
      var reserve = CompletableFuture.supplyAsync(() -> 1);
      //Exercise
      target.accept(list, reserve);
      //Verify
      assertThat(list.get(0)).isSameAs(reserve);
    }
  }

  @Nested
  class combinerのテスト {
    @Test
    void 実行不可として例外を送出すること() {
      //SetUp
      var target = new ReserveCollector<Integer>().combiner();
      //Exercise
      try {
        target.apply(List.of(), List.of());
        //Verify
        fail("例外が送出されなかった");
      } catch (UnsupportedOperationException e) {
        assertThat(e.getMessage()).contains("並列ストリームはサポートしていません");
      }
    }
  }

  @Nested
  class finisherのテスト {
    @Test
    void 並列解決したreserveの内容をreserveの順で結合して返却していること() {
      //SetUp
      var target = new ReserveCollector<Integer>().finisher();
      var reserves = List.of(
          CompletableFuture.supplyAsync(() -> {
            try {
              Thread.sleep(400);
            } catch (InterruptedException e) {
              fail("処理の待ちに失敗");
            }
            return 1;
          }),
          CompletableFuture.supplyAsync(() -> {
            try {
              Thread.sleep(100);
            } catch (InterruptedException e) {
              fail("処理の待ちに失敗");
            }
            return 2;
          }));
      //Exercise
      var startTime = System.currentTimeMillis();
      var actual = target.apply(reserves);
      var endTime = System.currentTimeMillis();
      //Verify
      assertThat(actual.join()).containsExactly(1, 2);
      assertThat(startTime - endTime).isLessThan(400 + 100);
    }

    @Test
    void reserveが空なら空を返すこと() {
      //SetUp
      var target = new ReserveCollector<Integer>().finisher();
      var reserves = new ArrayList<CompletableFuture<Integer>>();
      //Exercise
      var actual = target.apply(reserves);
      //Verify
      assertThat(actual.join()).isEmpty();
    }
  }

  @Nested
  class characteristicsのテスト {
    @Test
    void 入力値が反映されていること() {
      //SetUp
      var target = new ReserveCollector<Integer>(Collector.Characteristics.CONCURRENT);
      //Exercise
      var actual = target.characteristics();
      //Verify
      assertThat(actual).containsExactly(Collector.Characteristics.CONCURRENT);
    }
  }

}