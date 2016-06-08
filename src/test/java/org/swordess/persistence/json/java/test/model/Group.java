package org.swordess.persistence.json.java.test.model;

import org.swordess.persistence.Id;
import org.swordess.persistence.json.JsonEntity;

@JsonEntity(filename = "group")
public class Group {

    private Long id;

    @Id
    public Long isId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
