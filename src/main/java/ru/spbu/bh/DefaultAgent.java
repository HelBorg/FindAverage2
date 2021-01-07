package ru.spbu.bh;


import jade.core.Agent;
import lombok.Data;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Data
public class DefaultAgent extends Agent {

    private List<String> linkedAgents;
    private Double currentValue;

    private Map<String, Double> agentToNum;

    private Map<Integer, Double> init = new HashMap<Integer, Double>() {{
        put(1, 8.0);
        put(2, 22.0);
        put(3, 15.0);
        put(4, 5.0);
        put(5, 0.0);
    }};

    @Override
    protected void setup() {
        int id = getId();

        Object[] arguments = getArguments();
        if (arguments != null && arguments[0] instanceof String[]) {
            linkedAgents = Arrays.asList((String[]) arguments[0]);
        } else {
            linkedAgents = new ArrayList<>();
        }

        currentValue = init.get(id);

        agentToNum = new HashMap<>();

        System.out.println("Agent " + id + " with random number " + currentValue);

        addBehaviour(new FindAverage(this, TimeUnit.SECONDS.toMillis(1)));
    }

    public int getId() {
        return Integer.parseInt(getAID().getLocalName().substring(0, 1));
    }

    public void addAgentNum(String sender, Double senderNumber) {
        agentToNum.put(sender, senderNumber);
    }

    public void updateNumber() {
        double alpha = 1 / (1 + (float) this.linkedAgents.size());
        double sum = agentToNum.values()
                .stream()
                .mapToDouble(value -> alpha * (value - currentValue))
                .sum();
        currentValue = currentValue + sum;
    }

}
