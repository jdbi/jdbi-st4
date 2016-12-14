# JDBI-ST4

Provides an easy way to externalize SQL statements for JDBI in
[StringTemplate 4](https://github.com/antlr/stringtemplate4) [StringTemplate Group Files](https://github.com/antlr/stringtemplate4/blob/master/doc/groups.md).

# Usage


## SQL Object API 

This library is typically used to externalize SQL into StringTemplate Group Files. Take the following example Dao:

```java
package org.jdbi.st4;

@UseST4StatementLocator
public interface Dao {

    @SqlUpdate
    void createSomethingTable();

    @SqlUpdate
    int insertSomething(int id, String name);

    @SqlQuery
    @MapResultAsBean
    Something findById(int id, @Define("columns") String... columns);

    @SqlQuery("select concat('Hello, ', name, '!') from something where id = :0")
    String findGreetingFor(int id);
}
```

The `@UseST4StatementLocator` annotation tells JDBI to use the `ST4StatementLocator` for this object. By default, it
looks for a stringtemplate group file on the classpath at `/com/example/Dao.sql.stg`. Basically, it looks for 
`<ClassName>.sql.stg` in the same package as `<ClassName>`. With a maven project, you'd achieve this by putting it in `src/main/resources/com/example/<ClassName>.sq.stg`.
 
If we look at the stringtemplate group file:

```
createSomethingTable() ::= <%
    create table something (id int primary key, name varchar)
%>

insertSomething() ::= <%
    insert into something (id, name) values (:0, :1)
%>

findById(columns) ::= <%
    select < columns; separator="," > from something where id = :0
%>
```

We can then exercise it:

```java
DBI dbi = new DBI(h2);
Dao dao = dbi.onDemand(Dao.class);

dao.createSomethingTable();
dao.insertSomething(7, "Jan");
dao.insertSomething(1, "Brian");

Something jan = dao.findById(7, "id", "name");
assertThat(jan.getId()).as("Jan's ID").isEqualTo(7);
assertThat(jan.getName()).as("Jan's Name").isEqualTo("Jan");

Something partial = dao.findById(7, "name");
assertThat(partial.getId()).as("Jan's ID").isEqualTo(0); // default int value
assertThat(partial.getName()).as("Jan's Name").isEqualTo("Jan");

String greeting = dao.findGreetingFor(7);
assertThat(greeting).isEqualTo("Hello, Jan!");

```

We find a template defined for each method. You can override this by naming the template to use in the `@SqlUpdate`
or `@SqlQuery` annotation. For example, using `@SqlQuery("woof")` would look for a template named `woof` in that group.

For the `findById(columns)` template, we need to pass in the columns we want to template into the SQL. We make them
available to the template by putting them on the `StatementContext` in JDBI. The easiest way to do this is with the 
`@Define` annotation on another parameter, which we do here. However, values set on the `Handle` or `DBI` will also
be available.

Finally, note that if a template is not found (such as for `findGreetingFor` the "name" (sql literal in this case))
is compiled as a string template and evaluated.

## Fluent API

We can use the same mechanisms with the fluent api, though in this case we have to tell JDBI where to 
find the stringtemplate group, as there is no object to use as the basis:

```java
DBI dbi = new DBI(h2);
dbi.setStatementLocator(ST4StatementLocator.fromClasspath("/org/jdbi/st4/ExampleTest.Dao.sql.stg"));

dbi.useHandle((h) -> {
    h.execute("createSomethingTable");

    int numCreated = h.createStatement("insertSomething")
                      .bind("0", 0)
                      .bind("1", "Jan")
                      .execute();
    assertThat(numCreated).as("number of rows inserted").isEqualTo(1);

    String name = h.createQuery("findById")
                  .bind("0", 0)
                  .define("columns", "name")
                  .mapTo(String.class)
                  .first();
    assertThat(name).as("Jan's Name").isEqualTo("Jan");
});

```

In this case we set the template group as the same one from the sql object example. There are a few ways to specify
which to use, check out the factory methods and their javadocs on `ST4StatementLocator` to explore. 



# License

[Apache License 2.0](LICENSE)
