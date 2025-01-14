package io.weaviate.client.v1.graphql.query.argument;

import io.weaviate.client.v1.graphql.query.util.Serializer;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Bm25Argument implements Argument {
  String query;
  String[] properties;

  @Override
  public String build() {
    Set<String> arg = new LinkedHashSet<>();

    arg.add(String.format("query:%s", Serializer.quote(query)));
    if (properties != null) {
      arg.add(String.format("properties:%s", Serializer.arrayWithQuotes(properties)));
    }

    return String.format("bm25:{%s}", String.join(" ", arg));
  }
}
