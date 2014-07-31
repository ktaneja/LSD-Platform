package com.accenture.techlabs.sensordata.dao;


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

import static java.lang.System.out;

/**
 * Class used for connecting to Cassandra database.
 */
public class CassandraConnection
{
   /** Cassandra Cluster. */
   private Cluster cluster;

   /** Cassandra Session. */
   private Session session;
   
  
   
   private static CassandraConnection connection;
   
   private CassandraConnection(){
	   String serverIP = "10.1.185.134"; // get from property file
	   String keyspace = "sensor_data";
	   connect(serverIP, keyspace);
   }

   public static CassandraConnection getConnection(){
	   if(connection == null){
		   connection = new CassandraConnection();
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
	   CassandraConnection connection = getConnection();
	   
	   ResultSet rs = connection.getSession().execute("select * from sample_data;");
	   while(rs.iterator().hasNext()){
		   System.out.println(rs.iterator().next());
	   }


	}

}