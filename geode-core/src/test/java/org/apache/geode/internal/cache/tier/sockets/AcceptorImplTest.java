/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.internal.cache.tier.sockets;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.geode.CancelCriterion;
import org.apache.geode.cache.wan.GatewayTransportFilter;
import org.apache.geode.distributed.internal.DistributionConfigImpl;
import org.apache.geode.internal.cache.InternalCache;
import org.apache.geode.internal.net.SocketCreator;
import org.apache.geode.internal.net.SocketCreatorFactory;
import org.apache.geode.test.junit.categories.UnitTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Category(UnitTest.class)
public class AcceptorImplTest {

  @Before
  public void before() throws Exception {
    DistributionConfigImpl distributionConfig = new DistributionConfigImpl(new Properties());
    SocketCreatorFactory.setDistributionConfig(distributionConfig);
  }

  @After
  public void after() throws Exception {
    SocketCreatorFactory.close();
  }

  @Test
  public void constructWithDefaults() throws Exception {
    /*
     * Problems:
     * 
     * this.clientNotifier = CacheClientNotifier.getInstance(cache, this.stats, maximumMessageCount,
     * messageTimeToLive, connectionListener, overflowAttributesList, isGatewayReceiver);
     * 
     * this.healthMonitor = ClientHealthMonitor.getInstance(internalCache, maximumTimeBetweenPings,
     * this.clientNotifier.getStats());
     * 
     * LoggingThreadGroup / ThreadFactory / ThreadPoolExecutor
     * 
     * isAuthenticationRequired = this.securityService.isClientSecurityRequired();
     * 
     * isIntegratedSecurity = this.securityService.isIntegratedSecurity();
     * 
     * 
     * String postAuthzFactoryName =
     * this.cache.getDistributedSystem().getProperties().getProperty(SECURITY_CLIENT_ACCESSOR_PP);
     * 
     */

    int port = 0;
    String bindHostName = SocketCreator.getLocalHost().getHostName();
    boolean notifyBySubscription = false;
    int socketBufferSize = 1;
    int maximumTimeBetweenPings = 0;
    InternalCache internalCache = null;
    int maxConnections = 0;
    int maxThreads = 0;
    int maximumMessageCount = 0;
    int messageTimeToLive = 0;
    ConnectionListener listener = null;
    List overflowAttributesList = null;
    boolean isGatewayReceiver = false;
    List<GatewayTransportFilter> transportFilter = Collections.emptyList();
    boolean tcpNoDelay = false;
    CancelCriterion cancelCriterion = null;

    AcceptorImpl acceptor = new AcceptorImpl(port, bindHostName, notifyBySubscription,
        socketBufferSize, maximumTimeBetweenPings, internalCache, maxConnections, maxThreads,
        maximumMessageCount, messageTimeToLive, listener, overflowAttributesList, isGatewayReceiver,
        transportFilter, tcpNoDelay, cancelCriterion);

    assertThat(acceptor).isNotNull();
  }
}
