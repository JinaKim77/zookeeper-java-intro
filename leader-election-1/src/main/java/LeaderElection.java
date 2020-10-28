import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

//this class is able to watch and handle zookeeper events
public class LeaderElection implements Watcher {

    //create zookeeper object
    private ZooKeeper zooKeeper;

    //address of the zookeeper server with hostname and default port number
    private static final String ZOOKEEPER_ADDRESS = "localhost:2181";
    private static final int SESSION_TIMEOUT=3000;
    //to store the name of the parent node
    private static final String ELECTION_NAMESPACE="/election";

    private boolean leader = false;
    //to store the name of the znode created by this application.
    private String currentZnodeName;

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        LeaderElection leaderElection = new LeaderElection();
        leaderElection.connectToZookeeper();
        leaderElection.volunteerForLeadership();
        leaderElection.electLeader();
        leaderElection.run();
        System.out.println("Disconnected from zookeeper, exiting application");
    }

    //method that will create the connection to zookeeper
    public void connectToZookeeper() throws IOException {
        //create a new zookeeper object and save it in the zookeeper variable
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS,SESSION_TIMEOUT,this);
                                                                        //this will handle any events you have, and any events
                                                                        //that happen in zookeeper will be processed by
                                                                        //the process method
    }

    //this methoid will return a string wichi is full path to znode that's created.
    public void volunteerForLeadership() throws KeeperException, InterruptedException {
        //to give a name to child znode
        String znodePrefix=ELECTION_NAMESPACE+"/c_";
        String znodeFullPath = zooKeeper.create(znodePrefix, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        //this is ephemeral znode, when application ends, it will be removed
        this.currentZnodeName = znodeFullPath.replace(ELECTION_NAMESPACE + "/","");
        System.out.println("znode name:"+currentZnodeName);
    }

    //to check what the current children of the election znode,
    //and decide I should be the leader or not.
    public void electLeader() throws KeeperException, InterruptedException {
        //get the current children of the election znode.
        List<String> children = zooKeeper.getChildren(ELECTION_NAMESPACE,false);

        Collections.sort(children);
        //to check what's the smallest child znode
        String smallestChild= children.get(0);

        //this shows how many znodes are currently present under the election.
        System.out.println(children.size()+" znode present");

        //System.out.println("smallest child is"+ smallestChild);

        if(smallestChild.equals(currentZnodeName)){
            System.out.println("I am the leader");
            leader=true;
            return;
        }else{
            System.out.println("I am not the leader, "+ smallestChild +" is the leader.");
            leader=false;
        }
    }

    public void run() throws InterruptedException {
        //synchronized block
        synchronized (zooKeeper){
            zooKeeper.wait();
        }
    }


    public void close(){
    }

    //Watcher interface has process method, so you need to override this method
    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()){
            case None:
                if(watchedEvent.getState()==Event.KeeperState.SyncConnected) {
                    System.out.println("Successfully connected to zookeeper");
                }else{
                    synchronized (zooKeeper) {
                        System.out.println("Disconnected from zookeeper event");
                        zooKeeper.notifyAll();
                    }
                }
        }

    }

    //******************************
    // Needed for tests, don't edit
    //*******************************

    public boolean isLeader() {
        return leader;
    }
}
