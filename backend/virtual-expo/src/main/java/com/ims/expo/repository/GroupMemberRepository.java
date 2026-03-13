package com.ims.expo.repository;

import com.ims.expo.entity.GroupMember;
import com.ims.expo.entity.GroupMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, GroupMemberId> {
}