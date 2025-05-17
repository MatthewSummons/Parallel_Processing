package TwentyFourGame.Client.Parsing.AST;

import TwentyFourGame.Client.Parsing.TokenType;

public class BinOp_Node extends AST_Node{
    public final AST_Node left;
    public final TokenType op;
    public final AST_Node right;

    public BinOp_Node(AST_Node left, TokenType op, AST_Node right) {
        if (op != TokenType.ADD && 
            op != TokenType.SUB && 
            op != TokenType.MUL && 
            op != TokenType.DIV
        ) {
            throw new IllegalArgumentException("Invalid operator: " + op);
        } if (op == null || op == TokenType.EOF) {
            throw new IllegalArgumentException("Operator cannot be null or EOF");
        }
        
        this.left = left;
        this.op = op;
        this.right = right;
    }

    @Override
    public String toString() {
        String opStr;
        
        switch (op) {
            case ADD:  opStr = "+"; break;
            case SUB:  opStr = "-"; break;
            case MUL: opStr = "*"; break;
            case DIV:  opStr = "/"; break;
            default:   throw new IllegalStateException("Unexpected value: " + op);
        }
        return "(" + left.toString() + " " + opStr + " " + right.toString() + ")";
    }
}
