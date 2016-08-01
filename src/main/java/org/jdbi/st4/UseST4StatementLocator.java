package org.jdbi.st4;

import org.skife.jdbi.v2.sqlobject.SqlStatementCustomizer;
import org.skife.jdbi.v2.sqlobject.SqlStatementCustomizerFactory;
import org.skife.jdbi.v2.sqlobject.SqlStatementCustomizingAnnotation;
import org.skife.jdbi.v2.tweak.StatementLocator;

import java.lang.annotation.*;
import java.lang.reflect.Method;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@SqlStatementCustomizingAnnotation(UseST4StatementLocator.LocatorFactory.class)
public @interface UseST4StatementLocator {

    class LocatorFactory implements SqlStatementCustomizerFactory {

        @Override
        public SqlStatementCustomizer createForMethod(final Annotation annotation,
                                                      final Class sqlObjectType,
                                                      final Method method) {
            return q -> {
                final StatementLocator locator = ST4StatementLocator.instance(sqlObjectType);
                q.setStatementLocator(locator);
            };
        }

        @Override
        public SqlStatementCustomizer createForType(final Annotation annotation, final Class sqlObjectType) {
            return q -> {
                final StatementLocator locator = ST4StatementLocator.instance(sqlObjectType);
                q.setStatementLocator(locator);
            };
        }

        @Override
        public SqlStatementCustomizer createForParameter(final Annotation annotation,
                                                         final Class sqlObjectType,
                                                         final Method method,
                                                         final Object arg) {
            throw new UnsupportedOperationException("Annotation cannot be applied to parameter");
        }
    }
}
