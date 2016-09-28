package connectfour.analysis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Board class contains a representation of the connect four board, all current legal moves, the calculated value of the board, and whose turn it is.
 * 
 * 
 * @author Folz
 *
 */
public class Board implements Serializable{
	public static final int NUM_COLUMNS = 7;
	public static final int NUM_ROWS = 6;
	private byte[][] board; //row, column
	private byte[] columnCount;
	private boolean isRedTurn; //colors are red and yellow, red goes first
	private boolean legalColumns[]; //keeps track of what columns it is legal to make a move on
	private double value;
	
	/**
	 * Default constructor creates an empty board with red going first.
	 * 
	 */
	public Board(){
		isRedTurn    = true;
		board        = new byte[NUM_ROWS][NUM_COLUMNS];
		columnCount  = new byte[NUM_COLUMNS];
		legalColumns = generateLegalColumns();
		value        = this.evaluateScore();
	}
	
	/**
	 * This constructor makes a move on a new board.
	 * 
	 * @param oldBoard The board to make the move on.
	 * @param move The move to make on the given board.
	 */
	public Board(Board oldBoard, int moveColumn){
		byte[][] temp = new byte[NUM_ROWS][];
		for(int r = 0; r < NUM_ROWS; ++r){
			temp[r] = Arrays.copyOf(oldBoard.getBoard()[r], NUM_COLUMNS);
		}
		this.board        = temp;
		this.columnCount  = Arrays.copyOf(oldBoard.columnCount, NUM_COLUMNS);
		this.isRedTurn    = !oldBoard.isRedTurn;
		
		
		board[columnCount[moveColumn]][moveColumn] = (byte)(oldBoard.isRedTurn? 1:-1);
		columnCount[moveColumn]++;
		
		
		legalColumns = generateLegalColumns();
		value = this.evaluateScore();
	}
	
	/**
	 * This constructor creates a clone of the board.
	 * 
	 * @param oldBoard Board to clone.
	 */
	public Board(Board oldBoard){
		byte[][] temp = new byte[NUM_ROWS][];
		for(int r = 0; r < NUM_ROWS; ++r){
			temp[r] = Arrays.copyOf(oldBoard.getBoard()[r], NUM_COLUMNS);
		}
		
		this.board        = temp;
		this.columnCount  = Arrays.copyOf(oldBoard.columnCount, NUM_COLUMNS);
		this.isRedTurn    = oldBoard.isRedTurn;
		this.legalColumns = generateLegalColumns();
		this.value        = this.evaluateScore();
	}
	

	
	/**
	 * Generates all the columns upon which a legal move can be made
	 * 
	 * @return boolean array indicating which columns have a legal move
	 */
	private boolean[] generateLegalColumns() {
		boolean[] temp = new boolean[NUM_COLUMNS];
		
		for(int i = 0; i < NUM_COLUMNS; ++i){
			temp[i] = columnCount[i] < NUM_COLUMNS-1;
		}
		
		return temp;
	}
	
	/**
     * Calculates a value for the board which represents which side has how much of an advantage.  Positive scores favor white, negative scores favor black, and the higher the absolute value of the score, the greater the advantage.
     * 
     * @return Calculated value of the board
     */
	public double evaluateScore(){
		double score = 0;

		double[][] squareModifier = {
				{0.917, 1.000, 1.050, 1.107, 1.050, 1.000, 0.917 },
				{1.000, 1.083, 1.125, 1.150, 1.125, 1.083, 1.000},
				{1.050, 1.125, 1.159, 1.173, 1.159, 1.125, 1.050},
				{1.050, 1.125, 1.159, 1.173, 1.159, 1.125, 1.050},
				{1.000, 1.083, 1.125, 1.150, 1.125, 1.083, 1.000},
				{0.917, 1.000, 1.050, 1.107, 1.050, 1.000, 0.917}
		}; //weighted chance of an individual square being involved in the winning group of 4, idea modified from http://programmers.stackexchange.com/questions/263514/why-does-this-evaluation-function-work-in-a-connect-four-game-in-java
		
		for(int c = 0; c < NUM_COLUMNS; ++c){
			for(int r = 0; r < NUM_ROWS; ++r){
				if(board[r][c] != 0){
					score += lookForWin(r,c)? 10_000 * board[r][c] : board[r][c] * squareModifier[r][c];
				}
			}
		}
		return score;
	}
	
	public boolean lookForWin(int r, int c){
		boolean isWin = false;
		//horizontal
		if(c < 4){
			for(int i = 1; i < 4; ++i){
				if(board[r][c] == board[r][c+i]){
					isWin |= i == 3;
				}else{
					break;
				}
			}
		}
		//vertical
		if(r < 3){
			for(int i = 1; i < 4; ++i){
				if(board[r][c] == board[r+i][c]){
					isWin |= i == 3;
				}else{
					break;
				}
			}
		}

		//diagonal A
		if(r < 3 && c < 4){
			for(int i = 1; i < 4; ++i){
				if(board[r][c] == board[r+i][c+i]){
					isWin |= i == 3;
				}else{
					break;
				}
			}
		}
		
		//diagonal B
		if(r < 3 && c > 2){
			for(int i = 1; i < 4; ++i){
				if(board[r][c] == board[r+i][c-i]){
					isWin |= i == 3;
				}else{
					break;
				}
			}
		}
		return isWin;
	}
	
	/**
     * Method that prints the current state of the board to the console.
     */
	public void printBoard(){
		String str = "  0    1    2    3    4    5    6    \n";
		
		for(int i = 0; i < 13; ++i){
			if(i%2 == 0){
				str += "+----+----+----+----+----+----+----+ \n";
			} else {
				for(int j = 0; j < 7; ++j){ //make line
					str += "| ";
					if(board[(13-i)/2 -1][j] == 0){ //square is empty
						str += "   ";
					} else if(board[(13-i)/2 -1][j] == 1){ //square is red
						str += "RR ";
					} else if(board[(13-i)/2 -1][j] == -1){ //square is yellow
						str += "YY ";
					} else {
						str += board[(13-i)/2 -1][j];
					}
				}
				str += "|\n";
			}
		}
		str += "  0    1    2    3    4    5    6    \n";
		
		System.out.println(str);
	}
	
	public byte[][] getBoard() {
		return board;
	}
	
	public int getNumOfMoves(){
		int sum = 0;
		for(int c : columnCount){
			sum += c;
		}
		return sum;
	}
	
	public byte getColumnCountAt(int column){
		return columnCount[column];
	}
	
	public byte[] getColumnCount() {
		return columnCount;
	}
	
	public boolean[] getLegalColumns(){
		return generateLegalColumns();
	}
	
	public boolean isRedTurn() {
		return isRedTurn;
	}
	
	public void setRedTurn(boolean isRedTurn) {
		this.isRedTurn = isRedTurn;
	}

	public double getValue() {
		return evaluateScore();
	}

	public ArrayList<Integer> generateMoveList() {
		ArrayList<Integer> moves = new ArrayList<Integer>();
		legalColumns = generateLegalColumns();
		for(int i = 0; i < NUM_COLUMNS; ++i){
			if(legalColumns[i]){
				moves.add(i);
			}
		}
		return moves;
	}

	public void setColumnCount(byte[] columnCount) {
		this.columnCount = columnCount;
	}
	
}
