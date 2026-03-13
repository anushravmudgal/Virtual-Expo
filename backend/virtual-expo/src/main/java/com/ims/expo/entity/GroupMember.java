package com.ims.expo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "group_members")
@Getter
@Setter
@NoArgsConstructor
public class GroupMember {

    @EmbeddedId
    private GroupMemberId id = new GroupMemberId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId") // Maps back to the embedded ID
    @JoinColumn(name = "group_id")
    @JsonIgnore
    private StudentGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId") // Maps back to the embedded ID
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "project_role")
    private String projectRole;
}