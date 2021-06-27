package org.apache.zookeeper.observerMasterTest;

import org.apache.zookeeper.common.X509Exception;
import org.apache.zookeeper.server.quorum.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.apache.zookeeper.server.quorum.ZabUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

/**
 * Test the ObserverMaster method "startForwarding()"
 */
@RunWith(value = Parameterized.class)
public class ObserverMasterStartForwardingTest {

    boolean expectedResult;

    private static File tmpDir;
    private static Leader leader;


    private static final int LAST_PACKET_ID = 50;
    private static final int FIRST_PACKET_ID = 25;

    //Observer class to test
    private ObserverMaster obsM;
    private LearnerHandler lh;
    private boolean useQueue;
    private long lastSeenZxid;


    public ObserverMasterStartForwardingTest(boolean expectedResult, LearnerHandler lh, long lastSeenZxid, boolean useQueue) {
        obsM = new ObserverMaster(null,null, 2020); //the arguments are null because we don't use them in test
        this.expectedResult = expectedResult;
        this.lh = lh;
        this.lastSeenZxid = lastSeenZxid;
        this.useQueue = useQueue;

    }



    @Before
    public void setUp() {
        System.out.println("starting setup");

        if (useQueue){
            for (int i = FIRST_PACKET_ID; i < LAST_PACKET_ID; i++) {
                QuorumPacket qp = new QuorumPacket(0, i, "test".getBytes(), null);
                obsM.cacheCommittedPacket(qp);
            }
        }
    }


    @After
    public void tearDown(){
        System.out.println("starting tearDown");

        leader.shutdown("end of test");
        tmpDir.delete();

        System.out.println("tearDown finished");


    }

    @Parameterized.Parameters
    public static Collection<?> getTestParameters() throws NoSuchFieldException, X509Exception, IllegalAccessException, IOException {

        LearnerHandler validLh = createLearner(); //create a LearnerHandler, developers code helps a lot

        return Arrays.asList(new Object[][]{
                //boolean expectedResult, LearnerHandler lh, long lastSeenZxid, boolean useQueue
                {true, validLh, 30, true},
                {true, validLh, 30, false},
                {false, null, 30, true},
                {true, validLh, FIRST_PACKET_ID-1, true},
                {true, validLh, FIRST_PACKET_ID+1, true},
                {false, validLh, FIRST_PACKET_ID-2, true},
                {true, validLh, LAST_PACKET_ID+1, true}
        });

        //false only if lh LearnerHandle  is null or first packet id in the queue  > last packet seen by LearnerHandle"
    }


    @Test
    public void startForwarding() {
        boolean result;

        try {

             if (obsM.startForwarding(lh, lastSeenZxid)!=0) result=false;

             else result=true;

        } catch (NullPointerException e) {
            Assert.assertNull(lh);
            result=false;
        }

        Assert.assertEquals(result, expectedResult);
    }


    public static LearnerHandler createLearner() throws IOException, IllegalAccessException, NoSuchFieldException, X509Exception {
        Socket[] pair = getSocketPair();
        Socket leaderSocket = pair[0];
        Socket followerSocket = pair[1];
        Path path = Paths.get("/tmp/test");
        if(!Files.exists(path))
            Files.createFile(path);
        tmpDir = new File("/tmp/test");
        tmpDir.delete();
        tmpDir.mkdir();
        QuorumPeer peer = ZabUtils.createQuorumPeer(tmpDir);
        leader = ZabUtils.createLeader(tmpDir, peer);

        LearnerHandler lh = new LearnerHandler(leaderSocket, new BufferedInputStream(leaderSocket.getInputStream()), leader);
        lh.start();

        return lh;
    }



    static Socket[] getSocketPair() throws IOException {
        ServerSocket ss = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        InetSocketAddress endPoint = (InetSocketAddress) ss.getLocalSocketAddress();
        Socket s = new Socket(endPoint.getAddress(), endPoint.getPort());
        return new Socket[]{s, ss.accept()};
    }
}
