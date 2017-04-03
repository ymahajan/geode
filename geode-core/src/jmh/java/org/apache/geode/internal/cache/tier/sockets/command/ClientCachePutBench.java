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

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.apache.commons.io.FileUtils.*;
import static org.apache.geode.distributed.AbstractLauncher.Status.ONLINE;
import static org.apache.geode.test.dunit.NetworkUtils.getIPLiteral;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.distributed.AbstractLauncher.Status;
import org.apache.geode.distributed.ConfigurationProperties;
import org.apache.geode.distributed.ServerLauncher;
import org.apache.geode.distributed.internal.DistributionConfig;
import org.apache.geode.internal.AvailablePort;
import org.apache.geode.internal.net.SocketCreator;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Measurement(iterations = 5)
@Warmup(iterations = 5)
@Fork(1)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
@SuppressWarnings("unused")
public class ClientCachePutBench {

  @Test
  public void tempTest() throws Exception {
    String SERVER_XML_FILE_NAME =
        "/" + StringUtils.replace(ClientCachePutBench.class.getPackage().getName(), ".", "/")
            + "/ClientCachePutBench-server.xml";
    assertThat(new File(getClass().getResource(SERVER_XML_FILE_NAME).getFile())).exists();
  }

  @State(Scope.Benchmark)
  public static class ClientState {
    // public static final String SERVER_XML_FILE_NAME =
    // "/" + StringUtils.replace(ClientCachePutBench.class.getPackage().getName(), ".", "/")
    // + "/ClientCachePutBench-server.xml";
    public static final String REGION_NAME = "clientCachePutBench-region";

    public Random random;

    public int serverPort;
    public Process process;
    public ServerLauncher launcher;
    public File serverDirectory;

    public ClientCache clientCache;
    public Region<String, String> region;

    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Setup(Level.Trial)
    public void startServer() throws Exception {
      this.random = new Random(System.nanoTime());

      this.temporaryFolder.create();
      this.serverDirectory = this.temporaryFolder.getRoot();

      String SERVER_XML_FILE_NAME =
          "/" + StringUtils.replace(ClientCachePutBench.class.getPackage().getName(), ".", "/")
              + "/ClientCachePutBench-server.xml";

      URL srcServerXml = getClass().getResource(SERVER_XML_FILE_NAME);
      assertThat(srcServerXml).isNotNull();
      File destServerXml = new File(this.serverDirectory, SERVER_XML_FILE_NAME);
      copyURLToFile(srcServerXml, destServerXml);

      this.serverPort = AvailablePort.getRandomAvailablePort(AvailablePort.SOCKET);

      List<String> jvmArguments = getJvmArguments();

      List<String> command = new ArrayList<>();
      command.add(
          new File(new File(System.getProperty("java.home"), "bin"), "java").getCanonicalPath());
      for (String jvmArgument : jvmArguments) {
        command.add(jvmArgument);
      }
      command.add("-Dgemfire.cache-xml-file=" + destServerXml.getAbsolutePath());
      command.add("-cp");
      command.add(System.getProperty("java.class.path"));
      command.add(ServerLauncher.class.getName());
      command.add(ServerLauncher.Command.START.getName());
      command.add("server1");
      command.add("--server-port=" + this.serverPort);
      // put65Command.add("--redirect-output");

      this.process = new ProcessBuilder(command).directory(this.temporaryFolder.getRoot()).start();

      boolean sleep = false;
      while (sleep) {
        assertThat(this.process.isAlive()).isTrue();
        Thread.sleep(10000);
      }

      ServerLauncher serverLauncher = new ServerLauncher.Builder()
          .setWorkingDirectory(this.temporaryFolder.getRoot().getAbsolutePath()).build();

      await().atMost(2, MINUTES)
          .until(() -> assertThat(serverLauncher.status().getStatus()).isEqualTo(ONLINE));

      this.clientCache =
          new ClientCacheFactory().addPoolServer(getIPLiteral(), this.serverPort).create();
      this.region =
          this.clientCache.<String, String>createClientRegionFactory(ClientRegionShortcut.PROXY)
              .create(REGION_NAME);
    }

    @TearDown(Level.Trial)
    public void stopServer() throws Exception {
      try {
        this.clientCache.close(false);
        new ServerLauncher.Builder().setWorkingDirectory(this.serverDirectory.getAbsolutePath())
            .build().stop();
      } finally {
        if (this.process != null) {
          this.process.destroyForcibly();
        }
        this.temporaryFolder.delete();
      }
    }

    private List<String> getJvmArguments() {
      List<String> jvmArguments = new ArrayList<>();
      jvmArguments.add(
          "-D" + DistributionConfig.GEMFIRE_PREFIX + ConfigurationProperties.MCAST_PORT + "=0");
      jvmArguments.add(
          "-D" + DistributionConfig.GEMFIRE_PREFIX + ConfigurationProperties.LOCATORS + "\"\"");
      return jvmArguments;
    }
  }

  @Benchmark
  public void test(ClientState state, Blackhole blackhole) throws Exception {
    String key = "key-" + state.random.nextInt();
    String value = "value-" + state.random.nextInt();
    String oldValue = state.region.put(key, value);
    blackhole.consume(new Object[] {key, value, oldValue});
    blackhole.consume(oldValue);
  }

}
