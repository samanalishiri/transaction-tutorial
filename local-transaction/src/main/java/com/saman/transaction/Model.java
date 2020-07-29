package com.saman.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Try;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class Model {

    public static final int ID_INDEX = 1;

    public static final int CODE_INDEX = 2;

    public static final int NAME_INDEX = 3;

    @JsonIgnore
    protected final ObjectMapper objectMapper = new ObjectMapper();

    private final int id;

    private final String code;

    private final String name;

    private Model(Builder builder) {
        this.id = requireNonNull(builder.id, "id is null");
        this.code = requireNonNull(builder.code, "code is null");
        this.name = requireNonNull(builder.name, "name is null");
    }

    public static Builder persistenceBuilder(Integer id) {
        return new Builder(id);
    }

    public static Builder transientBuilder() {
        return new Builder(0);
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Model model = (Model) o;
        return id == model.id &&
                Objects.equals(code, model.code) &&
                Objects.equals(name, model.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, name);
    }

    @Override
    public String toString() {
        return Try.of(() -> objectMapper.writeValueAsString(this)).get();
    }

    public static class Builder {

        private Integer id;

        private String code;

        private String name;

        public Builder(Integer id) {
            this.id = id;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder of(Model model) {
            this.code = model.code;
            this.name = model.name;
            return this;
        }

        public Model build() {
            return new Model(this);
        }
    }
}
