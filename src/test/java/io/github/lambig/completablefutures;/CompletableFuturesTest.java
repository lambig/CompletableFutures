package io.github.lambig.completablefutures;

import static io.github.lambig.patterns.Patterns.equalsTo;
import static io.github.lambig.tuplite._2.Tuple2._2testAllWith;
import static io.github.lambig.tuplite._2.Tuple2.tuple2;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


import io.github.lambig.completablefutures.collectors.FlatReserveCollector;
import io.github.lambig.completablefutures.collectors.FlatResolveCollector;
import io.github.lambig.completablefutures.collectors.ResolveCollector;
import io.github.lambig.completablefutures.collectors.ReserveCollector;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CompletableFuturesTest {
  @Nested
  class flatResolveAllRightのテスト {
    @Test
    void coupleの右項を非同期解決していること() {
      //SetUp
      var reserves =
          Stream
              .of(
                  tuple2("A", CompletableFuture.supplyAsync(() -> {
                    try {
                      Thread.sleep(800);
                    } catch (InterruptedException e) {
                      fail("スリープ失敗");
                    }
                    return 1;
                  })),
                  tuple2("B", CompletableFuture.supplyAsync(() -> {
                    try {
                      Thread.sleep(400);
                    } catch (InterruptedException e) {
                      fail("スリープ失敗");
                    }
                    return 2;
                  })));
      //Exercise
      var start = System.currentTimeMillis();
      var actual = CompletableFutures.flatResolveAllRight(reserves).collect(toList());
      var elapsed = System.currentTimeMillis() - start;
      //Verify
      assertThat(elapsed).isLessThan(1000);
      System.out.println(elapsed);
      assertThat(actual.get(0)).matches(_2testAllWith("A"::equals, equalsTo(1)));
      assertThat(actual.get(1)).matches(_2testAllWith("B"::equals, equalsTo(2)));
    }
  }

  @Nested
  class flatResolveAllLeftのテスト {
    @Test
    void coupleの左項をすべて解決していること() {
      //SetUp
      var reserves =
          Stream
              .of(
                  tuple2(CompletableFuture.completedFuture(1), "A"),
                  tuple2(CompletableFuture.completedFuture(2), "B"));
      //Exercise
      var actual = CompletableFutures.flatResolveAllLeft(reserves).collect(toList());
      //Verify
      assertThat(actual.get(0)).matches(_2testAllWith(equalsTo(1), "A"::equals));
      assertThat(actual.get(1)).matches(_2testAllWith(equalsTo(2), "B"::equals));
    }
  }

  @Nested
  class thenApplyのテスト {
    @Test
    void 引数関数を適用したCompletableFutureを返していること() {
      //SetUp
      var target = CompletableFutures.<Integer, Integer>thenApply(num -> num + 1);
      var reserve = CompletableFuture.supplyAsync(() -> {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          fail("スリープに失敗しました");
        }
        return 1;
      });
      //Exercise
      var actual = target.apply(reserve);
      //Verify
      assertThat(actual).isNotCompleted();
      assertThat(actual.join()).isEqualTo(2);
    }
  }

  @Nested
  class supplyのテスト {
    @Test
    void supplierをCompletableFutureでラップしたStreamを返すこと() {
      //SetUp
      //Exercise
      var reserves = CompletableFutures.supply(() -> 1, () -> 2, () -> 3);
      //Verify
      var resolved = reserves.map(CompletableFutures::resolveAll).get();
      assertThat(resolved).containsExactly(1, 2, 3);
    }

    @Test
    void supplierが一つもない場合空Streamを返すこと() {
      //SetUp
      //Exercise
      var reserves = CompletableFutures.supply();
      //Verify
      var resolved = reserves.map(CompletableFutures::resolveAll).get();
      assertThat(resolved).isEmpty();
    }
  }

  @Nested
  class toResolvedAllのテスト {
    @Test
    void AllResolverのインスタンスを作成して返していること() {
      //SetUp
      //Exercise
      var actual1 = CompletableFutures.toResolvedAll();
      var actual2 = CompletableFutures.toResolvedAll();
      //Verify
      assertThat(actual1)
          .isInstanceOf(ResolveCollector.class)
          .isNotSameAs(actual2);
    }
  }

  @Nested
  class ResolveAll_可変長引数のテスト {
    @Test
    void 引数をすべて解決したStreamにしていること() {
      //SetUp
      //Exercise
      var resolved =
          CompletableFutures
              .resolveAll(
                  CompletableFuture.completedFuture(1),
                  CompletableFuture.completedFuture(2))
              .collect(toList());
      //Verify
      assertThat(resolved).containsExactly(1, 2);
    }

    @Test
    void 引数が空の場合_空を返すこと() {
      //SetUp
      //Exercise
      var resolved =
          CompletableFutures
              .resolveAll()
              .collect(toList());
      //Verify
      assertThat(resolved).isEmpty();
    }
  }

  @Nested
  class ResolveAll_Streamのテスト {
    @Test
    void 引数をすべて解決したStreamにしていること() {
      //SetUp
      //Exercise
      var resolved =
          CompletableFutures
              .resolveAll(
                  Stream
                      .of(
                          CompletableFuture.completedFuture(1),
                          CompletableFuture.completedFuture(2)))
              .collect(toList());
      //Verify
      assertThat(resolved).containsExactly(1, 2);
    }

    @Test
    void 引数が空の場合_空を返すこと() {
      //SetUp
      //Exercise
      var resolved =
          CompletableFutures
              .resolveAll(Stream.empty())
              .collect(toList());
      //Verify
      assertThat(resolved).isEmpty();
    }
  }

  @Nested
  class toFlatResolvedAllのテスト {
    @Test
    void StreamAllResolverのインスタンスを作成して返していること() {
      //SetUp
      //Exercise
      var actual1 = CompletableFutures.toFlatResolvedAll();
      var actual2 = CompletableFutures.toFlatResolvedAll();
      //Verify
      assertThat(actual1)
          .isInstanceOf(FlatResolveCollector.class)
          .isNotSameAs(actual2);
    }
  }


  @Nested
  class flatResolveAll_可変長引数のテスト {
    @Test
    void 引数をすべて解決したStreamにしていること() {
      //SetUp
      //Exercise
      var resolved =
          CompletableFutures
              .flatResolveAll(
                  CompletableFuture.completedFuture(Stream.of(1, 2)),
                  CompletableFuture.completedFuture(Stream.of(3, 4)))
              .collect(toList());
      //Verify
      assertThat(resolved).containsExactly(1, 2, 3, 4);
    }

    @Test
    void 引数が空の場合_空を返すこと() {
      //SetUp
      //Exercise
      var resolved =
          CompletableFutures
              .flatResolveAll()
              .collect(toList());
      //Verify
      assertThat(resolved).isEmpty();
    }
  }

  @Nested
  class flatResolveAll_Streamのテスト {
    @Test
    void 引数をすべて解決したStreamにしていること() {
      //SetUp
      //Exercise
      var resolved =
          CompletableFutures
              .flatResolveAll(
                  Stream.of(
                      CompletableFuture.completedFuture(Stream.of(1, 2)),
                      CompletableFuture.completedFuture(Stream.of(3, 4))))
              .collect(toList());
      //Verify
      assertThat(resolved).containsExactly(1, 2, 3, 4);
    }

    @Test
    void 引数が空の場合_空を返すこと() {
      //SetUp
      //Exercise
      var resolved =
          CompletableFutures
              .flatResolveAll(Stream.empty())
              .collect(toList());
      //Verify
      assertThat(resolved).isEmpty();
    }
  }

  @Nested
  class toReservedAllのテスト {
    @Test
    void AllReserverのインスタンスを作成して返していること() {
      //SetUp
      //Exercise
      var actual1 = CompletableFutures.toReservedAll();
      var actual2 = CompletableFutures.toReservedAll();
      //Verify
      assertThat(actual1)
          .isInstanceOf(ReserveCollector.class)
          .isNotSameAs(actual2);
    }
  }

  @Nested
  class reserveAll_可変長引数のテスト {
    @Test
    void 引数をすべて解決したStreamに解決するCompletableFutureにしていること() {
      //SetUp
      //Exercise
      var reserved =
          CompletableFutures
              .reserveAll(
                  CompletableFuture.completedFuture(1),
                  CompletableFuture.completedFuture(2));
      //Verify
      assertThat(reserved.join()).containsExactly(1, 2);
    }

    @Test
    void 引数が空の場合_空を返すこと() {
      //SetUp
      //Exercise
      var reserved =
          CompletableFutures
              .reserveAll();
      //Verify
      assertThat(reserved.join()).isEmpty();
    }
  }

  @Nested
  class reserveAll_Streamのテスト {
    @Test
    void 引数をすべて解決したStreamに解決するCompletableFutureにしていること() {
      //SetUp
      //Exercise
      var reserved =
          CompletableFutures
              .reserveAll(
                  Stream
                      .of(
                          CompletableFuture.completedFuture(1),
                          CompletableFuture.completedFuture(2)));
      //Verify
      assertThat(reserved.join().collect(toList())).containsExactly(1, 2);
    }

    @Test
    void 引数が空の場合_空を返すこと() {
      //SetUp
      //Exercise
      var reserved =
          CompletableFutures
              .reserveAll(Stream.empty());
      //Verify
      assertThat(reserved.join()).isEmpty();
    }
  }

  @Nested
  class toFlatReservedAllのテスト {
    @Test
    void StreamAllReserverのインスタンスを作成して返していること() {
      //SetUp
      //Exercise
      var actual1 = CompletableFutures.toFlatReservedAll();
      var actual2 = CompletableFutures.toFlatReservedAll();
      //Verify
      assertThat(actual1)
          .isInstanceOf(FlatReserveCollector.class)
          .isNotSameAs(actual2);
    }
  }


  @Nested
  class flatReserveAll_可変長引数のテスト {
    @Test
    void 引数をすべて解決したStreamに解決するCompletableFutureにしていること() {
      //SetUp
      //Exercise
      var reserved =
          CompletableFutures
              .flatReserveAll(
                  CompletableFuture.completedFuture(Stream.of(1, 2)),
                  CompletableFuture.completedFuture(Stream.of(3, 4)));
      //Verify
      assertThat(reserved.join()).containsExactly(1, 2, 3, 4);
    }

    @Test
    void 引数が空の場合_空を返すこと() {
      //SetUp
      //Exercise
      var reserved =
          CompletableFutures
              .flatReserveAll();
      //Verify
      assertThat(reserved.join()).isEmpty();
    }
  }

  @Nested
  class flatReserveAll_Streamのテスト {
    @Test
    void 引数をすべて解決したStreamに解決するCompletableFutureにしていること() {
      //SetUp
      //Exercise
      var reserved =
          CompletableFutures
              .flatReserveAll(
                  Stream.of(
                      CompletableFuture.completedFuture(Stream.of(1, 2)),
                      CompletableFuture.completedFuture(Stream.of(3, 4))));
      //Verify
      assertThat(reserved.join()).containsExactly(1, 2, 3, 4);
    }

    @Test
    void 引数が空の場合_空を返すこと() {
      //SetUp
      //Exercise
      var reserved =
          CompletableFutures
              .flatReserveAll(Stream.empty());
      //Verify
      assertThat(reserved.join()).isEmpty();
    }
  }
}