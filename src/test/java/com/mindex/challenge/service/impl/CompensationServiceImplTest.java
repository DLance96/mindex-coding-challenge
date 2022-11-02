package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.service.CompensationService;
import com.mindex.challenge.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CompensationServiceImplTest {

    private String compensationUrl;
    private String compensationIdUrl;
    private String employeeUrl;

    @Autowired
    private CompensationService compensationService;
    private EmployeeService employeeService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        compensationUrl = "http://localhost:" + port + "/compensation";
        compensationIdUrl = "http://localhost:" + port + "/compensation/{id}";
        employeeUrl = "http://localhost:" + port + "/employee";
    }

    @Test
    public void testCreateReadUpdate() {
        Employee testEmployee = new Employee();
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        Compensation compensation = new Compensation();
        compensation.setEmployeeId(createdEmployee.getEmployeeId());
        compensation.setEffectiveDate(new Date());
        compensation.setSalary(100000);

        Compensation createdCompensation = restTemplate.postForEntity(compensationUrl, compensation, Compensation.class)
                .getBody();

        assertCompensationEquivalence(createdCompensation, compensation);

        Compensation[] responseComps = restTemplate.getForEntity(compensationIdUrl,
                Compensation[].class, createdEmployee.getEmployeeId()).getBody();

        assertEquals(responseComps.length, 1);
        assertCompensationEquivalence(responseComps[0], compensation);
    }

    @Test
    public void testCompensationAddMultiple() {
        Employee testEmployee = new Employee();
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        Date date = new Date();

        Compensation compensation1 = new Compensation();
        compensation1.setEmployeeId(createdEmployee.getEmployeeId());
        compensation1.setEffectiveDate(date);
        compensation1.setSalary(100000);

        Compensation compensation2 = new Compensation();
        compensation2.setEmployeeId(createdEmployee.getEmployeeId());
        compensation2.setEffectiveDate(new Date(date.getTime() + 1000));
        compensation2.setSalary(200000);

        Compensation compensation3 = new Compensation();
        compensation3.setEmployeeId(createdEmployee.getEmployeeId());
        compensation3.setEffectiveDate(new Date(date.getTime() - 1000));
        compensation3.setSalary(300000);

        Compensation compensation4 = new Compensation();
        compensation4.setEmployeeId(createdEmployee.getEmployeeId());
        compensation4.setEffectiveDate(new Date(date.getTime() - 500));
        compensation4.setSalary(900000);

        restTemplate.postForEntity(compensationUrl, compensation1, Compensation.class);
        restTemplate.postForEntity(compensationUrl, compensation2, Compensation.class);

        Compensation[] responseComps = restTemplate.getForEntity(compensationIdUrl,
                Compensation[].class, createdEmployee.getEmployeeId()).getBody();

        assertEquals(responseComps.length, 2);
        assertCompensationEquivalence(responseComps[0], compensation2);
        assertCompensationEquivalence(responseComps[1], compensation1);

        restTemplate.postForEntity(compensationUrl, compensation3, Compensation.class);

        responseComps = restTemplate.getForEntity(compensationIdUrl,
                Compensation[].class, createdEmployee.getEmployeeId()).getBody();

        assertEquals(responseComps.length, 3);
        assertCompensationEquivalence(responseComps[0], compensation2);
        assertCompensationEquivalence(responseComps[1], compensation1);
        assertCompensationEquivalence(responseComps[2], compensation3);

        restTemplate.postForEntity(compensationUrl, compensation4, Compensation.class);

        responseComps = restTemplate.getForEntity(compensationIdUrl,
                Compensation[].class, createdEmployee.getEmployeeId()).getBody();

        assertEquals(responseComps.length, 4);
        assertCompensationEquivalence(responseComps[0], compensation2);
        assertCompensationEquivalence(responseComps[1], compensation1);
        assertCompensationEquivalence(responseComps[2], compensation4);
        assertCompensationEquivalence(responseComps[3], compensation3);
    }


    private static void assertCompensationEquivalence(Compensation expected, Compensation actual) {
        assertEquals(expected.getEmployeeId(), actual.getEmployeeId());
        assertEquals(expected.getEffectiveDate(), actual.getEffectiveDate());
        assertEquals(expected.getSalary(), actual.getSalary());
    }
}