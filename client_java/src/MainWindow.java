import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.List;

public class MainWindow {
	private Connection conn;
	private Stage primaryStage;
	private String username;
	public Boolean play_processed = false;

	//login
	public Label nameOfGame = new Label(Constants.gameTitle);
	public Label loginLabel = new Label(Constants.loginName);
	public Button login = new Button(Constants.buttonLogin);
	public TextField enterName = new TextField();


	//lobby
	public Label nameOfPlayer = new Label();
	public Label name = new Label("Your name: ");
	public Button play = new Button("Play");

	public MainWindow(Stage stage, List<String> args){
		this.conn = new Connection(args, this);
		String host = "192.168.50.3";
		int port = 10000;
		this.conn.connect(host, port);
		this.primaryStage = createLoginStage(stage);
	}


	public void show() {
		this.primaryStage.show();
	}

	public Stage createLoginStage(Stage stage) {
		stage = onCloseEvent(stage);
		nameOfGame.setFont(new Font(20));

		login.setMaxWidth(75);
		enterName.setOnKeyPressed(new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent ke)
			{
				if (ke.getCode().equals(KeyCode.ENTER))
				{
					conn.login(enterName.getText());
				}
			}
		});
		login.setOnAction(event -> {
			conn.login(enterName.getText());
		});

		GridPane loginPane = new GridPane();
		loginPane.setHgap(5);
		loginPane.setVgap(5);
		loginPane.add(nameOfGame, 6, 1, 1, 1);
		loginPane.add(loginLabel, 5, 6, 1, 1);
		loginPane.add(enterName, 6, 6, 1, 1);
		loginPane.add(login, 6, 7, 1, 1);

		BorderPane root = new BorderPane();
		root.setCenter(loginPane);

		Scene scene = new Scene(root, Constants.stageWidthLogin, Constants.stageHeightLogin);

		stage.setMinWidth(Constants.stageWidthLogin);
		stage.setMinHeight(Constants.stageHeightLogin);
		stage.setScene(scene);
		stage.setTitle(Constants.gameTitle);
		return stage;
	}

	public void processLogin(String msg) {
		String[] p = msg.split("-");
		Alert alert = null;

		if (p[1].equals("ack")) {
			primaryStage = createLobbyStage(primaryStage);
			alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setHeaderText("Login was successful");
			alert.setContentText("Login was successful.");
			alert.setResizable(true);
			alert.show();
			return;
		}else if(p[1].equals("nackfull")){
			alert = new Alert(Alert.AlertType.ERROR);
			alert.setHeaderText("Login failed");
			alert.setContentText("List of users is full.");
			alert.show();
		}else if(p[1].equals("nackname")){
			alert = new Alert(Alert.AlertType.ERROR);
			alert.setHeaderText("Login failed");
			alert.setContentText("List of users is full.");
			alert.show();
		}else{
			alert = new Alert(Alert.AlertType.ERROR);
			alert.setHeaderText("Login failed");
			alert.setContentText("Something went wrong");
			alert.show();
		}
	}

	public Stage createLobbyStage(Stage stage) {
		stage = onCloseEvent(stage);
		play.setMaxWidth(75);
		nameOfGame.setFont(new Font(20));
		nameOfPlayer.setFont(new Font(20));
		nameOfPlayer.setText(username);
		nameOfPlayer.setTextFill(javafx.scene.paint.Color.web(Color.Blue.getHexColor()));

		if (play_processed) {
			play.setDisable(true);
			play.setText("Queued");
		}
		else {
			play.setDisable(false);
			play.setText("Play");
			play.setOnAction(event -> {
				play();
			});
		}

		GridPane lobbyPane = new GridPane();
		lobbyPane.setHgap(5);
		lobbyPane.setVgap(5);

		lobbyPane.add(nameOfGame, 6, 1, 1, 1);
		lobbyPane.add(name, 5, 6, 1, 1);
		lobbyPane.add(nameOfPlayer, 6, 6, 1, 1);
		lobbyPane.add(play, 6, 7, 1, 1);

		BorderPane root = new BorderPane();
		root.setCenter(lobbyPane);

		Scene scene = new Scene(root, Constants.stageWidthLobby, Constants.stageHeightLobby);
		stage.setScene(scene);
		stage.setTitle(Constants.gameTitle);

		return stage;
	}

	public void play() {
		conn.joinLobby();
		this.play_processed = true;
		play.setDisable(true);
		play.setText("Queued");

	}

	public Stage onCloseEvent(Stage stage) {
		stage.setOnCloseRequest(event -> {
			conn.logout();
			System.exit(0);
			return;
		});

		return stage;
	}
}
