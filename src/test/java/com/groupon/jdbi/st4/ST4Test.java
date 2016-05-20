package com.groupon.jdbi.st4;

import org.junit.Test;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class ST4Test {

    @Test
    public void testFoo() throws Exception {
        final URL stg = ST4Test.class.getResource("st4behavior.stg");
        final STGroup group = new STGroupFile(stg, "UTF-8", '<', '>');
        final ST st = group.getInstanceOf("foo");
        st.add("name", "Brian");
        final String out = st.render();
        assertThat(out).isEqualTo("hello (: Brian :)");
    }
}
