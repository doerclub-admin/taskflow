package com.example.agentplatform.backend.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
@Data
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Lob
    private String description;

    @Column(nullable = false)
    private String status = "CREATED"; // Default status: CREATED, RUNNING, STOPPED, PAUSED, COMPLETED, FAILED

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "project_agents",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "agent_id")
    )
    @ToString.Exclude // Avoid issues with bidirectional relationships in toString
    @EqualsAndHashCode.Exclude // Avoid issues with bidirectional relationships in equals/hashCode
    private Set<Agent> agents = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Convenience methods for managing agents in the project
    public void addAgent(Agent agent) {
        this.agents.add(agent);
        // agent.getProjects().add(this); // If bidirectional relationship is needed on Agent side
    }

    public void removeAgent(Agent agent) {
        this.agents.remove(agent);
        // agent.getProjects().remove(this); // If bidirectional relationship is needed on Agent side
    }
}
