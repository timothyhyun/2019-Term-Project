/*
 * Timothy Hyun
 * Commander Schenk
 * AP Computer Science A
 */
package application;

import java.util.ArrayList;

public class Student implements UserInformation{
	private String firstName;
	private String lastName;
	private ArrayList<StudentGrades> grades;
	private int grade;
	private String pin;
	public int StudentID;
	
	public Student(String firstName, String lastName, int grade, String pin, int ID) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.grade = grade;
		this.pin = pin;
		this.StudentID = ID;
		this.grades = new ArrayList<StudentGrades>();
	}

	public ArrayList<StudentGrades> getGrades() {
		return grades;
	}

	public int getGrade() {
		return grade;
	}
	
	public void updateGrades(StudentGrades studentGrades) {
		for (int i = 0; i < grades.size(); i++) {
			if (studentGrades.gradebookName == grades.get(i).gradebookName) {
				grades.set(i, studentGrades);
			}
		}
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public int getID() {
		return StudentID;
	}
	
	
}
