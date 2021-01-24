
import javafx.scene.control.Alert;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

//import javafx.application.Platform;

public class Connection {

	public MainWindow mainWindow = null;
	private Socket socket = null;
	private DataOutputStream msgOut = null;
	private MessageReader mr = null;
	private String addr = "192.168.50.3";
	private int port = 10000;
	public Alert alert = new Alert(Alert.AlertType.ERROR);

	public Connection(List<String> args, MainWindow mainWindow) {
		switch(args.size()) {
			case 4:
				if (args.get(2).equals("-port") || args.get(2).equals("-p")) {
					try {
						this.port = Integer.parseInt(args.get(3));
					}
					catch (Exception e) {
						System.out.println("Writed port: '" + args.get(3) + "' is not a number, using default port: " + this.port);
					}
				}
				else {
					System.out.println("Third parameter is not -port or -p, using default port: " + this.port);
				}
			case 2:
				if (args.get(0).equals("-address") || args.get(0).equals("-a")) {
					try {
						if (checkIPv4(args.get(1))) {
							this.addr = args.get(1);
						}
						else {
							System.out.println("Writed address: '" + args.get(1) + "' is not a valid IPv4 address, using default address: " + this.addr);
						}
					}
					catch (Exception e) {
						System.out.println("Writed address: '" + args.get(1) + "' is not a valid IPv4 address, using default address: " + this.addr);
					}
				}
				else {
					System.out.println("First parameter is not -address or -a, using default address: " + this.addr);
				}
			default:
				break;
		}

		if(mainWindow != null){
			this.mainWindow = mainWindow;
		}
	}

	public static final boolean checkIPv4(final String ip) {
		boolean isIPv4;
		try {
			final InetAddress inet = InetAddress.getByName(ip);
			isIPv4 = inet.getHostAddress().equals(ip) && inet instanceof Inet4Address;
		}
		catch (final UnknownHostException e) {
			isIPv4 = false;
		}
		return isIPv4;
	}

	public boolean connect(String addr, int port) {

		try {
			socket = new Socket(addr, port);
		} catch (UnknownHostException e2) {
		} catch (IOException e2) {
		}
		if (socket == null) {
			alert.setHeaderText("Failed to connect.");
			alert.setContentText("");
			alert.show();
			System.out.println("Failed to connect.");
			return false;
		}
		try {
			socket.setSoTimeout(3 * 1000);
		} catch (SocketException e1) {
			System.out.println("Failed to connect.");
			alert.setHeaderText("Failed to connect.");
			alert.setContentText("");
			alert.show();
			return false;
		}
		InetAddress adr = socket.getInetAddress();
		System.out.println("Connecting to: " + adr.getHostAddress());

		try {
			msgOut = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Failed to connect.");
			alert.setHeaderText("Failed to connect.");
			alert.setContentText("");
			alert.show();
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
			alert.setHeaderText("Failed to send test message.");
			alert.setContentText("");
			alert.show();
			e.printStackTrace();
		}
	}

	public void login(String name) {System.out.println(name);
		String msg = genMsg(name, 1);
		mainWindow.client.setUserName(name);
		try {
			msgOut.write(msg.getBytes());
		} catch (IOException e) {
			System.out.println("Failed to send login message.");
			alert.setHeaderText("Failed to send login message.");
			alert.setContentText("");
			alert.show();
			e.printStackTrace();
		}
	}

	public void logout() {
		String msg = genMsg("", 2);
		try {
			msgOut.write(msg.getBytes());
		} catch (IOException e) {
			System.out.println("Failed to send logout message.");
			alert.setHeaderText("Failed to send logout message.");
			alert.setContentText("");
			alert.show();
		}
	}

	public void joinLobby() {
		String msg = genMsg("", 3);
		//mainWindow.client.setState(States.IN_LOBBY);
		try {
			msgOut.write(msg.getBytes());
		} catch (IOException e) {
			System.out.println("Failed to send join lobby message.");
			alert.setHeaderText("Failed to send join lobby message.");
			alert.setContentText("");
			alert.show();
		}
	}
/*
	public void getHealt(){
		String msg = genMsg("", 4);
		try {
			msgOut.write(msg.getBytes());
		} catch (IOException e) {
			System.out.println("Failed to send create room message.");
		}
	}
*/
	public void sendDMG(int dmg) {
		System.out.println("dmg:"+dmg);
		String msg = genMsg(""+dmg, 5);
		try {
			msgOut.write(msg.getBytes());
		} catch (IOException e) {
			System.out.println("Failed to send damage message.");
			alert.setHeaderText("Failed to send damage message.");
			alert.setContentText("");
			alert.show();
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
