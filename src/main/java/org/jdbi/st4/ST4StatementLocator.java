/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jdbi.st4;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.StatementLocator;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ST4StatementLocator implements StatementLocator {

    private final STGroup group;
    private static final ConcurrentMap<Class, STGroup> groups = new ConcurrentHashMap<>();

    public ST4StatementLocator(final STGroup group) {
        this.group = group;
    }

    @Override
    public String locate(final String name, final StatementContext ctx) throws Exception {
        ST st = this.group.getInstanceOf(name);
        if (st == null) {
            st = new ST(name);
        }

        for (final Map.Entry<String, Object> attr : ctx.getAttributes().entrySet()) {
            st.add(attr.getKey(), attr.getValue());
        }

        return st.render();
    }

    public static StatementLocator instance(final Class sqlObjectType) {

        if (groups.containsKey(sqlObjectType)) {
            return new ST4StatementLocator(groups.get(sqlObjectType));
        }

        // handle naming of inner classes as Outer.Inner.sql.stg instead of Outer$Inner.sql.stg
        final String fullName = sqlObjectType.getName();
        final String pkg = sqlObjectType.getPackage().getName();
        final String className = fullName.substring(pkg.length() + 1, fullName.length())
                                         .replace('$', '.');

        final URL url = sqlObjectType.getResource(className + ".sql.stg");
        final STGroup group = new STGroupFile(url, "UTF-8", '<', '>');
        final STGroup assoc = groups.putIfAbsent(sqlObjectType, group);
        if (assoc != null) {
            return new ST4StatementLocator(assoc);
        }
        else {
            return new ST4StatementLocator(group);
        }

    }
}
