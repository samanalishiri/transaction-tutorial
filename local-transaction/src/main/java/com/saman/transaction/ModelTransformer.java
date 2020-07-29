package com.saman.transaction;

import io.vavr.control.Try;

import java.sql.ResultSet;
import java.util.function.Function;

import static com.saman.transaction.Model.CODE_INDEX;
import static com.saman.transaction.Model.ID_INDEX;
import static com.saman.transaction.Model.NAME_INDEX;

/**
 * @author Saman Alishiri, samanalishiri@gmail.com
 */
public class ModelTransformer implements Function<ResultSet, Model> {

    @Override
    public Model apply(ResultSet tuple) {
        return Try.of(() -> Model.persistenceBuilder(tuple.getInt(ID_INDEX))
                .code(tuple.getString(CODE_INDEX))
                .name(tuple.getString(NAME_INDEX))
                .build())
                .get();
    }
}
