/* Anirudh Sivaramakrishnan 
 *             
 */
/*CITATION: https://www.youtube.com/watch?v=hZgntu7889Q (Youtube video)
 * JAVA - The Complete Reference -Herbert Schildt (9th edition)
 * http://www.jmarshall.com/easy/http/ HTTP Made Really Easy.(For http message format)
 * https://stackoverflow.com/questions/363681/how-do-i-generate-random-integers-within-a-specific-range-in-java
 * https://www.baeldung.com/java-timer-and-timertask
 */
package client_stub;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/* The Main Class of the Client */
public class ClientEngine extends javax.swing.JFrame {
	private static final long serialVersionUID = 1L;
	/*
	 * Declaring the User name, the address as local host port as 2222. Also the
	 * socket's buffered reader and writer
	 */
	String username, address = "localhost";
	ArrayList<String> users = new ArrayList<String>();
	int port = 2222;
	Boolean isConnected = false;

	Socket sock;
	BufferedReader reader;
	PrintWriter writer;
	//Initialize counter with a random integer between 0 and 50
    long counter = ThreadLocalRandom.current().nextInt(0, 51);
    //Start a timer task to increment the counter every one second
    TimerTask task = new TimerTask(){

		@Override
		public void run() {
			counter++;
			System.out.println("Parent method: "+counter);
			}
    	
    	
    };
    //Start a timer task to randomly choose a client every 10 seconds
    TimerTask sendLogicalTime = new TimerTask(){

		@Override
		public void run() {
			String userName = tfUsername.getText();
			writer.println(userName+":"+counter+":"+"clock");
			writer.flush();
			
		}
    	
    	
    };
    //Declare the timer object
    Timer timer;
    //Method to initialize the incoming reader constructor to listen to new messages 
	public void ListenThread() {
		Thread IncomingReader = new Thread(new IncomingReader());
		IncomingReader.start();
	}

	/* Method to add the User that is each client's user name */
	public void userAdd(String data) {
		users.add(data);

	}

	/* Method to remove the User (just for notification purposes) */
	public void userRemove(String data) {
		taChat.append(data + " is now offline.\n");
	}

	/*
	 * Method to write data to the users if 'done message is received form the
	 * server
	 */
	public void writeUsers() {
		String[] tempList = new String[(users.size())];
		users.toArray(tempList);
		for (String token : tempList) {
			// users.append(token + "\n");
		}
	}

	/*
	 * When disconnect button is pressed send disconnect notification to the
	 * server, by writing to the client's writer.
	 */
	public void sendDisconnect() {
		String bye = (username + ": :Disconnect");
		try {
			writer.println(bye);
			writer.flush();
		} catch (Exception e) {
			taChat.append("Could not send Disconnect message.\n");
		}
	}

	/*
	 * Disconnect the client from the server by closing the client socket, else
	 * throw an exception with message "failed to disconnect"
	 */
	public void Disconnect() {
		try {
			taChat.append("Disconnected.\n");
			sock.close();
			//When the client disconnects, cancel the task to send logical time to sever
			sendLogicalTime.cancel();
			//when the client disconnects, cancel the task which increments the counter
			task.cancel();
			//After disconnecting, new task always begins here.
			sendLogicalTime = new TimerTask(){

				@Override
				public void run() {
					String userName = tfUsername.getText();
					writer.println(userName+":"+counter+":"+"clock");
					writer.flush();
					
				}
		    	
		    	
		    };
		    //counter is reinitialized to random value
			counter = ThreadLocalRandom.current().nextInt(0, 51);
			//After disconnecting, new task always begins here.
			task = new TimerTask(){

				@Override
				public void run() {
					counter++;
					System.out.println("disconnect AND CONNECT method: "+counter);
				}
			};
			

			
		} catch (Exception ex) {
			ex.printStackTrace();
			taChat.append("Failed to disconnect. \n");
		}
		isConnected = false;
		tfUsername.setEditable(true);

	}

	/*
	 * The public class constructor which is used to initialize the UI
	 * Components of the client
	 */
	public ClientEngine() {
		initComponents();
	}

	/*
	 * IncomingReader class which is used to read incoming data from the server
	 * and based on the data received, handled by the client. This class
	 * implements the Runnable interface which invokes the client thread.
	 */
	public class IncomingReader implements Runnable {
		/*
		 * Overriding the run method (entry point of the thread) (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			/*
			 * Declaring a String array and a series of strings from the client
			 */
			//Initialize the timer object
			timer = new Timer("Timer");
			//For a delay of one second and a period of one second, execute the task
			timer.scheduleAtFixedRate(task, 1000, 1000);
			//For a delay of 5 seconds, send the logical time every 10 seconds.
			timer.scheduleAtFixedRate(sendLogicalTime, 5000, 10000);
			
			String[] data;
			String stream, done = "Done", connect = "Connect", disconnect = "Disconnect", chat = "Chat";

			try {
				/* Read from the Reader till not null */
				while ((stream = reader.readLine()) != null) {
					// Split data based on : operator

					data = stream.split(":");

					// If it is "Chat" tell the client UI and set caret position

					if (data[2].equals(chat)) {
						taChat.append(data[0] + ": " + data[1] + "\n");
						taChat.setCaretPosition(taChat.getDocument().getLength());
					}

					// If it is Connect, add the user name to the array list
					else if (data[2].equals(connect)) {
						taChat.removeAll();
						taChat.append(data[0] + ": " + data[1] + "\n");
						taChat.setCaretPosition(taChat.getDocument().getLength());
						userAdd(data[0]);

					}
					//Receive the remote client ID and its logical time and display it
					else if(data[2].equals("backToSender")){
						taChat.append("REMOTE CLIENT ID:"+data[0]+":LOCAL CLIENT LOGICAL TIME:"+counter+"\n");
						}
					//The lamport's logical clock algorithm is execute here!
					else if(data[2].equals("clock")){
						
						String uName = data[0];
						long clock = Long.parseLong(data[1]);
						//Ci-->max(ci,cj)
						//ci-->ci+1
						if(counter>=clock)
							taChat.append("SENDER ID:"+uName+":SENDER LOGICAL TIME: "+clock+" :LOCAL LOGICAL TIME: "+counter+" :NO ADJUSTMENTS NEEDED\n");
						else{
							taChat.append("LOCAL LOGICAL TIME:"+counter+"\n");
							counter = clock;
							counter+=1;
							taChat.append("SENDER ID:"+uName+":SENDER LOGICAL TIME: "+clock+" :LOCAL CLOCK ADJUSTED:"+counter+"\n");
						}
						
						
					}
					// If it is Disconnect, remove the User from the array list
					else if (data[2].equals(disconnect)) {
						userRemove(data[0]);

					}
					// Notify user once done
					else if (data[2].equals(done)) {

						writeUsers();
						users.clear();
					}
				}
			} catch (Exception ex) {
			}
		}
	}

	@SuppressWarnings("unchecked")
	/*
	 * The User interface component initialization goes here all the UI
	 * components, their positions and associated methods are declared here.
	 */
	private void initComponents() {

		lblUsername = new javax.swing.JLabel();
		tfUsername = new javax.swing.JTextField();
		btnConnect = new javax.swing.JButton();
		btnDisconnect = new javax.swing.JButton();
		jScrollPane1 = new javax.swing.JScrollPane();
		taChat = new javax.swing.JTextArea();
		tfChat = new javax.swing.JTextField();
		btnSend = new javax.swing.JButton();
		lblSendTo = new javax.swing.JLabel();
		tfSendTo = new javax.swing.JTextField();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle("Chat - Client's frame");
		setName("client");
		setResizable(false);

		lblUsername.setText("Username :");
		lblSendTo.setText("Send Message to:");

		tfUsername.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				tf_usernameActionPerformed(evt);
			}
		});
		tfSendTo.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				tf_usernameActionPerformed(evt);
			}
		});

		btnConnect.setText("Connect");
		btnConnect.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				b_connectActionPerformed(evt);
			}
		});

		btnDisconnect.setText("Disconnect");
		btnDisconnect.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				b_disconnectActionPerformed(evt);
			}
		});

		taChat.setColumns(20);
		taChat.setRows(5);
		jScrollPane1.setViewportView(taChat);

		btnSend.setText("SEND");
		btnSend.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				b_sendActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout
				.createSequentialGroup().addContainerGap()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup()
								.addComponent(tfChat, javax.swing.GroupLayout.PREFERRED_SIZE, 352,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(btnSend, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE))
						.addComponent(jScrollPane1)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
										.addComponent(lblUsername, javax.swing.GroupLayout.DEFAULT_SIZE, 62,
												Short.MAX_VALUE)

								).addGap(18, 18, 18).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)

										.addComponent(tfUsername, javax.swing.GroupLayout.DEFAULT_SIZE, 62,
												Short.MAX_VALUE))
								.addGap(18, 18, 18)
								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)

										.addComponent(lblSendTo, javax.swing.GroupLayout.DEFAULT_SIZE, 62,
												Short.MAX_VALUE))
								.addGap(18, 18, 18)
								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)

										.addComponent(tfSendTo, javax.swing.GroupLayout.DEFAULT_SIZE, 62,
												Short.MAX_VALUE))

								.addGroup(
										layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
												.addGroup(layout.createSequentialGroup().addComponent(btnConnect)
														.addGap(2, 2, 2).addComponent(btnDisconnect)
														.addGap(0, 0, Short.MAX_VALUE)))))
				.addContainerGap())
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)

						.addGap(201, 201, 201)));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()

						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)

								.addComponent(tfUsername).addComponent(lblUsername).addComponent(lblSendTo)
								.addComponent(tfSendTo).addComponent(btnConnect).addComponent(btnDisconnect))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 310,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(tfChat)
								.addComponent(btnSend, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)));

		pack();
	}

	private void tf_usernameActionPerformed(java.awt.event.ActionEvent evt) {
	}

	/*
	 * When the Connect button in the UI is pressed the following action is
	 * performed
	 * 
	 */
	private void b_connectActionPerformed(java.awt.event.ActionEvent evt) {
		// Check if the client is already connected to the server
		if (isConnected == false) {
			username = tfUsername.getText();
			tfUsername.setEditable(false);

			try {
				// Create a new socket at the local host , port 2222
				sock = new Socket(address, port);
				// Declare the input and output writers / readers of the socket
				InputStreamReader streamreader = new InputStreamReader(sock.getInputStream());
				reader = new BufferedReader(streamreader);
				writer = new PrintWriter(sock.getOutputStream());
				// Write to the socket the corresponding username which was
				// connected
				writer.println(username + ":has connected.:Connect");
				writer.flush();
				isConnected = true;
			} catch (Exception ex) {
				taChat.append("Cannot Connect! Try Again. \n");
				tfUsername.setEditable(true);
			}
			// Call the listen thread for incoming message from the server
			ListenThread();

		}
		// If the User is already connected
		else if (isConnected == true) {
			taChat.append("You are already connected. \n");
		}
	}

	// When the disconnect button is pressed
	private void b_disconnectActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_b_disconnectActionPerformed
		sendDisconnect();
		Disconnect();
	}

	// When the send button is pressed
	private void b_sendActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_b_sendActionPerformed
		String nothing = "";
		// If the text is empty do not send
		if ((tfChat.getText()).equals(nothing)) {
			tfChat.setText("");
			tfChat.requestFocus();
		}
		// Else write the message to the client socket telling the username,
		// message and chat
		else {
			try {
				if (tfSendTo.getText().equals(nothing)) {
					writer.println(username + ":" + tfChat.getText() + ":" + "Chat" + ":" + "");
					writer.flush(); // flushes the buffer
				} else {
					writer.println(username + ":" + tfChat.getText() + ":" + "Chat" + ":" + tfSendTo.getText());
					writer.flush(); // flushes the buffer
				}
			} catch (Exception ex) {
				taChat.append("Message was not sent. \n");
			}
			tfChat.setText("");
			tfChat.requestFocus();
		}

		tfChat.setText("");
		tfChat.requestFocus();
	}

	// The Main method
	public static void main(String args[]) {
		/*
		 * Calling the invoke later because new forking a new thread the current
		 * or (main) thread will wait till the new thread finishes, hence the UI
		 * will be blocked,
		 */
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			// The run method, which will call the ClientEngine Constructor
			public void run() {
				new ClientEngine().setVisible(true);
			}
		});
	}

	// Declaration of UI Components
	private javax.swing.JButton btnConnect;
	private javax.swing.JButton btnDisconnect;
	private javax.swing.JButton btnSend;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JTextField tfSendTo;
	private javax.swing.JLabel lblSendTo;
	private javax.swing.JLabel lblUsername;
	private javax.swing.JTextArea taChat;
	private javax.swing.JTextField tfChat;
	private javax.swing.JTextField tfUsername;
}
