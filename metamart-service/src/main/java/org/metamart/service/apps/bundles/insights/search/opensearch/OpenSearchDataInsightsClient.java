package org.metamart.service.apps.bundles.insights.search.opensearch;

import java.io.IOException;
import org.metamart.service.apps.bundles.insights.search.DataInsightsSearchInterface;
import os.org.opensearch.client.Request;
import os.org.opensearch.client.Response;
import os.org.opensearch.client.ResponseException;
import os.org.opensearch.client.RestClient;

public class OpenSearchDataInsightsClient implements DataInsightsSearchInterface {
  private final RestClient client;

  public OpenSearchDataInsightsClient(RestClient client) {
    this.client = client;
  }

  private Response performRequest(String method, String path) throws IOException {
    Request request = new Request(method, path);
    return client.performRequest(request);
  }

  private Response performRequest(String method, String path, String payload) throws IOException {
    Request request = new Request(method, path);
    request.setJsonEntity(payload);

    return client.performRequest(request);
  }

  @Override
  public void createLifecyclePolicy(String name, String policy) throws IOException {
    try {
      performRequest("PUT", String.format("/_plugins/_ism/policies/%s", name), policy);
    } catch (ResponseException ex) {
      // Conflict since the Policy already exists
      if (ex.getResponse().getStatusLine().getStatusCode() == 409) {
        performRequest("DELETE", String.format("/_plugins/_ism/policies/%s", name));
        performRequest("PUT", String.format("/_plugins/_ism/policies/%s", name), policy);
      } else {
        throw ex;
      }
    }
  }

  @Override
  public void createComponentTemplate(String name, String template) throws IOException {
    performRequest("PUT", String.format("/_component_template/%s", name), template);
  }

  @Override
  public void createIndexTemplate(String name, String template) throws IOException {
    performRequest("PUT", String.format("/_index_template/%s", name), template);
  }

  @Override
  public void createDataStream(String name) throws IOException {
    performRequest("PUT", String.format("/_data_stream/%s", name));
  }

  @Override
  public Boolean dataAssetDataStreamExists(String name) throws IOException {
    Response response = performRequest("HEAD", String.format("/%s", name));
    return response.getStatusLine().getStatusCode() == 200;
  }

  @Override
  public void createDataAssetsDataStream(String name) throws IOException {
    String resourcePath = "/dataInsights/opensearch";
    createLifecyclePolicy(
        "di-data-assets-lifecycle",
        readResource(String.format("%s/indexLifecyclePolicy.json", resourcePath)));
    createComponentTemplate(
        "di-data-assets-mapping",
        readResource(String.format("%s/indexMappingsTemplate.json", resourcePath)));
    createIndexTemplate(
        "di-data-assets", readResource(String.format("%s/indexTemplate.json", resourcePath)));
    createDataStream(name);
  }

  @Override
  public void deleteDataAssetDataStream(String name) throws IOException {
    performRequest("DELETE", String.format("_data_stream/%s", name));
  }
}
