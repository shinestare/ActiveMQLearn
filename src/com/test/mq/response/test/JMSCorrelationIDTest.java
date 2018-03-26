package com.test.mq.response.test;

import javax.jms.Connection;  
import javax.jms.DeliveryMode;  
import javax.jms.JMSException;  
import javax.jms.Message;  
import javax.jms.MessageConsumer;  
import javax.jms.MessageListener;  
import javax.jms.MessageProducer;  
import javax.jms.Queue;  
import javax.jms.Session;  
import javax.jms.TextMessage;  
  
import org.apache.activemq.ActiveMQConnectionFactory;  
import org.apache.activemq.command.ActiveMQQueue;  
  
public class JMSCorrelationIDTest {  
    private Queue queue;  
    private Session session;  
  
    public JMSCorrelationIDTest() throws JMSException {  
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("vm://localhost");  
        Connection connection = factory.createConnection();  
        connection.start();  
        queue = new ActiveMQQueue("testQueue");  
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);  
        setupConsumer("ConsumerA");  
        setupConsumer("ConsumerB");  
        setupConsumer("ConsumerC");  
        setupProducer("ProducerA", "ConsumerA");  
        setupProducer("ProducerB", "ConsumerB");  
        setupProducer("ProducerC", "ConsumerC");  
    }  
  
    private void setupConsumer(final String name) throws JMSException {  
        // 创建一个消费者，它只接受属于它自己的消息  
        MessageConsumer consumer = session.createConsumer(queue, "receiver='" + name + "'");  
        consumer.setMessageListener(new MessageListener() {  
            public void onMessage(Message m) {  
                try {  
                    MessageProducer producer = session.createProducer(queue);  
                    System.out.println(name + " get:" + ((TextMessage) m).getText());  
                    // 回复一个消息  
                    Message replyMessage = session.createTextMessage("Reply from " + name);  
                    // 设置JMSCorrelationID为刚才收到的消息的ID  
                    replyMessage.setJMSCorrelationID(m.getJMSMessageID());  
                    producer.send(replyMessage);  
                } catch (JMSException e) {  
                }  
            }  
        });  
    }  
  
    private void setupProducer(final String name, String consumerName)  
            throws JMSException {  
        MessageProducer producer = session.createProducer(queue);  
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);  
        // 创建一个消息，并设置一个属性receiver，为消费者的名字。  
        Message message = session.createTextMessage("Message from " + name);  
        message.setStringProperty("receiver", consumerName);  
        producer.send(message);  
        // 等待回复的消息  
        MessageConsumer replyConsumer = session.createConsumer(queue,"JMSCorrelationID='" + message.getJMSMessageID() + "'");  
        replyConsumer.setMessageListener(new MessageListener() {  
            public void onMessage(Message m) {  
                try {  
                    System.out.println(name + " get reply:" + ((TextMessage) m).getText());  
                } catch (JMSException e) {  
                }  
            }  
        });  
    }  
  
    public static void main(String[] args) throws Exception {  
        new JMSCorrelationIDTest();  
    }  
}  
