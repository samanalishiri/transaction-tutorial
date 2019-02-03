package com.saman.tutorial.transaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.logging.Logger;

@RunWith(Arquillian.class)
public class RepositoryTest {

    private final Logger logger = Logger.getLogger("RepositoryTest");

    @Inject
    private Repository userRepository;

    @Deployment
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackages(true, "com.saman.tutorial.transaction")
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    public void init() {
        userRepository.save(DataEntity.create(1, "code_1", "name_1"));
    }

    @Before
    public void truncate() {
        userRepository.delete(1);
    }

    @Test
    public void findByUserIdTest() {
        init();

        DataEntity user = userRepository.findById(1);
        Assert.assertNotNull(user);
        Assert.assertEquals(new Integer(1), user.getId());
    }

}
