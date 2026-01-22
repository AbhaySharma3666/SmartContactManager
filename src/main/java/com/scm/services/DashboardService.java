package com.scm.services;

import com.scm.entities.DashboardStats;
import com.scm.entities.User;

public interface DashboardService {
    DashboardStats getDashboardStats(User user);
}
