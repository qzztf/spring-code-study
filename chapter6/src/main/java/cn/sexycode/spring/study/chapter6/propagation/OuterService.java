package cn.sexycode.spring.study.chapter6.propagation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

/**
 * @author qzz
 */
@Service
public class OuterService {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private InnerService innerService;
    @Transactional
    public void outer() {
        Test test = new Test();
        test.setName("out");
        entityManager.persist(test);
        innerService.save1();
    }
}
