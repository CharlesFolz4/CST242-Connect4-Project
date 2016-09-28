package connectfour.util;

import java.util.Scanner;

import connectfour.analysis.Board;
import connectfour.analysis.ComputerPlayer;

/**
 * A command line interface for playing Connect Four
 * 
 * @author Emperor
 *
 */
public class ConnectFourCLI {
	static Board gameBoard;
	
	public static void main(String... args){
		gameBoard = new Board();
		playGame();
		
	}

	/**
	 * Provides an environment in which to play the game
	 */
	private static void playGame() {
		boolean continueGame = true;
		boolean p1IsHuman = true;
		boolean p2IsHuman = true;
		Scanner input = new Scanner(System.in);
		ComputerPlayer p1 = null;
		ComputerPlayer p2 = null;
		
		if(!p1IsHuman){
			p1 = new ComputerPlayer(true);
		}
		if(!p2IsHuman){
			p2 = new ComputerPlayer(false);
		}

		gameBoard.printBoard();
		while(continueGame){
			if(gameBoard.isRedTurn()){
				if(p1IsHuman){
					System.out.print("Red moves: ");
					interpretCommand(input.nextLine());
				} else {
					int move = p1.getEvenBetterMove(gameBoard, 1);
					gameBoard = new Board(gameBoard, move);
				}
				
			} else {
				if(p2IsHuman){
					System.out.print("Yellow moves: ");
					interpretCommand(input.nextLine());
				} else {
					int move = p2.getEvenBetterMove(gameBoard, 1);
					gameBoard = new Board(gameBoard, move);
				}
			}

			gameBoard.printBoard();
			System.out.println(gameBoard.evaluateScore());
			
			boolean temp = false;
			for(boolean c : gameBoard.getLegalColumns()){
				temp |= c;
			}
			if(Math.abs(gameBoard.getValue()) > 1_000){
				System.out.println("Player " + (gameBoard.isRedTurn()? "2":"1") + " wins!");
				break;
			}
			continueGame = temp;
			
		}
		System.out.println("Game ended.");
		input.close();
	}

	private static void interpretCommand(String command) {
		int move = -1;
		switch(command.toLowerCase()){
			case "0": move = 0; break;
			case "1": move = 1; break;
			case "2": move = 2; break;
			case "3": move = 3; break;
			case "4": move = 4; break;
			case "5": move = 5; break;
			case "6": move = 6; break;
			case "7": move = 7; break;
			
			case "show score": System.out.println(gameBoard.evaluateScore());
		}
		gameBoard = new Board(gameBoard, move);
	}
	
	
}
