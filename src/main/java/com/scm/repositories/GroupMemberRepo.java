package com.scm.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.scm.entities.GroupMember;

@Repository
public interface GroupMemberRepo extends JpaRepository<GroupMember, String> {

    long countByGroup_GroupId(String groupId);

    List<GroupMember> findByGroup_GroupId(String groupId);

    boolean existsByGroup_GroupIdAndContact_Id(String groupId, String contactId);
    
    void deleteByGroup_GroupIdAndContact_Id(String groupId, String contactId);
}