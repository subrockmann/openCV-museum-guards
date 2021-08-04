package gr.georkouk.theguardaians.models;

public class Settings {

    private String serverURI;

    private String clientID;

    private String username;

    private String password;

    private String topic;


    public Settings() {
        this.clientID = "";
        this.serverURI = "";
        this.username = "";
        this.password = "";
        this.topic = "";
    }

    public String getServerURI() {
        return serverURI;
    }

    public void setServerURI(String serverURI) {
        this.serverURI = serverURI;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

}
