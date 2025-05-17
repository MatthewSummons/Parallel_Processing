package TwentyFourGame.Client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import TwentyFourGame.Client.Parsing.Interpreter;

public class ExpressionEvaluator implements ActionListener {
            
    private JTextField expressionField;
    private JLabel resultLabel;
    private ArrayList<Integer> allowed;

    public ExpressionEvaluator(
        JTextField expressionField, JLabel resultLabel, String[] allowed
    ) {
        this.expressionField = expressionField;
        this.resultLabel = resultLabel;
        this.allowed = new ArrayList<>();
        for (String card : allowed) {
            switch(card.charAt(0)) {
                case 'A': this.allowed.add(1); break;
                case 'J': this.allowed.add(11); break;
                case 'Q': this.allowed.add(12); break;
                case 'K': this.allowed.add(13); break;
                default:  this.allowed.add(Integer.parseInt(card.substring(0, 1))); break;
            }
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String expression = expressionField.getText();
        if (Interpreter.checkTwentyFour(expression, allowed)) {
            resultLabel.setText(" = 24");
        } else {
            resultLabel.setText(" != 24");
        } 
        // expressionField.setText(""); // Clear the field after submission
    }

}