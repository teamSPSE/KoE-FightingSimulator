
import javafx.scene.control.Alert;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.List;

/**
 * trida starajici se o spojeni mezi serverem a klientem
 * trida obsahuje metody pro odeslani zprav na server
 */
public class Connection {
	/**
	 * instance hlavniho okna
	 */
	public MainWindow mainWindow = null;
	/**
	 * instance socketu pro spojeni na server
	 */
	private Socket socket = null;
	/**
	 * pomoci tohoto streamu se pocilaji data na server
	 */
	private DataOutputStream msgOut = null;
	/**
	 * listener zprav ze serveru
	 */
	private MessageReader mr = null;
	/**
	 * adresa serveru deklarovana na defaultni hodnotu 192.168.50.3
	 */
	public String addr = "192.168.50.3";
	/**
	 * port na kterem server posloucha deklarovany na defaultni hodnotu 10000
	 */
	public int port = 10000;
	/**
	 * slouzi pro vypis chyb v GUI
	 */
	public Alert alert = new Alert(Alert.AlertType.ERROR);

	/**
	 * konstruktor tridy Connection
	 * @param args argumenty z cmd
	 * @param mainWindow instance hlavniho okna
	 */
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
		//System.out.println("address: " + this.addr+" port:"+this.port);
		if(mainWindow != null){
			this.mainWindow = mainWindow;
		}
	}

	/**
	 * zkontroluje validitu predane IP ve formatu a.b.c.d
	 * @param ip IP ve formatu a.b.c.d
	 * @return	true -> ip je validni
	 */
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

	/**
	 * pripoji se na server
	 * @param addr adresa serveru
	 * @param port port serveru
	 * @return true pokud se pripojil spravne
	 */
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

	/**
	 * generuje zpravu
	 * @param val zprava
	 * @param t id zpravy
	 * @return retezec, ktery se posle na server
	 */
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
		System.out.println("zprava:"+msg);
		return msg;
	}

	/**
	 * prihlaseni uzivatele, posle pozadavek na server o prihlaseni
	 * @param name jmeno uzivatele
	 */
	public void login(String name) {System.out.println(name);
		String msg = genMsg(name, 1);
		mainWindow.client.setUserName(name);
		try {
			msgOut.write(msg.getBytes());
			System.out.println("login msg:"+msg);
		} catch (IOException e) {
			System.out.println("Failed to send login message.");
			alert.setHeaderText("Failed to send login message.");
			alert.setContentText("");
			alert.show();
			e.printStackTrace();
		}
	}

	/**
	 * odhlaseni uzivatele, posle pozadavek na server o odhlaseni
	 */
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

	/**
	 * pridani uzivatele do lobby, posle pozadavek na server o pridani uzivatele do lobby
	 */
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

	/**
	 * posle na server pozadavek na odecteni souperovo zdravi o parametr dmg
	 * @param dmg poskozeni
	 */
	public void sendDMG(int dmg) {
		System.out.println("dmg id:"+dmg);
		String msg = genMsg(""+dmg, 4);
		try {
			msgOut.write(msg.getBytes());
		} catch (IOException e) {
			System.out.println("Failed to send damage message.");
			alert.setHeaderText("Failed to send damage message.");
			alert.setContentText("");
			alert.show();
		}
	}

	/**
	 * posle na server odpoved, ze uspesne probehlo opetovne pripojeni
	 */
	public void gameReconResponse() {
		String msg = genMsg("", 10);
		try {
			msgOut.write(msg.getBytes());
		} catch (IOException e) {
			System.out.println("Failed to send game reconnected response message.");
			alert.setHeaderText("Failed to send game reconnected response message.");
			alert.setContentText("");
			alert.show();
		}
	}

	/**
	 * posle na server odpoved, ze se uspesne zapnula hra
	 */
	public void gameStartedResponse() {
		String msg = genMsg("", 11);
		try {
			msgOut.write(msg.getBytes());
		} catch (IOException e) {
			System.out.println("Failed to send game started response message.");
			alert.setHeaderText("Failed to send game started response message.");
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
