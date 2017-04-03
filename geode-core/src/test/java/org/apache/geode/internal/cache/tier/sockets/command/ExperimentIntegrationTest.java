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

import static org.apache.geode.distributed.AbstractLauncher.Status.ONLINE;
import static org.apache.geode.distributed.ServerLauncherUtils.*;
import static org.apache.geode.internal.cache.tier.sockets.AcceptorImpl.DEFAULT_HANDSHAKE_TIMEOUT_MS;
import static org.apache.geode.internal.cache.tier.sockets.CacheServerUtils.*;
import static org.apache.geode.internal.AvailablePort.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.server.CacheServer;
import org.apache.geode.distributed.ServerLauncher;
import org.apache.geode.internal.cache.tier.Acceptor;
import org.apache.geode.internal.cache.tier.Command;
import org.apache.geode.internal.cache.tier.sockets.AcceptorImpl;
import org.apache.geode.internal.cache.tier.sockets.Message;
import org.apache.geode.internal.cache.tier.sockets.ServerConnection;
import org.apache.geode.internal.net.SocketCreator;
import org.apache.geode.test.junit.categories.IntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.Socket;

@Category(IntegrationTest.class)
public class ExperimentIntegrationTest {

  private ServerLauncher serverLauncher;
  private ServerConnection serverConnection;

  @Before
  public void before() throws Exception {
    int serverPort = getRandomAvailablePort(SOCKET);

    this.serverLauncher =
        new ServerLauncher.Builder().setMemberName("server").setServerPort(serverPort).build();
    this.serverLauncher.start();

    Cache cache = getCache(this.serverLauncher);
    CacheServer cacheServer = getCacheServer(cache);
    AcceptorImpl acceptor = getAcceptorImpl(cacheServer);

    Socket mockSocket = mock(Socket.class);
    when(mockSocket.getInetAddress()).thenReturn(SocketCreator.getLocalHost());

    this.serverConnection =
        new ServerConnection(mockSocket, cache, null, null, DEFAULT_HANDSHAKE_TIMEOUT_MS,
            CacheServer.DEFAULT_SOCKET_BUFFER_SIZE, "client", Acceptor.CLIENT_TO_SERVER, acceptor);

    preConditions();
  }

  public void preConditions() throws Exception {
    assertThat(this.serverLauncher.status().getStatus()).isEqualTo(ONLINE);
  }

  @Test
  public void handlePutFromFakeClient() throws Exception {
    Message message = mock(Message.class);
    Command command = mock(Command.class);
    command.execute(message, this.serverConnection);
  }

}
