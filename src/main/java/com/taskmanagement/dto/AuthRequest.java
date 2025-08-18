package com.taskmanagement.dto;

public class AuthRequest {
    private String empId;
    private String name;
    private String password;

    public AuthRequest() {
    }

	public String getEmpId() {
		return empId;
	}

	@Override
	public String toString() {
		return "AuthRequest [empId=" + empId + ", name=" + name + ", password=" + password + "]";
	}

	public AuthRequest(String empId, String name, String password) {
		super();
		this.empId = empId;
		this.name = name;
		this.password = password;
	}

	public void setEmpId(String empId) {
		this.empId = empId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

    
}