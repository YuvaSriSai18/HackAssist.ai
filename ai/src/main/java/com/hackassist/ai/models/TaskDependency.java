package com.hackassist.ai.models;

import jakarta.persistence.*;

@Entity
@Table(name = "task_dependencies", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"task_id", "depends_on_id"})
})
public class TaskDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private ProjectTask task;

    @ManyToOne
    @JoinColumn(name = "depends_on_id", nullable = false)
    private ProjectTask dependsOn;

    public TaskDependency() {}

    public TaskDependency(Project project, ProjectTask task, ProjectTask dependsOn) {
        this.project = project;
        this.task = task;
        this.dependsOn = dependsOn;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public ProjectTask getTask() { return task; }
    public void setTask(ProjectTask task) { this.task = task; }

    public ProjectTask getDependsOn() { return dependsOn; }
    public void setDependsOn(ProjectTask dependsOn) { this.dependsOn = dependsOn; }
}
