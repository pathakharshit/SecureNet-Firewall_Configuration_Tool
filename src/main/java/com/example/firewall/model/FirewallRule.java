package com.example.firewall.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rules")
@Data
@NoArgsConstructor
public class FirewallRule {

    enum Protocol{
        TCP,
        UDP
    }

    public enum Action {
        ALLOW_IN("Allow In"),
        ALLOW_OUT("Allow Out"),
        DENY_IN("Deny In"),
        DENY_OUT("Deny Out");

        private final String displayName;

        Action(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }


    @Id
    private Long id;

    @Column()
    private String port;

    @Column()
    private Protocol protocol;

    @Column(nullable = false)
    private Action action;

    @Column()
    private String ipFrom;

    public FirewallRule(Long id, String port, String protocol, String action, String ipFrom) {
        this.id = id;
        this.port = port;
        setProtocol(protocol);
        setAction(action);
        this.ipFrom = ipFrom;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        if (protocol != null) {
            switch (protocol.toUpperCase()){
                case "TCP":
                    this.protocol = Protocol.TCP;
                    break;
                case "UDP":
                    this.protocol = Protocol.UDP;
                    break;

            }
        }else{
            this.protocol = null;
        }
    }

    public Action getAction() {
        return action;
    }

    public void setAction(String action) {
        switch (action.toUpperCase()){
            case "ALLOW IN":
                this.action = Action.ALLOW_IN;
                break;
            case "ALLOW OUT":
                this.action = Action.ALLOW_OUT;
                break;
            case "DENY IN":
                this.action = Action.DENY_IN;
                break;
            case "DENY OUT":
                this.action = Action.DENY_OUT;
                break;
        }
    }

    public String getIpFrom() {
        return ipFrom;
    }

    public void setIpFrom(String ipFrom) {
        this.ipFrom = ipFrom;
    }
}
