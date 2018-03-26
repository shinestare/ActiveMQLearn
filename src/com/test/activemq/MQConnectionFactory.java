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
	// 日志操作类
//    static Logger logger = Logger.getLogger(MQConnectionFactory.class);
	
	// 将适配后的数据发送到以下这个MQ队列，如果有用户订阅了此topic则给其推送消息
	private static String activemq_user = "admin";	// 用户名
	private static String activemq_password = "admin";	// 密码
	private static String activemq_url = "tcp://127.0.0.1:61616";	// 服务器地址
	
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
//			// 需要创建一个链接工厂然后设置到连接池中  
//	        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();  
//	        activeMQConnectionFactory.setUserName("admin");  
//	        activeMQConnectionFactory.setPassword("admin");  
//	        activeMQConnectionFactory.setBrokerURL("tcp://127.0.0.1:61616");  
//	        // 创建一个连接池对象 
//	        pooledConnectionFactory = new PooledConnectionFactory(); 
//	        // 设置连接池对象的连接属性
//	        pooledConnectionFactory.setConnectionFactory(activeMQConnectionFactory);
//	        // 设置连接池的一些参数
//	        pooledConnectionFactory.setMaxConnections(maxConnections);
//	        pooledConnectionFactory.setCreateConnectionOnStartup(true);
//	        pooledConnectionFactory.setMaximumActiveSessionPerConnection(maximumActiveSessionPerConnection);
//		} catch (Exception e) {
//			logger.error("建立MQ连接池失败：" + e);
//		}
	}
	
	 /** 
     * 获得链接池工厂 
     */  
    public PooledConnectionFactory getPooledConnectionFactory() {  
    	try {
			if(pooledConnectionFactory == null){
				// 需要创建一个链接工厂然后设置到连接池中  
		        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();  
		        activeMQConnectionFactory.setUserName(activemq_user);  
		        activeMQConnectionFactory.setPassword(activemq_password);  
		        activeMQConnectionFactory.setBrokerURL(activemq_url);  
		        // 创建一个连接池对象 
		        pooledConnectionFactory = new PooledConnectionFactory(); 
		        // 设置连接池对象的连接属性
		        pooledConnectionFactory.setConnectionFactory(activeMQConnectionFactory);
		        // 设置连接池的一些参数
		        pooledConnectionFactory.setMaxConnections(maxConnections);
		        pooledConnectionFactory.setCreateConnectionOnStartup(true);
		        pooledConnectionFactory.setMaximumActiveSessionPerConnection(maximumActiveSessionPerConnection);
		        pooledConnectionFactory.setIdleTimeout(5000);	// 空闲超时时间，毫秒级
//		        pooledConnectionFactory.setExpiryTimeout(5000);
			}
		} catch (Exception e) {
//			logger.error("建立MQ连接池失败：" + e);
			System.out.println("建立MQ连接池失败：" + e);
		}
    	return pooledConnectionFactory;
    } 
	/**
	 *  发送Topic消息，用于数据处理后发送到这个MQ上，如果有用户订阅，则会推送给他们实时数据
	 * @param originalData 原始数据
	 * @param newSubject 新的Subject
	 */
	public static void sendTopicMsg(String data, String newTopic){
		try {
			// 从连接池创建一个连接并启动
			Connection connection = getInstance().getPooledConnectionFactory().createConnection();
			connection.start();
			
		    // 通过连接获取session对象
		    Session session = connection.createSession(Boolean.TRUE, Session.AUTO_ACKNOWLEDGE);
		    // 创建mq主题对象  
			Topic topic = session.createTopic(newTopic);
			// 创建mq消费者
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
//			logger.error("sendTopicMsg失败：" + e);
			System.out.println("sendTopicMsg失败：" + e);
		}
	}
	
	/**
	 *  发送Topic消息，用于数据处理后发送到这个MQ上，如果有用户订阅，则会推送给他们实时数据
	 * @param originalData 原始数据
	 * @param newSubject 新的Subject
	 * @throws JMSException 
	 */
	public static void sendTopicTextMsg(String data, String newTopic) {
		Connection connection = null;  
        Session session = null;
		try {  
			// 从连接池创建一个连接并启动
			connection = getInstance().getPooledConnectionFactory().createConnection();
			connection.start();
		    // 通过连接获取session对象
		    session = connection.createSession(Boolean.TRUE, Session.AUTO_ACKNOWLEDGE);
		    // 创建mq主题对象
			Topic topic = session.createTopic(newTopic);
			// 创建mq消费者
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
			// 无论使用的自己的工厂还是连接池的，都要将会话关闭  
	        // 如果不关闭，在使用连接池的时可以看到效果，发送两次时只能发送一次，造成堵塞  
			session.commit();
			session.close();
			// 使用自己的工厂和连接池的区别是，运行后自己工厂链接调用关闭程序结束  
	        // 而调用连接池链接进行关闭实际上没有关闭，因为连接池要维护这个链接 
			connection.close();
//			logger.error("数据下发了=====");
			System.out.println("数据下发了=====");
			
		} catch (Exception e) {
//			logger.error("sendTopicTextMsg失败：" + e);
			System.out.println("sendTopicTextMsg失败：" + e);
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
     * 对象回收销毁时停止链接 
     */  
    @Override  
    protected void finalize() throws Throwable {  
        pooledConnectionFactory.stop();  
        super.finalize();  
    } 
}
