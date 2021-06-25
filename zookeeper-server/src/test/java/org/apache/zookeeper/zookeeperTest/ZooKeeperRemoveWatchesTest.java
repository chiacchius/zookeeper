package org.apache.zookeeper.zookeeperTest;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.test.ClientBase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeoutException;



/**
 * Test the Zookeeper method "removeWatches()"
 */
@RunWith(value = Parameterized.class)
public class ZooKeeperRemoveWatchesTest extends ClientBase {

    private final boolean expectedResult;
    private final boolean validWatcher;

    private ZooKeeper zk = null;
    private final boolean local;
    private ClientBase.CountdownWatcher watcher = new ClientBase.CountdownWatcher();

    private final String path;
    private Watcher.WatcherType watcherType;



    @Parameterized.Parameters
    public static Collection<?> getParameters(){
        return Arrays.asList(new Object[][] {

                //expectedResult, path, validWatcher, watcherType, local bool

                {true, "/noWatchPath", true, Watcher.WatcherType.Data, false },
                {true, null, true, Watcher.WatcherType.Data, false },
                {true, "/noWatchPath", false, Watcher.WatcherType.Data, false },
                {false, "/noWatchPath",true, null, false },
                {true, "/noWatchPath", true, Watcher.WatcherType.Children, true },

        });
    }

    public ZooKeeperRemoveWatchesTest (boolean expectedResult, String path, boolean validWatcher, Watcher.WatcherType watcherType, boolean local){

        this.expectedResult =expectedResult;
        this.watcherType=watcherType;
        this.path=path;
        this.local=local;
        this.validWatcher = validWatcher;

    }


    @Before
    public void setup() throws Exception {

        System.out.println("starting setUp");
        zk = createClient(watcher, hostPort, CONNECTION_TIMEOUT);

    }


    @After
    public void teardown() throws Exception {

        if (zk != null)
            zk.close();

    }

    //tries to remove watcher that doesn't exist, and so the server returns an error code
    @Test
    public void myTest()  {

        boolean result = false;
        System.out.println("starting test");

        try {
            watcher.waitForConnected(CONNECTION_TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }


        try {

            if (validWatcher) zk.removeWatches(path, watcher, watcherType, local);

            else zk.removeWatches(path, null, watcherType, local);

        } catch (KeeperException e) {   //NoWatcherException because a watcher with these parameters doesn't exist
            System.out.println("############## KEEPER");

            if (e.code().intValue() == KeeperException.Code.NOWATCHER.intValue()) {

                result = true;     //se res = true, it means that ihave a code error

            }

        } catch (IllegalArgumentException e) {     //invalid path or null watcher
            System.out.println("############## ILLEGAL ");
            result = true;

        } catch ( InterruptedException e ){  //server transaction is aborted then test fails
            System.out.println("############## INTERRUPTED ");
            result = false;

        } catch (NullPointerException e){
            System.out.println("############## NULL POINTER ");
            result = false;
        }

        System.out.println("test finished ");
        Assert.assertEquals(expectedResult,result);
    }


}