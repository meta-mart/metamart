package org.metamart.service.apps.bundles.changeEvent;

import org.metamart.schema.entity.events.EventSubscription;
import org.metamart.schema.entity.events.SubscriptionDestination;
import org.metamart.schema.type.ChangeEvent;
import org.metamart.service.apps.bundles.changeEvent.email.EmailPublisher;
import org.metamart.service.apps.bundles.changeEvent.feed.ActivityFeedPublisher;
import org.metamart.service.apps.bundles.changeEvent.gchat.GChatPublisher;
import org.metamart.service.apps.bundles.changeEvent.generic.GenericPublisher;
import org.metamart.service.apps.bundles.changeEvent.msteams.MSTeamsPublisher;
import org.metamart.service.apps.bundles.changeEvent.slack.SlackEventPublisher;

public class AlertFactory {
  public static Destination<ChangeEvent> getAlert(
      EventSubscription subscription, SubscriptionDestination config) {
    return switch (config.getType()) {
      case SLACK -> new SlackEventPublisher(subscription, config);
      case MS_TEAMS -> new MSTeamsPublisher(subscription, config);
      case G_CHAT -> new GChatPublisher(subscription, config);
      case WEBHOOK -> new GenericPublisher(subscription, config);
      case EMAIL -> new EmailPublisher(subscription, config);
      case ACTIVITY_FEED -> new ActivityFeedPublisher(subscription, config);
    };
  }
}
