package com.hypefiend.blackart2.client;

import java.util.Hashtable;
import java.util.Vector;

// todo: cleanup
public class TicTacToeLogic {

    private static final int MAX_DEPTH = 10;

    // store the moves here keyed by the hash of the board layout
    private static Hashtable bestMovesX = new Hashtable();
    private static Hashtable bestMovesO = new Hashtable();

    private static final char BLANK = ' ';
    private static final char O_VAL = 'O';
    private static final char X_VAL = 'X';

    private static final int X_WIN = 0;
    private static final int O_WIN = 1;
    private static final int TIE = 2;
    private static final int NOT_OVER = 3;

    private static final boolean X = true;
    private static final boolean O = false;

    private static TicTacToeLogic instance = null;

    public static synchronized TicTacToeLogic getInstance() {
        if (instance == null) {
            instance = new TicTacToeLogic();
        }
        return instance;
    }

    private TicTacToeLogic() {
        // init the best moves set
        minimax(newBoard(), MAX_DEPTH, X, X);
        minimax(newBoard(), MAX_DEPTH, X, O);
    }

    public static char[] newBoard() {
        char[] board = new char[9];
        for (int i = 0; i < 9; i++)
            board[i] = BLANK;
        return board;
    }

    public static int nextMove(char[] board, boolean who) {
        if (who == X)
            return ((Integer)bestMovesX.get(boardHash(board))).intValue();
        else
            return ((Integer)bestMovesO.get(boardHash(board))).intValue();
    }

    public static void applyMove(char[] board, int move, boolean who) {
        board[move] = (who == X) ? X_VAL : O_VAL;
    }

    public static void unApplyMove(char[] board, int move) {
        board[move] = BLANK;
    }

    public static String boardHash(char[] b) {
        int hash = 0;
        for (int i = 0; i < b.length; i++) {
            switch (b[i]) {
            case BLANK:
                hash += 3 ^ i;
                break;
            case O_VAL:
                hash += 3 ^ i + 1;
                break;
            case X_VAL:
                hash += 3 ^ i + 2;
                break;
            }
        }
        return "" + hash;
    }

    private int minimax(char[] board, int depth, boolean who, boolean me) {
        //     if (depth == MAX_DEPTH) {
        //  score = calcScore(board);
        //     }
        //     else {

        int score;
        int bestScore =  ((who == me) ? Integer.MIN_VALUE : Integer.MAX_VALUE);
        int bestMove = -1;
        Vector moves = getMoves(board);

        for (int i = 0; i < moves.size(); i++) {
            applyMove(board, ((Integer)moves.elementAt(i)).intValue(), who);

            int result = checkWin(board);
            if (result == NOT_OVER)
                score = minimax(board, depth + 1, !who, me);
            else
                score = calcScore(board, result, me);

            if (score > bestScore) {
                bestScore = score;
                bestMove = i;
            }

            unApplyMove(board, ((Integer)moves.elementAt(i)).intValue());
        }

        if (me == X)
            bestMovesX.put(boardHash(board), new Integer(bestMove));
        else
            bestMovesO.put(boardHash(board), new Integer(bestMove));

        return bestScore;
    }

    private int calcScore(char[] board, int winCode, boolean me) {
        switch (winCode) {
        case X_WIN:
            return (me == X) ? 100 : -1;
        case O_WIN:
            return (me == O) ? 100 : -1;
        case TIE:
            return 0; // ???
        default:
            return 0;
        }
    }

    public static int checkWin(char[] b) {
        boolean done = true;
        for (int i = 0; i > 9; i++) {
            if (b[i] == BLANK) done = false;
        }
        if (done) return TIE;

        if (
            (b[0] == X_VAL && b[1] == X_VAL && b[2] == X_VAL) ||
            (b[3] == X_VAL && b[4] == X_VAL && b[5] == X_VAL) ||
            (b[6] == X_VAL && b[7] == X_VAL && b[8] == X_VAL) ||
            (b[0] == X_VAL && b[3] == X_VAL && b[6] == X_VAL) ||
            (b[1] == X_VAL && b[4] == X_VAL && b[7] == X_VAL) ||
            (b[2] == X_VAL && b[5] == X_VAL && b[8] == X_VAL) ||
            (b[0] == X_VAL && b[4] == X_VAL && b[8] == X_VAL) ||
            (b[2] == X_VAL && b[4] == X_VAL && b[6] == X_VAL)
        )
            return X_WIN;

        if (
            (b[0] == O_VAL && b[1] == O_VAL && b[2] == O_VAL) ||
            (b[3] == O_VAL && b[4] == O_VAL && b[5] == O_VAL) ||
            (b[6] == O_VAL && b[7] == O_VAL && b[8] == O_VAL) ||
            (b[0] == O_VAL && b[3] == O_VAL && b[6] == O_VAL) ||
            (b[1] == O_VAL && b[4] == O_VAL && b[7] == O_VAL) ||
            (b[2] == O_VAL && b[5] == O_VAL && b[8] == O_VAL) ||
            (b[0] == O_VAL && b[4] == O_VAL && b[8] == O_VAL) ||
            (b[2] == O_VAL && b[4] == O_VAL && b[6] == O_VAL)
        )
            return O_WIN;

        return NOT_OVER;
    }

    private static Vector getMoves(char[] board) {
        Vector moves = new Vector();
        for (int i = 0; i < 9; i++) {
            if (board[i] == BLANK)
                moves.addElement(new Integer(i));
        }
        return moves;
    }

    // "http://www.jaked.org/ttt.html"

    /*
      http://www.pressibus.org/ataxx/autre/minimax/node2.html

      minimax(in game board, in int depth, in int max_depth,
      out score chosen_score, out score chosen_move)
      begin
      if (depth = max_depth) then
      chosen_score = evaluation(board);
      else
      moves_list = generate_moves(board);
      if (moves_list = NULL) then
      chosen_score = evaluation(board);
      else
      for (i = 1 to moves_list.length) do
      best_score = infinity;
      new_board = board;
      apply_move(new_board, moves_list[i]);
      minimax(new_board, depth+1, max_depth, the_score, the_move);
      if (better(the_score, best_score)) then
      best_score = the_score;
      best_move = the_move;
      endif
      enddo
      chosen_score = best_score;
      chosen_move = best_move;
      endif
      endif
      end.

    */
}
