package vn.neo.rabbitmq.config;

import java.nio.charset.StandardCharsets;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.AbstractMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;

import com.google.gson.Gson;

public class GsonMessageConverter extends AbstractMessageConverter {

    private final Gson gson = new Gson();

    @Override
    protected Message createMessage(Object object, MessageProperties messageProperties) {
        byte[] bytes = null;
        if (object instanceof String) {
            bytes = ((String) object).getBytes();
        } else {
            bytes = gson.toJson(object).getBytes();
        }
        messageProperties.setContentType("application/json");
        messageProperties.setContentEncoding("UTF-8");
        messageProperties.setContentLength(bytes.length);
        return new Message(bytes, messageProperties);
    }

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        return new String(message.getBody(), StandardCharsets.UTF_8);
    }
}
