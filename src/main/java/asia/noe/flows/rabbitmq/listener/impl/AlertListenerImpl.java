package asia.noe.flows.rabbitmq.listener.impl;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;

import com.google.gson.Gson;

import vn.neo.deliver.constant.DeliverCode;
import vn.neo.deliver.exception.DeliverException;
import vn.neo.deliver.scheduler.ProcessSchedulerPool;
import vn.neo.models.alert.AlertSC;
import vn.neo.rabbitmq.listener.AbstractListener;

/**
 * @author CuongNM
 *
 */
public class AlertListenerImpl extends AbstractListener {
    private Logger logger = LoggerFactory.getLogger(AlertListenerImpl.class);

    private String listenerId;
    private ProcessSchedulerPool processSchedulerPool;

    public AlertListenerImpl(ProcessSchedulerPool processSchedulerPool) {
        this.processSchedulerPool = processSchedulerPool;
    }

    @Override
    public void onMessage(Message message) {
        String queueName = null;
        String queuePhone = null;
        try {
            queueName = message.getMessageProperties().getConsumerQueue();

            Gson gson = new Gson();
            AlertSC asc = gson.fromJson(gson.fromJson(new String(message.getBody(), StandardCharsets.UTF_8), String.class), AlertSC.class);
            queuePhone = Optional.ofNullable(asc.getOa())
                    .orElseThrow(() -> new DeliverException(DeliverCode.NOT_FOUND, "oa trong ban tin alert-sc"));

            logger.info("[AlertListener][{}][{}] Received phone alert!", queueName, queuePhone);
            processSchedulerPool.executeAlert(queueName, queuePhone, asc);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[AlertListener][E][{}][{}] Exception, message: {}", queueName, queuePhone, e.getMessage());
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