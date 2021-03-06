/*
 * Copyright (c) 2020, Mulesoft, LLC. All rights reserved.
 * Use of this source code is governed by a BSD 3-Clause License
 * license that can be found in the LICENSE.txt file.
 */
package com.mulesoft.tools.migration.integration;

import static org.apache.commons.io.FileUtils.forceMkdir;

import org.mule.tck.junit4.rule.DynamicPort;

import com.mulesoft.tools.migration.integration.sftp.SftpServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;

@RunWith(Parameterized.class)
public class SftpMigrationTestCase extends EndToEndTestCase {

  @Rule
  public TemporaryFolder sourceTemp = new TemporaryFolder();

  @Rule
  public final DynamicPort sftpSourcePort = new DynamicPort("sftpPort");

  @Rule
  public TemporaryFolder destinationTemp = new TemporaryFolder();

  @Rule
  public final DynamicPort sftpDestinationPort = new DynamicPort("sftpDestinationPort");

  @Parameters(name = "{0}")
  public static Object[] params() {
    return new Object[] {
        "sftp"
    };
  }

  private final String appToMigrate;

  public SftpMigrationTestCase(String appToMigrate) {
    this.appToMigrate = appToMigrate;
  }

  private SftpServer sftpSourceServer;
  private SftpServer sftpDestinationServer;

  @Before
  public void before() throws Exception {
    File sourceRoot = sourceTemp.newFolder();
    forceMkdir(new File(sourceRoot, "source"));
    sftpSourceServer = new SftpServer(sftpSourcePort.getNumber(), sourceRoot.toPath());

    File targetRoot = destinationTemp.newFolder();
    forceMkdir(new File(targetRoot, "target"));
    sftpDestinationServer = new SftpServer(sftpDestinationPort.getNumber(), targetRoot.toPath());

    sftpSourceServer.start();
    sftpDestinationServer.start();
  }

  @After
  public void after() {
    sftpSourceServer.stop();
    sftpDestinationServer.stop();
  }

  @Test
  public void test() throws Exception {
    simpleCase(appToMigrate, "-M-DsftpSourcePort=" + sftpSourcePort.getNumber(),
               "-M-DsftpDestinationPort=" + sftpDestinationPort.getNumber());
  }
}
