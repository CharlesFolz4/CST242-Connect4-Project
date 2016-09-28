package connectfour.analysis;

import java.util.ArrayList;


/**
 * Node class represents a node on a tree of board positions that could result from the current state of the board, along with a value determined by the minimax algorithm, and the move required to reach the given node.
 * 
 * @author Folz
 *
 */
public class Node {
	private Node parent;
	private int moveData;
	private short[] boardData;
	private boolean isRedTurn;
	private byte[] columnCount;
	private double nodeValue;
	private boolean isTerminal;
	private int plyToEnd;
	private ArrayList<Node> children;
	
	/**
	 * This constructor used only for generating the root node of the tree.
	 * 
	 * @param board Initial state of the board for the entire tree.
	 */
	public Node(Board board){
		this.boardData = compressBoard(board);
		children = new ArrayList<Node>();
		isTerminal = Math.abs(board.getValue()) > 1_000;
	}
	
	/**
	 * Create new node on the tree connected to the given parent node.
	 * 
	 * @param parent Node to be this node's parent node.
	 * @param int Move required to reach this node's state of the board.
	 */
	public Node(Node parent, int move){
		this.parent = parent;
		this.moveData = move;
		Board temp = new Board(parent.getBoardData(), this.moveData);
		isTerminal = Math.abs(temp.getValue()) > 1_000;
		this.boardData = compressBoard(temp);
		children = new ArrayList<Node>();
	}
	
	/**
	 * A method to compress a Board object into a much smaller and memory-friendly representation for storage
	 * 
	 * @param board board to compress
	 * @return compressed form of the board
	 */
	private short[] compressBoard(Board board){
		isRedTurn = board.isRedTurn();
		columnCount = board.getColumnCount();
		short[] compressedBoardData = new short[6];
		short row = 0;
		
		for(int r = 0; r < Board.NUM_ROWS; ++r){
			byte[] squares = board.getBoard()[r];
			
			for(int c = 0; c < Board.NUM_COLUMNS; ++c){
				row <<= 2;
				
				switch(squares[c]){
					case  1: row += (short)1; break;
					case -1: row += (short)2; break; 
					default: row += (short)0; break;
				}
				
			}
			compressedBoardData[r] = row;
		}
		
		
		return compressedBoardData;
	}
	
	/**
	 * A  method to decompress boards that were compressed for storage
	 * 
	 * @param board board to decompress
	 * @return decompressed board
	 */
	private Board decompressBoard(short[] board){
		Board tempBoard = new Board();

		for(int r = 0; r < Board.NUM_ROWS; ++r){
			short row = board[(Board.NUM_ROWS-1)-r];
			byte squareByte = 0;
			
			for(int c = 0; c < Board.NUM_COLUMNS; ++c){
				squareByte = (byte)row;
				squareByte <<= 6;
				squareByte >>= 6;
				squareByte &=  3;
				
				int temp;
				switch(squareByte){
					case  1: temp =  1; break;
					case  2: temp = -1; break;
					default: temp =  0; break;
				}
				tempBoard.getBoard()[(Board.NUM_ROWS-1)-r][(Board.NUM_COLUMNS-1)-c] = (byte)temp;
				row >>= 2;
			}
			
		}
		tempBoard.setRedTurn(isRedTurn);
		tempBoard.setColumnCount(columnCount);
		
		return tempBoard;
	}
	
	
	/**
	 * Uses the minimax theorem to generate a value of the current state of the board based on minimizing the maximum possible loss of all future legal states of the board 
	 * 
	 * @return Calculated value of the selected node according to the minimax theorem.
	 */
	public double getMinimaxValue() {
		//if at leaf
		if(this.getChildren().isEmpty()){
			nodeValue = this.getBoardData().getValue();
		} else if(this.getBoardData().isRedTurn()){ //possible difference
			nodeValue = this.getChild(0).getMinimaxValue();
			
			for(Node n: this.getChildren()){
				double nValue = n.getMinimaxValue();
				if(nodeValue < nValue){ 
					nodeValue = nValue;
				}
			}
		} else {
			nodeValue = this.getChild(0).getMinimaxValue();
			
			for(Node n:this.getChildren()){
				double nValue = n.getMinimaxValue();
				if(nodeValue > nValue){
					nodeValue = nValue;
				}
			}
		}
		
		return nodeValue;
	}
	
	/**
	 * Generates the particular sequence of moves required to reach the selected node.
	 * 
	 * @return Sequence of moves required to reach the selected node.
	 */
	public String routeToString(){
		String str = "";
		try{
			str += parent.routeToString();
		} catch (NullPointerException ex){
			//at root
		}
		str += " -> " + moveData;
		return str;
	}
	
	
	
	public Node getParent(){
		return parent;
	}
	
	public int getMoveData(){
		return moveData;
	}
	
	public Board getBoardData(){
		return decompressBoard(boardData);
	}
	
	public boolean isRedTurn(){
		return isRedTurn;
	}
	
	public double getNodeValue(){
		return nodeValue;
	}
	
	public boolean isTerminal() {
		return isTerminal;
	}
	
	public int getPlyToEnd() {
		return plyToEnd;
	}
	
	public ArrayList<Node> getChildren(){
		return children;
	}
	
	public Node getChild(int index){
		return children.get(index);
	}
	
	public void addChild(Node child){
		children.add(child);
	}
	
	public void setNodeValue(double nodeValue){
		this.nodeValue = nodeValue;
	}
	
	public void setTerminal(boolean isTerminal) {
		this.isTerminal = isTerminal;
	}
	
	
	
}
