/*
 * Timothy Hyun
 * Commander Schenk
 * AP Computer Science A
 * Master Project
 */
package application;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.PasswordField;

/**
 * @author timothyhyun
 *
 */
public class RootLayoutController {
	// Section 1:
	@FXML
	private TextField studentID;
	@FXML
	private PasswordField studentLoginPin;
	@FXML
	private Button btnStudentLogin;
	@FXML
	private TextField studentLoginStatus;
	// Section 2:
	@FXML
	private TextField studentFirstName;
	@FXML
	private TextField studentLastName;
	@FXML
	private PasswordField studentPin;
	@FXML
	private TextField studentGrade;
	@FXML
	private Button btnStudentCreateAccount;
	@FXML
	private TextField studentCreateStatus;
	
	// Section 3:
	@FXML
	private TextField teacherID;
	@FXML
	private PasswordField teacherLoginPin;
	@FXML
	private Button teacherLogin;
	@FXML
	private TextField teacherLoginStatus;
	
	// Section 4:
	@FXML
	private TextField teacherFirstName;
	@FXML
	private TextField teacherLastName;
	@FXML
	private PasswordField  teacherPin;
	@FXML
	private Button btnTeacherCreateAccount;
	@FXML
	private TextField teacherCreateStatus;
	
	public static Student currentStudent = new Student("first ", "last ", 0, "pin ", 0);
	
	public static Teacher currentTeacher = new Teacher("first", "last", "pin", 0);
	
	public static String ipAddress = "108.196.8.191";
	
	public static List<String> ids = new ArrayList<String>();
	public static List<String> names = new ArrayList<String>();
	private Main mainApp;
	
	public RootLayoutController() {
	
	}
	
	@FXML
	public void initialize() {
	
		
	}
	
	@FXML
	public void studentLogin() throws IOException{		
			JSONObject obj = new JSONObject();
			obj.put("userId", studentID.getText());
			obj.put("password", studentLoginPin.getText());
			StringWriter out = new StringWriter();
			try {
				obj.writeJSONString(out);	
			String example = out.toString();
			String example1 = Utility.post("http://"+RootLayoutController.ipAddress+":8001/authStudent", example);
		
			if (example1.length() > 0) {
			JSONObject current = new JSONObject();
			current.put("reqUserId", studentID.getText());
			current.put("reqPassword", studentLoginPin.getText());
			current.put("targetId", studentID.getText());
			StringWriter outs = new StringWriter();
			try {
				
				current.writeJSONString(outs);
				String example2 = outs.toString();
				String studentData = Utility.post("http://"+RootLayoutController.ipAddress+":8001/getStudentData", example2);
				
				Utility.decodeJSON(studentData, "name", "String");
				String name = Utility.decodedString;
				String[] fullName = name.split("\\s+");
				String firstName = fullName[0];
				String lastName = fullName[1];
				
				Utility.decodeJSON(studentData, "userId", "String");
				String studentID = Utility.decodedString;
				int studentIDI = Integer.parseInt(studentID);
				Utility.decodeJSON(studentData, "grade", "String");
				String grade = Utility.decodedString;
				int grades = Integer.parseInt(grade);
				RootLayoutController.currentStudent = new Student(firstName, lastName, grades, 
						studentLoginPin.getText(), studentIDI);
				// load student information
				Object objects = new JSONParser().parse(studentData);
	    		JSONObject jo = (JSONObject) objects;
	    		
	    		// test to if there are any classes
	    		if (jo.get("classes") != null) {
				Utility.decodeJSONArray(studentData, "classes");
				List<JSONObject> classList = new ArrayList<JSONObject>();
				
				for (int i = 0; i < Utility.objectsz.size(); i++) {
					classList.add(Utility.objectsz.get(i));
				}
				
				for (int i = 0; i < classList.size(); i++) {
					Utility.decodeJSON(classList.get(i).toString(), "name", "String");
					String className = Utility.decodedString;
					Utility.decodeJSONArray(classList.get(i).toString(), "assignments");
					List<JSONObject> assignments = new ArrayList<JSONObject>();
					
					System.out.println(studentData);
					Utility.decodeJSONArrayStrings(classList.get(i).toString(), "categoryWeights");
					
					int[] weightsList = {0,0,0,0};
					for (int k = 0; k < Utility.objectString.size(); k++) {
						weightsList[k] = Integer.parseInt(Utility.objectString.get(k));
					}
					StudentGrades temp = new StudentGrades(name, className, studentID, weightsList);
					for (int j = 0; j < Utility.objectsz.size(); j++) {
						assignments.add(Utility.objectsz.get(j));
					}
					if (assignments != null) {
					for (int j = 0; j < assignments.size(); j++) {
						Utility.decodeJSON(assignments.get(j).toString(), "name", "String");
						String gradeName = Utility.decodedString;
						Utility.decodeJSON(assignments.get(j).toString(), "date", "String");
						String gradeDate = Utility.decodedString;
						Utility.decodeJSON(assignments.get(j).toString(), "category", "String");
						String gradeCategory = Utility.decodedString;
						Utility.decodeJSON(assignments.get(j).toString(), "totalPoints", "String");
						int totalPoints = Integer.parseInt(Utility.decodedString);
						Utility.decodeJSON(assignments.get(j).toString(), "earnedPoints", "String");
						int pointsEarned = Integer.parseInt(Utility.decodedString);
						Grade tempGrade = null;
						if (gradeCategory == "Quiz") {
							tempGrade = new QuizGrade(totalPoints, pointsEarned, gradeName, gradeDate);
						} else if(gradeCategory == "Test") {
							tempGrade = new TestGrade(totalPoints, pointsEarned, gradeName, gradeDate);
						} else if (gradeCategory == "HW") {
							tempGrade = new HWGrade(totalPoints, pointsEarned, gradeName, gradeDate);
						} else {
							tempGrade = new FinalGrade(totalPoints, pointsEarned, gradeName, gradeDate);
						}
						temp.addGrade(tempGrade);
					}
					RootLayoutController.currentStudent.getGrades().add(temp);
				}
				}	
	    		}	
			} catch (IOException e) {
				e.printStackTrace();
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Stage stage = (Stage) btnStudentLogin.getScene().getWindow();
			Parent root;
			try {
				root = FXMLLoader.load(getClass().getResource("StudentView.fxml"));
				Scene scene = new Scene(root);
				stage.setScene(scene);
				stage.show();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
			
			} catch (IOException e) {
				studentLoginStatus.textProperty().setValue("Invalid Input");
			}				
			}
	

	
	
	public void studentCreateAccount() throws MalformedURLException, ProtocolException, IOException {
		
		JSONObject obj = new JSONObject();	
		JSONObject ids = new JSONObject();
		ids.put("id", " ");
		StringWriter ex = new StringWriter();
		ids.writeJSONString(ex);
		String temp = ex.toString();
		String idsx = Utility.post("http://"+RootLayoutController.ipAddress+":8001/getNextId", temp);
		
		try {
			Utility.decodeJSON(idsx, "length", "String");
		} catch (ParseException e1) {
			
			e1.printStackTrace();
		}
		String id = Utility.decodedString;
		obj.put("studentId", id); 
		obj.put("password", studentPin.getText()); 
		obj.put("name", studentFirstName.getText() +" "+ studentLastName.getText());
		//combine first and last name and put as string
		obj.put("grade", studentGrade.getText());
		
		StringWriter out = new StringWriter();
		try {
			obj.writeJSONString(out);
		
		String example = out.toString();
		String example1 = Utility.post("http://"+RootLayoutController.ipAddress+":8001/createStudentAccount", example);
		} catch (IOException e) {
			studentLoginStatus.textProperty().setValue("Invalid Input");
		}
		studentCreateStatus.textProperty().setValue("Account Created. ID: " + id.toString());
		studentFirstName.textProperty().setValue("");
		studentLastName.textProperty().setValue("");
		studentGrade.textProperty().setValue("");
		studentPin.textProperty().setValue("");
	}
	
	public void teacherLogin() {
		Utility.objectsz.clear();
		Utility.objectString.clear();
		JSONObject obj = new JSONObject();
		obj.put("userId", teacherID.getText());
		obj.put("password", teacherLoginPin.getText());
		StringWriter out = new StringWriter();
		try {
			obj.writeJSONString(out);	
		String example = out.toString();
		String example1 = Utility.post("http://"+RootLayoutController.ipAddress+":8001/authTeacher", example);
		if (example1.length() > 0) {
		JSONObject current = new JSONObject();
		current.put("reqUserId", teacherID.getText());
		current.put("reqPassword", teacherLoginPin.getText());
		current.put("targetId", teacherID.getText());
		StringWriter outs = new StringWriter();
		try {
			
			current.writeJSONString(outs);
			String example2 = outs.toString();
			String teacherData = Utility.post("http://"+RootLayoutController.ipAddress+":8001/getTeacherData", example2);
			
			Utility.decodeJSON(teacherData, "name", "String");
			String name = Utility.decodedString;
			String[] fullName = name.split("\\s+");
			String firstName = fullName[0];
			String lastName = fullName[1];
			
			Utility.decodeJSON(teacherData, "userId", "String");
			String teacherID = Utility.decodedString;
			int teacherIDI = Integer.parseInt(teacherID);
			RootLayoutController.currentTeacher = new Teacher(firstName, lastName, 
					teacherLoginPin.getText(), teacherIDI);
			Utility.objectsz.clear();
			Utility.objectString.clear();
			
			// Loads Grades
			Object objects = new JSONParser().parse(teacherData);
    		JSONObject jo = (JSONObject) objects;
    		
    		// test to if there are any classes
    		if (jo.get("classes") != null) {
			Utility.decodeJSONArray(teacherData, "classes");
			
			
			List<JSONObject> classList = new ArrayList<JSONObject>();
			
			for (int i = 0; i < Utility.objectsz.size(); i++) {
				classList.add(Utility.objectsz.get(i));
			}
			Utility.objectsz.clear();
			// Assignments Load
			for (int i = 0; i < classList.size(); i++) {
				int[] weights = new int[4];
				Utility.decodeJSON(classList.get(i).toString(), "name", "String");
				
				String className = Utility.decodedString;
				Utility.objectString.clear();
				Utility.objectsz.clear();
				Utility.decodeJSONArrayStrings(classList.get(i).toString(), "categoryWeights");
				List<String> weightsList = new ArrayList<String>();
				for (int k = 0; k < Utility.objectString.size(); k++) {
					weightsList.add(Utility.objectString.get(k));
				}
				Utility.decodeJSONArrayStrings(classList.get(i).toString(), "studentNames");
				for (int o = 0; o < Utility.objectString.size(); o++) {
					names.add(Utility.objectString.get(o));
				}
				Utility.decodeJSONArrayStrings(classList.get(i).toString(), "studentIds");
				for (int f = 0; f < Utility.objectString.size(); f++) {
					ids.add(Utility.objectString.get(f));
				}
				Utility.objectString.clear();
				for (int j = 0; j < weightsList.size(); j++) {
					weights[j] = Integer.parseInt(weightsList.get(j));
					
					
				}
				Gradebook temp = new Gradebook(className, weights);
				RootLayoutController.currentTeacher.addGradebook(temp);
				Utility.objectString.clear();
				Utility.objectsz.clear();
				Object objectsz = new JSONParser().parse(classList.get(i).toString());
	    		JSONObject joz = (JSONObject) objectsz;
	    		// test to see if there are any assignments
	    		if (joz.get("classAssignments") !=null) {
				Utility.decodeJSONArray(classList.get(i).toString(), "classAssignments");
				
				List<JSONObject> assignment = new ArrayList<JSONObject>();
				for (int l = 0; l < Utility.objectsz.size(); l++) {
					assignment.add(Utility.objectsz.get(l));
				}
				ArrayList<String> studentNames = new ArrayList<String>();
				for (int m = 0; m < assignment.size(); m++) {
					String dateCreated = assignment.get(m).get("date").toString();
					String assignmentName = assignment.get(m).get("name").toString();
					String assignmentType = assignment.get(m).get("category").toString();
					int pointsEarned = Integer.parseInt(assignment.get(m).get("earnedPoints").toString());
					int totalPoints = Integer.parseInt(assignment.get(m).get("totalPoints").toString());
					String studentName = assignment.get(m).get("assignedTo").toString();
					int studentID = Integer.parseInt(assignment.get(m).get("assignedToId").toString());
					Grade temp2 = null;
					
					
					if (assignmentType.contains("Quiz")) {
						temp2 = new QuizGrade(totalPoints, pointsEarned, assignmentName, dateCreated);
					} else if (assignmentType.contains("Test")) {
						temp2 = new TestGrade(totalPoints, pointsEarned, assignmentName, dateCreated);
					} else if (assignmentType.contains("HW")) {
						temp2 = new HWGrade(totalPoints, pointsEarned, assignmentName, dateCreated);
					} else {
						temp2 = new FinalGrade(totalPoints, pointsEarned, assignmentName, dateCreated);
					}
					
					if (!studentNames.contains(studentName)) {
						studentNames.add(studentName);
						StudentGrades temp1 = new StudentGrades(studentName, className, Integer.toString(studentID));
						RootLayoutController.currentTeacher.addStudent(temp, temp1);
						RootLayoutController.currentTeacher.addGrade(className, studentName, temp2);
						
					} else {
						RootLayoutController.currentTeacher.addGrade(className, studentName, temp2);
						
						
					}
					
				}
	    		}
	    		
				
			}
    		}
			Utility.objectsz.clear();
			Utility.objectString.clear();
		} catch (IOException e) {
			e.printStackTrace();	
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Stage stage = (Stage) teacherLogin.getScene().getWindow();
		Parent root;
		try {
			root = FXMLLoader.load(getClass().getResource("TeacherView.fxml"));
			Scene scene = new Scene(root);
			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
		
		} catch (IOException e) {
			teacherLoginStatus.textProperty().setValue("Invalid Input");
		}				
	}
	public void teacherCreateAccount() throws MalformedURLException, ProtocolException, IOException {
		JSONObject obj = new JSONObject();	
		JSONObject ids = new JSONObject();
		ids.put("id", " ");
		StringWriter ex = new StringWriter();
		ids.writeJSONString(ex);
		String temp = ex.toString();
		String idsx = Utility.post("http://"+RootLayoutController.ipAddress+":8001/getNextId", temp);
		try {
			Utility.decodeJSON(idsx, "length", "String");
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		String id = Utility.decodedString;
		obj.put("teacherId", id.toString()); //contact server with /getNextId for the next ID to use. Returns json with field "length"
		obj.put("password", teacherPin.getText()); //put the pin in here as a string
		obj.put("name", teacherFirstName.getText() +" "+ teacherLastName.getText()); //combine first and last name and put as string
		StringWriter out = new StringWriter();
		try {
			obj.writeJSONString(out);
		
		String example = out.toString();
		String example1 = Utility.post("http://"+RootLayoutController.ipAddress+":8001/createTeacherAccount", example);
		} catch (IOException e) {
			teacherCreateStatus.textProperty().setValue("Invalid Input");
		}
		teacherCreateStatus.textProperty().setValue("Account Created. ID: " + id.toString());
		teacherFirstName.textProperty().setValue("");
		teacherLastName.textProperty().setValue("");
		teacherPin.textProperty().setValue("");
	}
	
	
	
	// @param mainApp
	public void setMainApp(Main mainApp) {
		this.mainApp = mainApp;
	}
}
