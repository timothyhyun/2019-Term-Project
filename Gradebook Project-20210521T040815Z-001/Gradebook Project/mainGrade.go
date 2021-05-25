package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"strings"
	"time"
)

/*
*--------------------------------------------------------------------------------------------------*
|									   GradeBook Server v1.2									   |
|		Developed by Daniel Cooper for Timothy Hyun's grade book project. 5/3/2019				   |
|							Written in Go and hosted on a Raspberry Pi.							   |
*--------------------------------------------------------------------------------------------------*
 */

type SignedIn struct {
	UserIds []string `json:"userIds"`
	Names []string `json:"names"`
	Type []bool `json:"type"`
	logInTime []time.Time
}

var currentUsers SignedIn

type lengthOut struct {
	Length int `json:"length"`
}

type SignOutRequest struct {
	UserId string `json:"userId"`
}

type GetDataRequest struct {
	RequestingUserId string `json:"reqUserId"`
	RequestingPassword string `json:"reqPassword"`
	TargetUserId string `json:"targetId"`
}

type AuthRequest struct {
	UserId string `json:"userId"`
	Password string `json:"password"`
}

type AuthInfo struct {
	UserIds []string
	UserNames []string
	UserType []bool //false for student, true for teacher
	UserPassword []string
}

type Assignment struct {
	Date string `json:"date"`
	Name string `json:"name"`
	Category string `json:"category"`
	TotalPoints float32 `json:"totalPoints"`
	EarnedPoints float32 `json:"earnedPoints"`
}

type AssignmentClass struct {
	Date string `json:"date"`
	Name string `json:"name"`
	Category string `json:"category"`
	AssignedTo string `json:"assignedTo"`
	AssignedToId string `json:"assignedToId"`
	TotalPoints float32 `json:"totalPoints"`
	EarnedPoints float32 `json:"earnedPoints"`
}

type NewClassRequest struct {
	RequestingUserId string `json:"reqUserId"`
	RequestingPassword string `json:"reqPassword"`
	Name string	`json:"name"`
	CategoryNames []string `json:"categoryNames"`
	CategoryWeights []string `json:"categoryWeights"`
}

type RemClassRequest struct {
	RequestingUserId string `json:"reqUserId"`
	RequestingPassword string `json:"reqPassword"`
	Name string	`json:"name"`
}

type AddStudentToClassRequest struct {
	RequestingUserId string `json:"reqUserId"`
	RequestingPassword string `json:"reqPassword"`
	StudentName string `json:"studentName"`
	StudentId string `json:"studentId"`
	TargetClassName string `json:"targetClass"`
}

type RemoveStudentFromClassRequest struct {
	RequestingUserId string `json:"reqUserId"`
	RequestingPassword string `json:"reqPassword"`
	StudentId string `json:"studentId"`
}

type createStudentAccountRequest struct {
	StudentId string `json:"studentId"`
	Name string `json:"name"`
	Password string `json:"password"`
	Grade string `json:"grade"`
}

type createTeacherAccountRequest struct {
	TeacherId string `json:"teacherId"`
	Name string `json:"name"`
	Password string `json:"password"`
}

type AssignmentRequest struct {
	RequestingUserId string `json:"reqUserId"`
	RequestingPassword string `json:"reqPassword"`
	TargetId string `json:"targetId"`
	TargetClass string `json:"targetClass"`
	Name string `json:"name"`
	Category string `json:"category"`
	TotalPoints float32
	EarnedPoints float32
}

type ClassContainer struct {
	Name string	`json:"name"`
	StudentNames []string `json:"studentNames"`
	StudentIds []string `json:"studentIds"`
	CategoryNames []string `json:"categoryNames"`
	CategoryWeights []string `json:"categoryWeights"`
	ClassAssignments []AssignmentClass `json:"classAssignments"`
}

type Class struct {
	Name string `json:"name"`
	Assignments []Assignment `json:"assignments"`
	CategoryWeights []string `json:"categoryWeights"`
	CategoryNames []string `json:"categoryNames"`
}

type Student struct {
	Name string `json:"name"`
	UserId string `json:"userId"`
	Grade string `json:"grade"`
	Teachers []string `json:"teachers"`
	Classes []Class `json:"classes"`
}

type Teacher struct {
	Name string `json:"name"`
	UserId string `json:"userId"`
	StudentIds []string `json:"studentIds"`
	Classes []ClassContainer `json:"classes"`
}

func readFile(directory string) []byte {
	file, err := ioutil.ReadFile(directory)
	if err != nil && directory == "authInfo.json"{
		var authContents AuthInfo
		authFile, _ := json.Marshal(authContents)
		ioutil.WriteFile("authInfo.json", authFile, 0644)
	}
	return file
}

func readTeacherFile(userId string) []byte {
	file := readFile(strings.Join([]string{"teachers/", userId, ".json"}, ""))
	return file
}

func readStudentFile(userId string) []byte {
	file := readFile(strings.Join([]string{"students/", userId, ".json"}, ""))
	return file
}

func addStudent(out http.ResponseWriter, req *http.Request) {
	fmt.Println("Adding Student to a Class")
	if req.Method != "POST" {
		out.WriteHeader(http.StatusMethodNotAllowed)
		return
	}
	var classReq AddStudentToClassRequest
	decoder := json.NewDecoder(req.Body)
	err := decoder.Decode(&classReq)
	if err != nil {
		out.WriteHeader(http.StatusBadRequest)
	}
	fileContents := readFile("authInfo.json")
	var authContents AuthInfo
	json.Unmarshal(fileContents, &authContents)
	if len(classReq.StudentName) > 0 {
		for i:=0; len(authContents.UserNames) > i; i++ {
			if strings.ToLower(authContents.UserNames[i]) == strings.ToLower(classReq.StudentName) && authContents.UserType[i] == false {
				classReq.StudentId = authContents.UserIds[i]
			}
		}
	}
	if len(classReq.StudentId) == 0 {
		out.WriteHeader(http.StatusBadRequest)
		return
	}
	for i:=0; len(authContents.UserIds) > i; i++ {
		if authContents.UserIds[i] == classReq.RequestingUserId {
			if authContents.UserPassword[i] != classReq.RequestingPassword || authContents.UserType[i] != true {
				out.WriteHeader(http.StatusUnauthorized)
				return
			}
			var teacherContents Teacher
			teacherFile := readTeacherFile(classReq.RequestingUserId)
			json.Unmarshal(teacherFile, &teacherContents)
			var studentContents Student
			studentFile := readStudentFile(classReq.StudentId)
			json.Unmarshal(studentFile, &studentContents)
			for i:=0; len(teacherContents.Classes) > i; i++ {
				if teacherContents.Classes[i].Name == classReq.TargetClassName {
					teacherContents.Classes[i].StudentIds = append(teacherContents.Classes[i].StudentIds, classReq.StudentId)
					teacherContents.Classes[i].StudentNames = append(teacherContents.Classes[i].StudentNames, studentContents.Name)
					studentContents.Teachers = append(studentContents.Teachers, classReq.RequestingUserId)
					studentContents.Classes = append(studentContents.Classes, Class{
						Name:            classReq.TargetClassName,
						Assignments:     nil,
						CategoryWeights: teacherContents.Classes[i].CategoryWeights,
						CategoryNames:   teacherContents.Classes[i].CategoryNames,
					})
				}
			}
			newFile, _ := json.Marshal(teacherContents)
			err := ioutil.WriteFile(strings.Join([]string{"teachers/", classReq.RequestingUserId, ".json"}, ""), newFile, 0644)
			if err != nil {
				out.WriteHeader(http.StatusInternalServerError)
				return
			}
			newFile2, _ := json.Marshal(studentContents)
			err = ioutil.WriteFile(strings.Join([]string{"students/", classReq.StudentId, ".json"}, ""), newFile2, 0644)
			if err != nil {
				out.WriteHeader(http.StatusInternalServerError)
				return
			}
			dummyAssignment := AssignmentRequest{
				RequestingUserId:   classReq.RequestingUserId,
				RequestingPassword: classReq.RequestingPassword,
				TargetId:           classReq.StudentId,
				TargetClass:        classReq.TargetClassName,
				Name:               "Dummy Assignment",
				Category:           "Test",
				TotalPoints:        0,
				EarnedPoints:       0,
			}
			jsonStr, _ := json.Marshal(dummyAssignment)
			req, err := http.NewRequest("POST", "http://localhost:8001/addAssignment", bytes.NewBuffer(jsonStr))
			req.Header.Set("X-Custom-Header", "myvalue")
			req.Header.Set("Content-Type", "application/json")

			client := &http.Client{}
			resp, err := client.Do(req)
			if err != nil {
				out.WriteHeader(http.StatusInternalServerError)
				return;
			}
			defer resp.Body.Close()
			out.Header().Set("Access-Control-Allow-Origin", "*")
			out.Write([]byte(classReq.StudentId))
			out.WriteHeader(http.StatusOK)
		}
	}
}

func removeStudent(out http.ResponseWriter, req *http.Request) {

}

func addAssignment(out http.ResponseWriter, req *http.Request) {
	fmt.Println("Adding Assignment")
	if req.Method != "POST" {
		out.WriteHeader(http.StatusMethodNotAllowed)
		return
	}
	var assignmentReq AssignmentRequest
	decoder := json.NewDecoder(req.Body)
	err := decoder.Decode(&assignmentReq)
	if err != nil {
		out.WriteHeader(http.StatusBadRequest)
	}
	fileContents := readFile("authInfo.json")
	var authContents AuthInfo
	json.Unmarshal(fileContents, &authContents)
	for i:=0; len(authContents.UserIds) > i; i++ {
		if authContents.UserIds[i] == assignmentReq.RequestingUserId {
			if authContents.UserPassword[i] != assignmentReq.RequestingPassword || authContents.UserType[i] != true {
				out.WriteHeader(http.StatusUnauthorized)
				return
			}
			var teacherContents Teacher
			teacherFile := readTeacherFile(assignmentReq.RequestingUserId)
			json.Unmarshal(teacherFile, &teacherContents)
			for a:=0; len(teacherContents.Classes) > a; a++ {
				if teacherContents.Classes[a].Name == assignmentReq.TargetClass {
					for b:=0; len(teacherContents.Classes[a].StudentNames) > b; b++ {
						if teacherContents.Classes[a].StudentIds[b] == assignmentReq.TargetId {
							var studentContents Student
							studentFile := readStudentFile(teacherContents.Classes[a].StudentIds[b])
							json.Unmarshal(studentFile, &studentContents)
							for c:=0; len(studentContents.Classes) > c; c++ {
								if studentContents.Classes[c].Name == assignmentReq.TargetClass {
									t := time.Now()
									studentContents.Classes[c].Assignments = append(studentContents.Classes[c].Assignments, Assignment{
										Date:          t.Format("01-02-2006"),
										Name:          assignmentReq.Name,
										Category:      assignmentReq.Category,
										TotalPoints:   assignmentReq.TotalPoints,
										EarnedPoints:  assignmentReq.EarnedPoints,
									})
									teacherContents.Classes[a].ClassAssignments = append(teacherContents.Classes[a].ClassAssignments, AssignmentClass{
										Date:         t.Format("01-02-2006"),
										Name:         assignmentReq.Name,
										Category:     assignmentReq.Category,
										AssignedTo:   studentContents.Name,
										AssignedToId: studentContents.UserId,
										TotalPoints:  assignmentReq.TotalPoints,
										EarnedPoints: assignmentReq.EarnedPoints,
									})
									newFile, _ := json.Marshal(studentContents)
									err := ioutil.WriteFile(strings.Join([]string{"students/", assignmentReq.TargetId, ".json"}, ""), newFile, 0644)
									if err != nil {
										out.WriteHeader(http.StatusInternalServerError)
										return
									}
									newFile2, _ := json.Marshal(teacherContents)
									err = ioutil.WriteFile(strings.Join([]string{"teachers/", assignmentReq.RequestingUserId, ".json"}, ""), newFile2, 0644)
									if err != nil {
										out.WriteHeader(http.StatusInternalServerError)
										return
									}
									out.WriteHeader(http.StatusOK)
									return
								}
							}
						}
					}
				}
			}
			out.Header().Set("Access-Control-Allow-Origin", "*")
			out.Header().Set("Content-type", "application/json")
			out.WriteHeader(http.StatusOK)
		}
	}
}

func editAssignment(out http.ResponseWriter, req *http.Request) {
	fmt.Println("Editing assignment")
	if req.Method != "POST" {
		out.WriteHeader(http.StatusMethodNotAllowed)
		return
	}
	var assignmentReq AssignmentRequest
	decoder := json.NewDecoder(req.Body)
	err := decoder.Decode(&assignmentReq)
	if err != nil {
		out.WriteHeader(http.StatusBadRequest)
	}
	fileContents := readFile("authInfo.json")
	var authContents AuthInfo
	json.Unmarshal(fileContents, &authContents)
	for i:=0; len(authContents.UserIds) > i; i++ {
		if authContents.UserIds[i] == assignmentReq.RequestingUserId {
			if authContents.UserPassword[i] != assignmentReq.RequestingPassword || authContents.UserType[i] != true {
				out.WriteHeader(http.StatusUnauthorized)
				return
			}
			var teacherContents Teacher
			teacherFile := readTeacherFile(assignmentReq.RequestingUserId)
			json.Unmarshal(teacherFile, &teacherContents)
			for a:=0; len(teacherContents.Classes) > a; a++ {
				if teacherContents.Classes[a].Name == assignmentReq.TargetClass {
					for b:=0; len(teacherContents.Classes[a].StudentNames) > b; b++ {
						if teacherContents.Classes[a].StudentIds[b] == assignmentReq.TargetId {
							var studentContents Student
							studentFile := readStudentFile(teacherContents.Classes[a].StudentIds[b])
							json.Unmarshal(studentFile, &studentContents)
							for c:=0; len(studentContents.Classes) > c; c++ {
								if studentContents.Classes[c].Name == assignmentReq.TargetClass {
									for d:=0; len(studentContents.Classes[c].Assignments) > d; d++ {
										if studentContents.Classes[c].Assignments[d].Name == assignmentReq.Name {
											studentContents.Classes[c].Assignments[d].EarnedPoints = assignmentReq.EarnedPoints
											studentContents.Classes[c].Assignments[d].TotalPoints = assignmentReq.TotalPoints
										}
									}
									for d:=0; len(teacherContents.Classes[a].ClassAssignments) > d; d++ {
										if teacherContents.Classes[a].ClassAssignments[d].Name == assignmentReq.Name {
											teacherContents.Classes[a].ClassAssignments[d].TotalPoints = assignmentReq.TotalPoints
											teacherContents.Classes[a].ClassAssignments[d].EarnedPoints = assignmentReq.EarnedPoints
										}
									}
									newFile, _ := json.Marshal(studentContents)
									err := ioutil.WriteFile(strings.Join([]string{"students/", assignmentReq.TargetId, ".json"}, ""), newFile, 0644)
									if err != nil {
										out.WriteHeader(http.StatusInternalServerError)
										return
									}
									newFile2, _ := json.Marshal(teacherContents)
									err = ioutil.WriteFile(strings.Join([]string{"teachers/", assignmentReq.RequestingUserId, ".json"}, ""), newFile2, 0644)
									if err != nil {
										out.WriteHeader(http.StatusInternalServerError)
										return
									}
									out.WriteHeader(http.StatusOK)
								}
							}
						}
					}
				}
			}
			out.Header().Set("Access-Control-Allow-Origin", "*")
			out.Header().Set("Content-type", "application/json")
			out.WriteHeader(http.StatusOK)
		}
	}
}

func removeAssignment(out http.ResponseWriter, req *http.Request) {
	fmt.Println("Removing an Assignment.")
	if req.Method != "POST" {
		out.WriteHeader(http.StatusMethodNotAllowed)
		return
	}
	var assignmentReq AssignmentRequest
	decoder := json.NewDecoder(req.Body)
	err := decoder.Decode(&assignmentReq)
	if err != nil {
		out.WriteHeader(http.StatusBadRequest)
	}
	fileContents := readFile("authInfo.json")
	var authContents AuthInfo
	json.Unmarshal(fileContents, &authContents)
	for i:=0; len(authContents.UserIds) > i; i++ {
		if authContents.UserIds[i] == assignmentReq.RequestingUserId {
			if authContents.UserPassword[i] != assignmentReq.RequestingPassword || authContents.UserType[i] != true {
				out.WriteHeader(http.StatusUnauthorized)
				return
			}
			var teacherContents Teacher
			teacherFile := readTeacherFile(assignmentReq.RequestingUserId)
			json.Unmarshal(teacherFile, &teacherContents)
			for a:=0; len(teacherContents.Classes) > a; a++ {
				if teacherContents.Classes[a].Name == assignmentReq.TargetClass {
					for b:=0; len(teacherContents.Classes[a].StudentNames) > b; b++ {
						if teacherContents.Classes[a].StudentIds[b] == assignmentReq.TargetId {
							var studentContents Student
							studentFile := readStudentFile(teacherContents.Classes[a].StudentIds[b])
							json.Unmarshal(studentFile, &studentContents)
							for c:=0; len(studentContents.Classes) > c; c++ {
								if studentContents.Classes[c].Name == assignmentReq.TargetClass {
									for d:=0; len(studentContents.Classes[c].Assignments) > d; d++ {
										if studentContents.Classes[c].Assignments[d].Name == assignmentReq.Name {
											studentContents.Classes[c].Assignments = append(studentContents.Classes[c].Assignments[d:], studentContents.Classes[c].Assignments[:d+1]...)
										}
									}
									for d:=0; len(teacherContents.Classes[a].ClassAssignments) > d; d++ {
										if teacherContents.Classes[a].ClassAssignments[d].Name == assignmentReq.Name {
											teacherContents.Classes[a].ClassAssignments = append(teacherContents.Classes[a].ClassAssignments[d:], teacherContents.Classes[a].ClassAssignments[:d+1]...)
										}
									}
									newFile, _ := json.Marshal(studentContents)
									err := ioutil.WriteFile(strings.Join([]string{"students/", assignmentReq.TargetId, ".json"}, ""), newFile, 0644)
									if err != nil {
										out.WriteHeader(http.StatusInternalServerError)
										return
									}
									newFile2, _ := json.Marshal(teacherContents)
									err = ioutil.WriteFile(strings.Join([]string{"teachers/", assignmentReq.RequestingUserId, ".json"}, ""), newFile2, 0644)
									if err != nil {
										out.WriteHeader(http.StatusInternalServerError)
										return
									}
									out.WriteHeader(http.StatusOK)
								}
							}
						}
					}
				}
			}
			out.Header().Set("Access-Control-Allow-Origin", "*")
			out.Header().Set("Content-type", "application/json")
			out.WriteHeader(http.StatusOK)
		}
	}
}

func getTeacherData(out http.ResponseWriter, req *http.Request) {
	fmt.Println("Retrieving teacher data.")
	if req.Method != "POST" {
		out.WriteHeader(http.StatusMethodNotAllowed)
		return
	}
	var dataReq GetDataRequest
	decoder := json.NewDecoder(req.Body)
	err := decoder.Decode(&dataReq)
	if err != nil {
		out.WriteHeader(http.StatusBadRequest)
	}
	fileContents := readFile("authInfo.json")
	var authContents AuthInfo
	json.Unmarshal(fileContents, &authContents)
	for i:=0; len(authContents.UserIds) > i; i++ {
		if authContents.UserIds[i] == dataReq.RequestingUserId {
			if authContents.UserPassword[i] != dataReq.RequestingPassword || authContents.UserType[i] != true {
				out.WriteHeader(http.StatusUnauthorized)
				return
			}
			teacherFile := readTeacherFile(dataReq.TargetUserId)
			out.Write(teacherFile)
			out.Header().Set("Access-Control-Allow-Origin", "*")
			out.Header().Set("Content-type", "application/json")
			out.WriteHeader(http.StatusOK)
		}
	}
}

func getStudentData(out http.ResponseWriter, req *http.Request) {
	fmt.Println("Retrieving Student data.")
	if req.Method != "POST" {
		out.WriteHeader(http.StatusMethodNotAllowed)
		return
	}
	var dataReq GetDataRequest
	decoder := json.NewDecoder(req.Body)
	err := decoder.Decode(&dataReq)
	if err != nil {
		out.WriteHeader(http.StatusBadRequest)
	}
	fileContents := readFile("authInfo.json")
	var authContents AuthInfo
	json.Unmarshal(fileContents, &authContents)
	for i:=0; len(authContents.UserIds) > i; i++ {
		if authContents.UserIds[i] == dataReq.RequestingUserId {
			if authContents.UserPassword[i] != dataReq.RequestingPassword {
				out.WriteHeader(http.StatusUnauthorized)
				return
			}
			studentFile := readStudentFile(dataReq.TargetUserId)
			out.Write(studentFile)
			out.Header().Set("Access-Control-Allow-Origin", "*")
			out.Header().Set("Content-type", "application/json")
			out.WriteHeader(http.StatusOK)
		}
	}
}

func authStudent(out http.ResponseWriter, req *http.Request) {
	fmt.Println("Authenticating Student...")
	if req.Method != "POST" {
		out.WriteHeader(http.StatusMethodNotAllowed)
		return
	}
	var authReq AuthRequest
	decoder := json.NewDecoder(req.Body)
	err := decoder.Decode(&authReq)
	if err != nil {
		out.WriteHeader(http.StatusBadRequest)
	}
	fileContents := readFile("authInfo.json")
	var authContents AuthInfo
	json.Unmarshal(fileContents, &authContents)
	for i:=0; len(authContents.UserIds) > i; i++ {
		if authContents.UserIds[i] == authReq.UserId {
			if authContents.UserPassword[i] != authReq.Password || authContents.UserType[i] != false {
				out.WriteHeader(http.StatusUnauthorized)
				return
			}
			studentFile := readStudentFile(authReq.UserId)
			var studentContents Student
			json.Unmarshal(studentFile, &studentContents)
			out.Header().Set("Access-Control-Allow-Origin", "*")
			out.Header().Set("Content-type", "application/json")
			out.WriteHeader(http.StatusOK)
			out.Write(studentFile)
			fmt.Printf("Student Id %v has been authenticated!\n", authReq.UserId)
			currentUsers.UserIds = append(currentUsers.UserIds, authReq.UserId)
			currentUsers.Type = append(currentUsers.Type, false)
			currentUsers.Names = append(currentUsers.Names, studentContents.Name)
			currentUsers.logInTime = append(currentUsers.logInTime, time.Now())
		}
	}
}

func authTeacher(out http.ResponseWriter, req *http.Request) {
	fmt.Println("Authenticating Teacher")
	if req.Method != "POST" {
		out.WriteHeader(http.StatusMethodNotAllowed)
		return
	}
	var authReq AuthRequest
	decoder := json.NewDecoder(req.Body)
	err := decoder.Decode(&authReq)
	if err != nil {
		out.WriteHeader(http.StatusBadRequest)
	}
	fileContents := readFile("authInfo.json")
	var authContents AuthInfo
	json.Unmarshal(fileContents, &authContents)
	for i:=0; len(authContents.UserIds) > i; i++ {
		if authContents.UserIds[i] == authReq.UserId {
			if authContents.UserPassword[i] != authReq.Password || authContents.UserType[i] != true {
				out.WriteHeader(http.StatusUnauthorized)
				return
			}
			teacherFile := readTeacherFile(authReq.UserId)
			var teacherContents Student
			json.Unmarshal(teacherFile, &teacherContents)
			out.Header().Set("Access-Control-Allow-Origin", "*")
			out.Header().Set("Content-type", "application/json")
			out.WriteHeader(http.StatusOK)
			out.Write(teacherFile)
			fmt.Printf("Teacher Id %v has been authenticated!\n", authReq.UserId)
			currentUsers.UserIds = append(currentUsers.UserIds, authReq.UserId)
			currentUsers.Type = append(currentUsers.Type, true)
			currentUsers.Names = append(currentUsers.Names, teacherContents.Name)
			currentUsers.logInTime = append(currentUsers.logInTime, time.Now())
		}
	}
}

func addClass(out http.ResponseWriter, req *http.Request) {
	fmt.Println("Adding a class")
	if req.Method != "POST" {
		out.WriteHeader(http.StatusMethodNotAllowed)
		return
	}
	var classReq NewClassRequest
	decoder := json.NewDecoder(req.Body)
	err := decoder.Decode(&classReq)
	if err != nil {
		out.WriteHeader(http.StatusBadRequest)
	}
	fileContentsAuth := readFile("authInfo.json")
	var authContents AuthInfo
	json.Unmarshal(fileContentsAuth, &authContents)
	for i:=0; len(authContents.UserIds) > i; i++ {
		if authContents.UserIds[i] == classReq.RequestingUserId {
			if authContents.UserPassword[i] != classReq.RequestingPassword || authContents.UserType[i] != true {
				out.WriteHeader(http.StatusUnauthorized)
				return
			}

			teacherFile := readTeacherFile(classReq.RequestingUserId)
			var teacherContents Teacher
			err := json.Unmarshal(teacherFile, &teacherContents)
			if err != nil {
				out.WriteHeader(http.StatusInternalServerError)
			}
			teacherContents.Classes = append(teacherContents.Classes, ClassContainer{
				Name:               classReq.Name,
				StudentNames:       nil,
				StudentIds:         nil,
				CategoryNames:      classReq.CategoryNames,
				CategoryWeights:    classReq.CategoryWeights,
			})
			newFileContents, err := json.Marshal(teacherContents)
			if err != nil {
				out.WriteHeader(http.StatusInternalServerError)
				return
			}
			err = ioutil.WriteFile(strings.Join([]string{"teachers/", classReq.RequestingUserId, ".json"}, ""), newFileContents, 0644)
			if err != nil {
				out.WriteHeader(http.StatusInternalServerError)
				return
			}
			out.Header().Set("Access-Control-Allow-Origin", "*")
			out.Header().Set("Content-type", "application/json")
			out.WriteHeader(http.StatusOK)
		}
	}

}

func removeClass(out http.ResponseWriter, req *http.Request) {
	fmt.Println("Removing a class")
	if req.Method != "POST" {
		out.WriteHeader(http.StatusMethodNotAllowed)
		return
	}
	var classReq RemClassRequest
	decoder := json.NewDecoder(req.Body)
	err := decoder.Decode(&classReq)
	if err != nil {
		out.WriteHeader(http.StatusBadRequest)
	}
	fileContentsAuth := readFile("authInfo.json")
	var authContents AuthInfo
	json.Unmarshal(fileContentsAuth, &authContents)
	for i:=0; len(authContents.UserIds) > i; i++ {
		if authContents.UserIds[i] == classReq.RequestingUserId {
			if authContents.UserPassword[i] != classReq.RequestingPassword || authContents.UserType[i] != true {
				out.WriteHeader(http.StatusUnauthorized)
				return
			}

			teacherFile := readTeacherFile(classReq.RequestingUserId)
			var teacherContents Teacher
			err := json.Unmarshal(teacherFile, &teacherContents)
			if err != nil {
				out.WriteHeader(http.StatusInternalServerError)
			}
			for i:=0;len(teacherContents.Classes) >i; i++ {
				if teacherContents.Classes[i].Name == classReq.Name {
					for a:=0; len(teacherContents.Classes[i].StudentIds) > i; a++ {
						var studentContents Student
						studentFile := readStudentFile(teacherContents.Classes[i].StudentIds[a])
						err := json.Unmarshal(studentFile, &studentContents)
						if err != nil {
							out.WriteHeader(http.StatusInternalServerError)
							return
						}
						for b:=0; len(studentContents.Classes) > b; b++ {
							if studentContents.Classes[b].Name == classReq.Name {
								studentContents.Classes = append(studentContents.Classes[:b], studentContents.Classes[b+1:]...)
							}
						}
					}
					teacherContents.Classes = append(teacherContents.Classes[:i], teacherContents.Classes[i+1:]...)
					newFileContents, err := json.Marshal(teacherContents)
					if err != nil {
						out.WriteHeader(http.StatusInternalServerError)
						return
					}
					err = ioutil.WriteFile(strings.Join([]string{"teachers/", classReq.RequestingUserId, ".json"}, ""), newFileContents, 0644)
					if err != nil {
						out.WriteHeader(http.StatusInternalServerError)
						return
					}
					out.Header().Set("Access-Control-Allow-Origin", "*")
					out.Header().Set("Content-type", "application/json")
					out.WriteHeader(http.StatusOK)
				}
			}
		}
	}
}

func createStudentAccount(out http.ResponseWriter, req *http.Request) {
	fmt.Println("Creating Student Account")
	if req.Method != "POST" {
		out.WriteHeader(http.StatusMethodNotAllowed)
		return
	}
	var accountReq createStudentAccountRequest
	decoder := json.NewDecoder(req.Body)
	err := decoder.Decode(&accountReq)
	if err != nil {
		out.WriteHeader(http.StatusBadRequest)
		return
	}
	if len(accountReq.StudentId) == 0 || len(accountReq.Name) == 0 || len(accountReq.Password) == 0 {
		out.WriteHeader(http.StatusBadRequest)
		return
	}
	fileContents := readFile("authInfo.json")
	var authContents AuthInfo
	json.Unmarshal(fileContents, &authContents)
	authContents.UserType = append(authContents.UserType, false)
	authContents.UserPassword = append(authContents.UserPassword, accountReq.Password)
	authContents.UserIds = append(authContents.UserIds, accountReq.StudentId)
	authContents.UserNames = append(authContents.UserNames, accountReq.Name)
	newFileContents, err := json.Marshal(authContents)
	if err != nil {
		out.WriteHeader(http.StatusInternalServerError)
		return
	}
	err = ioutil.WriteFile("authInfo.json", newFileContents, 0644)
	if err != nil {
		out.WriteHeader(http.StatusInternalServerError)
		return
	}
	studentContents := Student{
		Name:     accountReq.Name,
		UserId:   accountReq.StudentId,
		Grade:	  accountReq.Grade,
		Teachers: nil,
		Classes:  nil,
	}
	newFileContents2, err := json.Marshal(studentContents)
	if err != nil {
		out.WriteHeader(http.StatusInternalServerError)
		return
	}
	err = ioutil.WriteFile(strings.Join([]string{"students/", accountReq.StudentId, ".json"}, ""), newFileContents2, 0644)
	if err != nil {
		out.WriteHeader(http.StatusInternalServerError)
		return
	}
	out.WriteHeader(http.StatusOK)
}

func createTeacherAccount(out http.ResponseWriter, req *http.Request) {
	fmt.Println("Creating Teacher Account...")
	if req.Method != "POST" {
		out.WriteHeader(http.StatusMethodNotAllowed)
		return
	}
	var accountReq createTeacherAccountRequest
	decoder := json.NewDecoder(req.Body)
	err := decoder.Decode(&accountReq)
	if err != nil {
		out.WriteHeader(http.StatusBadRequest)
		return
	}
	if len(accountReq.TeacherId) == 0 || len(accountReq.Name) == 0 || len(accountReq.Password) == 0 {
		out.WriteHeader(http.StatusBadRequest)
		return
	}
	fileContents := readFile("authInfo.json")
	var authContents AuthInfo
	json.Unmarshal(fileContents, &authContents)
	authContents.UserType = append(authContents.UserType, true)
	authContents.UserPassword = append(authContents.UserPassword, accountReq.Password)
	authContents.UserIds = append(authContents.UserIds, accountReq.TeacherId)
	authContents.UserNames = append(authContents.UserNames, accountReq.Name)
	newFileContents, err := json.Marshal(authContents)
	if err != nil {
		out.WriteHeader(http.StatusInternalServerError)
		return
	}
	err = ioutil.WriteFile("authInfo.json", newFileContents, 0644)
	if err != nil {
		out.WriteHeader(http.StatusInternalServerError)
		return
	}
	teacherContents := Teacher{
		Name:       accountReq.Name,
		UserId:     accountReq.TeacherId,
		StudentIds: nil,
		Classes:    nil,
	}
	newFileContents2, err := json.Marshal(teacherContents)
	if err != nil {
		out.WriteHeader(http.StatusInternalServerError)
		return
	}
	err = ioutil.WriteFile(strings.Join([]string{"teachers/", accountReq.TeacherId, ".json"}, ""), newFileContents2, 0644)
	if err != nil {
		out.WriteHeader(http.StatusInternalServerError)
		return
	}
	out.WriteHeader(http.StatusOK)
}

func getNextId(out http.ResponseWriter, req *http.Request) {
	fmt.Println("Getting Next ID Available")
	if req.Method != "POST" {
		out.WriteHeader(http.StatusMethodNotAllowed)
		return
	}
	fileContents := readFile("authInfo.json")
	var authContents AuthInfo
	json.Unmarshal(fileContents, &authContents)
	var totalLength lengthOut
	totalLength.Length = len(authContents.UserIds)
	output, _ := json.Marshal(totalLength)
	out.Header().Set("Access-Control-Allow-Origin", "*")
	out.Header().Set("Content-type", "application/json")
	out.Write(output)
	out.WriteHeader(http.StatusOK)
}

func signOut(out http.ResponseWriter, req *http.Request) {
	if req.Method != "POST" {
		out.WriteHeader(http.StatusMethodNotAllowed)
		return
	}
	var authReq SignOutRequest
	decoder := json.NewDecoder(req.Body)
	err := decoder.Decode(&authReq)
	if err != nil {
		out.WriteHeader(http.StatusBadRequest)
	}
	for i:=0; len(currentUsers.logInTime) > i; i++ {
		if currentUsers.UserIds[i] == authReq.UserId {
			fmt.Printf("User ID: %v has been signed out.\n", currentUsers.UserIds[i])
			currentUsers.logInTime = append(currentUsers.logInTime[:i], currentUsers.logInTime[i+1:]...)
			currentUsers.Names = append(currentUsers.Names[:i], currentUsers.Names[i+1:]...)
			currentUsers.Type = append(currentUsers.Type[:i], currentUsers.Type[i+1:]...)
			currentUsers.UserIds = append(currentUsers.UserIds[:i], currentUsers.UserIds[i+1:]...)
		}
	}
	out.WriteHeader(http.StatusOK)
}

func getCurrentUsers(out http.ResponseWriter, req *http.Request) {
	output, _ := json.Marshal(currentUsers)
	out.Write(output)
	out.WriteHeader(http.StatusOK)
}

func getDate(out http.ResponseWriter, req *http.Request) {
	t := time.Now()
	out.Write([]byte(t.Format("01-02-2006")))
	out.WriteHeader(http.StatusOK)
}


func forever() {
	for {
		time.Sleep(10*time.Second)
		for i:=0; len(currentUsers.logInTime) > i; i++ {
			if time.Since(currentUsers.logInTime[i]) > time.Hour {
				fmt.Printf("User ID: %v has been idling for one hour and has been signed out.\n", currentUsers.UserIds[i])
				currentUsers.logInTime = append(currentUsers.logInTime[:i], currentUsers.logInTime[i+1:]...)
				currentUsers.Names = append(currentUsers.Names[:i], currentUsers.Names[i+1:]...)
				currentUsers.Type = append(currentUsers.Type[:i], currentUsers.Type[i+1:]...)
				currentUsers.UserIds = append(currentUsers.UserIds[:i], currentUsers.UserIds[i+1:]...)
			}
		}
	}
}

func initServer() {
	http.HandleFunc("/addStudent", addStudent)
	http.HandleFunc("/removeStudent", removeStudent)
	http.HandleFunc("/addAssignment", addAssignment)
	http.HandleFunc("/editAssignment", editAssignment)
	http.HandleFunc("/removeAssignment", removeAssignment)
	http.HandleFunc("/getTeacherData", getTeacherData)
	http.HandleFunc("/getStudentData", getStudentData)
	http.HandleFunc("/authStudent", authStudent)
	http.HandleFunc("/authTeacher", authTeacher)
	http.HandleFunc("/createStudentAccount", createStudentAccount)
	http.HandleFunc("/createTeacherAccount", createTeacherAccount)
	http.HandleFunc("/addClass", addClass)
	http.HandleFunc("/removeClass", removeClass)
	http.HandleFunc("/getNextId", getNextId)
	http.HandleFunc("/signOut", signOut)
	http.HandleFunc("/getCurrentUsers", getCurrentUsers)
	http.HandleFunc("/getDate", getDate)
	http.ListenAndServe(":8001", nil)
}

func main() {
	go initServer()
	fmt.Println("GradeBook v1.2 Active on port 8001")
	go forever()
	select {} // block forever
}
