package com.taskmanager.application;

import com.taskmanager.application.domain.task.Task;
import com.taskmanager.application.domain.task.valueobject.TaskDescription;
import com.taskmanager.application.domain.task.valueobject.TaskTitle;
import com.taskmanager.application.domain.user.valueobject.UserId;

public class Sample {

    public static Task task() {
        return task("I need todo something");
    }

    public static Task task(String title) {
        Task task = new Task(TaskTitle.from(title), UserId.from("00000000f6b5a229daa5525d"));
        task.setDescription(TaskDescription.from("But I don't know what"));
        return task;
    }
}
