package org.metamart.service.events;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import org.metamart.service.resources.events.EventResource.EventList;

public interface EventPublisher
    extends EventHandler<EventPubSub.ChangeEventHolder>, LifecycleAware {

  void publish(EventList events);
}
