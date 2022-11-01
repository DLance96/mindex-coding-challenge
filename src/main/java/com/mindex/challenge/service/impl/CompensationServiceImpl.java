package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.service.CompensationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;

@Service
public class CompensationServiceImpl implements CompensationService {

    private static final Logger LOG = LoggerFactory.getLogger(CompensationServiceImpl.class);

    @Autowired
    private CompensationRepository compensationRepository;

    @Override
    public Compensation create(Compensation compensation) {
        LOG.debug("Creating compensation [{}]", compensation);

        compensationRepository.insert(compensation);

        return compensation;
    }

    @Override
    public Compensation[] read(String id) {
        LOG.debug("Creating compensation with employee id [{}]", id);

        Compensation[] compensations = compensationRepository.findByEmployeeId(id);

        if (compensations == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        Arrays.sort(compensations, new CompensationComparator());
        return compensations;
    }

    // Created in order to have all the compensations return in a most recent date order
    private class CompensationComparator implements Comparator<Compensation> {
        public int compare(Compensation a, Compensation b) {
            if( a.getEffectiveDate().after(b.getEffectiveDate()) ) {
                return -1;
            }
            else if (a.getEffectiveDate().before(b.getEffectiveDate())) {
                return 1;
            }
            else {
                return 0;
            }
        }
    }
}
