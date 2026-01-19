package com.scm.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Feedback {
    
    @Id
    private String feedbackId;
    
    @Column(nullable = false)
    private String subject;
    
    @Column(length = 2000, nullable = false)
    private String message;
    
    private int rating;
    
    private LocalDateTime createdAt;
    
    @ManyToOne
    private User user;
}
