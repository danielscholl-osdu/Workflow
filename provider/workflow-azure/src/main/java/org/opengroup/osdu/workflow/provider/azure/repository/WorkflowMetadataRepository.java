package org.opengroup.osdu.workflow.provider.azure.repository;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.SqlQuerySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.azure.cosmosdb.CosmosStore;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.workflow.exception.ResourceConflictException;
import org.opengroup.osdu.workflow.exception.WorkflowNotFoundException;
import org.opengroup.osdu.workflow.model.WorkflowMetadata;
import org.opengroup.osdu.workflow.provider.azure.config.AzureWorkflowEngineConfig;
import org.opengroup.osdu.workflow.provider.azure.config.CosmosConfig;
import org.opengroup.osdu.workflow.provider.azure.model.WorkflowMetadataDoc;
import org.opengroup.osdu.workflow.provider.interfaces.IWorkflowMetadataRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static org.opengroup.osdu.workflow.provider.azure.utils.WorkflowMetadataUtils.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class WorkflowMetadataRepository implements IWorkflowMetadataRepository {
  private static final String LOGGER_NAME = WorkflowMetadataRepository.class.getName();

  private final CosmosConfig cosmosConfig;

  private final CosmosStore cosmosStore;

  private final DpsHeaders dpsHeaders;

  private final JaxRsDpsLog logger;

  private final AzureWorkflowEngineConfig workflowEngineConfig;

  @Override
  public WorkflowMetadata createWorkflow(final WorkflowMetadata workflowMetadata) {
    final WorkflowMetadataDoc workflowMetadataDoc = buildWorkflowMetadataDoc(workflowEngineConfig, workflowMetadata);
    try {
      cosmosStore.createItem(dpsHeaders.getPartitionId(), cosmosConfig.getDatabase(),
          cosmosConfig.getWorkflowMetadataCollection(), workflowMetadataDoc.getPartitionKey(),
          workflowMetadataDoc);
    } catch (AppException e) {
      String workflowName = workflowMetadataDoc.getWorkflowName();
      if(e.getError().getCode() == 409) {
        final String errorMessage = String.format("Workflow with name %s already exists", workflowName);
        logger.error(LOGGER_NAME, errorMessage);
        throw new ResourceConflictException(workflowName, errorMessage);
      } else {
        throw e;
      }
    }
    return buildWorkflowMetadata(workflowMetadataDoc);
  }

  @Override
  public WorkflowMetadata getWorkflow(final String workflowName) {
    Optional<WorkflowMetadataDoc> workflowMetadataDoc =
      cosmosStore.findItem(
        dpsHeaders.getPartitionId(),
        cosmosConfig.getDatabase(),
        cosmosConfig.getWorkflowMetadataCollection(),
        workflowName,
        workflowName,
        WorkflowMetadataDoc.class
      );
    if (null == workflowMetadataDoc || !workflowMetadataDoc.isPresent()) {
      final String errorMessage = String.format("Workflow: %s doesn't exist", workflowName);
      logger.error(LOGGER_NAME, errorMessage);
      throw new WorkflowNotFoundException(errorMessage);
    }
    return buildWorkflowMetadata(workflowMetadataDoc.get());
  }

  @Override
  public void deleteWorkflow(String workflowName) {
      cosmosStore.deleteItem(
          dpsHeaders.getPartitionId(),
          cosmosConfig.getDatabase(),
          cosmosConfig.getWorkflowMetadataCollection(),
          workflowName,
          workflowName
      );
  }

  @Override
  public List<WorkflowMetadata> getAllWorkflowForTenant(String prefix) {
    try {
      SqlQuerySpec sqlQuerySpec = buildSqlQuerySpecForGetAllWorkflow(prefix);
      final List<WorkflowMetadataDoc> workflowMetadataDocs = cosmosStore.queryItems(
              dpsHeaders.getPartitionId(),
              cosmosConfig.getDatabase(),
              cosmosConfig.getWorkflowMetadataCollection(),
              sqlQuerySpec,
              new CosmosQueryRequestOptions(),
              WorkflowMetadataDoc.class);
      return convertWorkflowMetadataDocsToWorkflowMetadataList(workflowMetadataDocs);
    } catch (CosmosException e) {
      throw new AppException(e.getStatusCode(), e.getMessage(), e.getMessage(), e);
    }
  }
}
