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

package org.opengroup.osdu.workflow.ibm.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.workflow.ReplaceCamelCase;
import org.opengroup.osdu.workflow.ibm.property.AirflowProperties;
import org.opengroup.osdu.workflow.service.SubmitIngestService;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
class SubmitIngestServiceTest {

  @Mock
  private HTTPHelper httpHelper;

  @Mock
  private AirflowProperties airflowProperties;

  @Mock
  private HttpRequest httpRequest;

  @Mock
  private HttpResponse httpResponse;

  SubmitIngestService submitIngestService;

  @BeforeEach
  void setUp() {
    submitIngestService = new SubmitIngestServiceImpl(airflowProperties, httpHelper);
  }
  
  @Test
  void shouldStartWorkflow() throws IOException {
    // given
    HashMap<String, Object> data = new HashMap<>();
    data.put("key", "value");
    given(airflowProperties.getUrl()).willReturn("http://test-airflow");
    given(httpHelper.getIapClientId(eq("http://test-airflow"))).willReturn("client-id");
    given(httpHelper.buildIapRequest(anyString(), eq("client-id"), eq(data))).willReturn(httpRequest);
    given(httpRequest.execute()).willReturn(httpResponse);
    given(httpResponse.getContent()).willReturn(new ByteArrayInputStream( "test".getBytes() ));

    // when
    boolean result = submitIngestService.submitIngest("dag-name", data);

    // then
    InOrder inOrder = Mockito.inOrder(airflowProperties, httpHelper);
    inOrder.verify(airflowProperties).getUrl();
    inOrder.verify(httpHelper).getIapClientId(eq("http://test-airflow"));
    inOrder.verify(httpHelper).buildIapRequest(anyString(), anyString(), eq(data));
    inOrder.verifyNoMoreInteractions();
  }
  
//  @Test
//  void shouldThrowExceptionIfRequestFails() throws IOException {
//
//    // given
//    HashMap<String, Object> data = new HashMap<>();
//    data.put("key", "value");
//    given(airflowProperties.getUrl()).willReturn("http://test-airflow");
//    given(googleIapHelper.getIapClientId(eq("http://test-airflow"))).willReturn("client-id");
//    given(googleIapHelper.buildIapRequest(anyString(), eq("client-id"), eq(data))).willReturn(httpRequest);
//    given(httpRequest.execute()).willThrow(new IOException("test-exception"));
//
//    // when
//    Throwable thrown = catchThrowable(() -> submitIngestService.submitIngest("dag-name", data));
//
//    // then
//    then(thrown).satisfies(exception -> {
//      then(exception).isInstanceOf(OsduRuntimeException.class);
//      then(exception).hasMessage("Request execution exception");
//    });
//  }

}
