package com.mewlog.service.invitation.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;


@Data
@Document(collection = "invitations")
public class Invitation {
    @Id
    String token;
    String animalId;

    public Invitation(String animalId, String token) {
        this.animalId = animalId;
        this.token = token;
    }
}
