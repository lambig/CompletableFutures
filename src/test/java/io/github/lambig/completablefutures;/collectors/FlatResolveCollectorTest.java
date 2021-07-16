package io.github.lambig.completablefutures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


import io.github.lambig.completablefutures.collectors.FlatResolveCollector;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collector;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class FlatResolveCollectorTest {
  @Nested
  class supplierのテスト {
    @Test
    void arrayListの新規インスタンスを供給していること() {
      //SetUp
      var target = new FlatResolveCollector<Integer>().supplier();
      //Exercise
      var actual1 = target.get();
      var actual2 = target.get();
      //Verify
      assertThat(actual1)
          .isInstanceOf(ArrayList.class)
          .isEmpty();
      assertThat(actual1).isNotSameAs(actual2);
    }
  }

  @Nested
  class accumulatorのテスト {
    @Test
    void reserveをリストに格納していること() {
      //SetUp
      var target = new FlatResolveCollector<Integer>().accumulator();
      var list = new ArrayList<CompletableFuture<Stream<Integer>>>();
      var reserve = CompletableFuture.supplyAsync(() -> Stream.of(1, 2, 3));
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
      var target = new FlatResolveCollector<Integer>().combiner();
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
    void 解決したreserveの内容をreserveの順で結合して返却していること() {
      //SetUp
      var target = new FlatResolveCollector<Integer>().finisher();
      var reserves = List.of(
          CompletableFuture.supplyAsync(() -> {
            try {
              Thread.sleep(400);
            } catch (InterruptedException e) {
              fail("処理の待ちに失敗");
            }
            return Stream.of(1, 2, 3);
          }),
          CompletableFuture.supplyAsync(() -> {
            try {
              Thread.sleep(100);
            } catch (InterruptedException e) {
              fail("処理の待ちに失敗");
            }
            return Stream.of(4, 5, 6);
          }));
      //Exercise
      var startTime = System.currentTimeMillis();
      var actual = target.apply(reserves);
      var endTime = System.currentTimeMillis();
      //Verify
      assertThat(actual).containsExactly(1, 2, 3, 4, 5, 6);
      assertThat(startTime - endTime).isLessThan(400 + 100);
    }

    @Test
    void reserveが空なら空を返すこと() {
      //SetUp
      var target = new FlatResolveCollector<Integer>().finisher();
      var reserves = List.<CompletableFuture<Stream<Integer>>>of();
      //Exercise
      var actual = target.apply(reserves);
      //Verify
      assertThat(actual).isEmpty();
    }
  }

  @Nested
  class characteristicsのテスト {
    @Test
    void 入力値が反映されていること() {
      //SetUp
      var target = new FlatResolveCollector<Integer>(Collector.Characteristics.CONCURRENT);
      //Exercise
      var actual = target.characteristics();
      //Verify
      assertThat(actual).containsExactly(Collector.Characteristics.CONCURRENT);
    }
  }
}