package com.test.activemq;

import java.util.ResourceBundle;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.apache.log4j.Logger;

public class MQConnectionFactory {
	// ��־������
//    static Logger logger = Logger.getLogger(MQConnectionFactory.class);
	
	// �����������ݷ��͵��������MQ���У�������û������˴�topic�����������Ϣ
	private static String activemq_user = "admin";	// �û���
	private static String activemq_password = "admin";	// ����
	private static String activemq_url = "tcp://127.0.0.1:61616";	// ��������ַ
	
	private static PooledConnectionFactory pooledConnectionFactory = null;  
	private static int maximumActiveSessionPerConnection = 300;
	private static int maxConnections = 100;
	private static MQConnectionFactory m_Instance = null;
	
	public static MQConnectionFactory getInstance() {
		if(m_Instance == null) {
			synchronized(MQConnectionFactory.class){
				if(m_Instance == null) {
					m_Instance = new MQConnectionFactory();
				}
			}
		}
		return m_Instance;
	}
	private MQConnectionFactory(){
//		try {
//			// ��Ҫ����һ�����ӹ���Ȼ�����õ����ӳ���  
//	        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();  
//	        activeMQConnectionFactory.setUserName("admin");  
//	        activeMQConnectionFactory.setPassword("admin");  
//	        activeMQConnectionFactory.setBrokerURL("tcp://127.0.0.1:61616");  
//	        // ����һ�����ӳض��� 
//	        pooledConnectionFactory = new PooledConnectionFactory(); 
//	        // �������ӳض������������
//	        pooledConnectionFactory.setConnectionFactory(activeMQConnectionFactory);
//	        // �������ӳص�һЩ����
//	        pooledConnectionFactory.setMaxConnections(maxConnections);
//	        pooledConnectionFactory.setCreateConnectionOnStartup(true);
//	        pooledConnectionFactory.setMaximumActiveSessionPerConnection(maximumActiveSessionPerConnection);
//		} catch (Exception e) {
//			logger.error("����MQ���ӳ�ʧ�ܣ�" + e);
//		}
	}
	
	 /** 
     * ������ӳع��� 
     */  
    public PooledConnectionFactory getPooledConnectionFactory() {  
    	try {
			if(pooledConnectionFactory == null){
				// ��Ҫ����һ�����ӹ���Ȼ�����õ����ӳ���  
		        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();  
		        activeMQConnectionFactory.setUserName(activemq_user);  
		        activeMQConnectionFactory.setPassword(activemq_password);  
		        activeMQConnectionFactory.setBrokerURL(activemq_url);  
		        // ����һ�����ӳض��� 
		        pooledConnectionFactory = new PooledConnectionFactory(); 
		        // �������ӳض������������
		        pooledConnectionFactory.setConnectionFactory(activeMQConnectionFactory);
		        // �������ӳص�һЩ����
		        pooledConnectionFactory.setMaxConnections(maxConnections);
		        pooledConnectionFactory.setCreateConnectionOnStartup(true);
		        pooledConnectionFactory.setMaximumActiveSessionPerConnection(maximumActiveSessionPerConnection);
		        pooledConnectionFactory.setIdleTimeout(5000);	// ���г�ʱʱ�䣬���뼶
//		        pooledConnectionFactory.setExpiryTimeout(5000);
			}
		} catch (Exception e) {
//			logger.error("����MQ���ӳ�ʧ�ܣ�" + e);
			System.out.println("����MQ���ӳ�ʧ�ܣ�" + e);
		}
    	return pooledConnectionFactory;
    } 
	/**
	 *  ����Topic��Ϣ���������ݴ�����͵����MQ�ϣ�������û����ģ�������͸�����ʵʱ����
	 * @param originalData ԭʼ����
	 * @param newSubject �µ�Subject
	 */
	public static void sendTopicMsg(String data, String newTopic){
		try {
			// �����ӳش���һ�����Ӳ�����
			Connection connection = getInstance().getPooledConnectionFactory().createConnection();
			connection.start();
			
		    // ͨ�����ӻ�ȡsession����
		    Session session = connection.createSession(Boolean.TRUE, Session.AUTO_ACKNOWLEDGE);
		    // ����mq�������  
			Topic topic = session.createTopic(newTopic);
			// ����mq������
			MessageProducer producer = session.createProducer(topic);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			
			MapMessage message = session.createMapMessage();
			message.setString("data", data);
			producer.send(message);
			producer.close();
			session.commit();
			session.close();
			connection.close();
		} catch (Exception e) {
//			logger.error("sendTopicMsgʧ�ܣ�" + e);
			System.out.println("sendTopicMsgʧ�ܣ�" + e);
		}
	}
	
	/**
	 *  ����Topic��Ϣ���������ݴ�����͵����MQ�ϣ�������û����ģ�������͸�����ʵʱ����
	 * @param originalData ԭʼ����
	 * @param newSubject �µ�Subject
	 * @throws JMSException 
	 */
	public static void sendTopicTextMsg(String data, String newTopic) {
		Connection connection = null;  
        Session session = null;
		try {  
			// �����ӳش���һ�����Ӳ�����
			connection = getInstance().getPooledConnectionFactory().createConnection();
			connection.start();
		    // ͨ�����ӻ�ȡsession����
		    session = connection.createSession(Boolean.TRUE, Session.AUTO_ACKNOWLEDGE);
		    // ����mq�������
			Topic topic = session.createTopic(newTopic);
			// ����mq������
			MessageProducer producer = session.createProducer(topic);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			
			/*String iso = new String(originalData.getBytes("GBK"), "ISO-8859-1");
            TextMessage message = session.createTextMessage();
            message.setStringProperty("language", "java");
            message.setText(iso);*/
            
			TextMessage message = session.createTextMessage();
			message.setText(data);
			producer.send(message);
			message.clearBody();  
			message.clearProperties();   
			producer.close();
			// ����ʹ�õ��Լ��Ĺ����������ӳصģ���Ҫ���Ự�ر�  
	        // ������رգ���ʹ�����ӳص�ʱ���Կ���Ч������������ʱֻ�ܷ���һ�Σ���ɶ���  
			session.commit();
			session.close();
			// ʹ���Լ��Ĺ��������ӳص������ǣ����к��Լ��������ӵ��ùرճ������  
	        // ���������ӳ����ӽ��йر�ʵ����û�йرգ���Ϊ���ӳ�Ҫά��������� 
			connection.close();
//			logger.error("�����·���=====");
			System.out.println("�����·���=====");
			
		} catch (Exception e) {
//			logger.error("sendTopicTextMsgʧ�ܣ�" + e);
			System.out.println("sendTopicTextMsgʧ�ܣ�" + e);
			e.printStackTrace();
		} 
//		finally {  
//			if (session != null) {  
//                session.close();  
//            }
//			if (connection != null) {  
//                connection.close();  
//            }
//        } 
	}
	
    private void closeSession(Session session) {  
        try {  
            if (session != null) {  
                session.close();  
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
  
    private void closeConnection(Connection connection) {  
        try {  
            if (connection != null) {  
                connection.close();  
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
    
    /** 
     * �����������ʱֹͣ���� 
     */  
    @Override  
    protected void finalize() throws Throwable {  
        pooledConnectionFactory.stop();  
        super.finalize();  
    } 
}
