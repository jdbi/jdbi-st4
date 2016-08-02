package org.jdbi.st4;

import org.junit.Rule;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.stringtemplate.v4.STGroupString;

public class DynamicLookupTest {

    @Rule
    public H2Rule h2 = new H2Rule();

    @Test
    public void testFoo() throws Exception {
        final DBI dbi = new DBI(this.h2.getDataSource());
        dbi.setStatementLocator(new ST4StatementLocator((ctx) -> new STGroupString(ctx.getAttribute("template")
                                                                                      .toString())));

        try (Handle h = dbi.open()) {
            h.define("template", "create() ::= <<  create table <name> (id int primary key) >>");
            h.define("name", "something");
            h.execute("create");
        }
    }

}
