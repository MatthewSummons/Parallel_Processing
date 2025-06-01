package TwentyFourGame.Client.Parsing;

import java.util.ArrayList;

import TwentyFourGame.Client.Parsing.AST.AST_Node;
import TwentyFourGame.Client.Parsing.AST.BinOp_Node;
import TwentyFourGame.Client.Parsing.AST.Int_Node;

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

    private static double evaluate(AST_Node ast) {
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

    public static boolean checkTwentyFour(String input, ArrayList<Integer> allowed) {
        Parser parser = new Parser();
        AST_Node ast = parser.parse(input);

        if (ast == null) {
            System.err.println("Invalid expression: " + input);
            return false; // Invalid expression
        }

        ArrayList<Integer> result = validate(allowed, ast);
        if (result.isEmpty()) {
            double evaluationResult = evaluate(ast);
            System.out.println("Evaluation Result: " + evaluationResult);
            return (Math.abs(evaluationResult - 24) < 1e-6);
        } else {
            System.err.println("Unused or invalid values: " + result);
            return false;
        }
    }
}
