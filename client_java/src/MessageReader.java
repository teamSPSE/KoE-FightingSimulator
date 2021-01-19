
import java.io.*;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

//import javafx.application.Platform;

public class MessageReader extends Thread {

	BufferedReader msgIn = null;
	Socket sock;
	Connection con;
	DataOutputStream msgOut;
	boolean flag = false;
	String pingMsg = "00213";
	String pingRespMsg = "00214";

	public MessageReader(Connection con, Socket sock, DataOutputStream msgOut) {
		this.sock = sock;
		try {
			this.msgIn = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		} catch (IOException e) {
			System.out.println("Failed to get input stream.");
			System.exit(1);
		}
		this.con = con;
		this.msgOut = msgOut;
	}

	@Override
	public void run() {
		System.out.println("Message receiving thread start.");
		while (true) {
			if (flag) {
				System.out.println("Message receiving thread end.");
				try {
					msgIn.close();
				} catch (IOException e) {
					System.out.println("Failed to close connection.");
				}
				break;
			}
			String msg = null;
			try {
				//msg = msgIn.readLine();
				char messageArr[] = new char[32];
				msgIn.read(messageArr);
				msg = new String(messageArr);
			} catch (IOException e) {
				msg = null;
			}
			if (msg != null) {
				parseMsg(msg);
			} else {
		//		System.out.println("Socket read timeout.");
		//		LOGGER.log(Level.INFO, "Socket read timeout.");
				try {
			//		System.out.println("Sending ping message.");
		//			LOGGER.log(Level.INFO, "Sending ping message.");
					msgOut.write(pingMsg.getBytes());
				} catch (IOException e) {
					System.out.println("Server closed connection. Program will quit.");
					//System.out.println("Failed to send ping message.");
		//			LOGGER.log(Level.INFO, "Failed to send ping message.");
					
		//			LOGGER.log(Level.INFO, "Server closed connection. Program will quit.");
					//Platform.exit();
					System.exit(0);
				}
				try {
					msg = msgIn.readLine();
				} catch (IOException e) {
					System.out.println("Server not responding. Attempting to reconnect.");
			//		LOGGER.log(Level.INFO, "Server not responding. Attempting to reconnect.");
					//Platform.runLater(() -> BattleShips.reconWait());
					try {
						sock.close();
					} catch (IOException e1) {
						System.out.println("Failed to close socket. Application will quit.");
				//		LOGGER.log(Level.INFO, "Failed to close socket. Application will quit.");
						e1.printStackTrace();
					}
					int i = 0;
					while (i < 12) {
						sock = con.reconnect();
						if (sock == null) {
							i++;
							try {
								sleep(5000);
							} catch (InterruptedException e1) {
					//			LOGGER.log(Level.INFO, "Thread error. Application quit.");
								System.out.println("Thread error. Application quit.");
								//Platform.exit();
								System.exit(1);
							}
						} else {
							break;
						}
					}
					if (sock == null) {
						System.out.println("Failed to reconnect. Application quit.");
				//		LOGGER.log(Level.INFO, "Failed to reconnect. Application quit.");
						//Platform.runLater(() -> BattleShips.serverUn());
						break;
					} else {
						System.out.println("Reconnection succesful.");
				//		LOGGER.log(Level.INFO, "Reconnection succesful.");
						try {
							this.msgIn = new BufferedReader(new InputStreamReader(sock.getInputStream()));
						} catch (IOException e5) {
							System.out.println("Failed to get input stream.");
				//			LOGGER.log(Level.INFO, "Failed to get input stream.");
							//Platform.exit();
							System.exit(1);
						}
						this.msgOut = con.getMsgOut();
					/*	if (BattleShips.userName != null) {
							con.sendRecon(BattleShips.userName);
						}*/
						continue;
					}
				}
		//		System.out.println("Server responded to ping message.");
		//		LOGGER.log(Level.INFO, "Server responded to ping message.");
			}
		}
	}

	public synchronized void parseMsg(String msg) {
		if (msg == null) {
			System.out.println("Server closed connection. Program will quit.");
	//		LOGGER.log(Level.INFO, "Server closed connection. Program will quit.");
			return;
		}

		String[] p = msg.split("-");
		if (p[0].equals("logi")) {
	//		System.out.println("Received login respose: " + msg + ", message size: " + msg.length());
	//		LOGGER.log(Level.INFO, "Received login response: " + msg + ", message size: " + msg.length());
			System.out.println(msg);
		} else if(p[0].equals("logo")){
		//	System.out.println("Message not recognized. Program will quit.");
			System.out.println("2->"+msg);
		} else if (p[0].equals("ping")) {
			try {
				msgOut.write(pingRespMsg.getBytes());
			} catch (IOException e) {
				System.out.println("Cannot send ping response.");
				//		LOGGER.log(Level.INFO, "Cannot send ping response.");
			}
		} else{
			System.out.println("Message not recognized. Program will quit. msg:"+msg);
			//Platform.exit();
			System.exit(0);
		}
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

}
