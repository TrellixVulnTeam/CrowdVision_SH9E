package com.tcc.CrowdVision.Controller.WebSocket;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.tcc.CrowdVision.Server.Detection.DetectionManager;

@Component
public class SocketDetectionHandler extends TextWebSocketHandler{

	static List<WebSocketSession> sessions = new CopyOnWriteArrayList();
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception
	{
		sessions.add(session);
		DetectionManager detectionManager = DetectionManager.getInstance();
		try {
			session.sendMessage(new TextMessage(detectionManager.getFrames((String) session.getAttributes().get("userId"), false, null)));
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception
	{
		sessions.remove(session);
	}
	
	public static void send() 
	{
		DetectionManager detectionManager = DetectionManager.getInstance();
		
		for (WebSocketSession session : sessions) 
		{
			try {
				session.sendMessage(new TextMessage(detectionManager.getFrames((String) session.getAttributes().get("userId"), false, null)));
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
}
