package com.scm.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.scm.entities.Contact;
import com.scm.entities.DashboardStats;
import com.scm.entities.User;
import com.scm.repositories.ContactRepo;
import com.scm.repositories.GroupRepo;
import com.scm.services.DashboardService;

import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private ContactRepo contactRepo;

    @Autowired
    private GroupRepo groupRepo;

    @Override
    public DashboardStats getDashboardStats(User user) {
        long totalContacts = contactRepo.countByUser(user);
        long favoriteContacts = contactRepo.countByUserAndFavorite(user, true);
        long totalGroups = groupRepo.countByUser(user);
        
        List<Contact> recentContacts = contactRepo.findTop5ByUserOrderByIdDesc(user);
        
        return new DashboardStats(totalContacts, favoriteContacts, totalGroups, recentContacts);
    }
}
