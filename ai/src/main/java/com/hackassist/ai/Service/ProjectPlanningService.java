package com.hackassist.ai.Service;

import com.hackassist.ai.dto.plan.FeatureDTO;
import com.hackassist.ai.dto.plan.ModuleDTO;
import com.hackassist.ai.dto.plan.ProjectPlanDTO;
import com.hackassist.ai.dto.plan.RiskDTO;
import com.hackassist.ai.dto.plan.TaskDTO;
import com.hackassist.ai.models.Project;
import com.hackassist.ai.models.ProjectFeature;
import com.hackassist.ai.models.ProjectModule;
import com.hackassist.ai.models.ProjectRisk;
import com.hackassist.ai.models.ProjectTask;
import com.hackassist.ai.models.TaskDependency;
import com.hackassist.ai.models.User;
import com.hackassist.ai.repository.ProjectFeatureRepository;
import com.hackassist.ai.repository.ProjectModuleRepository;
import com.hackassist.ai.repository.ProjectRepository;
import com.hackassist.ai.repository.ProjectRiskRepository;
import com.hackassist.ai.repository.ProjectTaskRepository;
import com.hackassist.ai.repository.TaskDependencyRepository;
import com.hackassist.ai.repository.UserRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ProjectPlanningService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectFeatureRepository featureRepository;
    private final ProjectModuleRepository moduleRepository;
    private final ProjectTaskRepository taskRepository;
    private final ProjectRiskRepository riskRepository;
    private final TaskDependencyRepository dependencyRepository;

    public ProjectPlanningService(
        ProjectRepository projectRepository,
        UserRepository userRepository,
        ProjectFeatureRepository featureRepository,
        ProjectModuleRepository moduleRepository,
        ProjectTaskRepository taskRepository,
        ProjectRiskRepository riskRepository,
        TaskDependencyRepository dependencyRepository
    ) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.featureRepository = featureRepository;
        this.moduleRepository = moduleRepository;
        this.taskRepository = taskRepository;
        this.riskRepository = riskRepository;
        this.dependencyRepository = dependencyRepository;
    }

    public void saveProjectPlan(String projectId, ProjectPlanDTO dto, String userId) {
        if (dto == null) {
            throw new RuntimeException("Project plan payload is required");
        }
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Project project = projectRepository.findByProjectIdAndCreatedBy(projectId, user);
        if (project == null) {
            throw new RuntimeException("Project not found or not owned by user");
        }

        clearExistingPlan(project);

        Map<String, ProjectModule> modulesByKey = saveModules(project, dto.getModules());
        saveFeatures(project, dto.getFeatures());
        Map<String, ProjectTask> tasksByExternalId = saveTasks(project, dto.getTasks(), modulesByKey);
        saveDependencies(project, dto.getTasks(), tasksByExternalId);
        saveRisks(project, dto.getRisks());
    }

    private void clearExistingPlan(Project project) {
        dependencyRepository.deleteByProject(project);
        List<ProjectTask> existingTasks = taskRepository.findByProject(project);
        for (ProjectTask task : existingTasks) {
            task.getModules().clear();
        }
        taskRepository.deleteByProject(project);
        moduleRepository.deleteByProject(project);
        featureRepository.deleteByProject(project);
        riskRepository.deleteByProject(project);
    }

    private Map<String, ProjectModule> saveModules(Project project, List<ModuleDTO> modules) {
        Map<String, ProjectModule> result = new HashMap<>();
        if (modules == null) {
            return result;
        }
        for (ModuleDTO module : modules) {
            if (module == null || module.getKey() == null) {
                continue;
            }
            ProjectModule entity = new ProjectModule(module.getKey(), module.getName(), module.getDescription(), project);
            ProjectModule saved = moduleRepository.save(entity);
            result.put(module.getKey(), saved);
        }
        return result;
    }

    private void saveFeatures(Project project, List<FeatureDTO> features) {
        if (features == null) {
            return;
        }
        for (FeatureDTO feature : features) {
            if (feature == null || feature.getKey() == null) {
                continue;
            }
            ProjectFeature entity = new ProjectFeature(
                feature.getKey(),
                feature.getName(),
                feature.getDescription(),
                feature.getPriority(),
                project
            );
            featureRepository.save(entity);
        }
    }

    private Map<String, ProjectTask> saveTasks(
        Project project,
        List<TaskDTO> tasks,
        Map<String, ProjectModule> modulesByKey
    ) {
        Map<String, ProjectTask> result = new HashMap<>();
        if (tasks == null) {
            return result;
        }
        int index = 1;
        for (TaskDTO task : tasks) {
            if (task == null) {
                continue;
            }
            String externalId = task.getExternalId();
            if (externalId == null || externalId.isBlank()) {
                externalId = String.format("TSK-%03d", index++);
            }
            ProjectTask entity = new ProjectTask(
                externalId,
                task.getTitle(),
                task.getDescription(),
                safeValue(task.getPriority(), "MEDIUM"),
                safeValue(task.getStatus(), "TODO"),
                task.getEstimatedHours(),
                project
            );
            if (task.getModuleKey() != null && modulesByKey.containsKey(task.getModuleKey())) {
                entity.getModules().add(modulesByKey.get(task.getModuleKey()));
            }
            ProjectTask saved = taskRepository.save(entity);
            result.put(externalId, saved);
        }
        return result;
    }

    private void saveDependencies(Project project, List<TaskDTO> tasks, Map<String, ProjectTask> taskMap) {
        if (tasks == null) {
            return;
        }
        for (TaskDTO task : tasks) {
            if (task == null || task.getDependsOn() == null || task.getExternalId() == null) {
                continue;
            }
            ProjectTask owner = taskMap.get(task.getExternalId());
            if (owner == null) {
                continue;
            }
            for (String dependsOnId : task.getDependsOn()) {
                ProjectTask dependsOn = taskMap.get(dependsOnId);
                if (dependsOn == null) {
                    continue;
                }
                dependencyRepository.save(new TaskDependency(project, owner, dependsOn));
            }
        }
    }

    private void saveRisks(Project project, List<RiskDTO> risks) {
        if (risks == null) {
            return;
        }
        for (RiskDTO risk : risks) {
            if (risk == null) {
                continue;
            }
            ProjectRisk entity = new ProjectRisk(risk.getTitle(), risk.getImpact(), risk.getMitigation(), project);
            riskRepository.save(entity);
        }
    }

    private String safeValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
