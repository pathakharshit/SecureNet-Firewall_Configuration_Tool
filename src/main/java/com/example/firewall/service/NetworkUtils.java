package com.example.firewall.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.firewall.model.FirewallRule;
import com.example.firewall.repository.FirewallRuleRepository;

@Service
public class NetworkUtils {


    public List<FirewallRule> getNetworkConnections() {
        final String ubuntuPassword = "aybikekir%123!";
        List<FirewallRule> rules = new ArrayList<>();

        try {
            String enableCommand = String.format("echo %s | sudo -S ufw enable && echo %s | sudo -S ufw status numbered", ubuntuPassword, ubuntuPassword);
            Process process = executeCommand(enableCommand);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            boolean IsThereRule = false;
            String line;
            while ((line = reader.readLine()) != null) {

                if (line.contains("To")){
                    reader.readLine();
                    while((line = reader.readLine()) != null && line.length()>5){
                        String idPart = line.substring(0, 5);

                        String[] parts = line
                                .substring(5)
                                .trim().
                                replaceAll("\\s{2,}", " ").
                                split(" ");

                        if (parts[parts.length - 1].equals("(out)")){
                            if (parts.length == 5){
                                parseFirewallRule(rules, parts, idPart);
                            } else if (parts.length == 7) { // (v6) and (out)
                                parseFirewallRuleVSix(rules, parts, idPart);
                            }
                        } else if (parts.length == 4) {
                            parseFirewallRule(rules, parts, idPart);
                        } else if (parts.length == 6) { // (v6)
                            parseFirewallRuleVSix(rules, parts, idPart);
                        }
                    }
                    IsThereRule = true;
                    break;
                }
            }

            if(!IsThereRule){
                System.err.println("There is no rule. Implement later.");
            }

            int exitVal = process.waitFor();
            if (exitVal != 0) {
                System.err.println("Error: Command execution failed!");
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
        repository.saveAll(rules);
        return rules;
    }

    /**
     *
     */
    @Autowired
    private FirewallRuleRepository repository;

    public boolean isPortAllowed(int port) {
        List<FirewallRule> rules = repository.findAll();
        for (FirewallRule rule : rules) {
            if (rule.getPort().equals(String.valueOf(port)) && rule.getAction() == FirewallRule.Action.DENY_IN) {
                return false;
            }
        }
        return true;
    }

    private void parseFirewallRule(List<FirewallRule> rules, String[] parts, String idPart){
        int id = Integer.parseInt(idPart
                .replace("[", "")
                .replace("]", "")
                .trim());

        String[] portAndProtocol = parsePortAndProtocol(parts[0]);
        String port = portAndProtocol[0];
        String protocol = portAndProtocol[1];

        String action = parts[1] + " " + parts[2]; // Concatenate "ALLOW IN" or "DENY IN"
        String IpFrom = parts[3];

        rules.add(new FirewallRule((long) id, port, protocol, action, IpFrom));
    }

    private void parseFirewallRuleVSix(List<FirewallRule> rules, String[] parts, String idPart) {
        int id = Integer.parseInt(idPart
                .replace("[", "")
                .replace("]", "")
                .trim());

        String[] portAndProtocol = parsePortAndProtocol(parts[0]);
        String port = portAndProtocol[0];
        String protocol = portAndProtocol[1];

        String action = parts[2] + " " + parts[3]; // Concatenate "ALLOW IN" or "DENY IN"
        String IpFrom = parts[4] + " (v6)";

        rules.add(new FirewallRule((long) id, port, protocol, action, IpFrom));
    }

    private String[] parsePortAndProtocol(String portAndProtocol){
        if (portAndProtocol.contains("/")){
            return portAndProtocol.replace("/", " ").split(" ");
        }else{
            return new String[]{portAndProtocol, null};
        }
    }

//    private void olderCode(BufferedReader reader, String os, long i, List<FirewallRule> rules) throws IOException {
//        String line;
//        while ((line = reader.readLine()) != null) {
//            System.err.println("AAAAAAAA: " + line);
//
//            if (os.contains("win")) {
//                if (!line.contains("LocalAddress") && !line.isEmpty()) {
//                    String[] parts = line.trim().split(",");
//                    if (parts.length >= 6) {
//                        String state = parts[4].replace("\"", "");
//                        if (!state.equalsIgnoreCase("Bound") && !state.equalsIgnoreCase("Listen")) {
//                            String localIp = parts[0].replace("\"", "");
//                            int localPort = Integer.parseInt(parts[1].trim().replace("\"", ""));
//                            String remoteIp = parts[2].replace("\"", "");
//                            int remotePort = Integer.parseInt(parts[3].trim().replace("\"", ""));
//                            String processName = getProcessNameByPid(parts[5].replace("\"", ""));
//                            long startTime = System.currentTimeMillis(); // Zaman damgası için basit bir örnek
//                            i++;
//                            //rules.add(new FirewallRule(i, localIp, localPort, remoteIp, remotePort, "TCP", state, processName, startTime, 0L));
//                        }
//                    }
//                }
//            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
//                if (line.startsWith("tcp") || line.startsWith("udp")) {
//                    String[] parts = line.split("\\s+");
//                    String[] ipPort = parts[4].split(":");
//                    if (ipPort.length == 2) {
//                        String state = parts[6];
//                        if (!state.equalsIgnoreCase("Bound") && !state.equalsIgnoreCase("Listen")) {
//                            String localIp = ipPort[0];
//                            int localPort = Integer.parseInt(ipPort[1].trim());
//                            String remoteIp = parts[5].split(":")[0];
//                            int remotePort = Integer.parseInt(parts[5].split(":")[1]);
//                            i++;
//                            //rules.add(new FirewallRule(i, localIp, localPort, remoteIp, remotePort, parts[0], state, null, System.currentTimeMillis(), 0L));
//                        }
//                    }
//                }
//            }
//        }
//    }

    private Process executeCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);
        Process process = processBuilder.start();
        process.waitFor();
        return process;
    }

    public Optional<FirewallRule> getRuleById(Long id) {
        return repository.findById(id);
    }

    // Change it to: Delete the rule first if exists then add it as new rule
//    public FirewallRule updateConnection(Long id, FirewallRule updatedRule) {
//        return repository.findById(id)
//                .map(existingConnection -> {
//                    existingConnection.setLocalIp(updatedRule.getLocalIp());
//                    existingConnection.setLocalPort(updatedRule.getLocalPort());
//                    existingConnection.setRemoteIp(updatedRule.getRemoteIp());
//                    existingConnection.setRemotePort(updatedRule.getRemotePort());
//                    existingConnection.setProtocol(updatedRule.getProtocol());
//                    existingConnection.setState(updatedRule.getState());
//                    existingConnection.setProcessName(updatedRule.getProcessName());
//                    existingConnection.setDuration(updatedRule.getDuration());
//                    return repository.save(existingConnection);
//                }).orElseThrow(() -> new RuntimeException("Connection not found"));
//    }

    private static String getProcessNameByPid(String pid) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("tasklist", "/FI", "PID eq " + pid);
                Process process = processBuilder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(pid)) {
                        return line.split("\\s+")[0];
                    }
                }
                int exitVal = process.waitFor();
                if (exitVal != 0) {
                    System.err.println("Error: Command execution failed!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (os.contains("nix") || os.contains("nux") || os.contains("mac")){
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("ps", "-p", pid, "-o", "comm=");
                Process process = processBuilder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = reader.readLine();
                if (line != null) {
                    return line.trim();
                }

                int exitVal = process.waitFor();
                if (exitVal != 0) {
                    System.err.println("Error: Command execution failed!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "Bilinmeyen Süreç";
    }
}
