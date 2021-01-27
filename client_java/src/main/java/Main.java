
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * falesna hlavni trida aplikace, akorat vola MainWindow
 */
public class Main extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		MainWindow window = new MainWindow(stage, this.getParameters().getRaw());
		window.show();
	}
}
