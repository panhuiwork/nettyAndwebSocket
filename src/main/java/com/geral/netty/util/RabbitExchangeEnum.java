package com.geral.netty.util;

import lombok.Getter;

@Getter
public enum RabbitExchangeEnum {

	PUBLIC_EXCHANGE("websocketNetty.topic"),PUBLIC_QUE("websocketNetty.que");

	private String name;
	RabbitExchangeEnum(String name)
	{
		this.name=name;
	}

}
