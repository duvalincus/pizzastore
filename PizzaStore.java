/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class PizzaStore {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of PizzaStore
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public PizzaStore(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end PizzaStore

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            PizzaStore.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      PizzaStore esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the PizzaStore object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new PizzaStore (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Profile");
                System.out.println("2. Update Profile");
                System.out.println("3. View Menu");
                System.out.println("4. Place Order"); //make sure user specifies which store
                System.out.println("5. View Full Order ID History");
                System.out.println("6. View Past 5 Order IDs");
                System.out.println("7. View Order Information"); //user should specify orderID and then be able to see detailed information about the order
                System.out.println("8. View Stores"); 

                //**the following functionalities should only be able to be used by drivers & managers**
                System.out.println("9. Update Order Status");

                //**the following functionalities should ony be able to be used by managers**
                System.out.println("10. Update Menu");
                System.out.println("11. Update User");

                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: viewProfile(esql, authorisedUser); break;
                   case 2: updateProfile(esql); break;
                   case 3: viewMenu(esql); break;
                   case 4: placeOrder(esql); break;
                   case 5: viewAllOrders(esql, authorisedUser); break;
                   case 6: viewRecentOrders(esql, authorisedUser); break;
                   case 7: viewOrderInfo(esql, authorisedUser); break;
                   case 8: viewStores(esql); break;
                   case 9: updateOrderStatus(esql); break;
                   case 10: updateMenu(esql); break;
                   case 11: updateUser(esql); break;



                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "                  User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /**
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /**
    * Creates a new user
    **/
   public static void CreateUser(PizzaStore esql){
      /*login varchar(50) NOT NULL,
                     password varchar(30) NOT NULL,
                     role char(20) NOT NULL,
                     favoriteItems text,
                     phoneNum varchar(20) NOT NULL,
                     PRIMARY KEY(login)*/
      try {
         System.out.println("Enter username: ");
         String login = in.readLine();
         System.out.println("Enter password:");
         String password = in.readLine();
         System.out.println("Enter phone number:");
         String phoneNum = in.readLine();

         String query = String.format("INSERT INTO Users VALUES ('%s', '%s', 'customer', NULL, '%s');", login, password, phoneNum);
         esql.executeQueryAndPrintResult(query);
      }
      catch (Exception e){
         System.out.println(e);
      }


   }//end CreateUser
      

   /**
    * Check log in credentials for an existing user
    * @return User login or null if the user does not exist
    **/
   public static String LogIn(PizzaStore esql){
      try {
         System.out.println("Enter username: ");
         String login = in.readLine();
         System.out.println("Enter password:");
         String password = in.readLine();
         String query = String.format("SELECT u.login, u.password FROM Users u WHERE u.login = '%s' AND u.password = '%s'", login, password);
         List<List<String>> result = esql.executeQueryAndReturnResult(query);
         
         // debug printing:
         // System.out.println(result);
         // System.out.println(result.get(0).get(1)); 
         // System.out.println(password);
         // System.out.println(result.get(0).get(0).equals(login));

         if (result.get(0).get(0).equals(login) && result.get(0).get(1).equals(password)) {
            System.out.println("Username and password match! :}");
            return login;
         }
         else {
            System.out.println("User does not exist in database. :{");
            return null;
         }
      }
      catch (Exception e){
         System.out.println(e);
         return null;
      }
   }//end

// Rest of the functions definition go in here

   public static void viewProfile(PizzaStore esql, String user) {
      try {
         String query = String.format("SELECT * FROM Users u WHERE u.login = '%s';", user);
         // System.out.println(query);
         List<List<String>> res = esql.executeQueryAndReturnResult(query);
         // System.out.println(res);
         System.out.println(String.format("\nProfile Info: \n\nLogin: %s \nPassword: %s \nFavorite Items: %s \nPhone Number: %s\n", 
            res.get(0).get(0), res.get(0).get(1), res.get(0).get(3), res.get(0).get(4)));

      }
      catch(Exception e) {
         System.out.println(e);
      }
   }
   public static void updateProfile(PizzaStore esql) {}
   public static void viewMenu(PizzaStore esql) {
      try {
         System.out.println("item type?: ");
         String filter = in.readLine();
         System.out.println("price limit?: ");
         String price = in.readLine();
         System.out.println("sorted asc or desc? (enter nothing for no filter): ");
         String sort = in.readLine();

         String query = String.format("SELECT * FROM Items WHERE typeOfItem = '%s' AND price < %s;", filter, price);
         System.out.println(query);
         if (!sort.isEmpty()) {
            query = query.substring(0,query.length() - 1);
            query+= "ORDER BY price " + sort + ";";
         }

         // System.out.println(query);
         List<List<String>> res = esql.executeQueryAndReturnResult(query);
         // System.out.println(res);
         for (int i = 0; i < res.size(); i++) {
            System.out.println(
               String.format("Item: %s\nIngredients: %s\nType: %s\nPrice: %s\nDescription: %s\n", 
                  res.get(i).get(0), res.get(i).get(1), res.get(i).get(2), res.get(i).get(3), res.get(i).get(4))
            );
         }

      } catch (Exception e) {
         System.err.println(e);
      }
   }
   public static void placeOrder(PizzaStore esql) {}
   public static void viewAllOrders(PizzaStore esql, String login) {
      try {
         // System.out.println(role);
         String query = String.format("SELECT orderID FROM FoodOrder O");
         List<List<String>> res;

         if (isCustomer(esql, login)) {
            // System.out.println("executing customer query");
            res = esql.executeQueryAndReturnResult(
               query += String.format(" WHERE O.login = '%s';", login));
         }
         else {
            // System.out.println("executing non-customer query");
            res = esql.executeQueryAndReturnResult(query += ";");
            // System.out.println(res);
         }

         if (!res.isEmpty()) {
            for (int i = 0; i < res.size(); i++) {
               System.out.println(String.format("Order %s: %s", String.valueOf(i), res.get(i).get(0)));
            }
         }
         else {
            System.out.println("No orders found.");
         }

      } catch (Exception e) {
         System.err.println(e);
         ;
      }
   }
   public static void viewRecentOrders(PizzaStore esql, String login) {}
   public static void viewOrderInfo(PizzaStore esql, String login) {
      try {
         System.out.println("Enter order ID to view: ");
         String orderID = in.readLine();
   
         // System.out.println(role);
         
         if (isCustomer(esql, login)) {
            // System.out.println("executing customer query");
            List<List<String>> res = esql.executeQueryAndReturnResult(
               String.format("SELECT * FROM FoodOrder O, ItemsInOrder I WHERE O.login = '%s' AND O.orderID = %s AND I.orderID = O.orderID;", login, orderID));
            if (!res.isEmpty()) {
               System.out.println(String.format("Order Time: %s\nTotal Price: %s\nOrder Status: %s\n",
                     res.get(0).get(4), res.get(0).get(3), res.get(0).get(5)).trim());
            }
            else {
               System.out.println("Order is not your own, please choose your own order.\n");
            }
         }

         else {
            // System.out.println("executing non-customer query");
            List<List<String>> res = esql.executeQueryAndReturnResult(
                  String.format("SELECT * FROM FoodOrder O, ItemsInOrder I WHERE O.orderID = %s AND O.orderID = I.orderID;", orderID));
            
            System.out.println(String.format("Order Time: %s\nTotal Price: %s\nOrder Status: %s\n",
               res.get(0).get(4), res.get(0).get(3), res.get(0).get(5)).trim());
            System.out.println("Items:");
                  for (int i = 0; i < res.size(); i++) {
                     System.out.println(String.format("\tItem:  %s, Quantity: %s", res.get(i).get(7), res.get(i).get(8)));
            }
            // System.out.println(res);
         }
         
      } catch (Exception e) {
         System.err.println(e);;
      }
   }
   public static void viewStores(PizzaStore esql) {
      try {
         String query = String.format("SELECT * FROM Store;");
         // System.out.println(query);
         List<List<String>> res = esql.executeQueryAndReturnResult(query);
         // System.out.println(res);
         for (int i = 0; i < res.size(); i++) {
            System.out.println(
                  String.format("StoreID : %s\nAddress: %s, %s, %s \nIsOpen?: %s \nReview Score: %s\n",
                        res.get(i).get(0), res.get(i).get(1), res.get(i).get(2), res.get(i).get(3), res.get(i).get(4), res.get(i).get(5)));
         }

      } catch (Exception e) {
         System.out.println(e);
      }
   }
   public static void updateOrderStatus(PizzaStore esql) {}
   public static void updateMenu(PizzaStore esql) {}
   public static void updateUser(PizzaStore esql) {}

   /**
    * checks user login for their role
    * @param login login string
    * @param esql sql object
    * @return true if user is a customer
    **/
   public static Boolean isCustomer(PizzaStore esql, String login) {
      try {
         List<List<String>> res = esql.executeQueryAndReturnResult(String.format("SELECT role FROM Users WHERE login = '%s';", login));
   
         // System.out.println(res.get(0).get(0));
         
         // i hate bad data goddammit
         return res.get(0).get(0).trim().contains("customer");
         
      } catch (Exception e) {
         System.err.println(e);
      }
      return false;
   }


}//end PizzaStore

