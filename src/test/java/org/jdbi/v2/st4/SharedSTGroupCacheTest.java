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

import org.junit.Test;
import org.mockito.Mockito;
import org.skife.jdbi.v2.StatementContext;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class SharedSTGroupCacheTest {
    @Test
    public void testUseAndDontUseSTGroupCache() throws Exception {
        String sql;
        Path tmp = Files.createTempFile("test", ".stg");

        StatementContext ctx = Mockito.mock(StatementContext.class);

        // first write, and loaded into the cache
        Files.write(tmp, "test() ::= <<chirp>>".getBytes(StandardCharsets.UTF_8));
        sql = ST4StatementLocator.forURL(ST4StatementLocator.UseSTGroupCache.YES, tmp.toUri().toURL())
                                 .locate("test", ctx);
        assertThat(sql).isEqualTo("chirp");

        // change the template, but use cache which should not see changes
        Files.write(tmp, "test() ::= <<ribbit>>".getBytes(StandardCharsets.UTF_8));
        sql = ST4StatementLocator.forURL(ST4StatementLocator.UseSTGroupCache.YES, tmp.toUri().toURL())
                                 .locate("test", ctx);
        assertThat(sql).isEqualTo("chirp");

        // change the template and don't use cache, which should load changes
        Files.write(tmp, "test() ::= <<meow>>".getBytes(StandardCharsets.UTF_8));
        sql = ST4StatementLocator.forURL(ST4StatementLocator.UseSTGroupCache.NO, tmp.toUri().toURL())
                                 .locate("test", ctx);
        assertThat(sql).isEqualTo("meow");

        // change template again, don't use cache again, we should see the change
        Files.write(tmp, "test() ::= <<woof>>".getBytes(StandardCharsets.UTF_8));
        sql = ST4StatementLocator.forURL(ST4StatementLocator.UseSTGroupCache.NO, tmp.toUri().toURL())
                                 .locate("test", ctx);
        assertThat(sql).isEqualTo("woof");
    }
}
