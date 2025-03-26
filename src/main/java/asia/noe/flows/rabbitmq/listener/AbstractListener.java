package asia.noe.flows.rabbitmq.listener;

import org.springframework.amqp.core.MessageListener;

public abstract class AbstractListener implements MessageListener {

    public abstract void setListenerId(String listenerId);

    public abstract String getListenerId();
}
