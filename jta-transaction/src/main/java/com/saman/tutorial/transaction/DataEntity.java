package com.saman.tutorial.transaction;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Saman Alishiri, samanalishiri@gmail.com
 */
@Entity
@Table(name = "jta_transaction_test", schema = "transaction")
public class DataEntity {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    public DataEntity() {
    }

    public static DataEntity create(int id, String code, String name) {
        DataEntity model = new DataEntity();
        model.setId(id);
        model.setCode(code);
        model.setName(name);
        return model;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
