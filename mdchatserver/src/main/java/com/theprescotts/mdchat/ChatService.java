package com.theprescotts.mdchat;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Chat service methods, all in one class for the sake of brevity.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class ChatService {
	// Note: in a real application, would want to have this dependency injected,
	// but in the interest of simplicity, here's a static reference.
	// The point is, there's a single instance of ChatData somewhere because 
	// only one database instance can be active at a time, and instances of this
	// service class will be created for each request.
	private final ChatData data = ChatData.instance;
	
	/** Allows access to the request URL. */
	@Context HttpServletRequest request;
	
	/** A simple index object, linking to all the available data. */
	@GET
	public JSONObject root() throws JSONException, URISyntaxException {
		JSONObject obj = new JSONObject();
		obj.put("topics", formatURL("topics"));
		obj.put("users", formatURL("users"));
		return obj;
	}
	
	/** Index of all users. */
	@GET
	@Path("users")
	public JSONArray getUsers() throws JSONException {
		JSONArray users = new JSONArray();
		for (JSONObject user : data.getUsers()) {
			addLink(user, "users");
			users.put(user);
		}
		return users;
	}
	
	/** Get/create a user by name. */
	@GET
	@Path("users/{name}")
	public JSONObject getUser(@PathParam("name") String name) throws JSONException {
		JSONObject user = data.getUser(name);
		addLink(user, "users");
		return user;
	}
	
	/** Index of all topics. */
	@GET
	@Path("topics")
	public JSONArray getTopics() throws JSONException {
		JSONArray topics = new JSONArray();
		for (JSONObject topic : data.getTopics()) {
			addLink(topic, "topics");
			topics.put(topic);
		}
		return topics;
	}
	
	/** Get/create a topic by name. */
	@GET
	@Path("topics/{name}")
	public JSONObject getTopic(@PathParam("name") String name) throws JSONException {
		JSONObject topic = data.getTopic(name);
		addLink(topic, "topics");
		return topic;
	}

	/**
	 * Create a new message by POSTing to the topic's URI. This is _not_
	 * idempotent, so should probably redirect.
	 */
	@POST
	@Path("topics/{name}")
	public JSONObject postMessage(@PathParam("name") String topic, 
									@QueryParam("user") String user,
									@QueryParam("text") String text,
									@QueryParam("replyTo") Long replyTo)
			throws JSONException
	{
		return data.addMessage(topic, user, text, replyTo);
	}

	/**
	 * Alternative request for creating messages, using GET so you can use it 
	 * easily in a browser.
	 * 
	 * @see #postMessage(String, String, String, Long)
	 */
	@GET
	@Path("post")
	public JSONObject postMessageViaGET(@QueryParam("topic") String topic, 
										@QueryParam("user") String user,
										@QueryParam("text") String text,
										@QueryParam("replyTo") Long replyTo)
			throws JSONException
	{
		return data.addMessage(topic, user, text, replyTo);
	}
	
	//
	// Internal:
	//
	
	private void addLink(JSONObject obj, String path) throws JSONException {
		try {
			String uri = formatURL(path, obj.getString("name"));
			obj.put("uri", uri);
		}
		catch (URISyntaxException usx) {
			System.err.println("Error formatting link: " + usx);
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	private String formatURL(String path) throws URISyntaxException {
		URI uri = new URI(request.getRequestURL().toString());
		return uri.resolve("/" + path).toString();
	}
	
	private String formatURL(String path, String id) throws URISyntaxException {
		URI uri = new URI(request.getRequestURL().toString());
		return uri.resolve("/" + path + "/" + id).toString();
	}
}