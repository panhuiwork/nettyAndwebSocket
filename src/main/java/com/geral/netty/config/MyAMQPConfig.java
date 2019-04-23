package com.geral.netty.config;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.geral.netty.util.RabbitExchangeEnum;

/**
 * 设置消息传递格式
 * 
 * @author Panhui
 *
 */
@Configuration
public class MyAMQPConfig {

	/**
	 * 转换为json格式
	 * 
	 * @return
	 */
	@Bean
	public MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Autowired
	AmqpAdmin amqpAdmin;
	@Bean
	public void creatChange() {
		// 创建Exchange
		String exchangename=RabbitExchangeEnum.PUBLIC_EXCHANGE.getName();
		Exchange exchange = new TopicExchange(exchangename);
		amqpAdmin.declareExchange(exchange);
		System.out.println("DirectExchange创建完成");
		// 创建Queue
		String quename=RabbitExchangeEnum.PUBLIC_QUE.getName();
		amqpAdmin.declareQueue(new Queue(quename, true));
		// 创建绑定规则
		amqpAdmin.declareBinding(new Binding(quename, Binding.DestinationType.QUEUE,exchangename,quename, null));
	}
	
}
