package com.example.agentplatform.backend.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "agents")
@Data
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Lob // Large Object for potentially long descriptions
    private String description;

    @Column(name = "current_version_tag") // To store the tag of the active version, e.g., "v1.2"
    private String currentVersionTag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "derived_from_agent_id")
    private Agent derivedFromAgent; // Self-referencing for lineage

    @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AgentVersion> versions;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
