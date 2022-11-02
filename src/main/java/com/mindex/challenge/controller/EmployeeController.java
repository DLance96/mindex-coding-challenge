package com.mindex.challenge.controller;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class EmployeeController {
    private static final Logger LOG = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/employee")
    public Employee create(@RequestBody Employee employee) {
        LOG.debug("Received employee create request for [{}]", employee);

        return employeeService.create(employee);
    }

    @GetMapping("/employee/{id}")
    public Employee read(@PathVariable String id) {
        LOG.debug("Received employee read request for id [{}]", id);

        return employeeService.read(id);
    }

    @PutMapping("/employee/{id}")
    public Employee update(@PathVariable String id, @RequestBody Employee employee) {
        LOG.debug("Received employee update request for id [{}] and employee [{}]", id, employee);

        employee.setEmployeeId(id);
        return employeeService.update(employee);
    }

    @GetMapping("/employee/{id}/numberOfReports")
    public ResponseEntity numberOfReports(@PathVariable String id) {
        LOG.debug("Received employee numberOfReports request for id [{}]", id);

        ReportingStructure reportingStructure = employeeService.computeNumberOfReports(id);

        // Returns a 500 error in the case that a loop appears in the reporting, not necessary depending how
        // data is sanitized on consumption, ideally would be listed in API documentation for user's understanding as
        // possible error
        if(reportingStructure.getNumberOfReports() == -1) {
            return ResponseEntity.status(500).body(String.format(
                    "ERROR: Loop of reporting employees including Employee - %s was found", id));
        }
        else {
            return ResponseEntity.status(200).body(reportingStructure);
        }
    }
}
