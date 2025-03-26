package asia.noe.flows.rabbitmq.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import asia.noe.flows.rabbitmq.listener.AbstractListener;
import asia.noe.flows.rabbitmq.services.RabbitQueueService;


@Service
public class RabbitQueueServiceImpl implements RabbitQueueService {
    private static final Logger logger = LoggerFactory.getLogger(RabbitQueueServiceImpl.class);

    private final String LISTENER_KEY = "LS";

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Autowired
    private RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

    @Autowired
    private SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory;

    @Override
    public void stop() {
        rabbitListenerEndpointRegistry.stop();
    }

    @Override
    public void start() {
        rabbitListenerEndpointRegistry.start();
    }

    @Override
    public void addNewQueue(String queueName, String exchangeName, String routingKey) {
        Queue queue = new Queue(queueName, true, false, false);
        Binding binding = new Binding(
                queueName,
                Binding.DestinationType.QUEUE,
                exchangeName,
                routingKey,
                null);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareBinding(binding);
        this.addQueueToListener(exchangeName, queueName);
    }

    @Override
    public void deleteQueue(String queueName) {
        this.rabbitAdmin.deleteQueue(queueName);
        logger.info("[RabbitMQ] delete queue {}", queueName);
    }

    @Override
    public void addQueueToListener(String listenerId, String queueName) {
        if (!checkQueueExistOnListener(listenerId, queueName)) {
            this.getMessageListenerContainerById(listenerId).addQueueNames(queueName);
            logger.info("[RabbitMQ][{}] added queue {} to listener!", listenerId, queueName);
        } else {
            logger.info("[RabbitMQ][{}] given queue {} not exist on listener", listenerId, queueName);
        }
    }

    @Override
    public void removeQueueFromListener(String listenerId, String queueName) {
        logger.debug("[RabbitMQ][{}] removing queue: {}", listenerId, queueName);
        if (checkQueueExistOnListener(listenerId, queueName)) {
            this.getMessageListenerContainerById(listenerId).removeQueueNames(queueName);
            logger.info("[RabbitMQ][{}] removed queue: {}", listenerId, queueName);
        } else {
            logger.info("[RabbitMQ][{}] given queue {} not exist on given listener", listenerId, queueName);
        }
    }

    @Override
    public void removeAndDeleteQueueFromListener(String listenerId, String queueName) {
        logger.info("[RabbitMQ][{}] removing queue {}", listenerId, queueName);
        if (checkQueueExistOnListener(listenerId, queueName)) {
            this.getMessageListenerContainerById(listenerId).removeQueueNames(queueName);
            logger.debug("[RabbitMQ] deleting queue {} from rabbit management", queueName);
            this.rabbitAdmin.deleteQueue(queueName);
            logger.info("[RabbitMQ] deleted queue {} from rabbit management", queueName);
        } else {
            logger.info("[RabbitMQ][{}] given queue {}, not exist on given listener ", listenerId, queueName);
        }
    }

    @Override
    public Boolean checkQueueExistOnListener(String listenerId, String queueName) {
        try {
            String[] queueNames = this.getMessageListenerContainerById(listenerId).getQueueNames();
            
            if (queueNames == null || queueNames.length == 0) {
                logger.debug("[RabbitMQ][{}] there is no queue exist on listener", listenerId);
                return Boolean.FALSE;
            }
            
            for (String name : queueNames) {
                if (name.equals(queueName)) {
                    logger.debug("[RabbitMQ][{}] queue {} exist on listener, returning true", listenerId, queueName);
                    return Boolean.TRUE;
                }
            }
            
            logger.debug("[RabbitMQ][{}] queue {} not exist on listener, returning false", listenerId, queueName);
            return Boolean.FALSE;
        } catch (Exception e) {
            logger.error("[RabbitMQ][E][{}] error on checking queue exist on listener, mess: ", listenerId, e.getMessage());
            logger.error("[RabbitMQ][E] trace: " + e.getStackTrace());
            return Boolean.FALSE;
        }
    }

    @Override
    public String createMessageListener(AbstractListener messageListener) {
        String listenerId = LISTENER_KEY + RandomStringGenerator.generateRandomString(9);
        logger.debug("[RabbitMQ][{}] creating listener", listenerId);
        messageListener.setListenerId(listenerId);
        SimpleRabbitListenerEndpoint endpoint = new SimpleRabbitListenerEndpoint();
        endpoint.setId(listenerId);
        endpoint.setMessageListener(messageListener);
        this.rabbitListenerEndpointRegistry.registerListenerContainer(endpoint, simpleRabbitListenerContainerFactory, true);
        logger.info("[RabbitMQ][{}] created listener", listenerId);

        return listenerId;
    }

    @Override
    public void deleteMessageListener(String listenerId) {
        rabbitListenerEndpointRegistry.unregisterListenerContainer(listenerId);
        logger.info("[RabbitMQ][{}] delete listener", listenerId);
    }

    @Override
    public boolean isQueueExist(String queue) {
        return rabbitAdmin.getQueueProperties(queue) != null;
    }

    private AbstractMessageListenerContainer getMessageListenerContainerById(String listenerId) {
        logger.debug("[RabbitMQ][{}] get listener", listenerId);
        return ((AbstractMessageListenerContainer) this.rabbitListenerEndpointRegistry
                .getListenerContainer(listenerId));
    }
}
