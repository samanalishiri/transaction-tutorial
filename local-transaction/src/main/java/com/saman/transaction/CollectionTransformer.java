package com.saman.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class CollectionTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionTransformer.class.getSimpleName());

    private CollectionTransformer() {
    }

    public static <R> List<R> transform(ResultSet tuples, Function<ResultSet, R> transformer) {
        List<R> models = new ArrayList<>();

        try {
            while (tuples.next())
                models.add(transformer.apply(tuples));

        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }

        return models;
    }
}
