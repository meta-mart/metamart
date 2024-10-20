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

package org.metamart.service.socket;

import static org.metamart.service.exception.OMErrorPageHandler.setSecurityHeader;

import io.dropwizard.servlets.assets.AssetServlet;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.Nullable;
import org.metamart.service.config.OMWebConfiguration;

public class MetaMartAssetServlet extends AssetServlet {
  private final OMWebConfiguration webConfiguration;

  public MetaMartAssetServlet(
      String resourcePath, String uriPath, @Nullable String indexFile, OMWebConfiguration webConf) {
    super(resourcePath, uriPath, indexFile, "text/html", StandardCharsets.UTF_8);
    this.webConfiguration = webConf;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    setSecurityHeader(webConfiguration, resp);
    super.doGet(req, resp);
    if (!resp.isCommitted() && (resp.getStatus() == 404)) {
      resp.sendError(404);
    }
  }
}
