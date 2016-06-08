package org.swordess.persistence.json.java.test.model;

import org.swordess.persistence.Id;
import org.swordess.persistence.json.JsonEntity;

@JsonEntity(filename = "group")
public class Group {

    private Long id;
    private String name;

    @Id
    public Long isId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
