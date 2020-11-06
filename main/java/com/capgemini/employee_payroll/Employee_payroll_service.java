package com.capgemini.employee_payroll;

import java.util.logging.Logger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;
import com.capgemini.employee_payroll.EmployeePayrollException.Exception;

public class Employee_payroll_service {

	public enum IOService {
		CONSOLE_IO, FILE_IO, DB_IO, REST_IO
	}

	static Logger log = Logger.getLogger(Employee_payroll_service.class.getName());
	public List<Employee_payroll_Data> employeePayrollList;
	private Employee_payroll_DBService employeePayrollDBService;
	private EmployeePayrollNewDBService employeePayrollNewDBService;
	private Map<String, Double> employeePayrollMap;

	public Employee_payroll_service() {
		employeePayrollDBService = Employee_payroll_DBService.getInstance();
		employeePayrollNewDBService = EmployeePayrollNewDBService.getInstance();
	}

	public Employee_payroll_service(List<Employee_payroll_Data> employeePayrollList) {
		this();
		this.employeePayrollList = new ArrayList<>(employeePayrollList);
	}

	public Employee_payroll_service(Map<String, Double> employeePayrollMap) {
		this();
		this.employeePayrollMap = employeePayrollMap;
	}

	public static void main(String[] args) {
		List<Employee_payroll_Data> employeePayrollList = new ArrayList<Employee_payroll_Data>();
		Employee_payroll_service employeePayrollService = new Employee_payroll_service(employeePayrollList);
		Scanner consoleInputReader = new Scanner(System.in);
		employeePayrollService.readEmployeeData(consoleInputReader);
		employeePayrollService.writeEmployeeData(IOService.CONSOLE_IO);
	}

	public void readEmployeeData(Scanner consoleInputReader) {
		log.info("Enter employee ID : ");
		int id = Integer.parseInt(consoleInputReader.nextLine());
		log.info("Enter employee name : ");
		String name = consoleInputReader.nextLine();
		log.info("Enter employee salary : ");
		double salary = Double.parseDouble(consoleInputReader.nextLine());
		employeePayrollList.add(new Employee_payroll_Data(id, name, salary));
	}

	public List<Employee_payroll_Data> readEmployeepayrollData(IOService ioService) {
		if (ioService.equals(IOService.FILE_IO))
			return new Employee_payroll_FileIOService().readData();
		if (ioService.equals(IOService.DB_IO))
			return new EmployeePayrollNewDBService().readData();
		else
			return null;
	}

	public List<Employee_payroll_Data> readPayrollDataForRange(IOService ioService, LocalDate startDate,
			LocalDate endDate) {
		if (ioService.equals(IOService.DB_IO))
			this.employeePayrollList = employeePayrollNewDBService.readData();
		return employeePayrollList;
	}

	public void writeEmployeeData(IOService ioService) {
		if (ioService.equals(IOService.CONSOLE_IO))
			log.info("Employee Payroll Data to Console\n" + employeePayrollList);
		else if (ioService.equals(IOService.FILE_IO))
			new Employee_payroll_FileIOService().writeData(employeePayrollList);
	}

	public void printData(IOService ioService) {
		new Employee_payroll_FileIOService().printData();
	}

	public long countEntries(IOService ioService) {
		if (ioService.equals(IOService.FILE_IO))
			return new Employee_payroll_FileIOService().countEntries();
		return employeePayrollList.size();
	}

	public void updateEmployeeSalary(String name, double salary) throws EmployeePayrollException {
		int result = employeePayrollNewDBService.updateEmployeeData(name, salary);
		if (result == 0) {
			throw new EmployeePayrollException(Exception.DATA_NULL, "No Data to update ");
		}
		Employee_payroll_Data employeePayrollData = this.getEmployee_payroll_Data(name);
		if (employeePayrollData != null)
			employeePayrollData.salary = salary;
	}

	public void updateEmployeeSalary(String name, double salary, IOService ioService) {
		if (ioService.equals(IOService.REST_IO)) {
			int result = employeePayrollDBService.updateEmployeeData(name, salary);
			if (result == 0)
				return;
		}
		Employee_payroll_Data employeePayrollData = this.getEmployee_payroll_Data(name);
		if (employeePayrollData != null)
			employeePayrollData.salary = salary;
	}

	public void addEmployeeToPayRoll(String name, double salary, LocalDate start, String gender)
			throws EmployeePayrollException {
		employeePayrollList.add(employeePayrollNewDBService.addEmployeeToPayrollUC(name, salary, start, gender));
	}

	public void addEmployeeToPayrollIn(List<Employee_payroll_Data> employeePayrollDataList) {
		employeePayrollDataList.forEach(employeePayrollData -> {
			log.info("Employee being added : " + employeePayrollData.name);
			try {
				this.addEmployeeToPayRoll(employeePayrollData.name, employeePayrollData.salary,
						employeePayrollData.start, employeePayrollData.gender);
			} catch (EmployeePayrollException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info("Employee added : " + employeePayrollData.name);
		});
		log.info(" " + this.employeePayrollList);
	}

	public void addEmployeeToPayrollWithThreads(List<Employee_payroll_Data> employeePayrollDataList) {
		Map<Integer, Boolean> employeeStatus = new HashMap<>();
		employeePayrollDataList.forEach(employeePayrollData -> {
			Runnable task = () -> {
				employeeStatus.put(employeePayrollData.hashCode(), false);
				log.info(" being added : " + Thread.currentThread().getName());
				try {
					this.addEmployeeToPayRoll(employeePayrollData.name, employeePayrollData.salary,
							employeePayrollData.start, employeePayrollData.gender);
				} catch (EmployeePayrollException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				employeeStatus.put(employeePayrollData.hashCode(), true);
				log.info("Employee added : " + Thread.currentThread().getName());
			};
			Thread thread = new Thread(task, employeePayrollData.name);
			thread.start();
		});
		while (employeeStatus.containsValue(false)) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}
		log.info("" + this.employeePayrollList);
	}

	private Employee_payroll_Data getEmployee_payroll_Data(String name) {
		return this.employeePayrollList.stream().filter(emp_Data -> emp_Data.name.equals(name)).findFirst()
				.orElse(null);
	}

	public boolean checkEmployeePayrollInSyncWithDB(String name) {
		List<Employee_payroll_Data> employeePayrollList = employeePayrollDBService.getEmployeePayrollData(name);
		return employeePayrollList.get(0).equals(getEmployee_payroll_Data(name));
	}

	public Map<String, Double> readPayrollDataForAvgSalary(IOService ioService) {
		if (ioService.equals(IOService.DB_IO))
			this.employeePayrollMap = employeePayrollDBService.get_AverageSalary_ByGender();
		return employeePayrollMap;
	}

	public Map<String, Double> readPayrollDataForSumSalary(IOService ioService) {
		if (ioService.equals(IOService.DB_IO))
			this.employeePayrollMap = employeePayrollDBService.get_SumOfSalary_ByGender();
		return employeePayrollMap;
	}

	public Map<String, Double> readPayrollDataForMaxSalary(IOService ioService) {
		if (ioService.equals(IOService.DB_IO))
			this.employeePayrollMap = employeePayrollNewDBService.get_Max_Salary_ByGender();
		return employeePayrollMap;
	}

	public Map<String, Double> readPayrollDataForMinSalary(IOService ioService) {
		if (ioService.equals(IOService.DB_IO))
			this.employeePayrollMap = employeePayrollDBService.get_Min_Salary_ByGender();
		return employeePayrollMap;
	}

	public Map<String, Double> readPayrollDataFor_CountOfEmployee_ByGender(IOService ioService) {
		if (ioService.equals(IOService.DB_IO))
			this.employeePayrollMap = employeePayrollNewDBService.get_CountOfEmployee_ByGender();
		return employeePayrollMap;
	}

	public List<Employee_payroll_Data> readPayrollDataForActiveEmployees(IOService ioService) {
		if (ioService.equals(IOService.DB_IO))
			this.employeePayrollList = employeePayrollNewDBService.getActiveEmployees();
		return employeePayrollList;
	}

	public void addEmployeeToPayroll(Employee_payroll_Data employeePayrollData, IOService ioService)
			throws EmployeePayrollException {
		if (ioService.equals(IOService.DB_IO))
			this.addEmployeeToPayRoll(employeePayrollData.name, employeePayrollData.salary, employeePayrollData.start,
					employeePayrollData.gender);
		else
			employeePayrollList.add(employeePayrollData);

	}
}
