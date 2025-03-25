package edu.stage.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import jakarta.validation.constraints.Size; 

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 100, message = "Le titre ne doit pas dépasser 100 caractères")
    @Column(nullable = false)
    private String title;

    @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
    private String description;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Task(Long id, String title, String description, Priority priority, User user) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.user = user;
    }
    

}
