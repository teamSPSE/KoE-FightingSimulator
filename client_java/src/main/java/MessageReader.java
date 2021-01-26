
import javafx.application.Platform;
import javafx.scene.control.Alert;
import java.io.*;
import java.net.Socket;


public class MessageReader extends Thread {

	BufferedReader msgIn = null;
	Socket sock;
	Connection con;
	DataOutputStream msgOut;
	boolean flag = false;
	String pingMsg = "00213";

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
				msg = msgIn.readLine();
				//System.out.println(msg);
			} catch (IOException e) {
				msg = null;
				//System.out.println("msg err NULL");
			}

			if (msg != null) {
				parseMsg(msg);
			} else {
				try {
					//System.out.println("Sending ping message.");
					msgOut.write(pingMsg.getBytes());
				} catch (IOException e) {
					System.out.println("Server closed connection. Program will quit.");
					System.out.println("Failed to send ping message.");
					Platform.exit();
					System.exit(0);
				}
			}
		}
	}

	public synchronized void parseMsg(String msg) {
		System.out.println("msg:"+msg);
		if (msg == null) {
			System.out.println("Server closed connection. Program will quit.");
			return;
		}

		String[] p = msg.split("-");
		if (p[0].equals("logi")) {
			Platform.runLater(() -> con.mainWindow.processLogin(msg));
			System.out.println(msg);
		} else if(p[0].equals("logo")){
			if(p[1].equals("ack")){
				Platform.exit();
				System.exit(0);
			}else{
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setHeaderText("Logout failed");
				alert.setContentText("Something went wrong. Try it again.");
				alert.show();
			}
		} else if (p[0].equals("game")) {
			Platform.runLater(() -> {
				try {
					con.mainWindow.processGame(msg);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
		} else if (p[0].equals("lobby")) {
			if(p[1].equals("ack"))
				System.out.println("Succesfully joined lobby.");
		} else if (p[0].equals("alive")) {
			//System.out.println("Received ping response.");
		}else{
			System.out.println("Message not recognized. Program will quit. msg:"+msg);
			Platform.exit();
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
