package com.saman.tutorial.transaction;

import java.util.List;

public interface Repository {

    Integer save(DataEntity e);

    List<DataEntity> findAll();

    DataEntity findById(Integer id);

    void update(DataEntity e);

    void delete(Integer id);

    Long countAll();

    void truncate();

}
