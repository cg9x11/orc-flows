package asia.noe.flows;

import lombok.Getter;

@Getter
public class RabbitMQCredential {
    private final String host;
    private final int port;
    private final String username;
    private final String password;

    public RabbitMQCredential(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }
}
