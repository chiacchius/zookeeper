package org.apache.zookeeper.zookeeperTest;


import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.cli.CliException;
import org.apache.zookeeper.test.ClientBase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Test the Zookeeper method "getChildren()"
 */
@RunWith(value = Parameterized.class)
public class ZooKeeperGetChildrenTest extends ClientBase{

    private boolean expectedResult;

    private ZooKeeper zk;
    private String path;
    private boolean watch;

    public ZooKeeperGetChildrenTest(boolean expectedResult, String path, boolean watch){

        this.expectedResult=expectedResult;
        this.path=path;
        this.watch=watch;

    }

    @Before
    public void setup() throws Exception {

        zk = createClient();
        System.out.println("starting setUp");

        //znode creation

        zk.setData("/", "myTest".getBytes(), -1);
        zk.create("/path1", "myTest".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);
        zk.create("/path1/path2", "myTest".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);
        zk.create("/path1/path2/path3", "myTest".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);
        zk.create("/path1/path4", "myTest".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);
        zk.create("/path1/path5", "myTest".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);
        zk.create("/secondPath1", "myTest".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);
        zk.create("/secondPath1/secondPath2", "myTest".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);
        zk.create("/secondPath1/secondPath3", "myTest".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);

    }

    @After
    public void teardown() {

        System.out.println("starting tearDown");

        if (zk!=null){
            try {
                zk.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("tearDown finished");

    }

    @Parameterized.Parameters
    public static Collection<?> getParameters(){


        return Arrays.asList(new Object[][] {

                //boolean expectedResult, String path, boolean watch
                {true, "/secondPath1", false},
                {false, "/secondPath_1", false},
                {true, "/path1", true},
                {false, null, true},

        });


    }

    @Test
    public void getChildren(){

        System.out.println(" starting test ");

        boolean result;
        List<String> children = null;
        System.out.println(" ############ path: "+path);

        try {

            children = zk.getChildren(path, watch);

        } catch (KeeperException e) {       //wrong path
            System.out.println(" wrong path");
            e.printStackTrace();
            result=false;
            Assert.assertEquals(expectedResult ,result);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (IllegalArgumentException e){
            e.printStackTrace();
        }



        if (path !=null && path.equals("/path1")) {

            if ( (children.contains("path2"))  && (children.contains("path4")) && (children.contains("path5")) ){

                System.out.println(" \n--------path: "+path+"     children: "+children);
                if (children.size()==3){
                    result=true;
                    Assert.assertEquals(expectedResult ,result);

                }
                else {
                    result=false;
                    Assert.assertEquals(expectedResult ,result);

                }
            }

        }
        else if (path!=null && path.equals("/secondPath1")) {

            if ( (children.contains("secondPath2")) && (children.contains("secondPath3")) ){

                System.out.println(" \n--------path: "+path+"     children: "+children);
                if (children.size()==2){
                    result=true;
                    Assert.assertEquals(expectedResult ,result);

                }
                else {
                    result=false;
                    Assert.assertEquals(expectedResult ,result);

                }
            }

        }





    }


}
