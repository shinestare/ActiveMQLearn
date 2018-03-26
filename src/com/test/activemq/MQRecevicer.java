package com.test.activemq;

import javax.jms.Connection;
import javax.jms.JMSException;  
import javax.jms.Message;  
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;  
import javax.jms.Queue;  
import javax.jms.QueueConnection;  
import javax.jms.QueueReceiver;  
import javax.jms.QueueSession;  
import javax.jms.Session;  
import javax.jms.TextMessage;  
  
//import com.ibm.mq.jms.MQQueueConnectionFactory;  

/**
 * 
 * 原始的代码在下边，这是自己的测试代码，修改了ConnectionFactory等
 * @author wang
 *
 */
public class MQRecevicer implements MessageListener{  
  
    Connection conn;  
    Session session;
      
    final String QUEUE_NAME = "Q1";  
    final String QUEUE_NAME2 = "Q2";  
    boolean replyed = false;  
      
    public void openConnection() throws JMSException {  
        conn = MQConnectionFactory.getInstance().getPooledConnectionFactory().createConnection();  
        conn.start();        
    }  
      
    public void disConnection() throws JMSException {         
        conn.close();   
    }  
      
    public void recevicerMessage(String reply) throws JMSException, InterruptedException {  
        // 打开MQ连接
    	openConnection();
    	// 建立session
        session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);  
        // 创建接收队列
        Queue queue = session.createQueue(QUEUE_NAME);  
//        QueueReceiver recevier = session.createReceiver(queue); 
        // 创建消息消费者，接收发送端发过来的消息
        MessageConsumer consumer = session.createConsumer(queue);
          
        //同步方式接收消息并回复  
//      TextMessage textMessage = (TextMessage) recevier.receive();  
//      System.out.println("接收消息 : " + textMessage.getText() + "  JMSMessage" +textMessage.getJMSMessageID());   
//      Queue responseQueue = (Queue) textMessage.getJMSReplyTo();    
//      if(responseQueue != null){  
//          TextMessage responseMsg = session.createTextMessage();  
//          responseMsg.setJMSCorrelationID(textMessage.getJMSMessageID());  
//          responseMsg.setText("This message is reply from client..."+textMessage.getText());  
//          session.createSender(responseQueue).send(responseMsg);   
//          System.out.println("客户端回复队列："+responseQueue.toString()+"  JMSCorrelation"+responseMsg.getJMSCorrelationID());    
//      }else{  
//          System.out.println("服务端回复队列为空");    
//      }  
          
        //异步方式接收消息并回复  
//        recevier.setMessageListener(this);
        // 对消息消费者consumer设置监听器，监听发送过来的消息
        consumer.setMessageListener(this);
//        while(!replyed)  
//            Thread.sleep(1000);  
//           
//        conn.stop();  
//        consumer.close();  
//        session.close();  
//        disConnection();  
    }  
    
    // 消息消费者的监听器，接收发送端发过来的消息，并准备返回
    public void onMessage(Message message) {  
        try {  
        	// 发送端发送过来的消息
            String textMessage = ((TextMessage) message).getText();  
            System.out.println("接收消息 : " + textMessage + "  JMSMessage" +message.getJMSMessageID());   
            // 读取消息中的返回队列，并创建返回的消息队列
            Queue responseQueue = (Queue) message.getJMSReplyTo();    
            if(responseQueue != null){  
            	// 返回消息队列的消息内容
                TextMessage responseMsg = session.createTextMessage();
                // 设置回应消息的关联ID，关联ID来自于客户端传送过来的关联ID
                responseMsg.setJMSCorrelationID(message.getJMSMessageID());  
                // 设置返回消息内容
                responseMsg.setText("This message is reply from client：" + textMessage);  
//                session.createSender(responseQueue).send(responseMsg); 
                // 发送返回消息
                session.createProducer(responseQueue).send(responseMsg);   
                System.out.println("客户端回复队列：" + responseQueue + ", JMSCorrelationID = " + responseMsg.getJMSCorrelationID());    
            } else {  
                System.out.println("服务端回复队列为空");    
            }  
              
        } catch (JMSException e) {  
            e.printStackTrace();  
        } finally {  
//            replyed = true;
        }  
    }  
      
    public static void main(String[] args) throws JMSException, InterruptedException {  
        MQRecevicer mr = new MQRecevicer();  
        System.out.println("正在接收消息...");  
        mr.recevicerMessage("消息已经收到，这是接收端的回复！");  
        System.out.println("消息接收完毕！");  
    }  
}  

// 原始的代码在下面
//import com.ibm.mq.jms.MQQueueConnectionFactory; 
/*public class MQRecevicer implements MessageListener{  
	  
    MQQueueConnectionFactory mcf;  
    QueueConnection qconn;  
    QueueSession session;  
      
    final String HOSTNAME = "127.0.0.1";  
    final int PORT = 1414;  
    final String QUEUEMANAGER_NAME = "QM1";  
    final String QUEUE_NAME = "Q1";  
    final String QUEUE_NAME2 = "Q2";  
    boolean replyed = false;  
      
    public void openConnection() throws JMSException {  
        mcf = new MQQueueConnectionFactory();  
        mcf.setHostName(HOSTNAME);  
        mcf.setPort(PORT);  
        mcf.setQueueManager(QUEUEMANAGER_NAME);  
        qconn = mcf.createQueueConnection();  
        qconn.start();        
    }  
      
    public void disConnection() throws JMSException {         
        qconn.close();   
    }  
      
    public void recevicerMessage(String reply) throws JMSException, InterruptedException {  
        openConnection();   
        session = qconn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);  
        Queue queue = session.createQueue(QUEUE_NAME);  
        QueueReceiver recevier = session.createReceiver(queue);  
          
        //同步方式接收消息并回复  
//      TextMessage textMessage = (TextMessage) recevier.receive();  
//      System.out.println("接收消息 : " + textMessage.getText() + "  JMSMessage" +textMessage.getJMSMessageID());   
//      Queue responseQueue = (Queue) textMessage.getJMSReplyTo();    
//      if(responseQueue != null){  
//          TextMessage responseMsg = session.createTextMessage();  
//          responseMsg.setJMSCorrelationID(textMessage.getJMSMessageID());  
//          responseMsg.setText("This message is reply from client..."+textMessage.getText());  
//          session.createSender(responseQueue).send(responseMsg);   
//          System.out.println("客户端回复队列："+responseQueue.toString()+"  JMSCorrelation"+responseMsg.getJMSCorrelationID());    
//      }else{  
//          System.out.println("服务端回复队列为空");    
//      }  
          
        //异步方式接收消息并回复  
        recevier.setMessageListener(this);  
        while(!replyed)  
            Thread.sleep(1000);  
           
        qconn.stop();  
        recevier.close();  
        session.close();  
        disConnection();  
    }  
      
    public void onMessage(Message message) {  
        try {  
            String textMessage = ((TextMessage) message).getText();  
            System.out.println("接收消息 : " + textMessage + "  JMSMessage" +message.getJMSMessageID());   
            Queue responseQueue = (Queue) message.getJMSReplyTo();    
            if(responseQueue != null){  
                TextMessage responseMsg = session.createTextMessage();  
                responseMsg.setJMSCorrelationID(message.getJMSMessageID());  
                responseMsg.setText("This message is reply from client：" + textMessage);  
                session.createSender(responseQueue).send(responseMsg);   
                System.out.println("客户端回复队列：" + responseQueue + " JMSCorrelation" + responseMsg.getJMSCorrelationID());    
            } else {  
                System.out.println("服务端回复队列为空");    
            }  
              
        } catch (JMSException e) {  
            e.printStackTrace();  
        } finally {  
            replyed = true;  
        }  
    }  
      
    public static void main(String[] args) throws JMSException, InterruptedException {  
        MQRecevicer mr = new MQRecevicer();  
        System.out.println("正在接收消息...");  
        mr.recevicerMessage("消息已经收到，这是接收端的回复！");  
        System.out.println("消息接收完毕！");  
    }  
}  */