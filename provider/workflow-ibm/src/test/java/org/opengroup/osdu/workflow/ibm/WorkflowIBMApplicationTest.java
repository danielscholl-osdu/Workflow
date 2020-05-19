/*
 * Copyright 2020 IBM Corp
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.workflow.ibm;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Set;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.opengroup.osdu.workflow.ReplaceCamelCase;
import org.opengroup.osdu.workflow.service.WorkflowService;
import org.opengroup.osdu.workflow.service.WorkflowServiceImpl;
import org.opengroup.osdu.workflow.service.WorkflowStatusService;
import org.opengroup.osdu.workflow.service.WorkflowStatusServiceImpl;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.config.RoutingFunction;
import org.springframework.cloud.function.context.test.FunctionalSpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
@FunctionalSpringBootTest
class WorkflowIBMApplicationTest {

  @Inject
  private FunctionCatalog catalog;

  @Test
  void shouldHaveOnlyExactFunctions() {
    // when
    Set<String> names = catalog.getNames(null);

    // then
    Assertions.assertTrue(true, "silly assertion to be compliant with Sonar");
    then(names).hasSize(3)
        .containsExactlyInAnyOrder("startWorkflowFunction", "getStatusFunction",
            RoutingFunction.FUNCTION_NAME);
  }

  @TestConfiguration
  static class Config {

    @Bean
    WorkflowService workflowService() {
      return Mockito.mock(WorkflowServiceImpl.class);
    }

    @Bean
    WorkflowStatusService workflowStatusService() {
      return Mockito.mock(WorkflowStatusServiceImpl.class);
    }

  }
}
