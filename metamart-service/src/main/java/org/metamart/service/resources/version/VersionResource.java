/*
 *  Copyright 2021 DigiTrans
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.metamart.service.resources.version;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.InputStream;
import java.util.Properties;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.metamart.schema.api.MetaMartServerVersion;
import org.metamart.service.MetaMartApplication;
import org.metamart.service.resources.Collection;

@Slf4j
@Path("/v1/system/version")
@Tag(name = "System", description = "APIs related to System configuration and settings.")
@Produces(MediaType.APPLICATION_JSON)
@Collection(name = "version")
public class VersionResource {
  private static final MetaMartServerVersion META_MART_SERVER_VERSION;

  static {
    META_MART_SERVER_VERSION = new MetaMartServerVersion();
    try {
      InputStream fileInput = MetaMartApplication.class.getResourceAsStream("/catalog/VERSION");
      Properties props = new Properties();
      props.load(fileInput);
      META_MART_SERVER_VERSION.setVersion(props.getProperty("version", "unknown"));
      META_MART_SERVER_VERSION.setRevision(props.getProperty("revision", "unknown"));

      String timestampAsString = props.getProperty("timestamp");
      Long timestamp = timestampAsString != null ? Long.valueOf(timestampAsString) : null;
      META_MART_SERVER_VERSION.setTimestamp(timestamp);
    } catch (Exception ie) {
      LOG.warn("Failed to read catalog version file");
    }
  }

  @GET
  @Operation(
      operationId = "getCatalogVersion",
      summary = "Get version of metadata service",
      description = "Get the build version of MetaMart service and build timestamp.")
  public MetaMartServerVersion getCatalogVersion() {
    return META_MART_SERVER_VERSION;
  }
}
