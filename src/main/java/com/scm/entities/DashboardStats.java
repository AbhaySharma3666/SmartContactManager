package com.scm.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private long totalContacts;
    private long favoriteContacts;
    private long totalGroups;
    private List<Contact> recentContacts;
}
