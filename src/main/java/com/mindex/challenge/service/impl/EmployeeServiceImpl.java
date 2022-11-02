package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);

        employee.setEmployeeId(UUID.randomUUID().toString());
        employeeRepository.insert(employee);

        return employee;
    }

    @Override
    public Employee read(String id) {
        LOG.debug("Creating employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);

        return employeeRepository.save(employee);
    }

    @Override
    public ReportingStructure computeNumberOfReports(String id) {
        LOG.debug("Computing number of reports for employee with id [{}]", id);

        ReportingStructure returnReport = new ReportingStructure();
        returnReport.setEmployeeId(id);

        Employee employee = employeeRepository.findByEmployeeId(id);
        List<Employee> directReports = employee.getDirectReports();
        List<String> directReportIds = new ArrayList<>();

        if (directReports == null) {
            returnReport.setNumberOfReports(0);
            return returnReport;
        }

        for (Employee directReport : directReports) {
            directReportIds.add(directReport.getEmployeeId());
        }

        Set<String> totalReportIds = new HashSet<>(directReportIds);

        for (int i = 0; i < directReportIds.size(); i++) {
            Employee tempEmployee = employeeRepository.findByEmployeeId(directReportIds.get(i));

            if(tempEmployee.getEmployeeId().equals(id)) {
                returnReport.setNumberOfReports(-1);
                return returnReport;
            }

            List<Employee> tempDirectReports = tempEmployee.getDirectReports();
            int currentCount = totalReportIds.size();

            for (int j = 0; tempDirectReports != null && j < tempDirectReports.size(); j++) {
                totalReportIds.add(tempDirectReports.get(j).getEmployeeId());
                // Handling case of an Employee reporting to multiple people
                if (totalReportIds.size() > currentCount) {
                    directReportIds.add(tempDirectReports.get(j).getEmployeeId());
                }
            }
        }

        returnReport.setNumberOfReports(totalReportIds.size());

        return returnReport;
    }
}
