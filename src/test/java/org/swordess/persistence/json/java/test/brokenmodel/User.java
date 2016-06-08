package org.swordess.persistence.json.java.test.brokenmodel;

import org.swordess.persistence.Id;
import org.swordess.persistence.json.JsonEntity;

@JsonEntity(filename = "user")
public class User {

    private Long id;
    private String username;

    @Id
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
