
import javafx.application.Platform;
import javafx.scene.control.Alert;
import java.io.*;
import java.net.Socket;

/**
 * trida zpracovava zpravy odeslane serverem
 */
public class MessageReader extends Thread {

	/**
	 * cteci stream
	 */
	BufferedReader msgIn = null;
	/**
	 * socket serveru
	 */
	Socket sock;
	Connection con;
	DataOutputStream msgOut;
	/**
	 * ridi, zda se ma poslouchat nebo ne, nastevenim flag = true se spojeni uzavre
	 */
	boolean flag = false;
	String pingMsg = "00213";

	/**
	 *
	 * @param con		instance tridy Connection, spojeni mezi serverem a klientem
	 * @param sock		socket server
	 * @param msgOut	stream pomoco ktereho se odesilaji data na server
	 */
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

	/**
	 * poslouchani serveru ve vlastnim jadre
	 */
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
				parseMsg(msg);		//overuji zpravu
			} else {
				try {
					//System.out.println("Sending ping message.");
					msgOut.write(pingMsg.getBytes());	//posilam ping na server
				} catch (IOException e) {
					System.out.println("Server closed connection. Program will quit.");
					System.out.println("Failed to send ping message.");
					Platform.exit();
					System.exit(0);
				}
			}
		}
	}

	/**
	 * zpracovani zpravy
	 * @param msg zprava
	 */
	public synchronized void parseMsg(String msg) {
		System.out.println("msg:"+msg);
		if (msg == null) {
			System.out.println("Server closed connection. Program will quit.");
			return;
		}

		//rozdelim zpravu podle -
		String[] p = msg.split("-");
		if (p[0].equals("logi")){										//login
			Platform.runLater(() -> con.mainWindow.processLogin(msg));		//spustim login
			System.out.println(msg);
		} else if(p[0].equals("logo")){									//logout
			if(p[1].equals("ack")){											//ok
				Platform.exit();											//ukoncim program
				System.exit(0);
			}else{
				Alert alert = new Alert(Alert.AlertType.ERROR);				//vypis hlasky
				alert.setHeaderText("Logout failed");
				alert.setContentText("Something went wrong. Try it again.");
				alert.show();
			}
		} else if (p[0].equals("game")) {								//samotna hra
			Platform.runLater(() -> {
					con.mainWindow.processGame(msg);					//zpravu hry dale parsuji
			});
		} else if (p[0].equals("lobby")) {								//lobby
			if(p[1].equals("ack"))											//ok
				System.out.println("Succesfully joined lobby.");
		} else if (p[0].equals("alive")) {								//odpoved na ping
			//System.out.println("Received ping response.");
		}else{
			System.out.println("Message not recognized. Program will quit. msg:"+msg);	//pokud neni zprava rozpoznana, vypisu hlasku, moznost vypnuti programu
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
