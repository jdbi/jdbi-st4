package org.jdbi.st4;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Something {
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

        return new EqualsBuilder().append(this.id, something.id)
                                  .append(this.name, something.name)
                                  .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(this.id)
                                          .append(this.name)
                                          .toHashCode();
    }
}
