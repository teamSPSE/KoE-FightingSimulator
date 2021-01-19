
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		MainWindow window = new MainWindow(stage, this.getParameters().getRaw());
		window.show();
	}
}

/*
{
	public static void main(String argv[]) throws Exception
	{
		String host = "192.168.50.3";
		int port = 10000;

		Connection user1 = new Connection();
		user1.connect(host, port);
		user1.login("jirka");
		Thread.sleep(1000);
		user1.joinLobby();

		Connection user2 = new Connection();
		user2.connect(host, port);
		user2.login("pepa");
		Thread.sleep(1000);
		user2.joinLobby();

		Thread.sleep(3000);
		user1.logout();
		user2.logout();

	}
}
*/