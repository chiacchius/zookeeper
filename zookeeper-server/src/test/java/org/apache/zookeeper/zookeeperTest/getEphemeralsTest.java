package org.apache.zookeeper.zookeeperTest;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.test.ClientBase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.apache.zookeeper.ZooDefs.Ids;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Test the Zookeeper method "getEphemerals()"
 */
@RunWith(value = Parameterized.class)
public class getEphemeralsTest extends ClientBase {

    private boolean expectedResult;
    private static int persintent_cnt = 2;
    private int ephemeral_cnt;
    private String[] expected = null;
    private String path;
    private String BASE = "/test";
    private ZooKeeper zk;


    public getEphemeralsTest(Boolean expectedResult, String path, int ephemeral_cnt){

        this.expectedResult=expectedResult;
        this.path=path;
        this.ephemeral_cnt =ephemeral_cnt;

    }



    @Before
    public void setup() throws Exception {
        //super.setUp();
        System.out.println("starting setup");
        zk = createClient();

        //generate paths
        expected = new String[persintent_cnt * ephemeral_cnt];
        for (int p = 0; p < persintent_cnt; p++) {
            String base = BASE + p;
            zk.create(base, base.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            for (int e = 0; e < ephemeral_cnt; e++) {
                String ephem = base + "/ephem" + e;
                zk.create(ephem, ephem.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                expected[p * ephemeral_cnt + e] = ephem;
            }
        }
    }

    @After
    public void teardown() throws Exception {
        expected=null;
        //super.tearDown();
        zk.close();

    }

    @Parameterized.Parameters
    public static Collection<?> getParameters() {
        return Arrays.asList(new Object[][]{

                //expectedRes bool, string path, int ephemeral_cnt
                {true, "/test", 2}, //true also with all natural numbers
                {false, null, 2},
                {false, "invalidPath", 2},
                {true, "/test", 0}


        });
    }

    @Test
    public void getEphemerals() throws InterruptedException, KeeperException {


        System.out.println("starting test");
        boolean result = false;
        final String prefixPath = path + 0;
        List<String> actual = null;
        try {
            actual = zk.getEphemerals(prefixPath);
            if (actual.size()== ephemeral_cnt){
                result=true;
            }

        }catch (IllegalArgumentException e){ //invalid path
            System.out.println("######## INVALID PATH");
        }catch (Exception e){

        }


        Assert.assertEquals(result, expectedResult);
        if (!expectedResult){
            return;
        }
        for (int i = 0; i < ephemeral_cnt; i++) {
            String expectedPath = expected[i];
            Assert.assertTrue(actual.contains(expectedPath));
        }

    }

}
