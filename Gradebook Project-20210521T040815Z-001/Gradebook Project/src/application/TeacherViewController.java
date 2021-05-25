/*
 * Timothy Hyun
 * Commander Schenk
 * AP Computer Science A
 * Master Project
 */
package application;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class TeacherViewController {
	@FXML
	private Accordion grades;
	
	@FXML
	private Label teacherFirstName;
	@FXML
	private Label teacherLastName;
	@FXML
	private Label teacherID;
	@FXML
	private Button btnSignOut;
	// Add class
	@FXML
	private TextField addClassName;
	@FXML
	private TextField quizWeight;
	@FXML 
	private TextField testWeight;
	@FXML
	private TextField hwWeight;
	@FXML
	private TextField finalWeight;
	@FXML
	private Button btnAddClass;
	
	// Add Student
	@FXML
	private ComboBox<String> classSelect;
	@FXML
	private TextField addStudentName;
	@FXML
	private TextField addStudentID;
	@FXML
	private Button addStudent;
	
	// Add Grade
	@FXML
	private ComboBox<String> classNameSelect;
	@FXML
	private ComboBox<String> studentNameSelect;
	
	@FXML
	private ComboBox<String> typeSelect;
	@FXML 
	private TextField pointsEarned;
	@FXML
	private TextField totalPoints;
	@FXML
	private TextField assignmentName;
	@FXML
	private TextField dateDue;
	@FXML
	private Button addGrade;
	// list of class panes
	private ArrayList<TitledPane> panes = new ArrayList<TitledPane>();
	
	// list of accordions within class
	private ArrayList<Accordion> accordions = new ArrayList<Accordion>();
	//private ArrayList<ArrayList<>> gradePanes= new ArrayList<ArrayList<Accordion>>();
	private Main mainApp;
	public Teacher currentTeacher;
	Scene sceneSettings;
	public TeacherViewController() {
		
	}
	@FXML
	public void initialize(){
		
		this.currentTeacher = RootLayoutController.currentTeacher;
		this.teacherFirstName.setText(RootLayoutController.currentTeacher.getFirstName());
		this.teacherLastName.setText(RootLayoutController.currentTeacher.getLastName());
		this.teacherID.setText("ID: " + RootLayoutController.currentTeacher.getID());
		typeSelect.getItems().addAll("Quiz", "Test", "HW", "Final");
		
		if (RootLayoutController.currentTeacher.gradebooks.size()>0) {
			Accordion specificClass = new Accordion();
		for (int i = 0; i < currentTeacher.gradebooks.size(); i++) {

			classSelect.getItems().add(currentTeacher.gradebooks.get(i).getClassName());
			classNameSelect.getItems().add(currentTeacher.gradebooks.get(i).getClassName());
			TitledPane pane = new TitledPane();
			pane.setText("Class Name: " + this.currentTeacher.gradebooks.get(i).getClassName());
			
			grades.getPanes().add(pane);
			
			Accordion temp = new Accordion();
			pane.setContent(temp);
			
			
			
			
			for (int j = 0; j < currentTeacher.gradebooks.get(i).students.size(); j++) {
				
				TitledPane temp1 = new TitledPane();
				currentTeacher.gradebooks.get(i).students.get(j).gradeCalculation(currentTeacher.gradebooks.get(i).weights);
				temp1.setText("Student: " + currentTeacher.gradebooks.get(i).students.get(j).studentName + " ID: " +currentTeacher.gradebooks.get(i).students.get(j).studentID +
						 " Average: " + this.currentTeacher.gradebooks.get(i).students.get(j).currentGrade);
				temp.getPanes().add(temp1);
				
				TableView table = new TableView();
				temp1.setContent(table);
				
				TableColumn<String, Grade> dateCreated = new TableColumn<>("Date Created");
		        dateCreated.setCellValueFactory(new PropertyValueFactory<>("dateCreated"));
		        dateCreated.setPrefWidth(150);
		        TableColumn<String, Grade> assignmentName = new TableColumn<>("Assignment Name");
		        assignmentName.setCellValueFactory(new PropertyValueFactory<>("name"));
		        assignmentName.setPrefWidth(250);
		        TableColumn<String, Grade> category = new TableColumn<>("Category");
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
		        for (int k = 0; k < currentTeacher.gradebooks.get(i).students.get(j).getGrades().size(); k++) {
		        	Grade grades = currentTeacher.gradebooks.get(i).students.get(j).getGrades().get(k);
					if (!grades.getName().contains("Dummy Assignment")) {
						table.getItems().add(grades);
					}
				}
			}	
		}
	}
	
	classNameSelect.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
	      @Override
	      public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
	    	  if (classNameSelect.getValue() != null) {
	  			for (int i = 0; i < currentTeacher.gradebooks.size(); i++) {
	  				if (classNameSelect.getValue()==currentTeacher.gradebooks.get(i).getClassName()) {
	  					for (int j = 0; j < currentTeacher.gradebooks.get(i).students.size(); j++) {
	  						studentNameSelect.getItems().clear();
	  						studentNameSelect.getItems().add(currentTeacher.gradebooks.get(i).students.get(j).studentName
	  								+ " ID: " + currentTeacher.gradebooks.get(i).students.get(j).studentID);	
	  					}
	  				}
	  			}
	  		}
	      }  
	});
	}
	public void signOut() throws MalformedURLException, ProtocolException, IOException {
		JSONObject obj = new JSONObject();
		obj.put("userId", Integer.toString(this.currentTeacher.getID()));
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
			e.printStackTrace();
		}
	}
	public void addClass() throws IOException {
		JSONObject obj = new JSONObject();
		obj.put("reqUserId", Integer.toString(this.currentTeacher.getID()));
		obj.put("reqPassword", this.currentTeacher.getPin());
		obj.put("name", addClassName.getText());
		String[] categoryWeights = new String[4];
		categoryWeights[0] = quizWeight.getText();
		categoryWeights[1] = testWeight.getText();
		categoryWeights[2] = hwWeight.getText();
		categoryWeights[3] = finalWeight.getText();
		int total = 0;
		JSONArray category= new JSONArray();
		for (int i = 0; i < categoryWeights.length; i++) {
			category.add(categoryWeights[i]);
			total += Integer.parseInt(categoryWeights[i]);
		}
		JSONArray categoryNames = new JSONArray();
		categoryNames.add("Quiz");
		categoryNames.add("Test");
		categoryNames.add("HW");
		categoryNames.add("Final");
		obj.put("categoryNames", categoryNames);
		obj.put("categoryWeights", category);
		if (total == 100) {
		StringWriter out = new StringWriter();
		obj.writeJSONString(out);	
		String example = out.toString();
		String example1 = Utility.post("http://"+RootLayoutController.ipAddress+":8001/addClass", example);
			//updateTeacher();
			TitledPane pane = new TitledPane();
			pane.setText("Class Name: " + addClassName.getText());
			grades.getPanes().add(pane);
			//panes.add(pane);
			Accordion temp = new Accordion();
			for (int j = 0; j < grades.getPanes().size(); j++) {
				if (grades.getPanes().get(j).getText().contains(addClassName.getText())) {
					grades.getPanes().get(j).setContent(temp);
					break;
				}
			}
			classSelect.getItems().add(addClassName.getText());
			classNameSelect.getItems().add(addClassName.getText());
			
		}
		
}
	// adds student
	public void addStudent() throws IOException {
		JSONObject obj = new JSONObject();

		obj.put("reqUserId", Integer.toString(this.currentTeacher.getID()));
		obj.put("reqPassword", this.currentTeacher.getPin());
		if (addStudentID.getText() != null) {
			obj.put("studentId", addStudentID.getText());
		} else {
			obj.put("studentId", "");
			
		}
		
		obj.put("studentName", addStudentName.getText());
		obj.put("targetClass", classSelect.getValue());
		StringWriter out = new StringWriter();
		obj.writeJSONString(out);	
		String example = out.toString();
		String example1 = Utility.post("http://"+RootLayoutController.ipAddress+":8001/addStudent", example);
		
		for (int i = 0; i < grades.getPanes().size(); i++) {
			if (grades.getPanes().get(i).getText().contains(classSelect.getValue())) {
				
				TitledPane tempStudent = new TitledPane();
				tempStudent.setText("Student: " + addStudentName.getText() + " ID: " + addStudentID.getText() + " Average: Null");
				TableView table = new TableView();
				tempStudent.setContent(table);
				TableColumn<String, Grade> dateCreated = new TableColumn<>("Date Created");
		        dateCreated.setCellValueFactory(new PropertyValueFactory<>("dateCreated"));
		        dateCreated.setPrefWidth(150);
		        TableColumn<String, Grade> assignmentName = new TableColumn<>("Assignment Name");
		        assignmentName.setCellValueFactory(new PropertyValueFactory<>("name"));
		        assignmentName.setPrefWidth(250);
		        TableColumn<String, Grade> category = new TableColumn<>("Category");
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
		        
		        Node tests = grades.getPanes().get(i).getContent();
		        Accordion ex = (Accordion) tests;
				ex.getPanes().add(tempStudent);
				StudentGrades temp = new StudentGrades(addStudentName.getText(), classSelect.getValue(), addStudentID.getText());
				for (int j = 0; j < currentTeacher.gradebooks.size(); j++) {
					if (currentTeacher.gradebooks.get(j).getClassName().contains(classSelect.getValue())) {
						currentTeacher.gradebooks.get(j).addStudent(temp);
					}
				}
		        
		        
				}
			}
		}
	
	
	public void addGrade() throws IOException {
		String date = Utility.get("http://"+RootLayoutController.ipAddress+":8001/getDate");
		JSONObject obj = new JSONObject();
		obj.put("reqUserId", Integer.toString(this.currentTeacher.getID()));
		obj.put("reqPassword", this.currentTeacher.getPin());
		String combined = studentNameSelect.getValue();
		int colonIndex = combined.indexOf(":");
		String studentExtract = combined.substring(colonIndex+2, combined.length());
		
		obj.put("targetId", studentExtract);
		obj.put("targetClass", classNameSelect.getValue());
		obj.put("name", assignmentName.getText());
		obj.put("category", typeSelect.getValue());
		obj.put("totalPoints", Float.parseFloat(totalPoints.getText()));
		obj.put("earnedPoints", Float.parseFloat(pointsEarned.getText()));
		
		StringWriter out = new StringWriter();
		obj.writeJSONString(out);	
		String example = out.toString();
		Grade temp = null;
		String example1 = Utility.post("http://"+RootLayoutController.ipAddress+":8001/addAssignment", example);
		if (typeSelect.getValue().contains("Quiz")){
			temp = new QuizGrade(Integer.parseInt(totalPoints.getText()), Integer.parseInt(pointsEarned.getText()), assignmentName.getText(), date);
		} else if (typeSelect.getValue().contains("Test")) {
			temp = new TestGrade(Integer.parseInt(totalPoints.getText()), Integer.parseInt(pointsEarned.getText()), assignmentName.getText(), date);
		} else if (typeSelect.getValue().contains("HW")) {
			temp = new HWGrade(Integer.parseInt(totalPoints.getText()), Integer.parseInt(pointsEarned.getText()), assignmentName.getText(), date);
		} else if (typeSelect.getValue().contains("Final")) {
			temp = new FinalGrade(Integer.parseInt(totalPoints.getText()), Integer.parseInt(pointsEarned.getText()), assignmentName.getText(), date);
		}
		for (int i = 0; i < grades.getPanes().size(); i++) {
			if (grades.getPanes().get(i).getText().contains(classNameSelect.getValue())){
				for (int j = 0; j < ((Accordion)grades.getPanes().get(i).getContent()).getPanes().size(); j++) {
					if (((Accordion)grades.getPanes().get(i).getContent()).getPanes().get(j).getText().contains(studentNameSelect.getValue())) {
						Node tables = (((Accordion)grades.getPanes().get(i).getContent()).getPanes().get(j).getContent());
						TableView tableCast = (TableView)tables;
						tableCast.getItems().add(temp);
						for (int k = 0; k < this.currentTeacher.gradebooks.size(); k++) {
							if (this.currentTeacher.gradebooks.get(k).className==classNameSelect.getValue()) {
								for (int m = 0; m < this.currentTeacher.gradebooks.get(k).students.size(); m++) {
									if (this.currentTeacher.gradebooks.get(k).students.get(m).studentID == studentExtract) {
										this.currentTeacher.gradebooks.get(k).students.get(m).addGrade(temp);
										this.currentTeacher.gradebooks.get(k).students.get(m).gradeCalculation(currentTeacher.gradebooks.get(k).weights);
										
										((Accordion)grades.getPanes().get(i).getContent()).getPanes().get(j).setText("Student: " + currentTeacher.gradebooks.get(i).students.get(j).studentName + " ID: " +currentTeacher.gradebooks.get(i).students.get(j).studentID +
										 " Average: " + this.currentTeacher.gradebooks.get(k).students.get(m).currentGrade);
									}
								}
							}
						}
						}
					}
				}
			}
		}
		
	
	public void setMainApp(Main mainApp) {
		this.mainApp = mainApp;
	}
}
