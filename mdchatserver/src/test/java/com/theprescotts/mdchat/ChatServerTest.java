package com.theprescotts.mdchat;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.header.MediaTypes;

public class ChatServerTest {

    private SelectorThread threadSelector;
    
    private WebResource r;

    @Before
    public void setUp() throws Exception {
    	// Always start with a clean DB: 
    	ChatData.instance.clear();
    	
    	threadSelector = Main.startJerseyServer();

        Client c = Client.create();
        r = c.resource(Main.BASE_URI);
    }

    @After
    public void tearDown() throws Exception {
        threadSelector.stopEndpoint();
    }

    @Test
    public void noUsers() {
        JSONArray response = r.path("users").get(JSONArray.class);
        assertThat(response.length(), equalTo(0));
    }

    /** Getting a user by name is enough to add it to the DB. */
    @Test
    public void addUser() throws JSONException {
        JSONObject response1 = r.path("users/moss").get(JSONObject.class);
        assertThat(response1.getString("name"), equalTo("moss"));

        JSONArray response2 = r.path("users").get(JSONArray.class);
        assertThat(response2.length(), equalTo(1));
        assertThat(response2.getJSONObject(0).getString("name"), equalTo("moss"));
   }

    @Test
    public void noTopics() {
        JSONArray response = r.path("topics").get(JSONArray.class);
        assertThat(response.length(), equalTo(0));
    }

    /** Getting a topic by name is enough to add it to the DB. */
    @Test
    public void addTopic() throws JSONException {
        JSONObject response1 = r.path("topics/pets").get(JSONObject.class);
        assertThat(response1.getString("name"), equalTo("pets"));

        JSONArray response2 = r.path("topics").get(JSONArray.class);
        assertThat(response2.length(), equalTo(1));
        JSONObject topic = response2.getJSONObject(0);
		assertThat(topic.getString("name"), equalTo("pets"));
        assertThat(topic.has("messages"), equalTo(false));
    }
    
    @Test
    public void topicsSorted() throws JSONException {
    	// Create some topics in a random order:
        r.path("topics/pets").get(JSONObject.class);
        r.path("topics/bikes").get(JSONObject.class);
        r.path("topics/igloos").get(JSONObject.class);

        // Make sure they come back sorted by name:
        JSONArray response2 = r.path("topics").get(JSONArray.class);
        assertThat(response2.length(), equalTo(3));
        assertThat(response2.getJSONObject(0).getString("name"), equalTo("bikes"));
        assertThat(response2.getJSONObject(1).getString("name"), equalTo("igloos"));
        assertThat(response2.getJSONObject(2).getString("name"), equalTo("pets"));
    }
    
    @Test
    public void postOnce() throws JSONException {
    	// Note: no need to create the user and topic before posting.
    	
		JSONObject response1 = r.path("topics/pets")
    		.queryParam("user", "moss")
    		//.queryParam("replyTo", )  // TODO
    		.queryParam("text", "Who likes pets?")
    		.post(JSONObject.class);
        
        assertThat(response1.getString("user"), equalTo("moss"));
        assertThat(response1.has("replyTo"), equalTo(false));
        
        JSONObject response2 = 
        		r.path("topics/pets")
        			.get(JSONObject.class);
        
        JSONArray messages = response2.getJSONArray("messages");
        assertThat(messages.length(), equalTo(1));
        
        JSONObject msg = messages.getJSONObject(0);
        assertThat(msg.getString("text"), equalTo("Who likes pets?"));
		assertThat(msg.getLong("id"), equalTo(response1.getLong("id")));
    }

    /**
     * Same as the above, but using GET, which is easier to experiment with 
     * in a browser.
     */
    @Test
    public void postViaGET() throws JSONException {
    	// Note: no need to create the user and topic before posting.
    	
        JSONObject response1 = 
        		r.path("post")
	        		.queryParam("topic", "pets")
	        		.queryParam("user", "moss")
	        		//.queryParam("replyTo", )  // TODO
	        		.queryParam("text", "Who likes pets?")
	        		.get(JSONObject.class);
        
        assertThat(response1.getString("user"), equalTo("moss"));
        assertThat(response1.has("replyTo"), equalTo(false));
        
        JSONObject response2 = 
        		r.path("topics/pets")
        			.get(JSONObject.class);
        
        JSONArray messages = response2.getJSONArray("messages");
        assertThat(messages.length(), equalTo(1));
        
        JSONObject msg = messages.getJSONObject(0);
		assertThat(msg.getLong("id"), equalTo(response1.getLong("id")));
    }

    /**
     * Test if a WADL document is available at the relative path
     * "application.wadl".
     * <p>
     * This isn't good for much except visually checking that the Path 
     * annotations are doing what you expect.
     */
    @Test
    public void wadl() {
        String serviceWadl = r.path("application.wadl").
                accept(MediaTypes.WADL).get(String.class);
           
        System.out.println(serviceWadl);
        assertThat(serviceWadl.length(), greaterThan(0));
    }
}
