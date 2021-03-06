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
package org.jdbi.v2.st4;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.StatementLocator;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class ST4StatementLocator implements StatementLocator {

    private static final ConcurrentMap<String, STGroup> CACHE = new ConcurrentHashMap<>();

    private final Function<StatementContext, STGroup> group;

    public ST4StatementLocator(final STGroup group) {
        this((_ctx) -> group);
    }

    public ST4StatementLocator(final Function<StatementContext, STGroup> groupProvider) {
        this.group = groupProvider;
    }

    @Override
    public String locate(final String name, final StatementContext ctx) throws Exception {
        ST st = this.group.apply(ctx).getInstanceOf(name);
        if (st == null) {
            // if there is no template by this name in the group, treat it as a template literal.
            st = new ST(name);
        }

        // we add all context values, ST4 explodes if you add a value that lacks a formal argument,
        // iff hasFormalArgs is true. If it is false, it just uses values opportunistically. This is gross
        // but works. -brianm
        st.impl.hasFormalArgs = false;

        for (final Map.Entry<String, Object> attr : ctx.getAttributes().entrySet()) {
            st.add(attr.getKey(), attr.getValue());
        }

        return st.render();
    }

    /**
     * Obtains a locator based on a classpath path, using a global template group CACHE.
     */
    public static StatementLocator fromClasspath(String path) {
        return fromClasspath(UseSTGroupCache.YES, path);
    }

    /**
     * Obtains a locator based on a classpath path. Allows flag to indicate whether the global STGroup CACHE should be
     * used. In general, the only reasons to NOT use the CACHE are: (1) if you are fiddling with the templates during development
     * and want to make changes without having to restart the server; and (2) if you use something like the tomcat
     * deployer to push new versions without restarting the JVM which causes static maps to leak memory.
     */
    public static StatementLocator fromClasspath(UseSTGroupCache useCache, String path) {
        return forURL(useCache, ST4StatementLocator.class.getResource(path));
    }

    /**
     * Obtains a locator based on the type passed in, using a global template group CACHE.
     * <p>
     * STGroup is loaded from the classpath via sqlObjectType.getResource( ), such that names line
     * up with the package and class, so: com.example.Foo will look for /com/example/Foo.sql.stg . Inner classes
     * are seperated in the file name by a '.' not a '$', so com.example.Foo.Bar (Bar is an inner class of Foo) would
     * be at /com/example/Foo.Bar.sql.stg .
     */
    public static StatementLocator forType(final Class sqlObjectType) {
        return forType(UseSTGroupCache.YES, sqlObjectType);
    }

    /**
     * Obtains a locator based on the type passed in. Allows flag to indicate whether the global STGroup CACHE should be
     * used. In general, the only reasons to NOT use the CACHE are: (1) if you are fiddling with the templates during development
     * and want to make changes without having to restart the server; and (2) if you use something like the tomcat
     * deployer to push new versions without restarting the JVM which causes static maps to leak memory.
     * <p>
     * STGroup is loaded from the classpath via sqlObjectType.getResource( ), such that names line
     * up with the package and class, so: com.example.Foo will look for /com/example/Foo.sql.stg . Inner classes
     * are seperated in the file name by a '.' not a '$', so com.example.Foo.Bar (Bar is an inner class of Foo) would
     * be at /com/example/Foo.Bar.sql.stg .
     */
    public static StatementLocator forType(UseSTGroupCache useCache, final Class sqlObjectType) {
        return forURL(useCache, classToUrl(sqlObjectType));
    }

    public static StatementLocator forURL(UseSTGroupCache useCache, URL url) {
        final STGroup stg;
        if (useCache == UseSTGroupCache.YES) {
            stg = CACHE.computeIfAbsent(url.toString(), ST4StatementLocator::urlToSTGroup);
        }
        else {
            stg = urlToSTGroup(url.toString());
        }

        return new ST4StatementLocator(stg);
    }

    /**
     * Create a statement locator intended for setting on a DBI or Handle instance which will
     * lookup a template group to use based on the name of the sql object type for a particular query, using
     * the same logic as {@link UseST4StatementLocator}.
     */
    public static StatementLocator perType(UseSTGroupCache useCache) {
        return perType(useCache, new STGroup('<', '>'));
    }

    /**
     * Create a statement locator intended for setting on a DBI or Handle instance which will
     * lookup a template group to use based on the name of the sql object type for a particular query, using
     * the same logic as {@link UseST4StatementLocator}.
     * <p>
     * Supports a fallback template group for statements created not using a sql object.
     */
    public static StatementLocator perType(UseSTGroupCache useCache, String fallbackTemplateGroupPath) {
        return perType(useCache, ST4StatementLocator.class.getResource(fallbackTemplateGroupPath));
    }

    /**
     * Create a statement locator intended for setting on a DBI or Handle instance which will
     * lookup a template group to use based on the name of the sql object type for a particular query, using
     * the same logic as {@link UseST4StatementLocator}.
     * <p>
     * Supports a fallback template group for statements created not using a sql object.
     */
    public static StatementLocator perType(UseSTGroupCache useCache, URL baseTemplate) {
        return perType(useCache, urlToSTGroup(baseTemplate));
    }

    /**
     * Create a statement locator intended for setting on a DBI or Handle instance which will
     * lookup a template group to use based on the name of the sql object type for a particular query, using
     * the same logic as {@link UseST4StatementLocator}.
     * <p>
     * Supports a fallback template group for statements created not using a sql object.
     */
    public static StatementLocator perType(UseSTGroupCache useCache, STGroup fallbackTemplateGroup) {
        final StatementLocator fallback = new ST4StatementLocator(fallbackTemplateGroup);

        if (useCache == UseSTGroupCache.YES) {
            final ConcurrentMap<Class<?>, StatementLocator> sqlObjectCache = new ConcurrentHashMap<>();
            return (name, ctx) -> {
                if (ctx.getSqlObjectType() != null) {
                    StatementLocator sl = sqlObjectCache.computeIfAbsent(ctx.getSqlObjectType(), (c) -> {
                        return ST4StatementLocator.forType(useCache, ctx.getSqlObjectType());
                    });
                    return sl.locate(name, ctx);
                }
                else {
                    return fallback.locate(name, ctx);
                }
            };
        }
        else {
            // if we are not caching, let's not cache the lookup of the template group either!
            return (name, ctx) -> {
                if (ctx.getSqlObjectType() != null) {
                    return ST4StatementLocator.forType(useCache, ctx.getSqlObjectType()).locate(name, ctx);
                }
                else {
                    return fallback.locate(name, ctx);
                }
            };
        }
    }


    public enum UseSTGroupCache {
        YES, NO
    }

    private static URL classToUrl(Class c) {
        // handle naming of inner classes as Outer.Inner.sql.stg instead of Outer$Inner.sql.stg
        final String fullName = c.getName();
        final String pkg = c.getPackage().getName();
        final String className = fullName.substring(pkg.length() + 1, fullName.length()).replace('$', '.');
        return c.getResource(className + ".sql.stg");
    }

    private static STGroup urlToSTGroup(String u) {
        try {
            return urlToSTGroup(new URL(u));
        } catch (MalformedURLException e) {
            throw new IllegalStateException("a URL failed to roundtrip from a string!", e);
        }
    }

    private static STGroup urlToSTGroup(URL u) {
        return new STGroupFile(u, "UTF-8", '<', '>');

    }
}
