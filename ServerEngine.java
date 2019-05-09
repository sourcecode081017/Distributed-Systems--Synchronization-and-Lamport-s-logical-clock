/* Anirudh Sivaramakrishnan
 * 
 */
/*CITATION:https://www.youtube.com/watch?v=hZgntu7889Q (Youtube video)
 * https://stackoverflow.com/questions/1383797/java-hashmap-how-to-get-key-from-value
 * JAVA - The Complete Reference -Herbert Schildt (9th edition)
 * http://www.jmarshall.com/easy/http/ HTTP Made Really Easy.(For http message format)
 * https://stackoverflow.com/questions/363681/how-do-i-generate-random-integers-within-a-specific-range-in-java
 * https://www.baeldung.com/java-timer-and-timertask
 */

//Import Statements
package server_stub;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/*The Server engine Main class extending the JFrame to
 * Support UI Components
 */
public class ServerEngine extends javax.swing.JFrame {
	/*
	 * This hash map is used to map each client output stream with a user name.
	 * And the Arraylist holds the users connected to the server
	 */
	HashMap<PrintWriter, String> clientOutputStreams;
	ArrayList<String> users;

	/*
	 * The client handler class, that is used to listen to the incoming client
	 * connections and handle it
	 */
	public class ClientHandler implements Runnable {
		BufferedReader reader;
		Socket sock;
		PrintWriter client;

		// The client handler constructor invokes the client socket and
		// Printer for the client
		public ClientHandler(Socket clientSocket, PrintWriter user) {
			client = user;
			try {
				sock = clientSocket;
				// Instantiating the InputStream reader
				InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
				reader = new BufferedReader(isReader);
			} catch (Exception ex) {
				textChat.append("Unexpected error... \n");
			}

		}

		// The run method, starting point of the Thread
		@Override
		public void run() {
			/*
			 * Series to predefined connection strings, to describe the action
			 * performed like connect, disconnet and chat.
			 */
			String message, connect = "Connect", disconnect = "Disconnect", chat = "Chat";
			// The data string array to hold the username:message:mode:client
			String[] data;

			try {
				// Iterating through Message from the client
				while ((message = reader.readLine()) != null) {
					// Split the message from :
					data = message.split(":");
					// If the mode that is data[2] is Connect
					if (data[2].equals(connect)) {
						textChat.append(data[0] + " has connected\n");
						// append the user name as value to the client print
						// writer object
						clientOutputStreams.put(client, data[0]);
						// add the user name to the array list
						userAdd(data[0]);
						// Tell all connected clients if a client is connected
						tellEveryone(message);

					}
					// If the message is disconnect
					else if (data[2].equals(disconnect)) {
						// tell all the clients that the current client has
						// disconnect
						tellEveryone(message);
						textChat.append(data[0] + " has disconnected\n");
						// Remove the user from the user name list
						userRemove(data[0]);
					}
					/*
					 * If it is Chat and user name is empty is is 1-N broadcast
					 * if the username is not empty is is 1-1
					 */
					else if (data[2].equals(chat)) {
						if (data.length == 3)
							// Tell everyone the message
							tellEveryone(message);
						else
							// Tell only someone(particular client) the message
							tellSomeone(message, data[3]);
					}
					//Condition for forwarding logical clock
					else if(data[2].equals("clock")){
						//Select a randomly connected client
						int r = ThreadLocalRandom.current().nextInt(0,users.size());
						String uName = users.get(r);
						String clock = data[1];
						//Get its socket writer object
						Iterator it = clientOutputStreams.entrySet().iterator();
						while(it.hasNext()){
							try{
								Map.Entry pair = (Map.Entry) it.next();
								String s = (String) pair.getValue();
								if(s.equals(uName)&&!uName.equals(data[0])){
									PrintWriter writer = (PrintWriter) pair.getKey();
									writer.println(data[0]+":"+data[1]+":"+data[2]);
									writer.flush();
									textChat.setCaretPosition(textChat.getDocument().getLength());
									continue;
									
								}
								//Forward the logical time and ID to the corresponding remote client
								if(s.equals(data[0])&&!uName.equals(data[0])){
									PrintWriter writer = (PrintWriter) pair.getKey();
									writer.println(uName+":"+""+":"+"backToSender");
									writer.flush();
									textChat.setCaretPosition(textChat.getDocument().getLength());
									continue;
									
								}
							} 
							//Throw exception if there was an error forwarding
							catch(Exception e){
								e.printStackTrace();
								textChat.append("Error forwarding logical clock \n");
							}
							
						}
						
					}
					
					else {
						textChat.append("No Conditions were met. \n");
					}
				}
			}
			/*
			 * Expection is thrown if a client disconnects, it is caught and the
			 * message lost a connection is appended to the server GUI
			 */
			catch (Exception ex) {
				textChat.append("Lost a connection. \n");
				ex.printStackTrace();
				// Remove the client from the client output stream
				clientOutputStreams.remove(client);
			}
		}
	}

	// The server engine constructor, used to initialize components
	public ServerEngine() {
		initComponents();
	}

	@SuppressWarnings("unchecked")
	// Server GUI components
	private void initComponents() {

		jScrollPane1 = new javax.swing.JScrollPane();
		textChat = new javax.swing.JTextArea();
		btnStart = new javax.swing.JButton();
		btnEnd = new javax.swing.JButton();
		btnUsers = new javax.swing.JButton();
		btnClear = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle("Chat - Server's frame");
		setName("server");
		setResizable(false);

		textChat.setColumns(20);
		textChat.setRows(5);
		jScrollPane1.setViewportView(textChat);

		btnStart.setText("START");
		btnStart.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				b_startActionPerformed(evt);
			}
		});

		btnEnd.setText("END");
		btnEnd.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				b_endActionPerformed(evt);
			}
		});

		btnUsers.setText("Online Users");
		btnUsers.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				b_usersActionPerformed(evt);
			}
		});

		btnClear.setText("Clear");
		btnClear.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				b_clearActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout
						.createSequentialGroup().addContainerGap().addGroup(layout
								.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
										jScrollPane1)
								.addGroup(layout.createSequentialGroup()
										.addGroup(layout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
												.addComponent(btnEnd, javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(btnStart, javax.swing.GroupLayout.DEFAULT_SIZE, 75,
														Short.MAX_VALUE))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 291,
												Short.MAX_VALUE)
										.addGroup(layout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
												.addComponent(btnClear, javax.swing.GroupLayout.DEFAULT_SIZE,
														javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(btnUsers, javax.swing.GroupLayout.DEFAULT_SIZE, 103,
														Short.MAX_VALUE))))
						.addContainerGap())
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGap(209, 209, 209)));
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								layout.createSequentialGroup().addContainerGap()
										.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 340,
												Short.MAX_VALUE)
										.addGap(18, 18, 18)
										.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(btnStart).addComponent(btnUsers))
										.addGap(18, 18, 18)
										.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(btnClear).addComponent(btnEnd))
										.addGap(4, 4, 4)));

		pack();
	}

	// Stop the server by interrupting the thread
	private void b_endActionPerformed(java.awt.event.ActionEvent evt) {
		try {
			// Sleep for seconds
			Thread.sleep(5000);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		// Tell all clients that the server is stopping and will be disconnected
		tellEveryone("Server:is stopping and all users will be disconnected.\n:Chat");
		textChat.append("Server stopping... \n");
		textChat.setText("");
	}

	// This method handles the start server functionality.
	private void b_startActionPerformed(java.awt.event.ActionEvent evt) {
		// New server thread is invoked and started
		Thread starter = new Thread(new StartServer());
		starter.start();
		// Server started method is printed out
		textChat.append("Server started...\n");
	}

	// This method is used show the clients connected to the server in real time
	private void b_usersActionPerformed(java.awt.event.ActionEvent evt) {
		textChat.append("\n Online users : \n");
		// Iterate through the list of clients and append to the server GUI
		for (String current_user : users) {
			textChat.append(current_user);
			textChat.append("\n");
		}

	}

	// Clear the Server GUI text area
	private void b_clearActionPerformed(java.awt.event.ActionEvent evt) {
		textChat.setText("");
	}

	// The main method
	public static void main(String args[]) {
		/*
		 * Invoke later doesn't let the server GUI hang while it continuously
		 * listens for client connections
		 */
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			// Invoke the run method
			public void run() {
				new ServerEngine().setVisible(true);
			}
		});
	}

	// The start server class (this is called when the start sever button is
	// pressed in the GUI)
	public class StartServer implements Runnable {
		@Override
		// The run method
		public void run() {
			// instantiating the client output stream hash map
			clientOutputStreams = new HashMap<PrintWriter, String>();
			// instantiating the client username array list
			users = new ArrayList();
			String data;
			try {
				// listen at port 2222
				ServerSocket serverSock = new ServerSocket(2222);
				/*
				 * Continuously listen for client connections and if a client is
				 * connected fork a new thread to handle that client and start
				 * listening again
				 */
				while (true) {
					Socket clientSock = serverSock.accept();
					PrintWriter writer = new PrintWriter(clientSock.getOutputStream());
					clientOutputStreams.put(writer, "");
					Thread listener = new Thread(new ClientHandler(clientSock, writer));
					listener.start();
					textChat.append("Got a connection. \n");
				}
			} catch (Exception ex) {
				textChat.append("Error making a connection. \n");
			}
		}
	}

	// Method to add the client username to the array list
	public void userAdd(String data) {
		String message, add = ": :Connect", done = "Server: :Done", name = data;

		users.add(name);

		String[] tempList = new String[(users.size())];
		users.toArray(tempList);
		// Iterate through the message and tell all clients that particular
		// client has connected
		for (String token : tempList) {
			message = (token + add);
			tellEveryone(message);
		}
		// Tell every one the the action was done
		tellEveryone(done);
	}

	// Method to remove a client from the array list
	public void userRemove(String data) {
		String message, add = ": :Connect", done = "Server: :Done", name = data;
		users.remove(name);
		String[] tempList = new String[(users.size())];
		users.toArray(tempList);

		for (String token : tempList) {
			message = (token + add);
			// tell all clients that a client was disconnected from the server
			tellEveryone(message);
		}
		tellEveryone(done);
	}

	// Method to tell all connected clients a message
	public void tellEveryone(String message) {
		// Iterator object to iterate through the client output streams
		Iterator it = clientOutputStreams.entrySet().iterator();
		String httpMessage = "";
		// Iterate through all the client out put streams and print the message
		// in http (in the server)

		while (it.hasNext()) {
			try {
				Map.Entry pair = (Map.Entry) it.next();
				PrintWriter writer = (PrintWriter) pair.getKey();
				writer.println(message);
				String time = new SimpleDateFormat("yyyy/MM/dd_HH:mm:ss").format(Calendar.getInstance().getTime());
				int contentLength = message.length();
				String[] msg = message.split(":");
				if (msg[2].equals("Chat")) {
					String httpContent = "<html>\n<body>\n<p>" + msg[1] + "</p>\n</body>\n</html>";
					httpMessage = "\nHTTP/1.1 200 ok\nHost:localhost\nDate:" + time
							+ "\nContent-Type:text/html\nContent-Length:" + contentLength + "\n" + httpContent;
					textChat.append("Sending: " + httpMessage + "\n");
				}

				writer.flush();
				textChat.setCaretPosition(textChat.getDocument().getLength());

			} catch (Exception ex) {
				textChat.append("Error telling everyone. \n");
			}
		}
	}

	// Method to send message to a particular client
	public void tellSomeone(String message, String thatSomeone) {
		/*
		 * This method takes a particular client username as a parameter and
		 * prints the message only to that client output stream
		 */

		Iterator it = clientOutputStreams.entrySet().iterator();
		String httpMessage = "";
		while (it.hasNext()) {
			try {
				Map.Entry pair = (Map.Entry) it.next();
				String s = (String) pair.getValue();
				if (s.equals(thatSomeone)) {
					PrintWriter writer = (PrintWriter) pair.getKey();
					writer.println(message);
					String time = new SimpleDateFormat("yyyy/MM/dd_HH:mm:ss").format(Calendar.getInstance().getTime());
					int contentLength = message.length();
					String[] msg = message.split(":");
					// only if it is a chat message print the http format in the
					// server
					if (msg[2].equals("Chat")) {
						String httpContent = "<html>\n<body>\n<h1>" + msg[1] + "</h1>\n</body>\n</html>";
						httpMessage = "\nHTTP/1.1 200 ok\nHost:localhost\nDate:" + time
								+ "\nContent-Type:text/html\nContent-Length:" + contentLength + "\n" + httpContent;
						textChat.append("Sending: " + httpMessage + "\n");
					}
					writer.flush();
					textChat.setCaretPosition(textChat.getDocument().getLength());
					break;
				}
			} catch (Exception ex) {
				textChat.append("Error telling that someone. \n");
			}
		}
	}

	// Declaration of Server GUI Components
	private javax.swing.JButton btnClear;
	private javax.swing.JButton btnEnd;
	private javax.swing.JButton btnStart;
	private javax.swing.JButton btnUsers;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JTextArea textChat;

}
