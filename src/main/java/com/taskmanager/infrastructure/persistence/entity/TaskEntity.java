package com.taskmanager.infrastructure.persistence.entity;

import com.mongodb.lang.NonNull;
import lombok.Getter;
import lombok.Setter;
import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonRepresentation;
import org.bson.types.ObjectId;

@Getter
@Setter
public class TaskEntity {

    @BsonId
    private ObjectId id;  // MongoDB auto-generates ID

    @NonNull
    @BsonRepresentation(BsonType.OBJECT_ID)
    private String userId;

    @NonNull
    private String title;

    private String description;

    @NonNull
    private String status;

}
