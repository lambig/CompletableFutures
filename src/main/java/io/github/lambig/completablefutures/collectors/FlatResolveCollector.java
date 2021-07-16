package io.github.lambig.completablefutures.collectors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.experimental.Accessors;


@Getter
@Accessors(fluent = true)
public class FlatResolveCollector<Y> implements Collector<CompletableFuture<Stream<Y>>, List<CompletableFuture<Stream<Y>>>, Stream<Y>> {
  private final Set<Characteristics> characteristics;

  public FlatResolveCollector(Characteristics... characteristics) {
    this.characteristics = new HashSet<>(Arrays.asList(characteristics));
  }


  @Override
  public Supplier<List<CompletableFuture<Stream<Y>>>> supplier() {
    return ArrayList::new;
  }

  @Override
  public BiConsumer<List<CompletableFuture<Stream<Y>>>, CompletableFuture<Stream<Y>>> accumulator() {
    return List::add;
  }

  @Override
  public BinaryOperator<List<CompletableFuture<Stream<Y>>>> combiner() {
    return (any, thing) -> {
      throw new UnsupportedOperationException("並列ストリームはサポートしていません");
    };
  }

  @Override
  public Function<List<CompletableFuture<Stream<Y>>>, Stream<Y>> finisher() {
    return reserves -> {
      CompletableFuture.allOf(reserves.toArray(new CompletableFuture[0])).join();
      return reserves.stream().flatMap(CompletableFuture::join);
    };
  }
}
