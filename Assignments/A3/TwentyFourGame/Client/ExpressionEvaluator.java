package TwentyFourGame.Client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import TwentyFourGame.Client.Parsing.Interpreter;
import TwentyFourGame.Common.GameOverMessage;

public class ExpressionEvaluator implements ActionListener {
            
    private final String username;
    private JTextField expressionField;
    private JLabel resultLabel;
    private ArrayList<Integer> allowed;
    private final GameQueueSender sender;

    public ExpressionEvaluator(
        String username, JTextField expressionField, JLabel resultLabel, String[] allowed, GameQueueSender sender
    ) {
        this.username = username;
        this.expressionField = expressionField;
        this.resultLabel = resultLabel;
        this.sender = sender;
        this.allowed = new ArrayList<>();
        for (String card : allowed) {
            switch(card.charAt(0)) {
                case 'A': this.allowed.add(1);  break;
                case 'J': this.allowed.add(11); break;
                case 'Q': this.allowed.add(12); break;
                case 'K': this.allowed.add(13); break;
                case '1': if (card.charAt(1) == '0') {this.allowed.add(10); break;} // Handle 10 as a special case 
                default:  this.allowed.add(Integer.parseInt(card.substring(0, 1))); break;
            }
        }

        // Print the allowed cards for debugging
        System.out.println("Allowed cards: ");
        for (Integer card : this.allowed) {
            System.out.print(card + " ");
        } System.out.println();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String expression = expressionField.getText();
        if (Interpreter.checkTwentyFour(expression, allowed)) {
            resultLabel.setText(" = 24");
            // Send JMS message to join game queue
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    sender.sendGameOverMessage(new GameOverMessage(username, expression));
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get(); // Check for exceptions
                    } catch (Exception ex) {
                        System.err.println("Failed to send game over message: " + ex.getMessage());
                        // Notification.showError("Failed to communicate with server " + ex.getMessage(), parentFrame);
                    }
                }
            }; worker.execute();
        } else {
            resultLabel.setText(" != 24");
        } 
    }

}