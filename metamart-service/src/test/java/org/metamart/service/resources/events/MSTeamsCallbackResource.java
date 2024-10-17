package org.metamart.service.resources.events;

import static org.metamart.service.util.email.EmailUtil.getSmtpSettings;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.metamart.service.Entity;
import org.metamart.service.apps.bundles.changeEvent.msteams.TeamsMessage;

/** REST resource used for msteams callback tests. */
@Slf4j
@Path("v1/test/msteams")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MSTeamsCallbackResource extends BaseCallbackResource<TeamsMessage> {
  @Override
  protected String getTestName() {
    return "teamsTest";
  }

  public String getEntityUrlMSTeams() {
    return String.format(
        "%s/%s", getSmtpSettings().getMetaMartUrl(), Entity.EVENT_SUBSCRIPTION);
  }
}
