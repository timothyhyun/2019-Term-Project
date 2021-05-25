package application;

import java.util.ArrayList;

public class StudentGrades {
	public String studentName;
	
	public String gradebookName;
	public ArrayList<Grade> grades;
	public double currentGrade;
	public String studentID;
	public int[] weights;
	
	public StudentGrades(String studentName, String gradebookName, String studentID) {
			this.studentName = studentName;
			this.gradebookName = gradebookName;
			this.grades = new ArrayList<Grade>();
			this.currentGrade = 0;
			this.studentID = studentID;
			this.weights = null;
			
	}
	public StudentGrades(String studentName, String gradebookName, String studentID, int[] weights) {
		this.studentName = studentName;
		this.gradebookName = gradebookName;
		this.grades = new ArrayList<Grade>();
		this.currentGrade = 0;
		this.studentID = studentID;
		this.weights = weights;
	}
	public String getstudentName() {
		return studentName;
	}
	public String getGradebookName() {
		return gradebookName;
	}
	public ArrayList<Grade> getGrades() {
		return grades;
	}


	public void setGrades(ArrayList<Grade> grades) {
		this.grades = grades;
	}
	
	public void addGrade(Grade grade) {
		grades.add(grade);
	}

	
	public double gradeCalculation(int[] weights) {
		double[] gradeTotals = {0,0,0,0};
		double[] gradeEarned = {0,0,0,0};
		for (int i = 0; i < this.getGrades().size(); i++) {
			Grade temp = this.getGrades().get(i);
			
			if (temp.type == "Quiz") {
				gradeEarned[0] += temp.pointsEarned;
				gradeTotals[0] += temp.totalPoints;	
			} if (temp.type == "Test"){
				gradeEarned[1] += temp.pointsEarned;
				gradeTotals[1] += temp.totalPoints;
			} if (temp.type == "HW") {
				gradeEarned[2] +=temp.pointsEarned;
				gradeTotals[2] += temp.totalPoints;
			} if (temp.type == "Final") {
				gradeEarned[3] += temp.pointsEarned;
				gradeTotals[3] += temp.totalPoints;
			}
			int totalPoints = 100;
			if (gradeTotals[0] == 0) {
				totalPoints -= weights[0];
			} if (gradeTotals[1] == 0) {
				totalPoints-= weights[1];
			} if (gradeTotals[2] == 0) {
				totalPoints -= weights[2];
			} if (gradeTotals[3] == 0) {
				totalPoints -= weights[3];
			}
			double pointsEarned = 0;
			for (int j = 0; j < weights.length; j++) {
				if (gradeTotals[j] > 0) {
					//System.out.println(gradeEarned[j]);
					pointsEarned += ((gradeEarned[j]/gradeTotals[j]) * weights[j]);
				}
				
			}
		
			double unrounded= (pointsEarned / totalPoints) * 100;
			this.currentGrade = Math.round(unrounded*100.0)/100.0;
			
			
		}
		return currentGrade;
	}
}
