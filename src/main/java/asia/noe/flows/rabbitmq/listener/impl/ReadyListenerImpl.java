package asia.noe.flows.rabbitmq.listener.impl;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;

import com.google.gson.Gson;

import asia.noe.flows.rabbitmq.listener.AbstractListener;


/**
 * @author CuongNM
 *
 */
public class ReadyListenerImpl extends AbstractListener {
    private Logger logger = LoggerFactory.getLogger(ReadyListenerImpl.class);

    private String listenerId;
    private String processQueue;
    private ProcessSchedulerPool processSchedulerPool;

    public ReadyListenerImpl(String processQueue, ProcessSchedulerPool processSchedulerPool) {
        this.processSchedulerPool = processSchedulerPool;
        this.processQueue = processQueue;
    }

    @Override
    public void onMessage(Message message) {
        String queueName = null;
        String queuePhone = null;
        try {
            Gson gson = new Gson();
            queueName = message.getMessageProperties().getConsumerQueue();
            queuePhone = gson.fromJson(new String(message.getBody(), StandardCharsets.UTF_8), String.class);
            logger.info("[ReadyListener][{}][{}] Received phone!", queueName, queuePhone);
            processSchedulerPool.execute(queuePhone, processQueue, queuePhone);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[ReadyListener][E][{}][{}] Exception, message: {}", queueName, queuePhone, e.getMessage());
        }
    }

    @Override
    public void setListenerId(String listenerId) {
        this.listenerId = listenerId;
    }

    @Override
    public String getListenerId() {
        return listenerId;
    }
}