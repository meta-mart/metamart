package org.metamart.service.resources.events;

import static org.metamart.common.utils.CommonUtil.nullOrEmpty;
import static org.metamart.service.util.email.EmailUtil.getSmtpSettings;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.metamart.service.apps.bundles.changeEvent.slack.SlackMessage;

/** REST resource used for slack callback tests. */
@Slf4j
@Path("v1/test/slack")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SlackCallbackResource extends BaseCallbackResource<SlackMessage> {
  @Override
  protected String getTestName() {
    return "slackTest";
  }

  public String getEntityUrl(String prefix, String fqn, String additionalParams) {
    return String.format(
        "<%s/%s/%s%s|%s>",
        getSmtpSettings().getMetaMartUrl(),
        prefix,
        fqn.trim().replaceAll(" ", "%20"),
        nullOrEmpty(additionalParams) ? "" : String.format("/%s", additionalParams),
        fqn.trim());
  }
}
