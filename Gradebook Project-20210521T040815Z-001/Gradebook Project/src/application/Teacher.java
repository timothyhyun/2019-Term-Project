/*
 * Timothy Hyun
 * Commander Schenk
 * AP Computer Science A
 * Master Project
 */
package application;

import java.util.ArrayList;

public class Teacher implements UserInformation{
	public String firstName;
	public String lastName;
	public ArrayList<Gradebook> gradebooks;
	public int teacherID;
	public String pin;
	
	public Teacher(String firstName, String lastName, String pin, int ID) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.teacherID = ID;
		this.pin = pin;
		this.gradebooks = new ArrayList<Gradebook>();
	}
	
	public String getPin() {
		return this.pin;
	}
	
	public void setPin(String pin) {
		this.pin=pin;
	}
	public String getFirstName() {
		return this.firstName;
	}
	
	public String getLastName() {
		return this.lastName;
	}
	
	public int getID() {
		return this.teacherID;
	}
	
	public void addGradebook(Gradebook gradebook) {
		this.gradebooks.add(gradebook);
	}
	
	public void addStudent(Gradebook gradebook, StudentGrades student) {
		for (int i = 0; i < gradebooks.size(); i++) {
			if (gradebooks.get(i).className == gradebook.className) {
				this.gradebooks.get(i).addStudent(student);
			}
		}
		
	}
	
	public void addGrade(String className, String studentName, Grade grade) {
		for (int j = 0; j < this.gradebooks.size(); j++) {
			if (this.gradebooks.get(j).className== className) {
				for (int k = 0; k < this.gradebooks.get(j).students.size(); k++) {
					if (this.gradebooks.get(j).students.get(k).studentName.contains(studentName) || 
							studentName.contains(this.gradebooks.get(j).students.get(k).studentName)) {
						this.gradebooks.get(j).students.get(k).addGrade(grade);
						this.gradebooks.get(j).students.get(k).gradeCalculation(this.gradebooks.get(j).weights);
						
					}
				}
			}
		}
	}
}
