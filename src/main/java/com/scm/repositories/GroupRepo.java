package com.scm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.scm.entities.ContactGroup;
import com.scm.entities.User;
import java.util.List;

@Repository
public interface GroupRepo extends JpaRepository<ContactGroup, String> {
    List<ContactGroup> findByUser(User user);
    long countByUser(User user);
}
