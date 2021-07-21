package io.github.lambig.completablefutures._2;

import static io.github.lambig.tuplite._2.Tuple2.tuple2;


import io.github.lambig.completablefutures.CompletableTupleFutures;
import io.github.lambig.tuplite._2.Tuple2;
import java.util.concurrent.CompletableFuture;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Completable2Futures<V1, V2> implements CompletableTupleFutures<Tuple2<V1, V2>> {
  private final CompletableFuture<V1> future1;
  private final CompletableFuture<V2> future2;

  public static <V1, V2> Completable2Futures<V1, V2> of(
      CompletableFuture<V1> value1,
      CompletableFuture<V2> value2) {
    return new Completable2Futures<>(value1, value2);
  }

  @Override
  public CompletableFuture<Tuple2<V1, V2>> all() {
    return
        CompletableFuture
            .allOf(
                this.future1,
                this.future1)
            .thenApply(
                nothing ->
                    tuple2(
                        this.future1.join(),
                        this.future2.join()));
  }
}
