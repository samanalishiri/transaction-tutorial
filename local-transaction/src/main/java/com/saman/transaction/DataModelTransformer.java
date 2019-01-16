package com.saman.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.saman.transaction.DataModel.CODE_INDEX;
import static com.saman.transaction.DataModel.ID_INDEX;
import static com.saman.transaction.DataModel.NAME_INDEX;

/**
 * @author Saman Alishiri, samanalishiri@gmail.com
 */
public class DataModelTransformer implements Transformer<DataModel> {

    private final Logger logger = LoggerFactory.getLogger("DataModelTransformer");

    @Override
    public DataModel transform(ResultSet data) {
        DataModel model = new DataModel();

        try {
            model.setId(data.getInt(ID_INDEX));
            model.setCode(data.getString(CODE_INDEX));
            model.setName(data.getString(NAME_INDEX));

        } catch (SQLException e) {
            logger.error("can't transform DataModel");
        }

        return model;
    }
}
