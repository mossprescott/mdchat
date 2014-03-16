package com.theprescotts.mdchat;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.cypher.ExecutionResult;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.impl.util.StringLogger;
import org.neo4j.tooling.GlobalGraphOperations;

/**
 * Provides access to the DB of topics, users, and messages, stored in a neo4j
 * database located under the "build" directory.
 * <p>
 * Only one instance of this class can be active at a time, since it claims
 * exclusive ownership of the DB's files.
 */
public class ChatData {

	// Location of the DB under the project dir:
	private static final String DB_PATH = "build/store";

	
	// Node and relationship labels/types, forming the "schema" for the DB:
	
	private static final Label USER = DynamicLabel.label("USER");
	
	private static final Label TOPIC = DynamicLabel.label("TOPIC");
	
	private static final Label MESSAGE = DynamicLabel.label("MESSAGE");
	private static final RelationshipType IN = DynamicRelationshipType.withName("IN");
	private static final RelationshipType BY = DynamicRelationshipType.withName("BY");
	private static final RelationshipType REPLY_TO = DynamicRelationshipType.withName("REPLY_TO");
	
	
	// A single shared instance, where service instances can find it:
	public static final ChatData instance = new ChatData();
	
	// State:
	private final GraphDatabaseService graphDb;
	private final ExecutionEngine queryEngine;
	
	public ChatData() {
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
		queryEngine = new ExecutionEngine(graphDb, StringLogger.SYSTEM);
		
		// Make sure the DB stops gracefully when the process is killed:
		registerShutdownHook();
		
		// HACK: just so there's always some data to look at
		if (dbIsEmpty()) {
			try {
				System.out.println("adding dummy data");
				
				addMessage("pets", "moss", "Who likes pets?", null);
				addMessage("pets", "bert", "Who doesn't?", null);
				addMessage("pets", "ernie", "Isn't that a bit presumptuous?", null);
				
				addMessage("bikes", "moss", "Bikes are cool", null);
			} catch (JSONException x) {
				throw new RuntimeException(x);
			}
		}
	}
	
	/** Stop the DB connection. */
	public void close() {
		graphDb.shutdown();
	}

	/** Add a user by name, if it doesn't already exist. */
	public JSONObject getUser(String name) throws JSONException {
		try (Transaction tx = graphDb.beginTx()) {
			Node user = findOrCreateUser(name);
			
			tx.success();
			
			return translateUser(user);
		}	
	}

	public List<JSONObject> getUsers() throws JSONException {
		try (Transaction tx = graphDb.beginTx()) {
			ExecutionResult result = queryEngine.execute("match (n:USER) return n order by n.name");
			
			List<JSONObject> users = newArrayList();
			for (scala.collection.Iterator<Node> iter = result.columnAs("n"); iter.hasNext(); ) {
				users.add(translateUser(iter.next()));
			}
			
			tx.success();
			
			return users;
		}	
	}
	
	/** Add a topic by name, if it doesn't already exist. */
	public JSONObject getTopic(String name) throws JSONException {
		try (Transaction tx = graphDb.beginTx()) {
			Node topic = findOrCreateTopic(name);
			
			tx.success();
			
			return translateTopic(topic, true);
		}	
	}

	public List<JSONObject> getTopics() throws JSONException {
		try (Transaction tx = graphDb.beginTx()) {
			ExecutionResult result = queryEngine.execute("match (n:TOPIC) return n order by n.name");
			
			List<JSONObject> topics = newArrayList();
			for (scala.collection.Iterator<Node> iter = result.columnAs("n"); iter.hasNext(); ) {
				topics.add(translateTopic(iter.next(), false));
			}
			
			tx.success();
			
			return topics;
		}	
	}
	
	public JSONObject addMessage(String topicName, String userName, String text, Long replyTo) throws JSONException {
		try (Transaction tx = graphDb.beginTx()) {
			Node topic = findOrCreateTopic(topicName);
			Node user = findOrCreateUser(userName);
			
			Node msg = graphDb.createNode(MESSAGE);
			msg.createRelationshipTo(topic, IN); 
			msg.createRelationshipTo(user, BY);
			if (replyTo != null) {
				Node prevMsg = graphDb.getNodeById(replyTo);
				msg.createRelationshipTo(prevMsg, REPLY_TO);
			}
			msg.setProperty("text", text);

			tx.success();
			
			return translateMessage(msg);
		}	
	}

	/**
	 * Delete all data; mostly useful for automated tests.
	 */
	public void clear() {
		try (Transaction tx = graphDb.beginTx()) {
			Iterable<Node> allNodes = GlobalGraphOperations.at(graphDb).getAllNodes();
			for (Node n : allNodes) {
				// First delete all edges from the node:
				for (Relationship r : n.getRelationships()) {
					r.delete();
				}
				
				// Now delete the node itself:
				n.delete();
			}
			
			tx.success();
		}
	}

	
	// Translators: arguably these belong somewhere else, to separate
	// the DB code from the formatting code, but they need to be called while
	// the transaction is open, so simply calling them from the access methods
	// is simpler than adding a way to manage the transaction across multiple 
	// classes.

	public static JSONObject translateUser(Node node) throws JSONException {
		JSONObject obj = new JSONObject();

		obj.put("name", node.getProperty("name"));
		
		return obj;
	}

	public static JSONObject translateTopic(Node node, boolean deep) throws JSONException {
		JSONObject obj = new JSONObject();
		
		obj.put("name", node.getProperty("name"));

		// Note: these Iterables seem to be usable only once, unlike most, so
		// have to ask for them again below.
		obj.put("messageCount", count(node.getRelationships(IN, Direction.INCOMING)));

		if (deep) {
			JSONArray messages = new JSONArray();
			
			// Note: this seems to produce the messages in the order they were added,
			// which is good enough for the moment:
			Iterable<Relationship> mrs = node.getRelationships(IN, Direction.INCOMING);
			for (Relationship mr : mrs) {
				messages.put(translateMessage(mr.getStartNode()));
			}
			
			obj.put("messages", messages);
		}
		
		return obj;
	}

	public static JSONObject translateMessage(Node node) throws JSONException {
		JSONObject obj = new JSONObject();
		
		obj.put("id", node.getId());
		
		Node topic = node.getSingleRelationship(IN, Direction.OUTGOING).getEndNode();
		obj.put("topic", topic.getProperty("name"));
		
		Node user = node.getSingleRelationship(BY, Direction.OUTGOING).getEndNode();
		obj.put("user", user.getProperty("name"));
		
		Relationship toPrevious = node.getSingleRelationship(REPLY_TO, Direction.OUTGOING);
		if (toPrevious != null) {
			Node previous = toPrevious.getEndNode();
			obj.put("replyTo", previous.getId());
		}
		
		obj.put("text", node.getProperty("text"));
		
		return obj;
	}
	
	
	//
	// Internal:
	//
	
	/** Assuming a transaction is active, find or create a user by name. */
	private Node findOrCreateUser(String name) {
		Node topic = findByName(USER, name);
		
		if (topic == null) {
			topic = graphDb.createNode(USER);
			topic.setProperty("name", name);
		}
		return topic;
	}

	/** Assuming a transaction is active, find or create a topic by name. */
	private Node findOrCreateTopic(String name) {
		Node topic = findByName(TOPIC, name);
		
		if (topic == null) {
			topic = graphDb.createNode(TOPIC);
			topic.setProperty("name", name);
		}
		return topic;
	}

	private static int count(Iterable<?> iterable) {
		int count = 0;
		for (Object elem : iterable) count += 1;
		return count;
	}

	private Node findByName(Label label, String name) {
		// Note: neo4j resource iterators have to be closed after they're used, 
		// hence the somewhat awkward loop.
		ResourceIterable<Node> matchingUsers = graphDb.findNodesByLabelAndProperty(label, "name", name);
		try (ResourceIterator<Node> iter = matchingUsers.iterator()) {
			if (iter.hasNext()) {
				return iter.next();
			}
		}
		return null;
	}

	private boolean dbIsEmpty() {
		try (Transaction tx = graphDb.beginTx()) {
			Iterable<Node> allNodes = GlobalGraphOperations.at(graphDb).getAllNodes();
			return !allNodes.iterator().hasNext();
		}
	}

	// See http://docs.neo4j.org/chunked/stable/tutorials-java-embedded-setup.html#tutorials-java-embedded-setup-startstop
	private void registerShutdownHook() {
	    // Registers a shutdown hook for the Neo4j instance so that it
	    // shuts down nicely when the VM exits (even if you "Ctrl-C" the
	    // running application).
	    Runtime.getRuntime().addShutdownHook(new Thread() {
	        public void run() {
	        	if (graphDb.isAvailable(10)) {
	        		graphDb.shutdown();
	        	}
	        }
	    });
	}
}
