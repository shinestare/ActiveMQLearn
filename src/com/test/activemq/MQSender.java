package com.test.activemq;

import java.util.Scanner;

import javax.jms.Connection;
import javax.jms.JMSException;  
import javax.jms.Message;  
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;  
import javax.jms.MessageProducer;
import javax.jms.Queue;  
import javax.jms.QueueConnection;  
import javax.jms.QueueReceiver;  
import javax.jms.QueueSender;  
import javax.jms.QueueSession;  
import javax.jms.Session;  
import javax.jms.TextMessage;  
  
//import com.ibm.mq.jms.MQQueueConnectionFactory;  
  
public class MQSender implements MessageListener{  
  
    Connection conn;  
    Session session;
      
    final String QUEUE_NAME = "Q1";  
    final String QUEUE_NAME2 = "Q2";  
    boolean replyed = false;  
      
    private void openConnection() throws JMSException {  
    	conn = MQConnectionFactory.getInstance().getPooledConnectionFactory().createConnection();  
        conn.start();        
    }  
      
      
    private void sendMessage(String msgInfo) throws JMSException, InterruptedException {  
        // 打开MQ连接
    	openConnection();  
    	// 常见session
        session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE); 
        // 创建发送队列
        Queue queue = session.createQueue(QUEUE_NAME);  
        // 创建接收队列
        Queue responseQueue = session.createQueue(QUEUE_NAME2);  
//        QueueSender sender = session.createSender(queue);  
        // 创建发送端的消息生产者producer
        MessageProducer producer = session.createProducer(queue);  
        
        // 创建消息
        TextMessage msg = session.createTextMessage();   
//        msg.setJMSCorrelationID("123-123456");  
//        msg.setIntProperty("AccountID", 123);  
        //设置回复队列  
        msg.setJMSReplyTo(responseQueue);   //设置回复队列  
        // 对发送的消息内容赋值
        msg.setText(msgInfo);    
//        sender.send(msg); 
        // 发送端消息生产者发送消息
        producer.send(msg);
        System.out.println("消息发送 : JMSMessage" + msg.getJMSMessageID());  
          
        //接收回复信息  
        System.out.println("等待客户端回复队列："+ msg.getJMSReplyTo());  
//        String filter = "JMSCorrelationID='" + msg.getJMSMessageID() + "'";    
//        QueueReceiver reply = session.createReceiver(responseQueue,filter);
        // 发送端准备接收返回，监听返回队列,设置等待消息返回的JMSCorrelationID
        MessageConsumer consumer = session.createConsumer(responseQueue, "JMSCorrelationID='" + msg.getJMSMessageID() + "'");  
         
          
        //同步方式等待接收回复  
//        TextMessage resMsg = (TextMessage) reply.receive(60 * 1000);      
//        if(resMsg != null){   
//          System.out.println("客户端回复消息 : " + resMsg.getText() + " JMSCorrelation" + resMsg.getJMSCorrelationID());   
//        }else{  
//          System.out.println("等待超时！");  
//        }  
                  
          
        //异步方式接收回复  
//       reply.setMessageListener(this);
        // 接收返回消息的监听器
        consumer.setMessageListener(this);
//       while(!replyed)  
//           Thread.sleep(1000);  
//          
//        conn.stop();  
//        producer.close();  
//        session.close();  
//        disConnection();  
    }  

    // 监听返回消息的监听器
    public void onMessage(Message message) {  
        try {  
            String textMessage = ((TextMessage) message).getText();  
            System.out.println("客户端回复消息 : " + textMessage+ " JMSCorrelation" + message.getJMSCorrelationID());            
        } catch (JMSException e) {  
            e.printStackTrace();  
        }finally{  
            replyed = true;   
        }  
    }  
      
    private void disConnection() throws JMSException {        
        conn.close();   
    }  
      
      
    public static void main(String[] args) throws JMSException, InterruptedException {  
    	MQSender ms = new MQSender();  
        Scanner scan = new Scanner(System.in);  
        while (true) {
            System.out.print("输入信息：");  
            ms.sendMessage(scan.next());  
            System.out.print("消息发送完毕！"); 
        }
    }  
      
}  

// 原始的代码如下；
/*import com.ibm.mq.jms.MQQueueConnectionFactory;  
public class MQSender implements MessageListener{  
  
    MQQueueConnectionFactory mcf;  
    QueueConnection qconn;  
      
    final String HOSTNAME = "127.0.0.1";  
    final int PORT = 1414;  
    final String QUEUEMANAGER_NAME = "QM1";  
    final String QUEUE_NAME = "Q1";  
    final String QUEUE_NAME2 = "Q2";  
    boolean replyed = false;  
      
    private void openConnection() throws JMSException {  
        mcf = new MQQueueConnectionFactory();  
        mcf.setHostName(HOSTNAME);  
        mcf.setPort(PORT);  
        mcf.setQueueManager(QUEUEMANAGER_NAME);  
        qconn = mcf.createQueueConnection();  
        qconn.start();        
    }  
      
      
    private void sendMessage(String msgInfo) throws JMSException, InterruptedException {  
        openConnection();  
        QueueSession session = qconn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);  
        Queue queue = session.createQueue(QUEUE_NAME);  
        Queue responseQueue = session.createQueue(QUEUE_NAME2);  
        QueueSender sender = session.createSender(queue);  
          
        TextMessage msg = session.createTextMessage();   
//        msg.setJMSCorrelationID("123-123456");  
//        msg.setIntProperty("AccountID", 123);  
        msg.setJMSReplyTo(responseQueue);   //设置回复队列  
        msg.setText(msgInfo);    
        sender.send(msg);   
        System.out.println("消息发送 : JMSMessage" + msg.getJMSMessageID());  
          
        //接收回复信息  
        System.out.println("等待客户端回复队列："+ msg.getJMSReplyTo());  
        String filter = "JMSCorrelationID='" + msg.getJMSMessageID() + "'";    
        QueueReceiver reply = session.createReceiver(responseQueue,filter);  
         
          
        //同步方式等待接收回复  
//        TextMessage resMsg = (TextMessage) reply.receive(60 * 1000);      
//        if(resMsg != null){   
//          System.out.println("客户端回复消息 : " + resMsg.getText() + " JMSCorrelation" + resMsg.getJMSCorrelationID());   
//        }else{  
//          System.out.println("等待超时！");  
//        }  
                  
          
        //异步方式接收回复  
       reply.setMessageListener(this);  
       while(!replyed)  
           Thread.sleep(1000);  
          
        qconn.stop();  
        sender.close();  
        session.close();  
        disConnection();  
    }  
      
    public void onMessage(Message message) {  
        try {  
            String textMessage = ((TextMessage) message).getText();  
            System.out.println("客户端回复消息 : " + textMessage+ " JMSCorrelation" + message.getJMSCorrelationID());            
        } catch (JMSException e) {  
            e.printStackTrace();  
        }finally{  
            replyed = true;   
        }  
    }  
      
    private void disConnection() throws JMSException {        
        qconn.close();   
    }  
      
      
    public static void main(String[] args) throws JMSException, InterruptedException {  
        MQSender ms = new MQSender();  
        Scanner scan = new Scanner(System.in);  
        System.out.print("输入信息：");  
        ms.sendMessage(scan.next());  
        System.out.print("消息发送完毕！");  
    }  
      
}  */