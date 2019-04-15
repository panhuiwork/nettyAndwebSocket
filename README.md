# nettyAndwebSocket
基于springboot开发netty.

前端websocket
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8" />
		<title>Netty+WebSocket案例</title>
	</head>
	<body>
		<div id="">发送消息：</div><br>
		<input type="text" name="messageContent" id="messageContent"/>
		<input type="button" name="" id="" value="发送" onclick="CHAT.chat()"/>
		
		<hr>
		
		<div id="">接收消息：</div><br>
		<div id="receiveNsg" style="background-color: gainsboro;"></div>
		<script type="text/javascript">
			window.CHAT = {
				socket: null,
				//初始化
				init: function(){
					//首先判断浏览器是否支持WebSocket
					if (window.WebSocket){
						CHAT.socket = new WebSocket("ws://localhost:7000/ws");
						CHAT.socket.onopen = function(){
							console.log("客户端与服务端建立连接成功");
						},
						CHAT.socket.onmessage = function(e){
							console.log("接收到消息："+e.data);
							var receiveNsg = window.document.getElementById("receiveNsg");
							var html = receiveNsg.innerHTML;
							receiveNsg.innerHTML = html + "<br>" + e.data; 
						},
						CHAT.socket.onerror = function(){
							console.log("发生错误");
						},
						CHAT.socket.onclose = function(){
							console.log("客户端与服务端关闭连接成功");
						}						
					}else{
						alert("升级下浏览器吧");
					}
				},
				chat: function(){
					var msg = window.document.getElementById("messageContent");
					CHAT.socket.send(msg.value);
				}
			}
			
			CHAT.init();
			
		</script>
		
	</body>
</html>
后端springboot+netty实现

package com.geral.netty.config;

import java.net.InetSocketAddress;

import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class NettyServer {
    public void start(InetSocketAddress address){
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(address)
                    .childHandler(new ServerChannelInitializer())
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            // 绑定端口，开始接收进来的连接
            ChannelFuture future = bootstrap.bind(address).sync();
            log.info("Server start listen at " + address.getPort());
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

package com.geral.netty.config;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;

public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
	@Override
	protected void initChannel(SocketChannel channel) throws Exception {
//        channel.pipeline().addLast("decoder",new StringDecoder(CharsetUtil.UTF_8));
//        channel.pipeline().addLast("encoder",new StringEncoder(CharsetUtil.UTF_8));
		// websocket需要添加这个处理

		ChannelPipeline channelPipeline = channel.pipeline();
		// 添加相应的助手类与处理器
		/**
		 * WebSokect基于Http，所以要有相应的Http编解码器，HttpServerCodec()
		 */
		channelPipeline.addLast(new HttpServerCodec());

		// 在Http中有一些数据流的传输，那么数据流有大有小，如果说有一些相应的大数据流处理的话，需要在此添加
		// ChunkedWriteHandler：为一些大数据流添加支持
		channelPipeline.addLast(new ChunkedWriteHandler());

		// UdineHttpMessage进行处理，也就是会用到request以及response
		// HttpObjectAggregator：聚合器，聚合了FullHTTPRequest、FullHTTPResponse。。。，当你不想去管一些HttpMessage的时候，直接把这个handler丢到管道中，让Netty自行处理即可
		channelPipeline.addLast(new HttpObjectAggregator(2048 * 64));

		// ================华丽的分割线：以上是用于支持Http协议================
		// ================华丽的分割线：以下是用于支持WebSoket==================

		// /ws：一开始建立连接的时候会使用到，可自定义
		// WebSocketServerProtocolHandler：给客户端指定访问的路由（/ws），是服务器端处理的协议，当前的处理器处理一些繁重的复杂的东西，运行在一个WebSocket服务端
		// 另外也会管理一些握手的动作：handshaking(close，ping，pong) ping + pong =
		// 心跳，对于WebSocket来讲，是以frames进行传输的，不同的数据类型对应的frames也不同
		channelPipeline.addLast(new WebSocketServerProtocolHandler("/ws"));

		// 添加自动handler，读取客户端消息并进行处理，处理完毕之后将相应信息传输给对应客户端
		channelPipeline.addLast(new ServerHandler());
	}
}

package com.geral.netty.config;

import java.time.LocalDate;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

//TextWebSocketFrame：处理消息的handler，在Netty中用于处理文本的对象，frames是消息的载体
public class ServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
	// 用于记录和管理所有客户端的channel，可以把相应的channel保存到一整个组中
	// DefaultChannelGroup：用于对应ChannelGroup，进行初始化
	private static ChannelGroup channelClient = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		System.out.println("channelActive----->");
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


	// 当客户端连接服务端（或者是打开连接之后）
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		System.out.println("当客户端连接服务端");
		channelClient.add(ctx.channel());
	}

	// 客户端断开
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		// 实际上是多余的，只要handler被移除，client会自动的把对应的channel移除掉
		System.out.println("客户端断开");
		channelClient.remove(ctx.channel());
		// 每一而channel都会有一个长ID与短ID
		// 一开始channel就有了，系统会自动分配一串很长的字符串作为唯一的ID，如果使用asLongText()获取的ID是唯一的，asShortText()会把当前ID进行精简，精简过后可能会有重复
		System.out.println("channel的长ID：" + ctx.channel().id().asLongText());
		System.out.println("channel的短ID：" + ctx.channel().id().asShortText());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
		System.out.println("============1===========");
		//如果存在channelRead 就不会走这个方法
		String content = msg.text();
		System.out.println("客户端传输的数据：" + content);
//		//只针对当前频道接听
//		ctx.writeAndFlush(new TextWebSocketFrame("【服务器于 " + LocalDate.now() + "接收到消息：】 ，消息内容为：" +content));
//		//这里用来所有前端接听消息
		channelClient.writeAndFlush(new TextWebSocketFrame("【服务器于 " + LocalDate.now() + "接收到消息：】 ，消息内容为：" +content));
		
	}
}

package com.geral.netty;

import java.net.InetSocketAddress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.geral.netty.config.NettyServer;

@SpringBootApplication
//http://localhost:8080/index.html
public class NettyAndwebSocketApplication  implements CommandLineRunner{
	@Value("${netty.port}")
	private int port;

	@Value("${netty.url}")
	private String url;
	@Autowired
	private NettyServer server;

	public static void main(String[] args) {
		SpringApplication.run(NettyAndwebSocketApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		InetSocketAddress address = new InetSocketAddress(url, port);
		System.out.println("run  .... . ... " + url);
		server.start(address);
	}

}

application.yml
netty:
  port: 7000
  url: localhost



