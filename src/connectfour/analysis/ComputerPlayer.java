package connectfour.analysis;

import java.util.ArrayList;

/**
 * A set of algorithms for playing Connect Four.
 * 
 * @author Folz
 *
 */
public class ComputerPlayer{
	private boolean isRed;
	
	public ComputerPlayer(boolean isRed){
		this.isRed = isRed;
	}
	
	/**
	 * Generates a random, but legal, move
	 * 
	 * @param moves Legal moves
	 * @return selected move
	 */
	public int getRandomMove(boolean[] moves) {
		int move = -1;
		while(move == -1){
			int temp = (int)(Math.random()*7);
			if(moves[temp]){
				move = temp;
			}
		}
		return move;
	}
	
	/**
	 * Makes a move while completely disregarding the idea that actions have consequences
	 * 
	 * @param board the board to make a move on
	 * @return Selected move
	 */
	public int getSimpleMove(Board board){
		boolean[] moves     = board.getLegalColumns();
		Board testBoard     = new Board(board);
		int selectedMove    = getRandomMove(moves);
		Board selectedBoard = new Board(testBoard, selectedMove);
		int nextMove;
		Board nextBoard;
		
		for(int i = 0; i < 7; ++i){
			if(i == selectedMove || !moves[i]) continue;
			nextMove  = i;
			nextBoard = new Board(board, i);
			if(this.isRed && nextBoard.getValue() > selectedBoard.getValue()){
				selectedMove  = nextMove;
				selectedBoard = nextBoard;
			} else if (!this.isRed && nextBoard.getValue() < selectedBoard.getValue()){
				selectedMove  = nextMove;
				selectedBoard = nextBoard;
			}
		}
		
		return selectedMove;
	}
	
	/**
	 * This method generates a tree of all possible future states of the given board to a given ply depth, then analyzes the tree for the best possible move using the minimax algorithm
	 * 
	 * @param board State of the board to analyze.
	 * @param ply Depth to analyze to.
	 * @return The selected move.
	 */
	public int getBetterMove(Board board, int ply){
		Node root = new Node(board);
		root = populateTree(root, ply);
		
		Node temp = root.getChild(0);
		for(Node n:root.getChildren()){ //traverse the tree
			double nValue = n.getMinimaxValue();
			if(this.isRed && (nValue > temp.getMinimaxValue())){ 
				temp = n;
			} else if (!this.isRed && (nValue < temp.getMinimaxValue())){ 
				temp = n;
			}
		}
		
		return temp.getMoveData();
	}
	
	/**
	 * This method generates a tree of most of the possible future states of the given board to a given ply depth while analyzing the tree for the best possible move using the minimax algorithm with alpha-beta pruning
	 * 
	 * @param board State of the board to analyze.
	 * @param ply Depth to analyze to.
	 * @return The selected move.
	 */
	public int getEvenBetterMove(Board board, int ply){
		Node root = new Node(board);
		root = populateAlphaBetaTree(root, ply, Integer.MIN_VALUE, Integer.MAX_VALUE);
		Node temp = root.getChild(0);
		for(Node n:root.getChildren()){ //traverse the tree
			double nVal = n.getMinimaxValue();
			double tempVal = temp.getMinimaxValue();
			if(this.isRed && (nVal > tempVal)){ 
				temp = n;
			} else if (!this.isRed && (nVal < tempVal)){ 
				temp = n;
			}
		}
		
		return temp.getMoveData();
	}

	/**
	 * Generates the tree.
	 * 
	 * @param root Node to generate the children of.
	 * @param ply How many ply left there are to generate down the tree.
	 * @return Returns the root node of a subsection of the tree.
	 */
	private Node populateTree(Node root, int ply) {
		if(ply > 0){
			for(int move: root.getBoardData().generateMoveList()){
				root.addChild(new Node(root, move));
			}
			--ply;
			for(Node n: root.getChildren()){
				populateTree(n, ply);
			}
		}
		return root;
	}
	
	/**
	 * Generates a tree without the branches pruned by alpha-beta pruning
	 * 
	 * @param root Node to generate the children of.
	 * @param ply how many ply left there are to generate down the tree.
	 * @param alpha the value of the maximizing player's best move
	 * @param beta the value of the minimizing player's best move
	 * @return Returns the root node of a subsection of the tree.
	 * Citation: Based on the chess alpha-beta code provided by Professor Kopec
	 */
	private Node populateAlphaBetaTree(Node root, int ply, double alpha, double beta){
		Node temp;
		Board board = root.getBoardData();
		if(Math.abs(board.getValue()) > 1_000){
			root.setNodeValue(board.evaluateScore()); //set leaf score
		} else if(ply > 0){//if tree isn't at a leaf
			--ply;
			
			ArrayList<Integer> moves = board.generateMoveList();
			for(int move: moves){
				temp = new Node(root, move);
				root.addChild(temp);
				
				if(board.isRedTurn()){//if the maximizing player
					alpha = Math.max(alpha, populateAlphaBetaTree(temp, ply, alpha, beta).getMinimaxValue()); //alpha is the value of the maximizing player's best move
					if(alpha > beta){
						break; //branch won't happen
					}
				}else{// if the minimizing player
					if(!board.isRedTurn()){//if white, the maximizing player
						beta = Math.min(beta, populateAlphaBetaTree(temp, ply, alpha, beta).getMinimaxValue()); //beta is the value of the minimizing player's best move
						if(alpha > beta){
							break; //branch won't happen
						}
					}
				}
			}//for
		} else {//if tree is at leaf
			root.setNodeValue(board.evaluateScore()); //set leaf score
		}
		return root;
	}
	
	
}
