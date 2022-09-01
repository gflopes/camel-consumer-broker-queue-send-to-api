package br.com.gustavo.config;

import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.policy.JmsDefaultRedeliveryPolicy;
import org.apache.qpid.jms.policy.JmsRedeliveryPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class BrokerConfig {

	@Value("${broker.connect.uri}")
	private String brokerConnect;

	@Value("${broker.user}")
	private String brokerUser;

	@Value("${broker.password}")
	private String brokerPassword;

	@Primary
	@Bean(value = "jmsConnectionFactory")
	public PooledConnectionFactory jmsConnectionFactoryProduction() {
		return this.configConnectionFactory(brokerUser, brokerPassword);
	}

	public JmsConnectionFactory getJmsConnectionFactory(String user, String password) {
		final JmsDefaultRedeliveryPolicy redeliveryPolicy = new JmsDefaultRedeliveryPolicy();
		redeliveryPolicy.setMaxRedeliveries(0);

		final JmsConnectionFactory factory = new JmsConnectionFactory();

		factory.setRemoteURI(this.brokerConnect);
		factory.setUsername(user);
		factory.setPassword(password);
		factory.setPopulateJMSXUserID(true);
		factory.setValidatePropertyNames(false);

		return factory;
	}

	private PooledConnectionFactory configConnectionFactory(String user, String password) {
		final JmsConnectionFactory jmsConnectionFactory = this.getJmsConnectionFactory(user,password);

		final PooledConnectionFactory factory = new PooledConnectionFactory();
		factory.setIdleTimeout(2 * 60 * 1000);
		factory.setConnectionFactory(jmsConnectionFactory);
		factory.setReconnectOnException(true);
		factory.setMaxConnections(1);

		return factory;
	}
}
