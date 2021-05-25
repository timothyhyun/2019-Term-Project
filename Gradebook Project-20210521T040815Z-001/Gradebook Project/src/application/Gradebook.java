package application;

import java.util.ArrayList;

public class Gradebook {
	public static int ID;
	public String className;
	public ArrayList<StudentGrades> students;
	public int gradebookID;
	public int[] weights;
	
	public Gradebook(String className, int[] weights) {
		this.className = className;
		this.gradebookID = ++ID;
		this.weights = weights;
		this.students = new ArrayList<StudentGrades>();
	}
	public String getClassName() {
		return this.className;
	}
	
	public int getID() {
		return this.gradebookID;
	}
	
	public void addStudent(StudentGrades student) {
		students.add(student);
	}
	

	
}
