package com.geral.netty.config;


import java.util.concurrent.ConcurrentHashMap;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class NettyChannelContext {
	
	public static ChannelGroup channelClient;
	
	
	public static ConcurrentHashMap<String,ChannelHandlerContext> nettyMap;
	static 
	{
		channelClient=new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
		nettyMap=new ConcurrentHashMap<String, ChannelHandlerContext>();
	}
	
	public static void addNettyChannel(String id,ChannelHandlerContext ctx) 
	{
		if(nettyMap.containsKey(id))return;
		nettyMap.put(id, ctx);
	}
	
	public static ChannelHandlerContext getChannel(String id) 
	{
//		if(!nettyMap.containsKey(id))return null;
		return nettyMap.get(id);
	}
	
	public static void removeChannel(String id) 
	{
		if(!nettyMap.containsKey(id))return;
		nettyMap.remove(id);
	}
}
