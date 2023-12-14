package application;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;


public class ChatRoomLauncher extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			Parent login = FXMLLoader.load(getClass().getResource("Login.fxml"));
			Scene scene = new Scene(login, 400, 300);
			System.out.println("scene");
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}