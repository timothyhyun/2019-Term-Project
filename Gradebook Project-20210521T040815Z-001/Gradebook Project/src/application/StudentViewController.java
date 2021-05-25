/*
 * Timothy Hyun
 * Commander Schenk
 * AP Computer Science A
 * Master Project
 */
package application;

import java.io.IOException;
import java.io.StringWriter;

import org.json.simple.JSONObject;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class StudentViewController {
	// User Profile
	@FXML
	private Label studentID;
	@FXML
	private Label studentFirstName;
	@FXML
	private Label studentLastName;
	@FXML
	private Label studentGrade;
	@FXML
	private Button btnSignOut;
	@FXML
	private Accordion grades;
	
	public Student currentStudent;
	Scene sceneSettings;
	public Stage primaryStage;
	
	private Main mainApp;
	public StudentViewController() {
		
	}
		
	
	public void signOut() throws IOException {
		JSONObject obj = new JSONObject();
		obj.put("userId", Integer.toString(this.currentStudent.getID()));
		StringWriter out = new StringWriter();
		obj.writeJSONString(out);	
		String example = out.toString();
		String example1 = Utility.post("http://"+RootLayoutController.ipAddress+":8001/signOut", example);
		
		Stage stage = (Stage) btnSignOut.getScene().getWindow();
		Parent root;
		try {
			root = FXMLLoader.load(getClass().getResource("RootLayout.fxml"));
			Scene scene = new Scene(root);
			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public void initialize() {
		
		this.currentStudent = RootLayoutController.currentStudent;
		this.studentFirstName.setText(RootLayoutController.currentStudent.getFirstName());
		this.studentLastName.setText(RootLayoutController.currentStudent.getLastName());
		this.studentID.setText("ID: " + RootLayoutController.currentStudent.getID());
		this.studentGrade.setText("Grade: " + RootLayoutController.currentStudent.getGrade());
		if (this.currentStudent.getGrades().size()>0) {
		for (int i = 0; i < this.currentStudent.getGrades().size(); i++) {
			TitledPane pane = new TitledPane();
			pane.setText("Class Name: " + this.currentStudent.getGrades().get(i).gradebookName + 
					" Average: " + this.currentStudent.getGrades().get(i).gradeCalculation(this.currentStudent.getGrades().get(i).weights));
	        grades.getPanes().add(pane);
	        TableView table = new TableView();
	        pane.setContent(table);
	        TableColumn<String, Grade> dateCreated = new TableColumn<>("Date Created");
	        dateCreated.setCellValueFactory(new PropertyValueFactory<>("dateCreated"));
	        dateCreated.setPrefWidth(150);
	        TableColumn<String, Grade> assignmentName = new TableColumn<>("Assignment Name");
	        assignmentName.setCellValueFactory(new PropertyValueFactory<>("name"));
	        assignmentName.setPrefWidth(250);
	        TableColumn<String, Grade> category = new TableColumn<>("Assignment Name");
	        category.setCellValueFactory(new PropertyValueFactory<>("type"));
	        category.setPrefWidth(200);
	        TableColumn<Integer, Grade> pointsEarned = new TableColumn<>("Points Earned");
	        pointsEarned.setCellValueFactory(new PropertyValueFactory<>("pointsEarned"));
	        pointsEarned.setPrefWidth(125);
	        TableColumn<Integer, Grade> totalPoints = new TableColumn<>("Total Points");
	        totalPoints.setCellValueFactory(new PropertyValueFactory<>("totalPoints"));
	        totalPoints.setPrefWidth(125);
	        TableColumn<Double, Grade> percentage = new TableColumn<>("Percentage");
	        percentage.setCellValueFactory(new PropertyValueFactory<>("percentage"));
	        percentage.setPrefWidth(150);
	        table.getColumns().add(dateCreated);
	        table.getColumns().add(assignmentName);
	        table.getColumns().add(category);
	        table.getColumns().add(pointsEarned);
	        table.getColumns().add(totalPoints);
	        table.getColumns().add(percentage);
	        for (int j = 0; j < this.currentStudent.getGrades().get(i).getGrades().size(); j++) {
	        	if (!this.currentStudent.getGrades().get(i).getGrades().get(j).getName().contains("Dummy Assignment")) {
	        		table.getItems().add(this.currentStudent.getGrades().get(i).getGrades().get(j));
	        	}
	        	
	        }
	       
		}
		
	}
		
		
	}
	
	
	
	
	
	public void setMainApp(Main mainApp) {
		this.mainApp = mainApp;
	}
	
	
	
}
