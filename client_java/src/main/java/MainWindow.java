import com.sun.glass.ui.EventLoop;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class MainWindow {
	private Connection conn;
	private Stage primaryStage = null;
	public Client client = null;
	public Boolean play_processed = false;
	public boolean connected = false;

	//login
	public Label nameOfGame = new Label(Constants.gameTitle);
	public Label loginLabel = new Label(Constants.loginName);
	public Button login = new Button(Constants.buttonLogin);
	public TextField enterName = new TextField();


	//lobby
	public Label nameOfPlayer = new Label();
	public Label name = new Label("Your name: ");
	public Button play = new Button("Play");

	//images
	private static Image arena_background = null;
	private static Image galdiator_attack_left = null;
	private static Image galdiator_attack_right = null;
	private static Image galdiator_chill_left = null;
	private static Image galdiator_chill_right = null;

	//game
	public int enemyHealth = 100;
	public static Random r = new Random();
	public boolean healthUpdated = false;

	public MainWindow(Stage stage, List<String> args){
		this.conn = new Connection(args, this);
		String host = "192.168.50.3";
		int port = 10000;
		connected = this.conn.connect(host, port);
		this.primaryStage = createLoginStage(stage);
		this.client = new Client("");
	}


	public void show() {
		if(connected)
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
		nameOfPlayer.setText(client.getUserName());
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
		client.setState(States.IN_QUEUE);
		play.setDisable(true);
		play.setText("Queued");
	}

	public Stage onCloseEvent(Stage stage) {
		stage.setOnCloseRequest(event -> {
			if(client.getState() != States.LOGGING)
				conn.logout();
			else
				System.exit(0);
			return;
		});

		return stage;
	}

	public void processGame(String msg) throws InterruptedException {
		String[] p = msg.split("-");
		Alert alert = new Alert(Alert.AlertType.ERROR);
		//conn.getHealt();

		if (p[1].equals("started")){
			if(p[2].equals("1")){
				client.setState(States.YOU_PLAYING);
			}else if(p[2].equals("0")){
				client.setState(States.OPPONENT_PLAYING);
			}
			primaryStage = createArena(primaryStage);
			return;
		}else if(p[1].equals("update")){
			client.sethealth(Integer.parseInt(p[2]));
			enemyHealth = Integer.parseInt(p[3]);

			if (client.getState() == States.YOU_PLAYING) {
				client.setState(States.OPPONENT_PLAYING);
			}else{
				client.setState(States.YOU_PLAYING);
			}

			primaryStage = createArena(primaryStage);
			return;
		}else if(p[1].equals("finish")){
			Alert finish = new Alert(Alert.AlertType.CONFIRMATION);
			finish.setHeaderText("Game finished!");
			if(p[2].equals("1"))
				finish.setContentText("You are the WINNER!!!\nDo you want to play another match?");
			else if(p[2].equals("0"))
				finish.setContentText("You have lost :(\nDo you want to play another match?");
			else
				finish.setContentText("Something went wrong! We have no winner!\nDo you want to play another match?");

			Optional<ButtonType> result = finish.showAndWait();
			if (result.get() == ButtonType.OK){
				play_processed = false;
				client.setState(States.LOGGED);
				client.sethealth(100);
				enemyHealth = 100;
				primaryStage = createLobbyStage(primaryStage);
			} else {
				System.out.println("player cancel:"+client.getUserName());
				conn.logout();
			}

			return;
		} else {
			alert.setHeaderText("Game failed!");
			alert.setContentText("Something went wrong");
			alert.show();
		}
	}

	public static void load_images(){
		if(arena_background == null){
			try {
				FileInputStream stream = new FileInputStream("img/arena_background.png");
				arena_background = new Image(stream);
				/*stream = new FileInputStream("img/gladiator_attack.png");
				galdiator_attack_left = new Image(stream);
				stream = new FileInputStream("img/gladiator_chill.png");
				galdiator_chill_left = new Image(stream);
				stream = new FileInputStream("img/gladiator_attack_right.png");
				galdiator_attack_right = new Image(stream);
				stream = new FileInputStream("img/gladiator_chill_right.png");
				galdiator_chill_right = new Image(stream);*/

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	private Stage createArena(Stage stage) {
		stage = onCloseEvent(stage);
		String currentPlayer = "";

		load_images();

		GridPane info = new GridPane();
		info.setHgap(60);
		info.setVgap(20);


		Label playerHealth = new Label("Your HP: " + client.health);
		Label oponentHealth = new Label("Enemy HP: " + enemyHealth);
		Button fastAttack = new Button("fast attack");
		Button normalAttack = new Button("normall attack");
		Button hardAttack = new Button("hard attack");

		if (client.getState() == States.YOU_PLAYING) {
			currentPlayer = Constants.playerYou;
			fastAttack.setDisable(false);
			normalAttack.setDisable(false);
			hardAttack.setDisable(false);
		} else {
			currentPlayer = Constants.playerOpponent;
			fastAttack.setDisable(true);
			normalAttack.setDisable(true);
			hardAttack.setDisable(true);
		}

		Label nowPlaying = new Label("Now playing: " + currentPlayer);

		info.add(nowPlaying, 1, 2);
		info.add(playerHealth, 1, 5);
		info.add(oponentHealth, 1, 6);
		info.add(fastAttack, 1, 10);
		info.add(normalAttack, 1, 11);
		info.add(hardAttack, 1, 12);

		fastAttack.setOnAction(event -> {
			countAttack(1);
		});
		normalAttack.setOnAction(event -> {
			countAttack(2);
		});
		hardAttack.setOnAction(event -> {
			countAttack(3);
		});

		/*
		ImageView imageView_gladiator_left = new ImageView(galdiator_chill_left);
		ImageView imageView_gladiator_right = new ImageView(galdiator_chill_right);
*
		HBox arena = new HBox(200);
		arena.getChildren().add(imageView_gladiator_left);
		arena.getChildren().add(imageView_gladiator_right);
		Background background = new Background(new BackgroundImage(arena_background, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT));
		arena.setBackground(background);
		*/

		ImageView ImageView_arena_background = new ImageView(arena_background);

		BorderPane root = new BorderPane();
		root.setCenter(info);
		//root.setLeft(arena);
		root.setLeft(ImageView_arena_background);

		Scene scene = new Scene(root, Constants.stageWidthArena, Constants.stageHeightArena);
		stage.setScene(scene);
		stage.setTitle(Constants.gameTitle);

		return stage;
	}

	public void countAttack(int i) {
		int dmg = 0;
		int chance = r.nextInt(10);
		switch (i){
			case 1: dmg = 100;break;//(chance <= 7 ? 20 : 0);break;
			case 2: dmg = (chance <= 5 ? 40 : 0);break;
			case 3: dmg = (chance <= 2 ? 50 : 0);break;
			default: dmg=0;
		}
		conn.sendDMG(dmg);
	}
/*
	public void sethealth(String msg) {
		healthUpdated = true;
		String[] p = msg.split("-");
		if(p[0].equals("health")){
			client.sethealth(Integer.parseInt(p[1]));
			enemyHealth = Integer.parseInt((p[2]));
			System.out.println("here");
		}
	}*/
}
