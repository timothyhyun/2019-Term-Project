/*
 * Timothy Hyun
 * Commander Schenk
 * AP Computer Science A
 * Master Project
 */
package application;
	
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;

import javafx.scene.layout.AnchorPane;


/**
 * @author timothyhyun
 *
 */
public class Main extends Application {
	
	//private fields of a stage that will be the primary stage, and the root layout of our main window
		private Stage primaryStage;
		private AnchorPane rootLayout;
	
	
	@Override
	public void start(Stage primaryStage) {
		try {
			
			//Set application stage and provide title
			this.primaryStage = primaryStage;
			this.primaryStage.setTitle("Gradebook");	//Set the title of the Window
			
			//set the application icon
			//this.primaryStage.getIcons().add(new Image("file:resources/AppIcon.png"));

			//Here we call a method to set up the Root Layout
			initRootLayout();
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		launch(args);
	}
	
	public void initRootLayout() {
		
		try {
			//Main initialization routines for layout and app commencement
			
			//create an FXML Loader object
			FXMLLoader loader = new FXMLLoader();
			
			//Point the loader at the FXML file to load
			loader.setLocation(Main.class.getResource("/application/RootLayout.fxml"));
			
			//Sets the rootlayout field to the BorderPane FXM file injected via @FXML
			rootLayout = (AnchorPane) loader.load();
			
			
			//Sets a Scene object to this rootLayout
			Scene scene = new Scene(rootLayout);
			
			// Give the controller access to the main app.
	        RootLayoutController controller = loader.getController();
	        controller.setMainApp(this);
			
			//Sets the primaryScene Scene object to scene
			primaryStage.setScene(scene);
			
			//Shows the primaryStage object
			primaryStage.show();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}	
	
	
	
	
	
	
}
