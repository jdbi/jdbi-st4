package com.groupon.jdbi.st4;

import org.junit.Rule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Define;
import org.skife.jdbi.v2.sqlobject.helpers.MapResultAsBean;
import org.skife.jdbi.v2.util.StringColumnMapper;

import static org.assertj.core.api.Assertions.assertThat;

public class DaoTest {

    @Rule
    public final H2Rule h2 = new H2Rule();

    @Test
    public void testSimpleStatement() throws Exception {
        final DBI dbi = new DBI(this.h2.getDataSource());
        final OuterDao dao = dbi.onDemand(OuterDao.class);
        dao.createSomething2();
        dbi.useHandle((h) -> h.execute("insert into something (id, name) values (1, 'Kyle')"));
    }

    @Test
    public void testDefineSomething() throws Exception {
        final DBI dbi = new DBI(this.h2.getDataSource());
        final OuterDao dao = dbi.onDemand(OuterDao.class);
        dao.createSomething2();
        dao.insert2("something", 1, "Carlos");

        final String name = dbi.withHandle((h) -> h.createQuery("select name from something where id = 1")
                                                   .map(StringColumnMapper.INSTANCE)
                                                   .first());
        assertThat(name).isEqualTo("Carlos");
    }

    @Test
    public void testUseImportedTemplate() throws Exception {
        final DBI dbi = new DBI(this.h2.getDataSource());
        final OuterDao dao = dbi.onDemand(OuterDao.class);
        dao.createSomething2();
        dao.insert2("something", 1, "Paul");
        final Something s = dao.findById2(1);

        assertThat(s).isEqualTo(new Something(1, "Paul"));
    }

    @Test
    public void testSimpleStatementInnerClass() throws Exception {
        final DBI dbi = new DBI(this.h2.getDataSource());
        final InnerDao dao = dbi.onDemand(InnerDao.class);
        dao.createSomething();
        dbi.useHandle((h) -> h.execute("insert into something (id, name) values (1, 'Kyle')"));
    }

    @Test
    public void testDefineSomethingInnerClass() throws Exception {
        final DBI dbi = new DBI(this.h2.getDataSource());
        final InnerDao dao = dbi.onDemand(InnerDao.class);
        dao.createSomething();
        dao.insert("something", 1, "Carlos");

        final String name = dbi.withHandle((h) -> h.createQuery("select name from something where id = 1")
                                                   .map(StringColumnMapper.INSTANCE)
                                                   .first());
        assertThat(name).isEqualTo("Carlos");
    }

    @Test
    public void testUseImportedTemplateInnerClass() throws Exception {
        final DBI dbi = new DBI(this.h2.getDataSource());
        final InnerDao dao = dbi.onDemand(InnerDao.class);
        dao.createSomething();
        dao.insert("something", 1, "Paul");
        final Something s = dao.findById(1);

        assertThat(s).isEqualTo(new Something(1, "Paul"));

    }


    @UseST4StatementLocator
    public interface InnerDao {

        @SqlUpdate
        void createSomething();

        @SqlUpdate
        void insert(@Define("table") String table, @Bind("id") int id, @Bind("name") String name);

        @SqlQuery
        @MapResultAsBean
        Something findById(@Bind("id") int id);
    }

    public static class Something {
        private int id;
        private String name;

        public Something(final int id, final String name) {
            this.id = id;
            this.name = name;
        }

        public Something() {
            // for bean mappery
        }

        public void setId(final int id) {
            this.id = id;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public int getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Something something = (Something) o;

            if (this.id != something.id) return false;
            return this.name != null ? this.name.equals(something.name) : something.name == null;

        }

        @Override
        public int hashCode() {
            int result = this.id;
            result = 31 * result + (this.name != null ? this.name.hashCode() : 0);
            return result;
        }
    }
}
