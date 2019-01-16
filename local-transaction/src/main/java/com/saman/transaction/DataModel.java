package com.saman.transaction;

import java.util.Objects;

public class DataModel {

    public static final int ID_INDEX = 1;
    public static final int CODE_INDEX = 2;
    public static final int NAME_INDEX = 3;

    private int id;
    private String code;
    private String name;

    public int getId() {
        return id;
    }

    public static DataModel create(int id, String code, String name){
        DataModel model = new DataModel();
        model.setId(id);
        model.setCode(code);
        model.setName(name);
        return model;
    }

    public void setId(int id) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataModel dataModel = (DataModel) o;
        return id == dataModel.id &&
                Objects.equals(code, dataModel.code) &&
                Objects.equals(name, dataModel.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, name);
    }

    @Override
    public String toString() {
        return "DataModel{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
