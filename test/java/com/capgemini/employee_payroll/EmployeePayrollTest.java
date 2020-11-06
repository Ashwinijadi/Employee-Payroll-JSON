package com.capgemini.employee_payroll;

import java.util.Arrays;
import java.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.google.gson.Gson;
import io.restassured.*;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import com.capgemini.employee_payroll.Employee_payroll_service.IOService;

public class EmployeePayrollTest {

	@Before
	public void sertup() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 3000;
	}

	@Test
	public void givenEmployeesInJsonServerWhenRetrievedShouldMatchEmpCount() {
		Employee_payroll_Data[] arrayOfEmps = getEmployeeList();
		Employee_payroll_service employeePayrollSevice;
		employeePayrollSevice = new Employee_payroll_service(Arrays.asList(arrayOfEmps));
		long entries = employeePayrollSevice.countEntries(IOService.REST_IO);
		Assert.assertEquals(10, entries);
	}

	public Employee_payroll_Data[] getEmployeeList() {
		Response response = RestAssured.get("/payroll");
		System.out.println("employee payroll entries in jsonserver:\n" + response.asString());
		Employee_payroll_Data[] arrayOfEmployees = new Gson().fromJson(response.asString(),
				Employee_payroll_Data[].class);
		return arrayOfEmployees;
	}

	@Test
	public void givenNewEmpolyee_WhenAdded_ShouldMatch201ResponseAndCount() throws EmployeePayrollException {
		Response response = RestAssured.get("/payroll");
		System.out.println("Employee Payroll entries in JSONserver" + response.asString());
		Employee_payroll_Data[] arrayOfEmps = new Gson().fromJson(response.asString(), Employee_payroll_Data[].class);
		Employee_payroll_Data employeePayrollData = new Employee_payroll_Data(0, "Anila", 500000.00, LocalDate.now(),
				"F");
		Response response1 = addEmployeeToJsonServer(employeePayrollData);
		int statusCode = response1.getStatusCode();
		Assert.assertEquals(201, statusCode);
		Employee_payroll_service employeePayrollSevice;
		employeePayrollSevice = new Employee_payroll_service(Arrays.asList(arrayOfEmps));
		employeePayrollSevice.addEmployeeToPayroll(employeePayrollData, IOService.REST_IO);
		long entries = employeePayrollSevice.countEntries(IOService.REST_IO);
		Assert.assertEquals(8, entries);
	}

	private Response addEmployeeToJsonServer(Employee_payroll_Data empPayrollData) {
		String empJson = new Gson().toJson(empPayrollData);
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		request.body(empJson);
		return request.post("payroll");
	}
}
