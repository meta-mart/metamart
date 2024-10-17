package org.metamart.service.apps.bundles.insights.search;

import java.io.IOException;
import java.io.InputStream;
import org.metamart.service.exception.UnhandledServerException;

public interface DataInsightsSearchInterface {

  void createLifecyclePolicy(String name, String policy) throws IOException;

  void createComponentTemplate(String name, String template) throws IOException;

  void createIndexTemplate(String name, String template) throws IOException;

  void createDataStream(String name) throws IOException;

  default String readResource(String resourceFile) {
    try (InputStream in = getClass().getResourceAsStream(resourceFile)) {
      assert in != null;
      return new String(in.readAllBytes());
    } catch (Exception e) {
      throw new UnhandledServerException("Failed to load DataInsight Search Configurations.");
    }
  }

  void createDataAssetsDataStream(String name) throws IOException;

  void deleteDataAssetDataStream(String name) throws IOException;

  Boolean dataAssetDataStreamExists(String name) throws IOException;
}
