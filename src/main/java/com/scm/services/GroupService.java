package com.scm.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scm.entities.Contact;
import com.scm.entities.ContactGroup;
import com.scm.entities.GroupMember;
import com.scm.entities.User;
import com.scm.repositories.ContactRepo;
import com.scm.repositories.GroupMemberRepo;
import com.scm.repositories.GroupRepo;

@Service
public class GroupService {

    @Autowired
    private GroupRepo groupRepo;

    @Autowired
    private GroupMemberRepo memberRepo;
    
    @Autowired
    private ContactRepo contactRepo;

    public ContactGroup createGroup(String name, String desc, User user){
        ContactGroup g = new ContactGroup();
        g.setGroupId(UUID.randomUUID().toString());
        g.setName(name);
        g.setDescription(desc);
        g.setCreatedAt(LocalDateTime.now());
        g.setUser(user);
        return groupRepo.save(g);
    }

    public List<ContactGroup> getUserGroups(User user){
        return groupRepo.findByUser(user);
    }

    public long getMemberCount(String groupId){
        return memberRepo.countByGroup_GroupId(groupId);
    }

    public void deleteGroup(String groupId){
        groupRepo.deleteById(groupId);
    }
    
    public Optional<ContactGroup> getGroupById(String groupId){
        return groupRepo.findById(groupId);
    }
    
    public ContactGroup updateGroup(ContactGroup group){
        return groupRepo.save(group);
    }
    
    @Transactional
    public void addMember(String groupId, String contactId){
        ContactGroup group = groupRepo.findById(groupId).orElseThrow();
        Contact contact = contactRepo.findById(contactId).orElseThrow();
        
        if(!memberRepo.existsByGroup_GroupIdAndContact_Id(groupId, contactId)){
            GroupMember member = new GroupMember();
            member.setGroup(group);
            member.setContact(contact);
            member.setAddedAt(LocalDateTime.now());
            memberRepo.save(member);
        }
    }
    
    @Transactional
    public void removeMember(String groupId, String contactId){
        memberRepo.deleteByGroup_GroupIdAndContact_Id(groupId, contactId);
    }
    
    public List<GroupMember> getGroupMembers(String groupId){
        return memberRepo.findByGroup_GroupId(groupId);
    }
}
