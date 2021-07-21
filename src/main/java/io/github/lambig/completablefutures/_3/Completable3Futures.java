package io.github.lambig.completablefutures._3;

import static io.github.lambig.tuplite._3.Tuple3.tuple;


import io.github.lambig.completablefutures.CompletableTupleFutures;
import io.github.lambig.tuplite._3.Tuple3;
import java.util.concurrent.CompletableFuture;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Completable3Futures<V1, V2, V3> implements CompletableTupleFutures<Tuple3<V1, V2, V3>> {
  private final CompletableFuture<V1> future1;
  private final CompletableFuture<V2> future2;
  private final CompletableFuture<V3> future3;

  public static <V1, V2, V3> Completable3Futures<V1, V2, V3> of(
      CompletableFuture<V1> value1,
      CompletableFuture<V2> value2,
      CompletableFuture<V3> value3) {
    return new Completable3Futures<>(value1, value2, value3);
  }

  @Override
  public CompletableFuture<Tuple3<V1, V2, V3>> all() {
    return
        CompletableFuture
            .allOf(
                this.future1,
                this.future2,
                this.future3)
            .thenApply(
                nothing ->
                    tuple(
                        this.future1.join(),
                        this.future2.join(),
                        this.future3.join()));
  }
}
