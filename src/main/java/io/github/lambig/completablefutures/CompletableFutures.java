package io.github.lambig.completablefutures;

import static io.github.lambig.tuplite._2.Tuple2._2map1With;
import static io.github.lambig.tuplite._2.Tuple2._2map2With;
import static java.util.stream.Collectors.toList;


import io.github.lambig.completablefutures.collectors.FlatReserveCollector;
import io.github.lambig.completablefutures.collectors.FlatResolveCollector;
import io.github.lambig.completablefutures.collectors.ReserveCollector;
import io.github.lambig.completablefutures.collectors.ResolveCollector;
import io.github.lambig.tuplite._2.Tuple2;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CompletableFutures {
  public static <X, Y> Stream<Tuple2<X, Y>> flatResolveAllRight(Stream<Tuple2<X, CompletableFuture<Y>>> tuple2Stream) {
    var tuples = tuple2Stream.collect(toList());
    CompletableFuture.allOf(tuples.stream().map(Tuple2::_2).toArray(CompletableFuture[]::new)).join();
    return tuples.stream().map(_2map2With(CompletableFuture::join));
  }

  public static <X, Y> Stream<Tuple2<X, Y>> flatResolveAllLeft(Stream<Tuple2<CompletableFuture<X>, Y>> tuple2Stream) {
    var tuples = tuple2Stream.collect(toList());
    CompletableFuture.allOf(tuples.stream().map(Tuple2::_1).toArray(CompletableFuture[]::new)).join();
    return tuples.stream().map(_2map1With(CompletableFuture::join));
  }

  @SafeVarargs
  public static <X> Optional<Stream<CompletableFuture<X>>> supply(Supplier<X>... suppliers) {
    return Optional.of(suppliers).map(Arrays::stream).map(e -> e.map(CompletableFuture::supplyAsync));
  }

  public static <X, Y> Function<CompletableFuture<X>, CompletableFuture<Y>> thenApply(Function<X, Y> function) {
    return futureX -> futureX.thenApply(function);
  }

  public static <Y> Collector<CompletableFuture<Y>, ?, Stream<Y>> toResolvedAll() {
    return new ResolveCollector<>();
  }

  @SafeVarargs
  public static <Y> Stream<Y> resolveAll(CompletableFuture<Y>... reserves) {
    return Stream.of(reserves).collect(toResolvedAll());
  }

  public static <Y> Stream<Y> resolveAll(Stream<CompletableFuture<Y>> reserves) {
    return reserves.collect(toResolvedAll());
  }

  public static <Y> Collector<CompletableFuture<Stream<Y>>, ?, Stream<Y>> toFlatResolvedAll() {
    return new FlatResolveCollector<>();
  }

  @SafeVarargs
  public static <Y> Stream<Y> flatResolveAll(CompletableFuture<Stream<Y>>... reserves) {
    return Stream.of(reserves).collect(toFlatResolvedAll());
  }

  public static <Y> Stream<Y> flatResolveAll(Stream<CompletableFuture<Stream<Y>>> reserves) {
    return reserves.collect(toFlatResolvedAll());
  }

  public static <Y> Collector<CompletableFuture<Y>, ?, CompletableFuture<Stream<Y>>> toReservedAll() {
    return new ReserveCollector<>();
  }

  @SafeVarargs
  public static <Y> CompletableFuture<Stream<Y>> reserveAll(CompletableFuture<Y>... reserves) {
    return Stream.of(reserves).collect(toReservedAll());
  }

  public static <Y> CompletableFuture<Stream<Y>> reserveAll(Stream<CompletableFuture<Y>> reserves) {
    return reserves.collect(toReservedAll());
  }


  public static <Y> Collector<CompletableFuture<Stream<Y>>, ?, CompletableFuture<Stream<Y>>> toFlatReservedAll() {
    return new FlatReserveCollector<>();
  }

  @SafeVarargs
  public static <Y> CompletableFuture<Stream<Y>> flatReserveAll(CompletableFuture<Stream<Y>>... reserves) {
    return Stream.of(reserves).collect(toFlatReservedAll());
  }

  public static <Y> CompletableFuture<Stream<Y>> flatReserveAll(Stream<CompletableFuture<Stream<Y>>> reserves) {
    return reserves.collect(toFlatReservedAll());
  }
}
