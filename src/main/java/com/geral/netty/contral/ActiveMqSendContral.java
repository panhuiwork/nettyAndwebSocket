package com.geral.netty.contral;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.geral.netty.entity.Result;
import com.geral.netty.util.RabbitExchangeEnum;

@RestController
public class ActiveMqSendContral {

	@Autowired
	RabbitTemplate rabbitTemplate;
	
	@GetMapping("/sendmsg")
	public Result sendMsg() 
	{
		Result res=new Result(200,"发送成功！",null);
		rabbitTemplate.convertAndSend(RabbitExchangeEnum.PUBLIC_EXCHANGE.getName(),RabbitExchangeEnum.PUBLIC_QUE.getName(),res);
		return res;
	}
}
