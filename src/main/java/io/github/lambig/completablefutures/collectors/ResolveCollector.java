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
public class ResolveCollector<Y> implements Collector<CompletableFuture<Y>, List<CompletableFuture<Y>>, Stream<Y>> {
  private final Set<Characteristics> characteristics;

  public ResolveCollector(Characteristics... characteristics) {
    this.characteristics = new HashSet<>(Arrays.asList(characteristics));
  }

  @Override
  public Supplier<List<CompletableFuture<Y>>> supplier() {
    return ArrayList::new;
  }

  @Override
  public BiConsumer<List<CompletableFuture<Y>>, CompletableFuture<Y>> accumulator() {
    return List::add;
  }

  @Override
  public BinaryOperator<List<CompletableFuture<Y>>> combiner() {
    return (any, thing) -> {
      throw new UnsupportedOperationException("並列ストリームはサポートしていません");
    };
  }

  @Override
  public Function<List<CompletableFuture<Y>>, Stream<Y>> finisher() {
    return reserves -> {
      CompletableFuture.allOf(reserves.toArray(new CompletableFuture[0])).join();
      return reserves.stream().map(CompletableFuture::join);
    };
  }


}
