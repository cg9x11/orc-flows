package asia.noe.flows.rabbitmq.services;

import asia.noe.flows.rabbitmq.listener.AbstractListener;

public interface RabbitQueueService {
    void addNewQueue(String queueName, String exchangeName, String routingKey);

    void deleteQueue(String queueName);

    void addQueueToListener(String listenerId, String queueName);

    void removeQueueFromListener(String listenerId, String queueName);
    
    void removeAndDeleteQueueFromListener(String listenerId, String queueName);
    
    String createMessageListener(AbstractListener messageListener);
    
    void deleteMessageListener(String listenerId);

    Boolean checkQueueExistOnListener(String listenerId, String queueName);

    boolean isQueueExist(String queue);

    public void start();

    public void stop();
}
