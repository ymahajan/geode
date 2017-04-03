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
package org.apache.geode.internal.cache.tier.sockets.command;

import static org.apache.geode.SystemFailure.loadEmergencyClasses;
import static org.apache.geode.internal.cache.TXManagerImpl.NOTX;
import static org.mockito.Mockito.*;

import org.apache.geode.cache.Operation;
import org.apache.geode.internal.Version;
import org.apache.geode.internal.cache.GemFireCacheImpl;
import org.apache.geode.internal.cache.TXManagerImpl;
import org.apache.geode.internal.cache.tier.Command;
import org.apache.geode.internal.cache.tier.sockets.CacheServerStats;
import org.apache.geode.internal.cache.tier.sockets.ClientProxyMembershipID;
import org.apache.geode.internal.cache.tier.sockets.Message;
import org.apache.geode.internal.cache.tier.sockets.Part;
import org.apache.geode.internal.cache.tier.sockets.ServerConnection;
import org.apache.geode.test.junit.categories.UnitTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class Put65BenchTest {

  public Command put65Command;
  public ServerConnection mockServerConnection;
  public Message mockMessage;

  @Before
  public void setup() throws Exception {
    loadEmergencyClasses();

    this.put65Command = Put65.getCommand();

    this.mockServerConnection =
        mock(ServerConnection.class, withSettings().name("mockServerConnection"));
    when(this.mockServerConnection.getClientVersion()).thenReturn(Version.CURRENT);

    GemFireCacheImpl mockCache = mock(GemFireCacheImpl.class, withSettings().name("mockCache"));
    when(this.mockServerConnection.getCache()).thenReturn(mockCache);

    TXManagerImpl mockTxManager = mock(TXManagerImpl.class, withSettings().name("mockTxManager"));
    when(mockCache.getTxManager()).thenReturn(mockTxManager);

    CacheServerStats mockCacheServerStats =
        mock(CacheServerStats.class, withSettings().name("mockCacheServerStats"));
    when(this.mockServerConnection.getCacheServerStats()).thenReturn(mockCacheServerStats);

    ClientProxyMembershipID mockProxyId =
        mock(ClientProxyMembershipID.class, withSettings().name("mockProxyId"));
    when(this.mockServerConnection.getProxyID()).thenReturn(mockProxyId);

    Message mockErrorResponseMessage =
        mock(Message.class, withSettings().name("mockErrorResponseMessage"));
    when(this.mockServerConnection.getErrorResponseMessage()).thenReturn(mockErrorResponseMessage);

    Part mockRegionNamePart = mock(Part.class, withSettings().name("mockRegionNamePart"));
    when(mockRegionNamePart.getString()).thenReturn("mockRegionNamePart");

    Part mockOperationPart = mock(Part.class, withSettings().name("mockOperationPart"));
    when(mockOperationPart.getObject()).thenReturn(Operation.UPDATE);

    Part mockFlagsPart = mock(Part.class, withSettings().name("mockFlagsPart"));
    when(mockFlagsPart.getInt()).thenReturn(0);

    Part mockKeyPart = mock(Part.class, withSettings().name("mockKeyPart"));
    when(mockKeyPart.getObject()).thenReturn("mockKeyPart");
    when(mockKeyPart.getStringOrObject()).thenReturn("mockKeyPart");

    Part mockIsDeltaPart = mock(Part.class, withSettings().name("mockIsDeltaPart"));
    when(mockIsDeltaPart.getObject()).thenReturn(Boolean.FALSE);

    Part mockValuePart = mock(Part.class, withSettings().name("mockValuePart"));
    when(mockValuePart.getObject()).thenReturn("mockValuePart");

    Part mockEventPart = mock(Part.class, withSettings().name("mockEventPart"));
    when(mockEventPart.getObject()).thenReturn("mockEventPart");

    Part mockCallbackArgPart = mock(Part.class, withSettings().name("mockCallbackArgPart"));
    when(mockCallbackArgPart.getObject()).thenReturn("mockCallbackArgPart");

    this.mockMessage = mock(Message.class, withSettings().name("mockMessage"));

    when(this.mockMessage.getTransactionId()).thenReturn(NOTX);

    when(this.mockMessage.getPart(0)).thenReturn(mockRegionNamePart);
    when(this.mockMessage.getPart(1)).thenReturn(mockOperationPart);
    when(this.mockMessage.getPart(2)).thenReturn(mockFlagsPart);
    when(this.mockMessage.getPart(3)).thenReturn(mockKeyPart);
    when(this.mockMessage.getPart(4)).thenReturn(mockIsDeltaPart);
    when(this.mockMessage.getPart(5)).thenReturn(mockValuePart);
    when(this.mockMessage.getPart(6)).thenReturn(mockEventPart);
    when(this.mockMessage.getPart(7)).thenReturn(mockCallbackArgPart);
  }

  @Test
  public void benchmark() {
    this.put65Command.execute(this.mockMessage, this.mockServerConnection);
    // Message replyMessage = state.mockServerConnection.getReplyMessage();
    // blackhole.consume(replyMessage);
  }
}
