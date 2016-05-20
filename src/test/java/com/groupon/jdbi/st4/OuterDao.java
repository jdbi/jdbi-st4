package com.groupon.jdbi.st4;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Define;
import org.skife.jdbi.v2.sqlobject.helpers.MapResultAsBean;

@UseST4StatementLocator
public interface OuterDao {

    @SqlUpdate
    void createSomething2();

    @SqlUpdate
    void insert2(@Define("table") String table, @Bind("id") int id, @Bind("name") String name);

    @SqlQuery
    @MapResultAsBean
    DaoTest.Something findById2(@Bind("id") int id);
}
