package org.jdbi.st4;

import org.junit.Rule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Define;
import org.skife.jdbi.v2.sqlobject.helpers.MapResultAsBean;

import static org.assertj.core.api.Assertions.assertThat;

public class ExampleTest {
    @Rule
    public H2Rule h2 = new H2Rule();


    @Test
    public void testFluent() throws Exception {
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
    }

    @Test
    public void testDao() throws Exception {
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
    }

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
}
