package com.adyanta.onboarding.fenergo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Fenergo Journey model
 * Represents a business process flow in Fenergo
 */
public class FenergoJourney {
    
    @JsonProperty("journeyId")
    private String journeyId;
    
    @NotBlank(message = "Journey name is required")
    @JsonProperty("journeyName")
    private String journeyName;
    
    @JsonProperty("journeyType")
    private JourneyType journeyType;
    
    @JsonProperty("status")
    private JourneyStatus status;
    
    @JsonProperty("entityId")
    private String entityId;
    
    @JsonProperty("policyId")
    private String policyId;
    
    @JsonProperty("processId")
    private String processId;
    
    @JsonProperty("version")
    private String version;
    
    @JsonProperty("createdDate")
    private LocalDateTime createdDate;
    
    @JsonProperty("lastModifiedDate")
    private LocalDateTime lastModifiedDate;
    
    @JsonProperty("startedDate")
    private LocalDateTime startedDate;
    
    @JsonProperty("completedDate")
    private LocalDateTime completedDate;
    
    @JsonProperty("stages")
    private List<JourneyStage> stages;
    
    @JsonProperty("tasks")
    private List<JourneyTask> tasks;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    @JsonProperty("correlationId")
    private String correlationId;

    // Constructors
    public FenergoJourney() {
        this.createdDate = LocalDateTime.now();
        this.lastModifiedDate = LocalDateTime.now();
        this.status = JourneyStatus.DRAFT;
        this.version = "1.0";
    }

    // Getters and Setters
    public String getJourneyId() {
        return journeyId;
    }

    public void setJourneyId(String journeyId) {
        this.journeyId = journeyId;
    }

    public String getJourneyName() {
        return journeyName;
    }

    public void setJourneyName(String journeyName) {
        this.journeyName = journeyName;
    }

    public JourneyType getJourneyType() {
        return journeyType;
    }

    public void setJourneyType(JourneyType journeyType) {
        this.journeyType = journeyType;
    }

    public JourneyStatus getStatus() {
        return status;
    }

    public void setStatus(JourneyStatus status) {
        this.status = status;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public LocalDateTime getStartedDate() {
        return startedDate;
    }

    public void setStartedDate(LocalDateTime startedDate) {
        this.startedDate = startedDate;
    }

    public LocalDateTime getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(LocalDateTime completedDate) {
        this.completedDate = completedDate;
    }

    public List<JourneyStage> getStages() {
        return stages;
    }

    public void setStages(List<JourneyStage> stages) {
        this.stages = stages;
    }

    public List<JourneyTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<JourneyTask> tasks) {
        this.tasks = tasks;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    // Enums
    public enum JourneyType {
        ONBOARDING,
        PERIODIC_REVIEW,
        ENHANCED_DUE_DILIGENCE,
        SANCTIONS_SCREENING,
        AML_REVIEW,
        RISK_ASSESSMENT,
        COMPLIANCE_CHECK
    }

    public enum JourneyStatus {
        DRAFT,
        IN_PROGRESS,
        PENDING_APPROVAL,
        APPROVED,
        REJECTED,
        COMPLETED,
        CANCELLED,
        SUSPENDED
    }

    // Inner Classes
    public static class JourneyStage {
        @JsonProperty("stageId")
        private String stageId;
        
        @JsonProperty("stageName")
        private String stageName;
        
        @JsonProperty("stageOrder")
        private Integer stageOrder;
        
        @JsonProperty("status")
        private StageStatus status;
        
        @JsonProperty("startedDate")
        private LocalDateTime startedDate;
        
        @JsonProperty("completedDate")
        private LocalDateTime completedDate;
        
        @JsonProperty("tasks")
        private List<String> taskIds;

        // Getters and Setters
        public String getStageId() {
            return stageId;
        }

        public void setStageId(String stageId) {
            this.stageId = stageId;
        }

        public String getStageName() {
            return stageName;
        }

        public void setStageName(String stageName) {
            this.stageName = stageName;
        }

        public Integer getStageOrder() {
            return stageOrder;
        }

        public void setStageOrder(Integer stageOrder) {
            this.stageOrder = stageOrder;
        }

        public StageStatus getStatus() {
            return status;
        }

        public void setStatus(StageStatus status) {
            this.status = status;
        }

        public LocalDateTime getStartedDate() {
            return startedDate;
        }

        public void setStartedDate(LocalDateTime startedDate) {
            this.startedDate = startedDate;
        }

        public LocalDateTime getCompletedDate() {
            return completedDate;
        }

        public void setCompletedDate(LocalDateTime completedDate) {
            this.completedDate = completedDate;
        }

        public List<String> getTaskIds() {
            return taskIds;
        }

        public void setTaskIds(List<String> taskIds) {
            this.taskIds = taskIds;
        }

        public enum StageStatus {
            PENDING,
            IN_PROGRESS,
            COMPLETED,
            FAILED,
            SKIPPED
        }
    }

    public static class JourneyTask {
        @JsonProperty("taskId")
        private String taskId;
        
        @JsonProperty("taskName")
        private String taskName;
        
        @JsonProperty("taskType")
        private TaskType taskType;
        
        @JsonProperty("status")
        private TaskStatus status;
        
        @JsonProperty("assignedTo")
        private String assignedTo;
        
        @JsonProperty("dueDate")
        private LocalDateTime dueDate;
        
        @JsonProperty("startedDate")
        private LocalDateTime startedDate;
        
        @JsonProperty("completedDate")
        private LocalDateTime completedDate;
        
        @JsonProperty("priority")
        private TaskPriority priority;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("requirements")
        private List<String> requirements;
        
        @JsonProperty("outputs")
        private Map<String, Object> outputs;

        // Getters and Setters
        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }

        public String getTaskName() {
            return taskName;
        }

        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }

        public TaskType getTaskType() {
            return taskType;
        }

        public void setTaskType(TaskType taskType) {
            this.taskType = taskType;
        }

        public TaskStatus getStatus() {
            return status;
        }

        public void setStatus(TaskStatus status) {
            this.status = status;
        }

        public String getAssignedTo() {
            return assignedTo;
        }

        public void setAssignedTo(String assignedTo) {
            this.assignedTo = assignedTo;
        }

        public LocalDateTime getDueDate() {
            return dueDate;
        }

        public void setDueDate(LocalDateTime dueDate) {
            this.dueDate = dueDate;
        }

        public LocalDateTime getStartedDate() {
            return startedDate;
        }

        public void setStartedDate(LocalDateTime startedDate) {
            this.startedDate = startedDate;
        }

        public LocalDateTime getCompletedDate() {
            return completedDate;
        }

        public void setCompletedDate(LocalDateTime completedDate) {
            this.completedDate = completedDate;
        }

        public TaskPriority getPriority() {
            return priority;
        }

        public void setPriority(TaskPriority priority) {
            this.priority = priority;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getRequirements() {
            return requirements;
        }

        public void setRequirements(List<String> requirements) {
            this.requirements = requirements;
        }

        public Map<String, Object> getOutputs() {
            return outputs;
        }

        public void setOutputs(Map<String, Object> outputs) {
            this.outputs = outputs;
        }

        public enum TaskType {
            DATA_COLLECTION,
            DOCUMENT_VERIFICATION,
            IDENTITY_VERIFICATION,
            ADDRESS_VERIFICATION,
            COMPLIANCE_CHECK,
            RISK_ASSESSMENT,
            APPROVAL,
            NOTIFICATION,
            INTEGRATION_CALL
        }

        public enum TaskStatus {
            PENDING,
            IN_PROGRESS,
            COMPLETED,
            FAILED,
            CANCELLED,
            SKIPPED
        }

        public enum TaskPriority {
            LOW,
            MEDIUM,
            HIGH,
            CRITICAL
        }
    }
}
