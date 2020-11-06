package com.capgemini.employee_payroll;

import java.time.LocalDate;
import java.util.Objects;

public class Employee_payroll_Data {
	public int id;
	public String name;
	public double salary;
	public LocalDate start;
	public String gender;
	public String companyName;
	public int companyId;
	public String department[];
	public int departmentId;
	public String departmentName;
	public boolean active;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCompanyId() {
		return companyId;
	}

	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}

	public String[] getDepartment() {
		return department;
	}

	public void setDepartment(String[] department) {
		this.department = department;
	}

	public Employee_payroll_Data(int id, String name, double salary) {
		this.id = id;
		this.name = name;
		this.salary = salary;
	}

	public Employee_payroll_Data(int id, int departmentId) {
		this.id = id;
		this.departmentId = departmentId;
	}

	public Employee_payroll_Data(int id, String name, double salary, LocalDate start) {
		this(id, name, salary);
		this.start = start;
	}

	public Employee_payroll_Data(int id, String name, double salary, LocalDate start, String gender) {
		this(id, name, salary, start);
		this.gender = gender;
	}

	public Employee_payroll_Data(int id, String name, double salary, LocalDate start, String gender, boolean active) {
		this(id, name, salary, start, gender);
		this.active = active;
	}

	public Employee_payroll_Data(int id, String name, double salary, LocalDate start, String gender, int companyId) {
		this(id, name, salary, start, gender);
		this.companyId = companyId;
	}

	public Employee_payroll_Data(int id, String name, double salary, LocalDate start, String gender,
			String[] department) {
		this(id, name, salary, start, gender);
		this.department = department;
	}

	public Employee_payroll_Data(int id, String name, double salary, LocalDate start, String gender,
			String companyName, int companyId, String[] department) {
		this(id, name, salary, start, gender, department);
		this.companyName = companyName;
		this.companyId = companyId;
	}

	public Employee_payroll_Data(int departmentId, String departmentName) {
		this.departmentId = departmentId;
		this.departmentName = departmentName;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, gender, salary, start);
	}

	@Override
	public String toString() {
		return "Employee_payroll_Data [id=" + id + ", name=" + name + ", salary=" + salary + ", startDate=" + start
				+ "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Employee_payroll_Data that = (Employee_payroll_Data) obj;
		return id == that.id && Double.compare(that.salary, salary) == 0 && name.equals(that.name);
	}

	public void printDepartments() {
		String departments[] = this.getDepartment();
		for (String s : departments) {
			System.out.println("id: " + this.getId() + ":" + s);
		}
	}
}
