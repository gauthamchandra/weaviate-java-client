package io.weaviate.client.v1.graphql.query.builder;

import io.weaviate.client.v1.data.replication.model.ConsistencyLevel;
import io.weaviate.client.v1.filters.Operator;
import io.weaviate.client.v1.filters.WhereFilter;
import io.weaviate.client.v1.graphql.query.argument.AskArgument;
import io.weaviate.client.v1.graphql.query.argument.GroupArgument;
import io.weaviate.client.v1.graphql.query.argument.GroupByArgument;
import io.weaviate.client.v1.graphql.query.argument.GroupType;
import io.weaviate.client.v1.graphql.query.argument.NearImageArgument;
import io.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import io.weaviate.client.v1.graphql.query.argument.NearVectorArgument;
import io.weaviate.client.v1.graphql.query.argument.SortArgument;
import io.weaviate.client.v1.graphql.query.argument.SortArguments;
import io.weaviate.client.v1.graphql.query.argument.SortOrder;
import io.weaviate.client.v1.graphql.query.argument.WhereArgument;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.client.v1.graphql.query.fields.Fields;
import io.weaviate.client.v1.graphql.query.fields.GenerativeSearchBuilder;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GetBuilderTest {

  @Test
  public void testBuildSimpleGet() {
    // given
    Field name = Field.builder().name("name").build();
    // when
    String query = GetBuilder.builder().className("Pizza").fields(Fields.builder().fields(new Field[]{ name }).build()).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get{Pizza{name}}}", query);
  }

  @Test
  public void testBuildGetMultipleFields() {
    // given
    Field name = Field.builder().name("name").build();
    Field description = Field.builder().name("description").build();
    Fields fields = Fields.builder().fields(new Field[]{ name, description }).build();
    // when
    String query = GetBuilder.builder().className("Pizza").fields(fields).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get{Pizza{name description}}}", query);
  }

  @Test
  public void testBuildGetWhereFilter() {
    // given
    Field name = Field.builder().name("name").build();
    Fields fields = Fields.builder().fields(new Field[]{ name }).build();
    WhereArgument where1 = WhereArgument.builder()
      .filter(WhereFilter.builder()
        .path(new String[]{ "name" })
        .operator(Operator.Equal)
        .valueText("Hawaii")
        .build())
      .build();
    WhereArgument where2 = WhereArgument.builder()
      .filter(WhereFilter.builder()
        .operands(new WhereFilter[]{
          WhereFilter.builder()
            .path(new String[]{ "name" })
            .operator(Operator.Equal)
            .valueText("Hawaii")
            .build(),
          WhereFilter.builder()
            .path(new String[]{ "name" })
            .operator(Operator.Equal)
            .valueText("Doener")
            .build(),
        })
        .operator(Operator.Or)
        .build())
      .build();
    // when
    String query1 = GetBuilder.builder().className("Pizza").fields(fields).withWhereFilter(where1).build().buildQuery();
    String query2 = GetBuilder.builder().className("Pizza").fields(fields).withWhereFilter(where2).build().buildQuery();
    // then
    assertNotNull(query1);
    assertEquals("{Get{Pizza(where:{path:[\"name\"] valueText:\"Hawaii\" operator:Equal}){name}}}", query1);
    assertNotNull(query2);
    assertEquals("{Get{Pizza" +
            "(where:{operator:Or operands:[{path:[\"name\"] valueText:\"Hawaii\" operator:Equal},{path:[\"name\"] valueText:\"Doener\" operator:Equal}]})" +
            "{name}}}", query2);
  }

  @Test
  public void testBuildGetWithLimit() {
    // given
    Field name = Field.builder().name("name").build();
    Fields fields = Fields.builder().fields(new Field[]{ name }).build();
    // when
    String query = GetBuilder.builder()
            .className("Pizza")
            .fields(fields)
            .limit(2)
            .build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get{Pizza(limit:2){name}}}", query);
  }

  @Test
  public void testBuildGetWithLimitAndOffset() {
    // given
    Field name = Field.builder().name("name").build();
    Fields fields = Fields.builder().fields(new Field[]{ name }).build();
    // when
    String query = GetBuilder.builder()
            .className("Pizza")
            .fields(fields)
            .offset(0)
            .limit(2)
            .build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get{Pizza(limit:2,offset:0){name}}}", query);
  }

  @Test
  public void testBuildGetWithLimitAndAfter() {
    // given
    Field name = Field.builder().name("name").build();
    Fields fields = Fields.builder().fields(new Field[]{ name }).build();
    // when
    String query = GetBuilder.builder()
      .className("Pizza")
      .fields(fields)
      .after("00000000-0000-0000-0000-000000000000")
      .limit(2)
      .build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get{Pizza(limit:2,after:\"00000000-0000-0000-0000-000000000000\"){name}}}", query);
  }

  @Test
  public void testBuildGetWithNearText() {
    // given
    Field name = Field.builder().name("name").build();
    NearTextArgument nearText = NearTextArgument.builder().concepts(new String[]{ "good" }).build();
    // when
    String query = GetBuilder.builder()
            .className("Pizza")
            .fields(Fields.builder().fields(new Field[]{ name }).build())
            .withNearTextFilter(nearText).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get{Pizza(nearText:{concepts:[\"good\"]}){name}}}", query);
  }

  @Test
  public void testBuildGetWithNearVector() {
    Field name = Field.builder().name("name").build();

    // given (certainty)
    NearVectorArgument nearVectorWithCert = NearVectorArgument.builder()
            .vector(new Float[]{ 0f, 1f, 0.8f }).certainty(0.8f).build();
    // when (certainty)
    String queryWithCert = GetBuilder.builder().className("Pizza")
            .fields(Fields.builder().fields(new Field[]{ name }).build())
            .withNearVectorFilter(nearVectorWithCert).build().buildQuery();
    // then (certainty)
    assertNotNull(queryWithCert);
    assertEquals("{Get{Pizza(nearVector:{vector:[0.0,1.0,0.8] certainty:0.8}){name}}}", queryWithCert);

    // given (distance)
    NearVectorArgument nearVectorWithDist = NearVectorArgument.builder()
            .vector(new Float[]{ 0f, 1f, 0.8f }).distance(0.8f).build();
    // when (distance)
    String queryWithDist = GetBuilder.builder().className("Pizza")
            .fields(Fields.builder().fields(new Field[]{ name }).build())
            .withNearVectorFilter(nearVectorWithDist).build().buildQuery();
    // then (distance)
    assertNotNull(queryWithDist);
    assertEquals("{Get{Pizza(nearVector:{vector:[0.0,1.0,0.8] distance:0.8}){name}}}", queryWithDist);
  }

  @Test
  public void testBuildGetWithGroupFilter() {
    // given
    Field name = Field.builder().name("name").build();
    GroupArgument group = GroupArgument.builder().type(GroupType.closest).force(0.4f).build();
    // when
    String query = GetBuilder.builder().className("Pizza")
            .fields(Fields.builder().fields(new Field[]{ name }).build())
            .withGroupArgument(group).build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get{Pizza(group:{type:closest force:0.4}){name}}}", query);
  }

  @Test
  public void testBuildGetWithMultipleFilter() {
    // given
    Fields fields = Fields.builder()
            .fields(new Field[]{ Field.builder().name("name").build() })
            .build();
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(new String[]{ "good" })
            .build();
    WhereArgument where = WhereArgument.builder()
      .filter(WhereFilter.builder()
        .path(new String[]{ "name" })
        .operator(Operator.Equal)
        .valueText("Hawaii")
        .build())
      .build();
    Integer limit = 2;
    // when
    String query = GetBuilder.builder()
            .className("Pizza").fields(fields).withNearTextFilter(nearText).withWhereFilter(where).limit(limit)
            .build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get{Pizza(where:{path:[\"name\"] valueText:\"Hawaii\" operator:Equal},nearText:{concepts:[\"good\"]},limit:2){name}}}", query);
  }

  @Test
  public void testBuildGetWithNearTextWithConcepts() {
    // given
    Fields fields = Fields.builder()
            .fields(new Field[]{ Field.builder().name("name").build() })
            .build();
    NearTextArgument nearText = NearTextArgument.builder()
            .concepts(new String[]{ "good" })
            .build();
    // when
    String query = GetBuilder.builder()
            .className("Pizza").fields(fields).withNearTextFilter(nearText)
            .build().buildQuery();
    // then
    assertNotNull(query);
    assertEquals("{Get{Pizza(nearText:{concepts:[\"good\"]}){name}}}", query);
  }

  @Test
  public void testBuildGetWithAskAndCertainty() {
    // given
    Fields fields = Fields.builder()
            .fields(new Field[]{ Field.builder().name("name").build() })
            .build();
    AskArgument ask1 = AskArgument.builder()
            .question("Who are you?")
            .build();
    AskArgument ask2 = AskArgument.builder()
            .question("Who are you?")
            .properties(new String[]{ "prop1", "prop2" })
            .build();
    AskArgument ask3 = AskArgument.builder()
            .question("Who are you?")
            .properties(new String[]{ "prop1", "prop2" })
            .certainty(0.1f)
            .build();
    AskArgument ask4 = AskArgument.builder()
            .question("Who are you?")
            .properties(new String[]{ "prop1", "prop2" })
            .certainty(0.1f)
            .rerank(true)
            .build();
    // when
    String query1 = GetBuilder.builder()
            .className("Pizza").fields(fields).withAskArgument(ask1)
            .build().buildQuery();
    String query2 = GetBuilder.builder()
            .className("Pizza").fields(fields).withAskArgument(ask2)
            .build().buildQuery();
    String query3 = GetBuilder.builder()
            .className("Pizza").fields(fields).withAskArgument(ask3)
            .build().buildQuery();
    String query4 = GetBuilder.builder()
            .className("Pizza").fields(fields).withAskArgument(ask4)
            .build().buildQuery();
    // then
    assertNotNull(query1);
    assertEquals("{Get{Pizza(ask:{question:\"Who are you?\"}){name}}}", query1);
    assertNotNull(query2);
    assertEquals("{Get{Pizza(ask:{question:\"Who are you?\" properties:[\"prop1\",\"prop2\"]}){name}}}", query2);
    assertNotNull(query3);
    assertEquals("{Get{Pizza(ask:{question:\"Who are you?\" properties:[\"prop1\",\"prop2\"] certainty:0.1}){name}}}", query3);
    assertNotNull(query4);
    assertEquals("{Get{Pizza(ask:{question:\"Who are you?\" properties:[\"prop1\",\"prop2\"] certainty:0.1 rerank:true}){name}}}", query4);
  }

  @Test
  public void testBuildGetWithAskAndDistance() {
    // given
    Fields fields = Fields.builder()
            .fields(new Field[]{ Field.builder().name("name").build() })
            .build();
    AskArgument ask1 = AskArgument.builder()
            .question("Who are you?")
            .build();
    AskArgument ask2 = AskArgument.builder()
            .question("Who are you?")
            .properties(new String[]{ "prop1", "prop2" })
            .build();
    AskArgument ask3 = AskArgument.builder()
            .question("Who are you?")
            .properties(new String[]{ "prop1", "prop2" })
            .distance(0.1f)
            .build();
    AskArgument ask4 = AskArgument.builder()
            .question("Who are you?")
            .properties(new String[]{ "prop1", "prop2" })
            .distance(0.1f)
            .rerank(true)
            .build();
    // when
    String query1 = GetBuilder.builder()
            .className("Pizza").fields(fields).withAskArgument(ask1)
            .build().buildQuery();
    String query2 = GetBuilder.builder()
            .className("Pizza").fields(fields).withAskArgument(ask2)
            .build().buildQuery();
    String query3 = GetBuilder.builder()
            .className("Pizza").fields(fields).withAskArgument(ask3)
            .build().buildQuery();
    String query4 = GetBuilder.builder()
            .className("Pizza").fields(fields).withAskArgument(ask4)
            .build().buildQuery();
    // then
    assertNotNull(query1);
    assertEquals("{Get{Pizza(ask:{question:\"Who are you?\"}){name}}}", query1);
    assertNotNull(query2);
    assertEquals("{Get{Pizza(ask:{question:\"Who are you?\" properties:[\"prop1\",\"prop2\"]}){name}}}", query2);
    assertNotNull(query3);
    assertEquals("{Get{Pizza(ask:{question:\"Who are you?\" properties:[\"prop1\",\"prop2\"] distance:0.1}){name}}}", query3);
    assertNotNull(query4);
    assertEquals("{Get{Pizza(ask:{question:\"Who are you?\" properties:[\"prop1\",\"prop2\"] distance:0.1 rerank:true}){name}}}", query4);
  }

  @Test
  public void testBuildGetWithNearImageAndCertainty() throws FileNotFoundException {
    // given
    File imageFile = new File("src/test/resources/image/pixel.png");
    String base64File = new BufferedReader(new InputStreamReader(new FileInputStream("src/test/resources/image/base64.txt")))
            .lines().collect(Collectors.joining("\n"));
    String image = "data:image/png;base64,iVBORw0KGgoAAAANS";
    String expectedImage = "iVBORw0KGgoAAAANS";
    NearImageArgument nearImage1 = NearImageArgument.builder().imageFile(imageFile).build();
    NearImageArgument nearImage2 = NearImageArgument.builder().imageFile(imageFile).certainty(0.4f).build();
    NearImageArgument nearImage3 = NearImageArgument.builder().image(image).certainty(0.1f).build();
    Fields fields = Fields.builder()
            .fields(new Field[]{ Field.builder().name("name").build() })
            .build();
    // when
    String query1 = GetBuilder.builder()
            .className("Pizza").fields(fields).withNearImageFilter(nearImage1)
            .build().buildQuery();
    String query2 = GetBuilder.builder()
            .className("Pizza").fields(fields).withNearImageFilter(nearImage2)
            .build().buildQuery();
    String query3 = GetBuilder.builder()
            .className("Pizza").fields(fields).withNearImageFilter(nearImage3).limit(1)
            .build().buildQuery();
    assertNotNull(query1);
    assertEquals(String.format("{Get{Pizza(nearImage:{image:\"%s\"}){name}}}", base64File), query1);
    assertNotNull(query2);
    assertEquals(String.format("{Get{Pizza(nearImage:{image:\"%s\" certainty:0.4}){name}}}", base64File), query2);
    assertNotNull(query3);
    assertEquals(String.format("{Get{Pizza(nearImage:{image:\"%s\" certainty:0.1},limit:1){name}}}", expectedImage), query3);
  }

  @Test
  public void testBuildGetWithNearImageAndDistance() throws FileNotFoundException {
    // given
    File imageFile = new File("src/test/resources/image/pixel.png");
    String base64File = new BufferedReader(new InputStreamReader(new FileInputStream("src/test/resources/image/base64.txt")))
            .lines().collect(Collectors.joining("\n"));
    String image = "data:image/png;base64,iVBORw0KGgoAAAANS";
    String expectedImage = "iVBORw0KGgoAAAANS";
    NearImageArgument nearImage1 = NearImageArgument.builder().imageFile(imageFile).build();
    NearImageArgument nearImage2 = NearImageArgument.builder().imageFile(imageFile).distance(0.4f).build();
    NearImageArgument nearImage3 = NearImageArgument.builder().image(image).distance(0.1f).build();
    Fields fields = Fields.builder()
            .fields(new Field[]{ Field.builder().name("name").build() })
            .build();
    // when
    String query1 = GetBuilder.builder()
            .className("Pizza").fields(fields).withNearImageFilter(nearImage1)
            .build().buildQuery();
    String query2 = GetBuilder.builder()
            .className("Pizza").fields(fields).withNearImageFilter(nearImage2)
            .build().buildQuery();
    String query3 = GetBuilder.builder()
            .className("Pizza").fields(fields).withNearImageFilter(nearImage3).limit(1)
            .build().buildQuery();
    assertNotNull(query1);
    assertEquals(String.format("{Get{Pizza(nearImage:{image:\"%s\"}){name}}}", base64File), query1);
    assertNotNull(query2);
    assertEquals(String.format("{Get{Pizza(nearImage:{image:\"%s\" distance:0.4}){name}}}", base64File), query2);
    assertNotNull(query3);
    assertEquals(String.format("{Get{Pizza(nearImage:{image:\"%s\" distance:0.1},limit:1){name}}}", expectedImage), query3);
  }

  @Test
  public void testBuildGetWithSort() {
    // given
    Fields fields = Fields.builder()
            .fields(new Field[]{ Field.builder().name("name").build() })
            .build();
    SortArgument sort1 = SortArgument.builder().path(new String[]{ "property1" }).build();
    SortArgument sort2 = SortArgument.builder().path(new String[]{ "property2" }).order(SortOrder.desc).build();
    SortArgument sort3 = SortArgument.builder().path(new String[]{ "property3" }).order(SortOrder.asc).build();
    // when
    String query1 = GetBuilder.builder().className("Pizza").fields(fields)
            .withSortArguments(SortArguments.builder().sort(new SortArgument[]{sort1}).build())
            .build().buildQuery();
    String query2 = GetBuilder.builder().className("Pizza").fields(fields)
            .withSortArguments(SortArguments.builder().sort(new SortArgument[]{sort1, sort2}).build())
            .build().buildQuery();
    String query3 = GetBuilder.builder().className("Pizza").fields(fields)
            .withSortArguments(SortArguments.builder().sort(new SortArgument[]{sort1, sort2, sort3}).build())
            .build().buildQuery();
    // then
    assertNotNull(query1);
    assertEquals("{Get{Pizza(sort:[{path:[\"property1\"]}]){name}}}", query1);
    assertEquals("{Get{Pizza(sort:[{path:[\"property1\"]},{path:[\"property2\"] order:desc}]){name}}}", query2);
    assertEquals("{Get{Pizza(sort:[{path:[\"property1\"]},{path:[\"property2\"] order:desc},{path:[\"property3\"] order:asc}]){name}}}", query3);
  }

  @Test
  public void testBuildGetWithConsistencyLevel() {
    // given
    Fields fields = Fields.builder()
      .fields(new Field[]{ Field.builder().name("name").build() })
      .build();
    // when
    String withAll = GetBuilder.builder().className("Pizza").fields(fields)
      .withConsistencyLevel(ConsistencyLevel.ALL)
      .build().buildQuery();
    String withQuorum = GetBuilder.builder().className("Pizza").fields(fields)
      .withConsistencyLevel(ConsistencyLevel.QUORUM)
      .build().buildQuery();
    String withOne = GetBuilder.builder().className("Pizza").fields(fields)
      .withConsistencyLevel(ConsistencyLevel.ONE)
      .build().buildQuery();
    // then
    assertEquals("{Get{Pizza(consistencyLevel:ALL){name}}}", withAll);
    assertEquals("{Get{Pizza(consistencyLevel:QUORUM){name}}}", withQuorum);
    assertEquals("{Get{Pizza(consistencyLevel:ONE){name}}}", withOne);
  }

  @Test
  public void shouldBuildGetWithGenerativeSearchAndMultipleFieldsIncludingAdditional() {
    // given
    Fields fields = Fields.builder().fields(new Field[]{
      Field.builder().name("name").build(),
      Field.builder().name("description").build(),
      Field.builder().name("_additional").fields(new Field[]{
        Field.builder().name("id").build()
      }).build()
    }).build();

    // when
    String query = GetBuilder.builder()
      .className("Pizza")
      .fields(fields)
      .withGenerativeSearch(
        GenerativeSearchBuilder.builder()
          .singleResultPrompt("What is the meaning of life?")
          .groupedResultTask("Explain why these magazines or newspapers are about finance")
          .build()
      )
      .build().buildQuery();

    // then
    assertThat(query).isEqualTo("{Get{Pizza{name description _additional{id generate(" +
      "singleResult:{prompt:\"\"\"What is the meaning of life?\"\"\"} " +
      "groupedResult:{task:\"\"\"Explain why these magazines or newspapers are about finance\"\"\"})" +
      "{singleResult groupedResult error}}}}}");
  }

  @Test
  public void shouldBuildGetWithGenerativeSearchAndMultipleFields() {
    // given
    Fields fields = Fields.builder().fields(new Field[]{
      Field.builder().name("name").build(),
      Field.builder().name("description").build()
    }).build();

    // when
    String query = GetBuilder.builder()
      .className("Pizza")
      .fields(fields)
      .withGenerativeSearch(
        GenerativeSearchBuilder.builder()
          .singleResultPrompt("What is the meaning of life?")
          .groupedResultTask("Explain why these magazines or newspapers are about finance")
          .build()
      )
      .build().buildQuery();

    // then
    assertThat(query).isEqualTo("{Get{Pizza{name description _additional{generate(" +
      "singleResult:{prompt:\"\"\"What is the meaning of life?\"\"\"} " +
      "groupedResult:{task:\"\"\"Explain why these magazines or newspapers are about finance\"\"\"})" +
      "{singleResult groupedResult error}}}}}");
  }

  @Test
  public void shouldSupportDeprecatedWhereFilter() {
    WhereFilter where = WhereFilter.builder()
      .path(new String[]{ "name" })
      .operator(Operator.Equal)
      .valueText("Hawaii")
      .build();
    Fields fields = Fields.builder()
      .fields(Field.builder().name("name").build())
      .build();
    NearTextArgument nearText = NearTextArgument.builder()
      .concepts(new String[]{ "good" })
      .build();
    Integer limit = 2;

    String query = GetBuilder.builder()
      .className("Pizza")
      .fields(fields)
      .withNearTextFilter(nearText)
      .withWhereFilter(where)
      .limit(limit)
      .build().buildQuery();

    assertThat(query).isEqualTo("{Get{Pizza(where:{path:[\"name\"] valueText:\"Hawaii\" operator:Equal},nearText:{concepts:[\"good\"]},limit:2){name}}}");
  }

  @Test
  public void testBuildGetWithGroupBy() {
    // given
    Field[] hits = new Field[]{
      Field.builder().name("prop1").build(),
      Field.builder().name("_additional{distance}").build(),
    };

    Field group = Field.builder()
      .name("group")
      .fields(new Field[]{
        Field.builder().name("groupValue").build(),
        Field.builder().name("count").build(),
        Field.builder().name("maxDistance").build(),
        Field.builder().name("minDistance").build(),
        Field.builder().name("hits").fields(hits).build(),
      }).build();

    Fields fields = Fields.builder().fields(new Field[]{
      Field.builder().name("_additional").fields(new Field[]{ group }).build()
    }).build();

    GroupByArgument groupBy1 = GroupByArgument.builder().path(new String[]{ "prop1" }).build();
    GroupByArgument groupBy2 = GroupByArgument.builder().path(new String[]{ "prop1" }).groups(1).objectsPerGroup(3).build();
    // when
    String query1 = GetBuilder.builder().className("Pizza").fields(fields)
      .withGroupByArgument(groupBy1)
      .build().buildQuery();
    String query2 = GetBuilder.builder().className("Pizza").fields(fields)
      .withGroupByArgument(groupBy2)
      .build().buildQuery();
    // then
    assertNotNull(query1);
    assertEquals("{Get{Pizza(groupBy:{path:[\"prop1\"]}){_additional{group{groupValue count maxDistance minDistance hits{prop1 _additional{distance}}}}}}}", query1);
    assertEquals("{Get{Pizza(groupBy:{path:[\"prop1\"] groups:1 objectsPerGroup:3}){_additional{group{groupValue count maxDistance minDistance hits{prop1 _additional{distance}}}}}}}", query2);
  }
}
