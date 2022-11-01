package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeServiceImplTest {

    private String employeeUrl;
    private String employeeIdUrl;

    @Autowired
    private EmployeeService employeeService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        employeeUrl = "http://localhost:" + port + "/employee";
        employeeIdUrl = "http://localhost:" + port + "/employee/{id}";
    }

    @Test
    public void testCreateReadUpdate() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        // Create checks
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        assertNotNull(createdEmployee.getEmployeeId());
        assertEmployeeEquivalence(testEmployee, createdEmployee);


        // Read checks
        Employee readEmployee = restTemplate.getForEntity(employeeIdUrl, Employee.class, createdEmployee.getEmployeeId()).getBody();
        assertEquals(createdEmployee.getEmployeeId(), readEmployee.getEmployeeId());
        assertEmployeeEquivalence(createdEmployee, readEmployee);


        // Update checks
        readEmployee.setPosition("Development Manager");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Employee updatedEmployee =
                restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<>(readEmployee, headers),
                        Employee.class,
                        readEmployee.getEmployeeId()).getBody();

        assertEmployeeEquivalence(readEmployee, updatedEmployee);
    }

    @Test
    public void testNumberOfReports0Case() {
        Employee testEmployee1 = new Employee();
        Employee createdEmployee1 = restTemplate.postForEntity(employeeUrl, testEmployee1, Employee.class).getBody();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ReportingStructure reportingStructure =
                restTemplate.exchange(employeeIdUrl + "/numberOfReports",
                        HttpMethod.GET,
                        new HttpEntity<>(createdEmployee1, headers),
                        ReportingStructure.class,
                        createdEmployee1.getEmployeeId()).getBody();

        assertEquals(reportingStructure.getNumberOfReports(), 0);
    }

    @Test
    public void testNumberOfReports1Case() {
        Employee testEmployee1 = new Employee();
        Employee testEmployee2 = new Employee();

        Employee createdEmployee1 = restTemplate.postForEntity(employeeUrl, testEmployee1, Employee.class).getBody();

        testEmployee2.setDirectReports(new ArrayList<Employee>() {
            {
                add(createdEmployee1);
            }
        });
        Employee createdEmployee2 = restTemplate.postForEntity(employeeUrl, testEmployee2, Employee.class).getBody();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ReportingStructure reportingStructure =
                restTemplate.exchange(employeeIdUrl + "/numberOfReports",
                        HttpMethod.GET,
                        new HttpEntity<>(createdEmployee2, headers),
                        ReportingStructure.class,
                        createdEmployee2.getEmployeeId()).getBody();

        assertEquals(reportingStructure.getNumberOfReports(), 1);
    }

    @Test
    public void testNumberOfReportsLargeSimpleCase() {
        // Case of 5 is above 3 and 4
        //              3 is above 1 and 2
        // therefore 5 should have 4
        Employee testEmployee1 = new Employee();
        Employee testEmployee2 = new Employee();
        Employee testEmployee3 = new Employee();
        Employee testEmployee4 = new Employee();
        Employee testEmployee5 = new Employee();

        Employee createdEmployee1 = restTemplate.postForEntity(employeeUrl, testEmployee1, Employee.class).getBody();
        Employee createdEmployee2 = restTemplate.postForEntity(employeeUrl, testEmployee2, Employee.class).getBody();
        Employee createdEmployee4 = restTemplate.postForEntity(employeeUrl, testEmployee4, Employee.class).getBody();
        testEmployee3.setDirectReports(new ArrayList<Employee>() {
            {
                add(createdEmployee1);
                add(createdEmployee2);
            }
        });
        Employee createdEmployee3 = restTemplate.postForEntity(employeeUrl, testEmployee3, Employee.class).getBody();
        testEmployee5.setDirectReports(new ArrayList<Employee>() {
            {
                add(createdEmployee3);
                add(createdEmployee4);
            }
        });
        Employee createdEmployee5 = restTemplate.postForEntity(employeeUrl, testEmployee5, Employee.class).getBody();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ReportingStructure reportingStructure =
                restTemplate.exchange(employeeIdUrl + "/numberOfReports",
                        HttpMethod.GET,
                        new HttpEntity<>(createdEmployee5, headers),
                        ReportingStructure.class,
                        createdEmployee5.getEmployeeId()).getBody();

        assertEquals(reportingStructure.getNumberOfReports(), 4);
    }

    @Test
    public void testNumberOfReportsMultipleManagersCase() {
        // Case of 4 is above 2 and 3
        //              3 is above 1 and 2
        // therefore 4 should have 3 since 2 reports to 3 and 4 rather than being a separate report
        Employee testEmployee1 = new Employee();
        Employee testEmployee2 = new Employee();
        Employee testEmployee3 = new Employee();
        Employee testEmployee4 = new Employee();

        Employee createdEmployee1 = restTemplate.postForEntity(employeeUrl, testEmployee1, Employee.class).getBody();
        Employee createdEmployee2 = restTemplate.postForEntity(employeeUrl, testEmployee2, Employee.class).getBody();
        testEmployee3.setDirectReports(new ArrayList<Employee>() {
            {
                add(createdEmployee1);
                add(createdEmployee2);
            }
        });
        Employee createdEmployee3 = restTemplate.postForEntity(employeeUrl, testEmployee3, Employee.class).getBody();
        testEmployee4.setDirectReports(new ArrayList<Employee>() {
            {
                add(createdEmployee2);
                add(createdEmployee3);
            }
        });
        Employee createdEmployee4 = restTemplate.postForEntity(employeeUrl, testEmployee4, Employee.class).getBody();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ReportingStructure reportingStructure =
                restTemplate.exchange(employeeIdUrl + "/numberOfReports",
                        HttpMethod.GET,
                        new HttpEntity<>(createdEmployee4, headers),
                        ReportingStructure.class,
                        createdEmployee4.getEmployeeId()).getBody();

        assertEquals(reportingStructure.getNumberOfReports(), 3);
    }

    @Test
    public void testNumberOfReportsInternalLoopCase() {
        // Case of 1 under 2, 2 under 3, 3 under 4, and 3 under 1
        // Handles there being a loop within the reporting below the given employee, which I thought giving back the
        // result without loop recognition was ideal
        Employee testEmployee1 = new Employee();
        Employee testEmployee2 = new Employee();
        Employee testEmployee3 = new Employee();
        Employee testEmployee4 = new Employee();

        Employee createdEmployee1 = restTemplate.postForEntity(employeeUrl, testEmployee1, Employee.class).getBody();
        testEmployee2.setDirectReports(new ArrayList<Employee>() {
            {
                add(createdEmployee1);
            }
        });
        Employee createdEmployee2 = restTemplate.postForEntity(employeeUrl, testEmployee2, Employee.class).getBody();
        testEmployee3.setDirectReports(new ArrayList<Employee>() {
            {
                add(createdEmployee2);
            }
        });
        Employee createdEmployee3 = restTemplate.postForEntity(employeeUrl, testEmployee3, Employee.class).getBody();
        testEmployee4.setDirectReports(new ArrayList<Employee>() {
            {
                add(createdEmployee3);
            }
        });
        Employee createdEmployee4 = restTemplate.postForEntity(employeeUrl, testEmployee4, Employee.class).getBody();

        createdEmployee1.setDirectReports(new ArrayList<Employee>() {
            {
                add(createdEmployee3);
            }
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

       restTemplate.exchange(employeeIdUrl,
                HttpMethod.PUT,
                new HttpEntity<>(createdEmployee1, headers),
                Employee.class,
                createdEmployee1.getEmployeeId()).getBody();

        ReportingStructure reportingStructure =
                restTemplate.exchange(employeeIdUrl + "/numberOfReports",
                        HttpMethod.GET,
                        new HttpEntity<>(createdEmployee4, headers),
                        ReportingStructure.class,
                        createdEmployee4.getEmployeeId()).getBody();

        assertEquals(reportingStructure.getNumberOfReports(), 3);
    }


    private static void assertEmployeeEquivalence(Employee expected, Employee actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
    }
}
