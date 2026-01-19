package com.scm.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.scm.entities.Feedback;
import com.scm.entities.User;
import java.util.List;

@Repository
public interface FeedbackRepo extends JpaRepository<Feedback, String> {
    List<Feedback> findByUser(User user);
}
