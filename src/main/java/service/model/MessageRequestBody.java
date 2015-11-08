package service.model;

/**
 * Created by victorkai on 11/6/15.
 */
public class MessageRequestBody {

    private String username;
    private String text;
    private int timeout = 60;

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setTimeout(int timeout) {
        if(timeout >= 0) {
            this.timeout = timeout;
        }
    }

    public int getTimeout() {
        // don't allow negative timeout values
        if(timeout < 0) {
            timeout = 0;
        }
        return timeout;
    }
}
