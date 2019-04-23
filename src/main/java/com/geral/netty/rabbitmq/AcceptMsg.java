package com.geral.netty.rabbitmq;

import java.time.LocalDate;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.geral.netty.config.NettyChannelContext;
import com.geral.netty.entity.Result;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;


@Component
public class AcceptMsg {
	

	
	@RabbitHandler
	@RabbitListener(queues="websocketNetty.que")
	public void getMsg(Result res) 
	{
		System.out.println("收到消息："+res.getMsg()+"状态编码："+res.getCode());
		
//		NettyChannelContext.getChannel("");
//		System.out.println(NettyChannelContext.nettyMap.size());
	
		//自带通道
//		NettyChannelContext.channelClient.writeAndFlush(new TextWebSocketFrame("【服务器于 " + LocalDate.now() + "发送消息：】 ，消息内容为：" +res.getMsg()));

		
		//我定义的ConcurrmentHashmap
		//		for(ChannelHandlerContext ctx:NettyChannelContext.nettyMap.values()) 
//		{
////			ctx.writeAndFlush(new TextWebSocketFrame("【服务器于 " + LocalDate.now() + "发送消息：】 ，消息内容为：xxxxx" ));
////			ctx.writeAndFlush("MSG");
//		}
		
	}
}
