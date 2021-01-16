
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

//import javafx.application.Platform;

public class Connection {

	private Socket socket = null;
	private DataOutputStream msgOut = null;
	private MessageReader mr = null;
//	private final static Logger LOGGER = Logger.getLogger(Connection.class.getName());
//	private static FileHandler fh = null;
	private String addr;
	private int port;

	public Connection() {
//		try {
	//		fh = new FileHandler("ClientLog.log");
//		} catch (SecurityException | IOException e) {
//			System.out.println("Failed to initialize logger file handler.");
//		}
//		LOGGER.addHandler(fh);
//		SimpleFormatter form = new SimpleFormatter();
//		fh.setFormatter(form);
//		LOGGER.setUseParentHandlers(false);
	}

	public boolean connect(String addr, int port) {

		try {
			socket = new Socket(addr, port);
		} catch (UnknownHostException e2) {
		} catch (IOException e2) {
		}
		if (socket == null) {
			System.out.println("Failed to connect.");
			return false;
		}
		try {
			socket.setSoTimeout(3 * 1000);
		} catch (SocketException e1) {
//			LOGGER.log(Level.INFO, "Failed to set socket timeout.");
			System.out.println("Failed to connect.");
		}
		InetAddress adr = socket.getInetAddress();
	//	LOGGER.log(Level.INFO, "Connecting to: " + adr.getHostAddress());
		System.out.println("Connecting to: " + adr.getHostAddress());

		try {
			msgOut = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Failed to connect.");
	//		LOGGER.log(Level.INFO, "Failed to conect.");
			return false;
		}
		mr = new MessageReader(this, socket, msgOut);
		mr.start();
		this.addr = addr;
		this.port = port;
		return true;
	}

	public Socket reconnect() {
			try {
				socket = new Socket(addr, port);
			} catch (UnknownHostException e2) {
				System.out.println("error");
				return null;
				
			} catch (IOException e2) {
				return null;
			}
			if (socket == null) {
				return null;
			}
			try {
				socket.setSoTimeout(3 * 1000);
			} catch (SocketException e1) {
				return null;
			}
		try {
			msgOut = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Failed to open output stream. Aplication will quit.");
	//		LOGGER.log(Level.INFO, "Failed to open output stream. Aplication will quit.");
			//Platform.exit();
			System.exit(1);
		}
		return socket;
	}

	public String genMsg(String val, int t) {
		int l = val.length() + 2;
		String s;
		String msg;

		if ((l / 10) < 1) {
			s = "00" + l;
		} else if ((l / 100) < 1) {
			s = "0" + l;
		} else {
			s = Integer.toString(l);
		}
		if (t / 10 < 1) {
			msg = s + "0" + t + val;
		} else {
			msg = s + t + val;
		}
		return msg;
	}

	public void sendTestMsg(String msg){
		try {
			msgOut.write(msg.getBytes());
			msgOut.flush();
		} catch (IOException e) {
			System.out.println("Failed to send test message.");
			e.printStackTrace();
		}
	}

	public void login(String name) {
		String msg = genMsg(name, 1);
	//	LOGGER.log(Level.INFO, "Sending login request " + msg + ", message size: " + msg.length());
	//	System.out.println("Sending login request: " + msg + ", message size: " + msg.length());
		//BattleShips.bytesOut += msg.length();
		try {
			msgOut.write(msg.getBytes());
			//msgOut.flush();
		} catch (IOException e) {
			System.out.println("Failed to send login message.");
	//		LOGGER.log(Level.INFO, "Failed to send login message.");
			e.printStackTrace();
		}
	}

	public void logout() {
		String msg = genMsg("", 2);
//		LOGGER.log(Level.INFO, "Sending logout request " + msg + ", message size: " + msg.length());
	//	System.out.println("Sending logout request: " + msg + ", message size: " + msg.length());
	//	BattleShips.bytesOut += msg.length();
		try {
			msgOut.write(msg.getBytes());
			msgOut.flush();
		} catch (IOException e) {
			System.out.println("Failed to send logout message.");
	//		LOGGER.log(Level.INFO, "Failed to send logout message.");
		}
	}

	public void joinLobby() {
		String msg = genMsg("", 3);
		//	System.out.println("Sending create room request: " + msg + ", message size: " + msg.length());
		//	LOGGER.log(Level.INFO, "Sending create room request: " + msg + ", message size: " + msg.length());
		//BattleShips.bytesOut += msg.length();
		try {
			msgOut.write(msg.getBytes());
		} catch (IOException e) {
			System.out.println("Failed to send create room message.");
			//		LOGGER.log(Level.INFO, "Failed to send create room message.");
		}
	}


	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public DataOutputStream getMsgOut() {
		return msgOut;
	}

	public void setMsgOut(DataOutputStream msgOut) {
		this.msgOut = msgOut;
	}

	public MessageReader getMr() {
		return mr;
	}

	public void setMr(MessageReader mr) {
		this.mr = mr;
	}
}
