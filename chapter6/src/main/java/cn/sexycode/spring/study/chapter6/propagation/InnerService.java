package cn.sexycode.spring.study.chapter6.propagation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@Service
public class InnerService {
    @Autowired
    private EntityManager entityManager;
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save1(){
        Test test = new Test();
        test.setName("1");
        entityManager.persist(test);
        throw new IllegalArgumentException();
    }
}
