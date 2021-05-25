/*
 * Timothy Hyun
 * Commander Schenk
 * AP Computer Science A
 * Master Project
 */
package application;

public abstract class Grade {
	public String type;
	public int totalPoints;
	public int pointsEarned;
	public double percentage;
	public String name;
	public String dateCreated;
	
	public Grade(String type, int totalPoints,int pointsEarned, String name, String dateCreated) {
		this.type = type;
		this.totalPoints = totalPoints;
		this.pointsEarned = pointsEarned;
		this.percentage = Math.round(100.0*(100*(this.pointsEarned*1.0)/(this.totalPoints*1.0)))/100.0;
		this.name = name;
		this.dateCreated = dateCreated;
	}
	public String getType() {
		return this.type;
	}
	public int getTotalPoints() {
		return this.totalPoints;
	}
	public double getPercentage() {
		return Math.round(100.0*(100*(this.pointsEarned*1.0)/(this.totalPoints*1.0)))/100.0;
	}
	public void setTotalPoints(int totalPoints) {
		this.totalPoints = totalPoints;
	}
	
	public int getPointsEarned() {
		return this.pointsEarned;
	}
	
	public void setPointsEarned(int pointsEarned) {
		this.pointsEarned = pointsEarned;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}
	
}
