package io.github.lambig.completablefutures;

import io.github.lambig.tuplite.Tuple;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface CompletableTupleFutures<T extends Tuple> extends Supplier<CompletableFuture<T>> {
  @Override
  default CompletableFuture<T> get() {
    return this.all();
  }

  CompletableFuture<T> all();
}
