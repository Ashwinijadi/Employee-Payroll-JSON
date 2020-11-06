package com.capgemini.employee_payroll;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class Employee_payroll_DBService {
	private PreparedStatement employeePayrollDataStatement;
	private static Employee_payroll_DBService employeePayrollDBService;

	public Employee_payroll_DBService() {
	}

	public static Employee_payroll_DBService getInstance() {
		if (employeePayrollDBService == null)
			employeePayrollDBService = new Employee_payroll_DBService();
		return employeePayrollDBService;
	}

	public List<Employee_payroll_Data> readData() {
		String sql = "SELECT * FROM employee_payroll;";
		List<Employee_payroll_Data> employeePayrollList = new ArrayList<Employee_payroll_Data>();
		try (Connection connection = this.getConnection();) {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			employeePayrollList = this.getEmployeePayrollData(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	public int updateEmployeeData(String name, Double salary) {
		return this.updateEmployeeDataUsingPreparedStatement(name, salary);
	}

	private int updateEmployeeDataUsingPreparedStatement(String name, Double salary) {
		try (Connection connection = this.getConnection();) {
			String sql = "update employee_payroll set salary = ? where name= ? ;";
			PreparedStatement preparestatement = connection.prepareStatement(sql);
			preparestatement.setDouble(1, salary);
			preparestatement.setString(2, name);
			int update = preparestatement.executeUpdate();
			return update;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private void preparedStatementForEmployeeData() {
		try {
			Connection connection = this.getConnection();
			String sql = "Select * from employee_payroll WHERE name = ?";
			employeePayrollDataStatement = connection.prepareStatement(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<Employee_payroll_Data> getEmployeePayrollData(String name) {
		List<Employee_payroll_Data> employeePayrollList = null;
		if (this.employeePayrollDataStatement == null)
			this.preparedStatementForEmployeeData();
		try {
			employeePayrollDataStatement.setString(1, name);
			ResultSet resultSet = employeePayrollDataStatement.executeQuery();
			employeePayrollList = this.getEmployeePayrollData(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	private List<Employee_payroll_Data> getEmployeePayrollData(ResultSet resultSet) {
		List<Employee_payroll_Data> employeePayrollList = new ArrayList<>();
		try {
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				String name = resultSet.getString("name");
				double salary = resultSet.getDouble("salary");
				LocalDate startDate = resultSet.getDate("start").toLocalDate();
				employeePayrollList.add(new Employee_payroll_Data(id, name, salary, startDate));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	public List<Employee_payroll_Data> getEmployeeForDateRange(LocalDate startDate, LocalDate endDate) {
		String sql = String.format("SELECT * FROM employee_payroll WHERE START BETWEEN '%s' AND '%s';",
				Date.valueOf(startDate), Date.valueOf(endDate));
		return this.getEmployeePayrollDataUsingDB(sql);
	}

	private List<Employee_payroll_Data> getEmployeePayrollDataUsingDB(String sql) {
		ResultSet resultSet;
		List<Employee_payroll_Data> employeePayrollList = null;
		try (Connection connection = this.getConnection();) {
			PreparedStatement prepareStatement = connection.prepareStatement(sql);
			resultSet = prepareStatement.executeQuery(sql);
			employeePayrollList = this.getEmployeePayrollData(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	static Connection getConnection() throws SQLException {
		Connection connection;
		connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/payroll_service", "root",
				"Jashwini@2298");
		System.out.println("Connection successful: " + connection);
		return connection;
	}

	public Map<String, Double> get_AverageSalary_ByGender() {
		String sql = "SELECT gender,AVG(salary) as avg_salary FROM employee_payroll GROUP BY gender;";
		Map<String, Double> genderToAverageSalaryMap = new HashMap<>();
		try (Connection connection = this.getConnection();) {
			PreparedStatement prepareStatement = connection.prepareStatement(sql);
			ResultSet resultSet = prepareStatement.executeQuery(sql);
			while (resultSet.next()) {
				String gender = resultSet.getString("gender");
				double salary = resultSet.getDouble("avg_salary");
				genderToAverageSalaryMap.put(gender, salary);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return genderToAverageSalaryMap;
	}

	public Map<String, Double> get_SumOfSalary_ByGender() {
		String sql = "SELECT gender,SUM(salary) as sum_salary FROM employee_payroll GROUP BY gender;";
		Map<String, Double> genderToSumOfSalaryMap = new HashMap<>();
		try (Connection connection = this.getConnection();) {
			PreparedStatement prepareStatement = connection.prepareStatement(sql);
			ResultSet resultSet = prepareStatement.executeQuery(sql);
			while (resultSet.next()) {
				String gender = resultSet.getString("gender");
				double salary = resultSet.getDouble("sum_salary");
				genderToSumOfSalaryMap.put(gender, salary);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return genderToSumOfSalaryMap;
	}

	public Map<String, Double> get_Min_Salary_ByGender() {
		String sql = "SELECT gender,MIN(salary) as min_salary FROM employee_payroll GROUP BY gender;";
		Map<String, Double> genderToMinSalaryMap = new HashMap<>();
		try (Connection connection = this.getConnection();) {
			PreparedStatement prepareStatement = connection.prepareStatement(sql);
			ResultSet resultSet = prepareStatement.executeQuery(sql);
			while (resultSet.next()) {
				String gender = resultSet.getString("gender");
				double salary = resultSet.getDouble("min_salary");
				genderToMinSalaryMap.put(gender, salary);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return genderToMinSalaryMap;
	}
}
