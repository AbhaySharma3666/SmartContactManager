package com.scm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.scm.entities.UserRole;

@Repository
public interface UserRoleRepo extends JpaRepository<UserRole, Long> {
}
