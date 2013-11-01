package fi.jasoft.remoteconnection.shared;

import java.io.Serializable;

public class RemoteConnectionConfiguration implements Serializable {
	
	public static final String DEVELOPMENT_PEER_JS_KEY = "ggsb0b2ioiy919k9";
	
	private String id = null;
	
	private String key = DEVELOPMENT_PEER_JS_KEY;
	
	private int port = 9000;
	
	private String host = "0.peerjs.com";
	
	private boolean secure = false;
	
	private String config = "{ \"iceServers\": [{ \"url\": \"stun:stun.l.google.com:19302\" }] }";
	
	private int debug = 0;	
	
	private boolean scriptInjected = true;
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public boolean isSecure() {
		return secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public int getDebug() {
		return debug;
	}

	public void setDebug(int debug) {
		this.debug = debug;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isScriptInjected() {
		return scriptInjected;
	}

	public void setScriptInjected(boolean scriptInjected) {
		this.scriptInjected = scriptInjected;
	}
}
