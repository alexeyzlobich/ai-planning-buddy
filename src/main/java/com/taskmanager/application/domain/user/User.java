package com.taskmanager.application.domain.user;

import com.taskmanager.application.domain.shared.Entity;
import com.taskmanager.application.domain.user.valueobject.UserId;

public class User implements Entity<UserId> {

    private UserId id;

    public UserId getId() {
        return id;
    }
}
