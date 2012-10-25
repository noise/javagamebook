package com.hypefiend.blackart2.chatterbox;

import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;
import java.net.*;
import java.io.*;

import org.apache.log4j.*;

/**
 * ChatterServer,java
 *
 * A basic example of a multi-user chat application using the JDK 1.4 NIO libraries
 * 
 * @author <a href="mailto:bret@hypefiend.com">bret barker</a>
 * @version 1.0
 */
public class ChatterServer extends Thread {
    private static final int BUFFER_SIZE = 255;
    private static final long CHANNEL_WRITE_SLEEP = 10L;
    private static final int PORT = 10997;

    private Logger log = Logger.getLogger(ChatterServer.class);
    private ServerSocketChannel sSockChan;
    private Selector acceptSelector;
    private Selector readSelector;
    private SelectionKey selectKey;
    private boolean running;
    private LinkedList clients;
    private ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;
    private CharsetDecoder asciiDecoder;
  
    public static void main(String args[]) {
	// configure log4j
	BasicConfigurator.configure();
	
	// instantiate the ChatterServer and fire it up
	ChatterServer cs = new ChatterServer();
	cs.start();
    }
  
    public ChatterServer() {
	clients = new LinkedList();
	readBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
	writeBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
	asciiDecoder = Charset.forName( "US-ASCII").newDecoder();;
    }

    private void initServerSocket() {
	try {
	    // open a non-blocking server socket channel
	    sSockChan = ServerSocketChannel.open();
	    sSockChan.configureBlocking(false);

	    // bind to localhost on designated port
	    InetAddress addr = InetAddress.getLocalHost();
        log.info("binding to " + addr);
	    sSockChan.socket().bind(new InetSocketAddress(addr, PORT));

	    // get a selector for multiplexing the client channels
	    readSelector = Selector.open();
	}
	catch (Exception e) {
	    log.error("error initializing server", e);
	}
    }

    public void run() {
	initServerSocket();

	log.info("ChatterServer running");
	running = true;
	int numReady = 0;

	// block while we wait for a client to connect
	while (running) {
	    // check for new client connections
	    acceptNewConnections();
	    
	    // check for incoming mesgs
	    readIncomingMessages();
	    
	    // sleep a bit
	    try {
		Thread.sleep(100);
	    }
	    catch (InterruptedException ie) {
	    }
	}
    }
    
    private void acceptNewConnections() {
	try {
	    SocketChannel clientChannel;
	    // since sSockChan is non-blocking, this will return immediately 
	    // regardless of whether there is a connection available
	    while ((clientChannel = sSockChan.accept()) != null) {
		addNewClient(clientChannel);
		log.info("got connection from: " + clientChannel.socket().getInetAddress()); 
		sendBroadcastMessage("login from: " + clientChannel.socket().getInetAddress(), clientChannel);
		sendMessage(clientChannel, "\n\nWelcome to ChatterBox, there are " + 
			    clients.size() + " users online.\n");
		sendMessage(clientChannel, "Type 'quit' to exit.\n");
	    }		
	}
	catch (IOException ioe) {
	    log.warn("error during accept(): ", ioe);
	}
	catch (Exception e) {
	    log.error("exception in acceptNewConnections()", e);
	}
    }

    private void readIncomingMessages() {
	try {
	    // non-blocking select, returns immediately regardless of how many keys are ready
	    readSelector.selectNow();
	    
	    // fetch the keys
	    Set readyKeys = readSelector.selectedKeys();
	    
	    // run through the keys and process
	    Iterator i = readyKeys.iterator();
	    while (i.hasNext()) {
		SelectionKey key = (SelectionKey) i.next();
		i.remove();
		SocketChannel channel = (SocketChannel) key.channel();
		readBuffer.clear();
		
		// read from the channel into our buffer
		long nbytes = channel.read(readBuffer);
		
		// check for end-of-stream
		if (nbytes == -1) { 
		    log.info("disconnect: " + channel.socket().getInetAddress() + ", end-of-stream");
		    channel.close();
		    clients.remove(channel);
		    sendBroadcastMessage("logout: " + channel.socket().getInetAddress() , channel);
		}
		else {
		    // grab the StringBuffer we stored as the attachment
		    StringBuffer sb = (StringBuffer)key.attachment();
		    
		    // use a CharsetDecoder to turn those bytes into a string
		    // and append to our StringBuffer
		    readBuffer.flip( );
		    String str = asciiDecoder.decode( readBuffer).toString( );
		    readBuffer.clear( );
		    sb.append( str);
		    
		    // check for a full line
		    String line = sb.toString();
		    if ((line.indexOf("\n") != -1) || (line.indexOf("\r") != -1)) {
			line = line.trim();
			if (line.startsWith("quit")) {
			    // client is quitting, close their channel, remove them from the list and notify all other clients
			    log.info("got quit msg, closing channel for : " + channel.socket().getInetAddress());
			    channel.close();
			    clients.remove(channel);
			    sendBroadcastMessage("logout: " + channel.socket().getInetAddress(), channel);
			}
			else {
			    // got one, send it to all clients
			    log.info("broadcasting: " + line);
			    sendBroadcastMessage(channel.socket().getInetAddress() + ": " + line, channel);
			    sb.delete(0,sb.length());
			}
		    }
		}
		
	    }		
	}
	catch (IOException ioe) {
	    log.warn("error during select(): ", ioe);
	}
	catch (Exception e) {
	    log.error("exception in run()", e);
	}
	
    }
    
    private void addNewClient(SocketChannel chan) {
	// add to our list
	clients.add(chan);
	
	// register the channel with the selector 
	// store a new StringBuffer as the Key's attachment for holding partially read messages
	try {
	    chan.configureBlocking( false);
	    SelectionKey readKey = chan.register(readSelector, SelectionKey.OP_READ, new StringBuffer());
	}
	catch (ClosedChannelException cce) {
	}
	catch (IOException ioe) {
	}
    }
    
    private void sendMessage(SocketChannel channel, String mesg) {
	prepWriteBuffer(mesg);
	channelWrite(channel, writeBuffer);
    }
    
    private void sendBroadcastMessage(String mesg, SocketChannel from) {
	prepWriteBuffer(mesg);
	Iterator i = clients.iterator();
	while (i.hasNext()) {
	    SocketChannel channel = (SocketChannel)i.next();
	    if (channel != from) 
		channelWrite(channel, writeBuffer);
	}
    }
    
    private void prepWriteBuffer(String mesg) {
	// fills the buffer from the given string
	// and prepares it for a channel write
	writeBuffer.clear();
	writeBuffer.put(mesg.getBytes());
	writeBuffer.putChar('\n');
	writeBuffer.flip();
    }
    
    private void channelWrite(SocketChannel channel, ByteBuffer writeBuffer) {
	long nbytes = 0;
	long toWrite = writeBuffer.remaining();

	// loop on the channel.write() call since it will not necessarily
	// write all bytes in one shot
	try {
	    while (nbytes != toWrite) {
		nbytes += channel.write(writeBuffer);
		
		try {
		    Thread.sleep(CHANNEL_WRITE_SLEEP);
		}
		catch (InterruptedException e) {}
	    }
	}
	catch (ClosedChannelException cce) {
	}
	catch (Exception e) {
	} 
	
	// get ready for another write if needed
	writeBuffer.rewind();
    }
}
