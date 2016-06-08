package org.swordess.persistence.json.java.test.brokenmodel;

import org.swordess.persistence.Id;
import org.swordess.persistence.json.JsonEntity;

@JsonEntity(filename = "group")
public class Group {

    private Long id;

    @Id
    public Long fetchId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
