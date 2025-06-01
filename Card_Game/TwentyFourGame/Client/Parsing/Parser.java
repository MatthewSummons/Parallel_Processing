package TwentyFourGame.Client.Parsing;

import java.util.ArrayList;

import TwentyFourGame.Client.Parsing.AST.*;

public class Parser {
    private ArrayList<Token> tokens;
    private int position;
    private Token currentToken;

    public Parser() {
        this.position = 0;
    }

    public AST_Node parse(String input) {
        
        try {
            this.tokens = new Lexer(input).tokenize();
        } catch (IllegalArgumentException e) {
            System.err.println("Syntax error: " + e.getMessage());
            this.tokens = new ArrayList<>();
        }

        this.position = 0;
        this.currentToken = tokens.get(0);
        
        AST_Node expr = expression();
        
        if (currentToken.getType() != TokenType.EOF) {
            throw new IllegalStateException("Unexpected token: " + currentToken);
        }
        
        return expr;
    }
    
    private void advance() {
        position++;
        if (position < tokens.size()) {
            currentToken = tokens.get(position);
        } else {
            currentToken = new Token(TokenType.EOF, null);
        }
    }
    
    private void eat(TokenType tokenType) {
        if (currentToken.getType() == tokenType) {
            advance();
        } else {
            throw new IllegalStateException(
                "Expected " + tokenType + ", found " + currentToken.getType());
        }
    }
    
    // Grammar rule: expression -> term ((ADD|SUB) term)*
    private AST_Node expression() {
        AST_Node node = term();
        
        while (currentToken.getType() == TokenType.ADD || 
               currentToken.getType() == TokenType.SUB) {
            TokenType operator = currentToken.getType();
            eat(operator);
            node = new BinOp_Node(node, operator, term());
        }
        
        return node;
    }
    
    // Grammar rule: term -> factor ((MUL|DIV) factor)*
    private AST_Node term() {
        AST_Node node = factor();
        
        while (currentToken.getType() == TokenType.MUL || 
               currentToken.getType() == TokenType.DIV) {
            TokenType operator = currentToken.getType();
            eat(operator);
            node = new BinOp_Node(node, operator, factor());
        }
        
        return node;
    }
    
    // Grammar rule: factor -> NUMBER | LPAREN expression RPAREN
    private AST_Node factor() {
        Token token = currentToken;
        
        if (token.getType() == TokenType.NUMBER) {
            eat(TokenType.NUMBER);
            return new Int_Node((Integer) token.getValue());
        } else if (token.getType() == TokenType.LPAREN) {
            eat(TokenType.LPAREN);
            AST_Node node = expression();
            eat(TokenType.RPAREN);
            return node;
        } else {
            throw new IllegalStateException("Unexpected token: " + token);
        }
    }

    // Example usage of Parser
    public static void main(String[] args) {
        // Create a new Parser object
        Parser parser = new Parser();

        // Parse a sample expression
        String input = "(3 + (5 . 10)) * (J - Q) / q";
        try {
            AST_Node ast = parser.parse(input);
            System.out.println("AST: " + ast);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

