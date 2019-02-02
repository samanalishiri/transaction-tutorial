package com.saman.tutorial.transaction;

import java.util.List;

public interface CrudRepository {

    Integer save(DataEntity e);

    List<DataEntity> findAll();

    DataEntity findById(Integer id);

    void update(DataEntity model);

    void delete(Integer id);
}
