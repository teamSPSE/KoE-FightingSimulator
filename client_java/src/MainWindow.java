import javafx.event.EventHandler;
import javafx.scene.Scene;
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

	//login
	public Label nameOfGame = new Label(Constants.gameTitle);
	public Label loginLabel = new Label(Constants.loginName);
	public Button login = new Button(Constants.buttonLogin);
	public TextField enterName = new TextField();

	public MainWindow(Stage stage, List<String> args){
		this.conn = new Connection(args);
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

	public Stage onCloseEvent(Stage stage) {
		stage.setOnCloseRequest(event -> {
			conn.logout();
			System.exit(0);
			return;
		});

		return stage;
	}
}
