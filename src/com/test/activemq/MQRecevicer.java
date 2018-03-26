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
 * ԭʼ�Ĵ������±ߣ������Լ��Ĳ��Դ��룬�޸���ConnectionFactory��
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
        // ��MQ����
    	openConnection();
    	// ����session
        session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);  
        // �������ն���
        Queue queue = session.createQueue(QUEUE_NAME);  
//        QueueReceiver recevier = session.createReceiver(queue); 
        // ������Ϣ�����ߣ����շ��Ͷ˷���������Ϣ
        MessageConsumer consumer = session.createConsumer(queue);
          
        //ͬ����ʽ������Ϣ���ظ�  
//      TextMessage textMessage = (TextMessage) recevier.receive();  
//      System.out.println("������Ϣ : " + textMessage.getText() + "  JMSMessage" +textMessage.getJMSMessageID());   
//      Queue responseQueue = (Queue) textMessage.getJMSReplyTo();    
//      if(responseQueue != null){  
//          TextMessage responseMsg = session.createTextMessage();  
//          responseMsg.setJMSCorrelationID(textMessage.getJMSMessageID());  
//          responseMsg.setText("This message is reply from client..."+textMessage.getText());  
//          session.createSender(responseQueue).send(responseMsg);   
//          System.out.println("�ͻ��˻ظ����У�"+responseQueue.toString()+"  JMSCorrelation"+responseMsg.getJMSCorrelationID());    
//      }else{  
//          System.out.println("����˻ظ�����Ϊ��");    
//      }  
          
        //�첽��ʽ������Ϣ���ظ�  
//        recevier.setMessageListener(this);
        // ����Ϣ������consumer���ü��������������͹�������Ϣ
        consumer.setMessageListener(this);
//        while(!replyed)  
//            Thread.sleep(1000);  
//           
//        conn.stop();  
//        consumer.close();  
//        session.close();  
//        disConnection();  
    }  
    
    // ��Ϣ�����ߵļ����������շ��Ͷ˷���������Ϣ����׼������
    public void onMessage(Message message) {  
        try {  
        	// ���Ͷ˷��͹�������Ϣ
            String textMessage = ((TextMessage) message).getText();  
            System.out.println("������Ϣ : " + textMessage + "  JMSMessage" +message.getJMSMessageID());   
            // ��ȡ��Ϣ�еķ��ض��У����������ص���Ϣ����
            Queue responseQueue = (Queue) message.getJMSReplyTo();    
            if(responseQueue != null){  
            	// ������Ϣ���е���Ϣ����
                TextMessage responseMsg = session.createTextMessage();
                // ���û�Ӧ��Ϣ�Ĺ���ID������ID�����ڿͻ��˴��͹����Ĺ���ID
                responseMsg.setJMSCorrelationID(message.getJMSMessageID());  
                // ���÷�����Ϣ����
                responseMsg.setText("This message is reply from client��" + textMessage);  
//                session.createSender(responseQueue).send(responseMsg); 
                // ���ͷ�����Ϣ
                session.createProducer(responseQueue).send(responseMsg);   
                System.out.println("�ͻ��˻ظ����У�" + responseQueue + ", JMSCorrelationID = " + responseMsg.getJMSCorrelationID());    
            } else {  
                System.out.println("����˻ظ�����Ϊ��");    
            }  
              
        } catch (JMSException e) {  
            e.printStackTrace();  
        } finally {  
//            replyed = true;
        }  
    }  
      
    public static void main(String[] args) throws JMSException, InterruptedException {  
        MQRecevicer mr = new MQRecevicer();  
        System.out.println("���ڽ�����Ϣ...");  
        mr.recevicerMessage("��Ϣ�Ѿ��յ������ǽ��ն˵Ļظ���");  
        System.out.println("��Ϣ������ϣ�");  
    }  
}  

// ԭʼ�Ĵ���������
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
          
        //ͬ����ʽ������Ϣ���ظ�  
//      TextMessage textMessage = (TextMessage) recevier.receive();  
//      System.out.println("������Ϣ : " + textMessage.getText() + "  JMSMessage" +textMessage.getJMSMessageID());   
//      Queue responseQueue = (Queue) textMessage.getJMSReplyTo();    
//      if(responseQueue != null){  
//          TextMessage responseMsg = session.createTextMessage();  
//          responseMsg.setJMSCorrelationID(textMessage.getJMSMessageID());  
//          responseMsg.setText("This message is reply from client..."+textMessage.getText());  
//          session.createSender(responseQueue).send(responseMsg);   
//          System.out.println("�ͻ��˻ظ����У�"+responseQueue.toString()+"  JMSCorrelation"+responseMsg.getJMSCorrelationID());    
//      }else{  
//          System.out.println("����˻ظ�����Ϊ��");    
//      }  
          
        //�첽��ʽ������Ϣ���ظ�  
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
            System.out.println("������Ϣ : " + textMessage + "  JMSMessage" +message.getJMSMessageID());   
            Queue responseQueue = (Queue) message.getJMSReplyTo();    
            if(responseQueue != null){  
                TextMessage responseMsg = session.createTextMessage();  
                responseMsg.setJMSCorrelationID(message.getJMSMessageID());  
                responseMsg.setText("This message is reply from client��" + textMessage);  
                session.createSender(responseQueue).send(responseMsg);   
                System.out.println("�ͻ��˻ظ����У�" + responseQueue + " JMSCorrelation" + responseMsg.getJMSCorrelationID());    
            } else {  
                System.out.println("����˻ظ�����Ϊ��");    
            }  
              
        } catch (JMSException e) {  
            e.printStackTrace();  
        } finally {  
            replyed = true;  
        }  
    }  
      
    public static void main(String[] args) throws JMSException, InterruptedException {  
        MQRecevicer mr = new MQRecevicer();  
        System.out.println("���ڽ�����Ϣ...");  
        mr.recevicerMessage("��Ϣ�Ѿ��յ������ǽ��ն˵Ļظ���");  
        System.out.println("��Ϣ������ϣ�");  
    }  
}  */