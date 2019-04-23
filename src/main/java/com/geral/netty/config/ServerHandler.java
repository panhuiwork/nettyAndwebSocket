package com.geral.netty.config;

import java.time.LocalDate;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

//TextWebSocketFrame：处理消息的handler，在Netty中用于处理文本的对象，frames是消息的载体
//@Configuration
public class ServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
	// 用于记录和管理所有客户端的channel，可以把相应的channel保存到一整个组中
	// DefaultChannelGroup：用于对应ChannelGroup，进行初始化
//	ChannelGroup channelClient new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		System.out.println("channelActive----->");
	}



	// 当客户端连接服务端（或者是打开连接之后）
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		System.out.println("当客户端连接服务端");
		System.out.println("channel的长ID：" + ctx.channel().id().asLongText());
		System.out.println("channel的短ID：" + ctx.channel().id().asShortText());
//		channelClient().add(ctx.channel());
		NettyChannelContext.channelClient.add(ctx.channel());
		NettyChannelContext.addNettyChannel(ctx.channel().id().asLongText(), ctx);
	}

	// 客户端断开
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		// 实际上是多余的，只要handler被移除，client会自动的把对应的channel移除掉
		System.out.println("客户端断开");
		System.out.println("channel的长ID：" + ctx.channel().id().asLongText());
		System.out.println("channel的短ID：" + ctx.channel().id().asShortText());
		NettyChannelContext.channelClient.remove(ctx.channel());
		NettyChannelContext.removeChannel(ctx.channel().id().asLongText());
//		channelClient().remove(ctx.channel());
		// 每一而channel都会有一个长ID与短ID
		// 一开始channel就有了，系统会自动分配一串很长的字符串作为唯一的ID，如果使用asLongText()获取的ID是唯一的，asShortText()会把当前ID进行精简，精简过后可能会有重复
		
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
		//如果存在channelRead 就不会走这个方法
		String content = msg.text();
		System.out.println("客户端传输的数据：" + content);
//		//只针对当前频道接听
		ctx.writeAndFlush(new TextWebSocketFrame("【服务器于 " + LocalDate.now() + "接收到消息：】 ，消息内容为：" +content));
//		//这里用来所有前端接听消息
//		NettyChannelContext.getChannel(ctx.channel().id().asLongText()).writeAndFlush(new TextWebSocketFrame("【服务器于 " + LocalDate.now() + "接收到消息：】 ，消息内容为：" +content));
	}
	

//	@Override
//	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//		// TODO Auto-generated method stub
//		System.out.println("============2===========");
//		String content = ((TextWebSocketFrame)msg).text();
//		System.out.println("客户端传输的数据："+content);
//		
//		//只针对当前频道接听
//		ctx.writeAndFlush(new TextWebSocketFrame("【服务器于 " + LocalDate.now() + "接收到消息：】 ，消息内容为：" +content));
//		//这里用来所有前端接听消息
//		//		channelClient.writeAndFlush(new TextWebSocketFrame("【服务器于 " + LocalDate.now() + "接收到消息：】 ，消息内容为：" +content));
//	}
}