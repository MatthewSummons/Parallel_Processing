package Parsing;

import Parsing.AST.AST_Node;
import Parsing.AST.BinOp_Node;
import Parsing.AST.Int_Node;

import java.util.ArrayList;

public class Interpreter {

    /*
     * Checks if the AST uses all the values in the allowed list exactly once.
     * Return an ArrayList of integers that are not used in the AST.
     * A negative value indicates that the value was attempted to be used (but not
     * allowed). An empty list indicates that all values were used.
     */
    private static ArrayList<Integer> validate(ArrayList<Integer> allowed, AST_Node ast) {
        allowed = new ArrayList<>(allowed);
        if (ast instanceof Int_Node) {
            int node_value = ((Int_Node) ast).value;
            if (allowed.contains(node_value)) {
                allowed.remove(allowed.indexOf(node_value));
                return allowed;
            } else {
                allowed.add(-node_value); // Return value that was attempted to be used
                return allowed;
            }
        } else if (ast instanceof BinOp_Node) {
            BinOp_Node binOpNode = (BinOp_Node) ast;
            // Validating the children is commutative
            ArrayList<Integer> leftChecked = validate(new ArrayList<>(allowed), binOpNode.left);;
            return validate(new ArrayList<>(leftChecked), binOpNode.right);
        } else {
            throw new IllegalStateException("Unknown AST node type: " + ast.getClass());
        }
    }

    public double evaluate(AST_Node ast) {
        if (ast instanceof Int_Node) {
            return ((Int_Node) ast).value;
        } else if (ast instanceof BinOp_Node) {
            BinOp_Node binOpNode = (BinOp_Node) ast;
            double left = evaluate(binOpNode.left);
            double right = evaluate(binOpNode.right);
            switch (binOpNode.op) {
                case ADD: return left + right;
                case SUB: return left - right;
                case MUL: return left * right;
                case DIV: return left / right;
                default:
                    throw new IllegalStateException("Unexpected operator: " + binOpNode.op);
            }
        } else {
            throw new IllegalStateException("Unknown AST node type: " + ast.getClass());
        }
    }

    public static void main(String[] args) {
        AST_Node ast = new Parser().parse("J + Q * (k - J) + J + J + 3");
        ArrayList<Integer> allowed = new ArrayList<>();
        allowed.add(11);
        allowed.add(12);
        allowed.add(11);
        allowed.add(13);

        System.out.println(validate(allowed, ast));

        Interpreter interpreter = new Interpreter();
        double result = interpreter.evaluate(ast);
        System.out.println("Result: " + result);
    }
}
