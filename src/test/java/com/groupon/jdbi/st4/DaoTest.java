package com.groupon.jdbi.st4;

import org.junit.Rule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import static org.assertj.core.api.Assertions.assertThat;

public class DaoTest {

    @Rule
    public final H2Rule h2 = new H2Rule();

    @Test
    public void testFoo() throws Exception {
        final DBI dbi = new DBI(this.h2.getDataSource());
        final Dao dao = dbi.onDemand(Dao.class);
        dao.createSomething();
        dao.insertSomething(1, "Carlos");
        final String name = dao.findNameById(1);

        assertThat(name).isEqualTo("Carlos");
    }

    @UseST4StatementLocator
    public interface Dao {

        @SqlUpdate
        void createSomething();

        @SqlUpdate
        void insertSomething(@Bind("id") int id, @Bind("name") String name);

        @SqlQuery
        String findNameById(@Bind("id") int id);
    }
}
