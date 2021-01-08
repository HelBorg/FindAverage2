package ru.spbu.bh;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import lombok.extern.java.Log;
import org.apache.commons.collections4.MapUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log
public class FindAverage extends TickerBehaviour {

    private final int MAX_STEPS = 50;
    private final double EPS = 1e-8;

    public FindAverage(Agent a, long period) {
        super(a, period);
    }

    @Override
    protected void onTick() {
        Map<String, Double> receivedData = receiveData();
        processReceivedData(receivedData, (DefaultAgent) myAgent);
        Double previousState = ((DefaultAgent) this.myAgent).getCurrentValue();
        ((DefaultAgent) this.myAgent).updateNumber();

        double abs = Math.abs(previousState - ((DefaultAgent) this.myAgent).getCurrentValue());
        if (abs < EPS && abs > 0 || getTickCount() > MAX_STEPS) {
            this.stop((DefaultAgent) myAgent);
            return;
        }

        sendData();
    }

    private void sendData() {
        List<String> linkedAgents = ((DefaultAgent) myAgent).getLinkedAgents();
        if (linkedAgents == null) {
            return;
        }

        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        linkedAgents.forEach(agent -> {
                message.addReceiver(new AID(agent, AID.ISLOCALNAME));
        });

        Map<String, Double> contentToSend = new HashMap<String, Double>() {{
            double randomAdd = 2 * Math.random() - 1;
            put(myAgent.getLocalName(), ((DefaultAgent) myAgent).getCurrentValue() + randomAdd);
        }};

        try {
            message.setContentObject((Serializable) contentToSend);
        } catch (Exception e) {
            log.warning("Unable to generate message: " + contentToSend);
            e.printStackTrace();
        }

        myAgent.send(message);
    }

    private Map<String, Double> receiveData() {
        Map<String, Double> receivedData = new HashMap<>();

        ACLMessage msgRes = myAgent.receive();
        while (msgRes != null) {
            try {
                Object receivedContent = msgRes.getContentObject();
                if (receivedContent instanceof Map) {
                    receivedData.putAll((Map) receivedContent);
                }
            } catch (UnreadableException e) {
                this.stop((DefaultAgent) this.myAgent);
            } catch (Exception e) {
                log.warning("Invalid message content in received message" + msgRes);
            }
            msgRes = myAgent.receive();
        }
        return receivedData;
    }

    private void processReceivedData(Map<String, Double> content, DefaultAgent agent) {
        if (MapUtils.isEmpty(content)) {
            return;
        }

        content.forEach(agent::addAgentNum);
    }

    private void stop(DefaultAgent currentAgent) {
        if (currentAgent.getName().contains("Main")) {
            System.out.println("\n  AVG - " + currentAgent.getCurrentValue() + "\n");
        }

        currentAgent.doDelete();
        this.stop();
    }
}
