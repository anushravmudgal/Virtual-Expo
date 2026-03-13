package com.ims.expo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode // Required for composite keys!
public class GroupMemberId implements Serializable {

    @Column(name = "group_id")
    private String groupId;

    @Column(name = "user_id")
    private String userId;
}