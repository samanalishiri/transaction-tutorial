package com.saman.transaction;

import java.sql.ResultSet;

/**
 * @author Saman Alishiri, samanalishiri@gmail.com
 *
 */
public interface Transformer<T> {

    T transform(ResultSet data);
}
