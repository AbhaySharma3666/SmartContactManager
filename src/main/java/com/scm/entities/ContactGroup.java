package com.scm.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contact_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactGroup {
    
    @Id
    private String groupId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
