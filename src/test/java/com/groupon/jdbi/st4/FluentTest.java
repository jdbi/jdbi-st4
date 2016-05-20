package com.groupon.jdbi.st4;

import org.junit.Rule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.stringtemplate.v4.STGroupFile;

import static org.assertj.core.api.Assertions.assertThat;

public class FluentTest {

    @Rule
    public H2Rule h2 = new H2Rule();

    @Test
    public void testFoo() throws Exception {
        final DBI dbi = new DBI(this.h2.getDataSource());
        dbi.setStatementLocator(new ST4StatementLocator(new STGroupFile("com/groupon/jdbi/st4/DaoTest.InnerDao.sql.stg")));
        dbi.useHandle((h) -> {
            h.execute("createSomething");
            h.createStatement("insert")
             .define("table", "something")
             .bind("id", 1)
             .bind("name", "Ven")
             .execute();

            final Something s = h.createQuery("findById")
                                 .bind("id", 1)
                                 .map((index, r, ctx) -> new Something(r.getInt("id"), r.getString("name")))
                                 .first();
            assertThat(s).isEqualTo(new Something(1, "Ven"));
        });
    }
}
