package com.accenture.techlabs.sensordata.dbconnectors;


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

/**
 * Class used for connecting to Cassandra database.
 */
public class CassandraConnector
{
   /** Cassandra Cluster. */
   private Cluster cluster;

   /** Cassandra Session. */
   private Session session;
   
  
   
   private static CassandraConnector connection;
   
   private CassandraConnector(){
	   String serverIP = "10.1.185.134"; // get from property file
	   serverIP = "localhost";
	   String keyspace = "sensor_data";
	   connect(serverIP, keyspace);
   }

   public static CassandraConnector getConnection(){
	   if(connection == null){
		   connection = new CassandraConnector();
	   }
	   return connection;
   }
   /**
    * Connect to Cassandra Cluster specified by provided node IP
    * address and port number.
    *
    * @param node Cluster node IP address.
    * @param keyspace Cassandra Keyspace to connect to.
    */
   public void connect(String serverIP, String keyspace)
   {
	   cluster = Cluster.builder().addContactPoints(serverIP).build();
	   session = cluster.connect(keyspace);
   }

   /**
    * Provide my Session.
    *
    * @return My session.
    */
   public Session getSession()
   {
      return this.session;
   }

   /** Close cluster. */
   public void close()
   {
      cluster.close();
   }
   

   
   
   public static void main(String[] args) {
	   CassandraConnector connection = getConnection();
	   
	   ResultSet rs = connection.getSession().execute("select * from sample_data;");
	   while(rs.iterator().hasNext()){
		   System.out.println(rs.iterator().next());
	   }


	}

}