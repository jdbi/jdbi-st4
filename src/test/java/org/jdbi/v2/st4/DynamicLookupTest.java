package org.jdbi.v2.st4;

import org.junit.Rule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.IntegerColumnMapper;
import org.stringtemplate.v4.STGroupString;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DynamicLookupTest {

    @Rule
    public H2Rule h2 = new H2Rule();

    @Test
    public void testFancyDynamicTemplateLookups() throws Exception {
        final DBI dbi = new DBI(h2);
        dbi.setStatementLocator(new ST4StatementLocator((ctx) -> new STGroupString(ctx.getAttribute("template")
                                                                                      .toString())));

        try (Handle h = dbi.open()) {
            h.define("template", "create(name) ::= <% create table <name> (id int primary key) %>");

            h.createStatement("create")
             .define("name", "something")
             .execute();

            h.execute("insert into something (id) values (1)");

            final List<Integer> ids = h.createQuery("select")
                                       .define("template", "select() ::= <% select id from something %>")
                                       .map(IntegerColumnMapper.PRIMITIVE)
                                       .list();
            assertThat(ids).containsExactly(1);
        }
    }
}
