/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.workflow.di;

import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.entitlements.EntitlementsAPIConfig;
import org.opengroup.osdu.core.common.entitlements.EntitlementsFactory;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsFactory;
import org.opengroup.osdu.core.common.http.json.HttpResponseBodyMapper;
import org.opengroup.osdu.workflow.model.property.EntitlementProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;

@RequiredArgsConstructor
@Component
public class EntitlementsClientFactory extends AbstractFactoryBean<IEntitlementsFactory> {
  @Autowired
  private HttpResponseBodyMapper httpResponseBodyMapper;

  final EntitlementProperties entitlementProperties;

  @Inject
  private HttpResponseBodyMapper bodyMapper;

  @Override
  protected IEntitlementsFactory createInstance() {
    return new EntitlementsFactory(EntitlementsAPIConfig.builder()
        .rootUrl(entitlementProperties.getUrl())
        .apiKey(entitlementProperties.getAppKey())
        .build(), httpResponseBodyMapper);
  }

  @Override
  public Class<?> getObjectType() {
    return IEntitlementsFactory.class;
  }
}
