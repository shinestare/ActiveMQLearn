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
        // ��MQ����
    	openConnection();  
    	// ����session
        session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE); 
        // �������Ͷ���
        Queue queue = session.createQueue(QUEUE_NAME);  
        // �������ն���
        Queue responseQueue = session.createQueue(QUEUE_NAME2);  
//        QueueSender sender = session.createSender(queue);  
        // �������Ͷ˵���Ϣ������producer
        MessageProducer producer = session.createProducer(queue);  
        
        // ������Ϣ
        TextMessage msg = session.createTextMessage();   
//        msg.setJMSCorrelationID("123-123456");  
//        msg.setIntProperty("AccountID", 123);  
        //���ûظ�����  
        msg.setJMSReplyTo(responseQueue);   //���ûظ�����  
        // �Է��͵���Ϣ���ݸ�ֵ
        msg.setText(msgInfo);    
//        sender.send(msg); 
        // ���Ͷ���Ϣ�����߷�����Ϣ
        producer.send(msg);
        System.out.println("��Ϣ���� : JMSMessage" + msg.getJMSMessageID());  
          
        //���ջظ���Ϣ  
        System.out.println("�ȴ��ͻ��˻ظ����У�"+ msg.getJMSReplyTo());  
//        String filter = "JMSCorrelationID='" + msg.getJMSMessageID() + "'";    
//        QueueReceiver reply = session.createReceiver(responseQueue,filter);
        // ���Ͷ�׼�����շ��أ��������ض���,���õȴ���Ϣ���ص�JMSCorrelationID
        MessageConsumer consumer = session.createConsumer(responseQueue, "JMSCorrelationID='" + msg.getJMSMessageID() + "'");  
         
          
        //ͬ����ʽ�ȴ����ջظ�  
//        TextMessage resMsg = (TextMessage) reply.receive(60 * 1000);      
//        if(resMsg != null){   
//          System.out.println("�ͻ��˻ظ���Ϣ : " + resMsg.getText() + " JMSCorrelation" + resMsg.getJMSCorrelationID());   
//        }else{  
//          System.out.println("�ȴ���ʱ��");  
//        }  
                  
          
        //�첽��ʽ���ջظ�  
//       reply.setMessageListener(this);
        // ���շ�����Ϣ�ļ�����
        consumer.setMessageListener(this);
//       while(!replyed)  
//           Thread.sleep(1000);  
//          
//        conn.stop();  
//        producer.close();  
//        session.close();  
//        disConnection();  
    }  

    // ����������Ϣ�ļ�����
    public void onMessage(Message message) {  
        try {  
            String textMessage = ((TextMessage) message).getText();  
            System.out.println("�ͻ��˻ظ���Ϣ : " + textMessage+ " JMSCorrelation" + message.getJMSCorrelationID());            
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
            System.out.print("������Ϣ��");  
            ms.sendMessage(scan.next());  
            System.out.print("��Ϣ������ϣ�"); 
        }
    }  
      
}  

// ԭʼ�Ĵ������£�
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
        msg.setJMSReplyTo(responseQueue);   //���ûظ�����  
        msg.setText(msgInfo);    
        sender.send(msg);   
        System.out.println("��Ϣ���� : JMSMessage" + msg.getJMSMessageID());  
          
        //���ջظ���Ϣ  
        System.out.println("�ȴ��ͻ��˻ظ����У�"+ msg.getJMSReplyTo());  
        String filter = "JMSCorrelationID='" + msg.getJMSMessageID() + "'";    
        QueueReceiver reply = session.createReceiver(responseQueue,filter);  
         
          
        //ͬ����ʽ�ȴ����ջظ�  
//        TextMessage resMsg = (TextMessage) reply.receive(60 * 1000);      
//        if(resMsg != null){   
//          System.out.println("�ͻ��˻ظ���Ϣ : " + resMsg.getText() + " JMSCorrelation" + resMsg.getJMSCorrelationID());   
//        }else{  
//          System.out.println("�ȴ���ʱ��");  
//        }  
                  
          
        //�첽��ʽ���ջظ�  
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
            System.out.println("�ͻ��˻ظ���Ϣ : " + textMessage+ " JMSCorrelation" + message.getJMSCorrelationID());            
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
        System.out.print("������Ϣ��");  
        ms.sendMessage(scan.next());  
        System.out.print("��Ϣ������ϣ�");  
    }  
      
}  */