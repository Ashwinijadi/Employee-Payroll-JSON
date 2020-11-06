package com.capgemini.employee_payroll;

import java.sql.Connection;
import java.sql.Date;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;
import com.capgemini.employee_payroll.EmployeePayrollException.Exception;

public class EmployeePayrollNewDBService {
	Logger log = Logger.getLogger(EmployeePayrollNewDBService.class.getName());
	private static EmployeePayrollNewDBService employeePayrollNewDBService;
	private PreparedStatement employeePayrollNewDataStatement;
	private int connectionCounter = 0;

	public EmployeePayrollNewDBService() {
	}

	public static EmployeePayrollNewDBService getInstance() {
		if (employeePayrollNewDBService == null)
			employeePayrollNewDBService = new EmployeePayrollNewDBService();
		return employeePayrollNewDBService;
	}

	public List<Employee_payroll_Data> readData() {
		String sql = "SELECT e.id,e.name,e.gender,e.start,d.dept_name,p.basic_pay"
				+ " FROM employee_payroll e JOIN employee_department ed "
				+ "ON e.id=ed.id JOIN department d ON d.dept_id=ed.dept_id JOIN payroll_details p ON e.id=p.id ";
		return this.getEmployeePayrollDataUsingSQLQuery(sql);
	}

	private List<Employee_payroll_Data> getEmployeePayrollDataUsingSQLQuery(String sql) {
		List<Employee_payroll_Data> employeePayrollList = null;
		try (Connection connection = this.getConnection();) {
			PreparedStatement prepareStatement = connection.prepareStatement(sql);
			ResultSet resultSet = prepareStatement.executeQuery(sql);
			employeePayrollList = this.getEmployeePayrollData(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	private List<Employee_payroll_Data> getEmployeePayrollData(ResultSet resultSet) {
		List<Employee_payroll_Data> employeePayrollList = new ArrayList<>();
		List<String> department = new ArrayList<String>();
		try {
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				String name = resultSet.getString("name");
				String gender = resultSet.getString("gender");
				LocalDate startDate = resultSet.getDate("start").toLocalDate();
				String dept = resultSet.getString("dept_name");
				double salary = resultSet.getDouble("basic_pay");
				department.add(dept);
				String[] departmentArray = new String[department.size()];
				employeePayrollList
						.add(new Employee_payroll_Data(id, name, salary, startDate, gender, departmentArray));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	public List<Employee_payroll_Data> getEmployeePayrollData(String name) {
		List<Employee_payroll_Data> employeePayrollList = null;
		if (this.employeePayrollNewDataStatement == null)
			this.preparedStatementForEmployeeData();
		try {
			employeePayrollNewDataStatement.setString(1, name);
			ResultSet resultSet = employeePayrollNewDataStatement.executeQuery();
			employeePayrollList = this.getEmployeePayrollData(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	private void preparedStatementForEmployeeData() {
		try {
			Connection connection = this.getConnection();
			String sql = "  SELECT e.id,e.name,e.gender,e.start,d.dept_name,p.basic_pay"
					+ "    FROM employee_payroll e JOIN employee_department ed ON e.id=ed.id"
					+ "    JOIN department d ON d.dept_id=ed.dept_id"
					+ "    JOIN payroll_details p ON e.id=p.id where e.name= ? ;";
			employeePayrollNewDataStatement = connection.prepareStatement(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
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

	public int updateEmployeeData(String name, Double salary) {
		return this.updateEmployeeDataUsingPreparedStatement(name, salary);
	}

	private int updateEmployeeDataUsingPreparedStatement(String name, double salary) {
		try (Connection connection = this.getConnection();) {
			String sql = "UPDATE payroll_details SET basic_pay = ? WHERE id = "
					+ "(SELECT id from employee_payroll WHERE name = ? );";
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

	public Map<String, Double> get_Max_Salary_ByGender() {
		String sql = "SELECT gender,MAX(salary) as max_salary FROM employee_payroll GROUP BY gender;";
		Map<String, Double> genderToMaxSalaryMap = new HashMap<>();
		try (Connection connection = this.getConnection();) {
			PreparedStatement prepareStatement = connection.prepareStatement(sql);
			ResultSet resultSet = prepareStatement.executeQuery(sql);
			while (resultSet.next()) {
				String gender = resultSet.getString("gender");
				double salary = resultSet.getDouble("max_salary");
				genderToMaxSalaryMap.put(gender, salary);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return genderToMaxSalaryMap;
	}

	public Map<String, Double> get_CountOfEmployee_ByGender() {
		String sql = "SELECT gender,COUNT(salary) as emp_count FROM employee_payroll GROUP BY gender;";
		Map<String, Double> genderToCountMap = new HashMap<>();
		try (Connection connection = this.getConnection();) {
			PreparedStatement prepareStatement = connection.prepareStatement(sql);
			ResultSet resultSet = prepareStatement.executeQuery(sql);
			while (resultSet.next()) {
				String gender = resultSet.getString("gender");
				double salary = resultSet.getDouble("emp_count");
				genderToCountMap.put(gender, salary);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return genderToCountMap;
	}

	public Employee_payroll_Data addEmployeeToPayrollUC(String name, double salary, LocalDate start, String gender)
			throws EmployeePayrollException {
		int employeeId = -1;
		Connection connection = null;
		Employee_payroll_Data employeePayrollData = null;
		try {
			connection = Employee_payroll_DBService.getConnection();
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try (Statement statement = connection.createStatement()) {
			String sql = String.format(
					"INSERT INTO employee_payroll (name,gender,salary,start) VALUES('%s','%s','%s','%s')", name, gender,
					salary, Date.valueOf(start));
			int rowAffected = statement.executeUpdate(sql, statement.RETURN_GENERATED_KEYS);
			if (rowAffected == 1) {
				ResultSet result = statement.getGeneratedKeys();
				if (result.next())
					employeeId = result.getInt(1);
			}
			if (rowAffected == 0)
				throw new EmployeePayrollException(Exception.INSERTION_FAILED, "insertion failed");
		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw new EmployeePayrollException(Exception.INSERTION_FAILED, "insertion failed");
		} catch (EmployeePayrollException e) {
			System.out.println(e);
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		try (Statement statement = connection.createStatement()) {
			double deductions = salary * 0.2;
			double taxable_pay = salary - deductions;
			double tax = taxable_pay * 0.1;
			double net_pay = salary - tax;
			String sql = String.format(
					"INSERT INTO payroll_details (id,basic_pay,deductions,taxable_pay,tax,net_pay)" + ""
							+ "VALUES('%s','%s','%s','%s','%s','%s')",
					employeeId, salary, deductions, taxable_pay, tax, net_pay);
			int rowAffected = statement.executeUpdate(sql);
			if (rowAffected == 0)
				throw new EmployeePayrollException(Exception.INSERTION_FAILED, "insertion into table failed");
		} catch (EmployeePayrollException e1) {
			System.out.println(e1);
			try {
				connection.rollback();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw new EmployeePayrollException(Exception.INSERTION_FAILED, "insertion into table failed");
		}
		try (Statement statement = connection.createStatement()) {
			int dept_id = 101;
			String sql = String.format("INSERT INTO employee_department(id,dept_id) VALUES('%s','%s')", employeeId,
					dept_id);
			int rowAffected1 = statement.executeUpdate(sql);
			if (rowAffected1 == 1) {
				employeePayrollData = new Employee_payroll_Data(employeeId, name, salary, start);
			}
			if (rowAffected1 == 0)
				throw new EmployeePayrollException(Exception.INSERTION_FAILED, "insert into table failed");

		} catch (EmployeePayrollException e1) {
			System.out.println(e1);
			try {
				connection.rollback();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw new EmployeePayrollException(Exception.INSERTION_FAILED, "insert into table failed");
		}
		try {
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		return employeePayrollData;
	}

	public Employee_payroll_Data addEmployeeToPayroll(String name, double salary, LocalDate start, String gender) {
		int employeeId = -1;
		Employee_payroll_Data employee_payroll_Data = null;
		String sql = String.format("INSERT INTO employee_payroll(name,gender,salary,start) values('%s','%s','%s','%s')",
				name, gender, salary, Date.valueOf(start));
		try (Connection connection = this.getConnection()) {
			PreparedStatement preparedstatement = connection.prepareStatement(sql);
			int rowAffected = preparedstatement.executeUpdate(sql, PreparedStatement.RETURN_GENERATED_KEYS);
			if (rowAffected == 1) {
				ResultSet resultSet = preparedstatement.getGeneratedKeys();
				if (resultSet.next())
					employeeId = resultSet.getInt(1);
			}
			employee_payroll_Data = new Employee_payroll_Data(employeeId, name, salary, start);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employee_payroll_Data;
	}

	public List<Employee_payroll_Data> getActiveEmployees() {
		String sql = "select * from employee_payroll where active=1;";
		return this.getEmployeePayrollDataUsingDBActive(sql);
	}

	private List<Employee_payroll_Data> getEmployeePayrollDataNormalisedActive(ResultSet resultSet) {
		List<Employee_payroll_Data> employeePayrollList = new ArrayList<>();
		try {
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				String name = resultSet.getString("name");
				String gender = resultSet.getString("gender");
				LocalDate start = resultSet.getDate("start").toLocalDate();
				double salary = resultSet.getDouble("salary");
				boolean active = resultSet.getBoolean("active");
				employeePayrollList.add(new Employee_payroll_Data(id, name, salary, start, gender, active));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	private List<Employee_payroll_Data> getEmployeePayrollDataUsingDBActive(String sql) {
		ResultSet resultSet;
		List<Employee_payroll_Data> employeePayrollList = null;
		try (Connection connection = this.getConnection();) {
			PreparedStatement prepareStatement = connection.prepareStatement(sql);
			resultSet = prepareStatement.executeQuery(sql);
			employeePayrollList = this.getEmployeePayrollDataNormalisedActive(resultSet);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return employeePayrollList;
	}

	private Connection getConnection() throws SQLException {
		connectionCounter++;
		Connection connection;
		System.out.println("Connecting to database: ");
		connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/payroll_service", "root",
				"Jashwini@2298");
		log.info("Processing Thread:" + Thread.currentThread().getName() + "Connecting to database with Id:"
				+ connectionCounter + "connection is successfu!!!l" + connection);
		log.info("Connection successful: " + connection);
		return connection;
	}
}
