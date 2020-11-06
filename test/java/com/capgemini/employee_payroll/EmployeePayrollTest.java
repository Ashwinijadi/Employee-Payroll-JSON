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
		Response response = RestAssured.get("/payroll_service");
		System.out.println("employee payroll entries in jsonserver:\n" + response.asString());
		Employee_payroll_Data[] arrayOfEmployees = new Gson().fromJson(response.asString(),
				Employee_payroll_Data[].class);
		return arrayOfEmployees;
	}

}
