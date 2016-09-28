package connectfour.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import connectfour.analysis.Board;
import connectfour.analysis.ComputerPlayer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * A GUI for playing connect four.
 * 
 * @author Folz
 *
 */
public class ConnectFourGUI extends Application{
	Board gameBoard;
	GridPane board;
	int move;
	int moveCount;
	boolean p1isHuman;
	boolean p2isHuman;
	int ply = 9;
	
	Image square;
	Image p1Piece;
	Image p2Piece;
	
	
	@Override
	public void start(Stage primaryStage){
		gameBoard = new Board();
		
		board = new GridPane();
		board.setPadding(new Insets(5));
		board.setStyle("-fx-background-color: gold");
		loadImages();
		primaryStage.getIcons().add(raiseFlag());
		setUpBoard(primaryStage);

		BorderPane root = new BorderPane();
		root.setCenter(board);
		root.setTop(makeMenuBar(primaryStage));
		root.setBottom(makeCoordinates());
		root.setRight(makeSideView());
		
		primaryStage.setScene(new Scene(root, 950, 700, Color.WHITE));
		primaryStage.setTitle("Connect Four");
		primaryStage.show();
		
		
		setupDialog(primaryStage);
		primaryStage.setOnCloseRequest((event) -> {
			System.exit(0);;
		});
	}

	/**
	 * Creates a dialog window allowing the user to customize player settings for the game.
	 * 
	 * @param primaryStage The primary Stage of the entire application
	 */
	private void setupDialog(Stage primaryStage) {
		//disable game controls until game starts
		setDisable(true);
		
		Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        
        VBox setupBox = new VBox(10);
        
        HBox buttonBox            = new HBox(15);
        RadioButton localButton   = new RadioButton("Local Game");
        RadioButton networkButton = new RadioButton("Networked Game");
        buttonBox.getChildren().addAll(localButton, networkButton);
         
        VBox localGameBox     = makeLocalGameBox(primaryStage, dialog);
        VBox networkedGameBox = makeNetworkedGameBox(primaryStage, dialog);
        
        TabPane tabs   = new TabPane();
        Tab localTab   = new Tab("Local Game");
        Tab networkTab = new Tab("Networked Game");
        localTab.setContent(localGameBox);
        networkTab.setContent(networkedGameBox);
        tabs.getTabs().addAll(localTab, networkTab);
        tabs.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        
        
        setupBox.getChildren().addAll(tabs);
        
        Scene dialogScene = new Scene(setupBox, 450, 300);
        dialog.getIcons().add(raiseFlag());
        dialog.setTitle("Player Selction");
        dialog.setScene(dialogScene);
        dialog.show();
	}

	/**
	 * Creates a GUI element that controls the settings and creation of a networked game
	 * 
	 * @param primaryStage The primary Stage of the entire application
	 * @param dialog The parent dialog box the created element belongs to
	 * @return GUI element that allows for the creation of a networked game
	 */
	private VBox makeNetworkedGameBox(Stage primaryStage, Stage dialog) {
		VBox networkedGameBox = new VBox(10);
		
		
		HBox localPlayer          = new HBox(5);
		RadioButton localHuman    = new RadioButton("Human");
        RadioButton localComputer = new RadioButton("Computer");
        ToggleGroup localToggles  = new ToggleGroup();
        
        localHuman.setToggleGroup(localToggles);
        localComputer.setToggleGroup(localToggles);
        localToggles.selectToggle(localHuman);
        localPlayer.getChildren().addAll(new Label("Local Player: "), localHuman, localComputer);
        localPlayer.setAlignment(Pos.CENTER);
        
        HBox roleBox            = new HBox(5);
        RadioButton server      = new RadioButton("Server");
        RadioButton client      = new RadioButton("Client");
        ToggleGroup roleToggles = new ToggleGroup();
		server.setToggleGroup(roleToggles);
		client.setToggleGroup(roleToggles);
		roleToggles.selectToggle(server);
		roleBox.getChildren().addAll(new Label("Role: "), server, client);
		roleBox.setAlignment(Pos.CENTER);
		
		HBox serverBox = new HBox(5);
		Label status   = new Label("Waiting for user...");
		serverBox.setAlignment(Pos.CENTER);
		serverBox.getChildren().addAll(status);
		
		HBox clientBox    = new HBox(5);
		Label ipLabel     = new Label("Server IP: ");
		TextField ipField = new TextField();
		clientBox.getChildren().addAll(ipLabel, ipField);

		BorderPane connectionStatusPane = new BorderPane();
		connectionStatusPane.setCenter(serverBox);
		server.setOnAction((event) -> {
			connectionStatusPane.setCenter(serverBox);
		});
		client.setOnAction((event) -> {
			connectionStatusPane.setCenter(clientBox);
		});
		
		HBox acceptBox = new HBox();
		Button accept  = new Button("Accept");
		acceptBox.getChildren().add(accept);
		acceptBox.setAlignment(Pos.CENTER);
		
		networkedGameBox.getChildren().addAll(localPlayer, makeDifficultyBox(), roleBox, connectionStatusPane, accept);
		networkedGameBox.setAlignment(Pos.CENTER);
		
		accept.setOnAction((event) ->{ 
			Socket socket;
			ServerSocket serverSocket;
			DataInputStream in;
			DataOutputStream out;
			int port = 44444;
			
			if(roleToggles.getSelectedToggle().equals(server)){
				try{
					serverSocket = new ServerSocket(port);
					socket = serverSocket.accept();
					in = new DataInputStream(socket.getInputStream());
					out = new DataOutputStream(socket.getOutputStream());
					
					Thread serverThread = new Thread( () -> {
						try{
							out.writeUTF("ST");
							out.flush();
						}catch(IOException e){
							
						}
						while(true){
							try{
								String data = DataInputStream.readUTF(in);
								if(data.charAt(0) == 'A'){
									ComputerPlayer computer = new ComputerPlayer(gameBoard.isRedTurn());
									move = computer.getEvenBetterMove(gameBoard, ply);
									data = "M" + move + "T";
									out.writeUTF(data);
									out.flush();
									Platform.runLater(new Runnable() {
										@Override
										public void run() {
											playTurn(primaryStage);
										}
				        			});
								} else if(data.charAt(0) == 'M'){ //move
									move = Integer.parseInt(data.substring(1, 2));
									Platform.runLater(new Runnable() {
										@Override
										public void run() {
											playTurn(primaryStage);
										}
				        			});
									ComputerPlayer computer = new ComputerPlayer(gameBoard.isRedTurn());
									move = computer.getEvenBetterMove(gameBoard, ply);
									data = "M" + move + "T";
									out.writeUTF(data);
									out.flush();
									Platform.runLater(new Runnable() {
										@Override
										public void run() {
											playTurn(primaryStage);
										}
				        			});
								}
							}catch(IOException e){
								
							}
						}
					});
					serverThread.start();
				}catch(IOException e){
					
				}
			} else {
				String serverIP = ipField.getText();
				try {
					socket = new Socket(serverIP, port);
					in = new DataInputStream(socket.getInputStream());
					out = new DataOutputStream(socket.getOutputStream());
					
					Thread clientThread = new Thread( () -> {
						while(true){ //change to while game is still a thing
							try {
								String data = DataInputStream.readUTF(in);
								if(data.charAt(0) == 'S'){
									out.writeUTF("AT");
									out.flush();
								} else if(data.charAt(0) == 'M'){ //move
									move = Integer.parseInt(data.substring(1, 2));
									Platform.runLater(new Runnable() {
										@Override
										public void run() {
											playTurn(primaryStage);
										}
				        			});
									ComputerPlayer computer = new ComputerPlayer(gameBoard.isRedTurn());
									move = computer.getEvenBetterMove(gameBoard, ply);
									data = "M" + move + "T";
									out.writeUTF(data);
									out.flush();
									Platform.runLater(new Runnable() {
										@Override
										public void run() {
											playTurn(primaryStage);
										}
				        			});
								}
								data = "";
							} catch (IOException e) {
								
							}
						}
					});
					clientThread.start();
				} catch (IOException e) {
					
				}
			}
			dialog.close();
		});
		
		return networkedGameBox;
	}

	/**
	 * Creates a GUI element that controls the settings and creation of a local game
	 * 
	 * @param primaryStage The primary Stage of the entire application
	 * @param dialog The parent dialog box the created element belongs to
	 * @return GUI element that allows for the creation of a local game
	 */
	private VBox makeLocalGameBox(Stage primaryStage, Stage dialog) {
		VBox localGameBox = new VBox(10);
		
		HBox p1Box = new HBox(5);
        RadioButton p1Human    = new RadioButton("Human");
        RadioButton p1Computer = new RadioButton("Computer");
        ToggleGroup p1Toggles  = new ToggleGroup();
        
        p1Human.setToggleGroup(p1Toggles);
        p1Computer.setToggleGroup(p1Toggles);
        p1Toggles.selectToggle(p1Human);
        p1Box.getChildren().addAll(new Label("Player 1: "), p1Human, p1Computer);
        p1Box.setAlignment(Pos.CENTER);
        
        
        HBox p2Box = new HBox(5);
        RadioButton p2Human    = new RadioButton("Human");
        RadioButton p2Computer = new RadioButton("Computer");
        ToggleGroup p2Toggles  = new ToggleGroup();
        
        p2Human.setToggleGroup(p2Toggles);
        p2Computer.setToggleGroup(p2Toggles);
        p2Toggles.selectToggle(p2Human);
        p2Box.getChildren().addAll(new Label("Player 2: "), p2Human, p2Computer);
        p2Box.setAlignment(Pos.CENTER);
        
        
        Button acceptButton = new Button("Accept");
        acceptButton.setOnAction((event) -> {
        	p1isHuman = p1Toggles.getSelectedToggle().equals(p1Human);
        	p2isHuman = p2Toggles.getSelectedToggle().equals(p2Human);
        	
        	if(!p1isHuman && p2isHuman){ //let computer move first when p1 is computer
				setDisable(true);
        		Thread calcThread = new Thread( () -> {
        			move = new ComputerPlayer(gameBoard.isRedTurn()).getEvenBetterMove(gameBoard, ply);
        			Platform.runLater(new Runnable() {
						@Override
						public void run() {
							playTurn(primaryStage);
							setDisable(false);
						}
        			});
        		});
        		calcThread.start();
        	}
        	if(!p1isHuman && !p2isHuman){//if both are computers
				setDisable(true);
    			Thread calcThread = new Thread( () -> {
    				while(! (Math.abs(gameBoard.getValue()) > 1_000 || gameBoard.getNumOfMoves() >= 42) ){
            			move = new ComputerPlayer(gameBoard.isRedTurn()).getEvenBetterMove(gameBoard, ply);
            			Platform.runLater(new Runnable() {
    						@Override
    						public void run() {
    							playTurn(primaryStage);
    						}
            			});
    				}
        		});
        		calcThread.start();
        	}
        	setDisable(false);
        	dialog.close();
        });
        
        localGameBox.getChildren().addAll(p1Box, p2Box, makeDifficultyBox(), acceptButton);
        localGameBox.setPadding(new Insets(15));
        localGameBox.setAlignment(Pos.CENTER);
        
		return localGameBox;
	}

	/**
	 * Plays one turn of the game.
	 */
	private void playTurn(Stage primaryStage){
		gameBoard = new Board(gameBoard, move);
		
		int row = 6 - gameBoard.getColumnCount()[move];
		StackPane temp = (StackPane)getNodeByCoordinate(move, row);
		ImageView tempImg;
		if(gameBoard.isRedTurn()){ //move has already been made, so use color of previous turn
			tempImg = new ImageView(p2Piece);
			tempImg.setMouseTransparent(true);
			temp.getChildren().add(tempImg);
		} else {
			tempImg = new ImageView(p1Piece);
			tempImg.setMouseTransparent(true);
			temp.getChildren().add(tempImg);
		}
		
		if(Math.abs(gameBoard.getValue()) > 1_000 || gameBoard.getNumOfMoves() >= 42){
			endGameDialog(primaryStage);
		} 
	}

	/**
	 * Opens a dialog box informing the user of the victor, if any, of the game, and gives an option to start a new game
	 * 
	 * @param primaryStage The primary Stage of the entire application
	 */
	private void endGameDialog(Stage primaryStage) {
		setDisable(true); //game is over, of course the user can't interact with the board anymore
		
        Label endLabel = new Label();
        if(gameBoard.getNumOfMoves() >= 42){
        	endLabel.setText("Game is a draw.");
        } else {
        	endLabel.setText("Player " + (gameBoard.getValue() > 1_000? "1":"2") + " Wins!" );
        }
        endLabel.setAlignment(Pos.CENTER);
        
        Button newGame = new Button("New game?");
        newGame.setAlignment(Pos.CENTER);
        newGame.setOnAction((event) -> {
        	ConnectFourGUI application = new ConnectFourGUI();
        	Stage applicationStage     = new Stage();
        	application.start(applicationStage);
        	primaryStage.close();
        });

		VBox dialogBox = new VBox(10);
        dialogBox.getChildren().addAll(endLabel, newGame);
        dialogBox.setAlignment(Pos.CENTER);
        
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.setScene(new Scene(dialogBox, 250, 100));
        dialog.getIcons().add(raiseFlag());
        dialog.setTitle("Game Over.");
        dialog.show();
	}

	/**
	 * Creates a GUI component that controls certain meta-game aspects
	 * 
	 * @param primaryStage The primary Stage of the entire application
	 * @return GUI component that is the menu bar
	 */
	private Node makeMenuBar(Stage primaryStage) {
		MenuBar mainMenu = new MenuBar();
		
		Menu file = new Menu("File");
		
		MenuItem save = new MenuItem("Save");
		save.setOnAction((event) -> {
			FileChooser fileChooser = new FileChooser();
			FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Saved Game Files (*.sav)", "*.sav");
			fileChooser.getExtensionFilters().add(extensionFilter);
            File selectedFile = fileChooser.showSaveDialog(primaryStage);
            
			try{
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(selectedFile));
				oos.writeObject(gameBoard);
				oos.close();
			} catch (IOException e){
			
			} 
		});
		
		MenuItem load = new MenuItem("Load...");
		load.setOnAction((event) -> {
			FileChooser fileChooser = new FileChooser();
			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Saved Game Files (*.sav)", "*.sav");
	        fileChooser.getExtensionFilters().add(extFilter);
			File selectedFile = fileChooser.showOpenDialog(primaryStage);
			
			try{
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(selectedFile));
				gameBoard = (Board)ois.readObject();
				redraw(primaryStage);
				ois.close();
			}catch(ClassNotFoundException e){
				
			}catch(IOException e){
				
			}
		});
		
		MenuItem newGame = new MenuItem("New Game");
		newGame.setOnAction((event) -> {
        	ConnectFourGUI application = new ConnectFourGUI();
        	Stage applicationStage = new Stage();
        	application.start(applicationStage);
        	primaryStage.close();
        });
		
		file.getItems().addAll(save, load, newGame);
		
		
		Menu edit = new Menu("Edit");

		MenuItem changeDifficulty = new MenuItem("Change Difficulty");
		changeDifficulty.setOnAction((event) -> {
			VBox difficultyBox = makeDifficultyBox();
			Scene dialogScene = new Scene(difficultyBox, 300, 115);
			
			Stage dialog = new Stage();
			dialog.initOwner(primaryStage);
			dialog.getIcons().add(raiseFlag());
            dialog.setTitle("Difficulty Setting");
            dialog.setScene(dialogScene);
            dialog.show();
		});
		
		MenuItem advanced = new MenuItem("Advanced...");
		advanced.setOnAction((event) -> {
			VBox labels   = new VBox(25);
			Label p1Label = new Label("Player 1 Image: ");
			Label p2Label = new Label("Player 2 Image: ");
			labels.getChildren().addAll(p1Label, p2Label);
			labels.setAlignment(Pos.CENTER);
			
			VBox selections = new VBox(20);
			ChoiceBox<String> p1sprite = new ChoiceBox<String>();
			p1sprite.getItems().addAll("Red", "Black", "Purple", "Halloween", "Easter");
			p1sprite.getSelectionModel().select(0);
			p1sprite.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					String separator = File.separator;
					String path      = "src" + separator + "connectfour" + separator + "util" + separator + "images" + separator;
					File p1PieceFile;
					
					InputStream inputStream = null;
					switch(newValue.intValue()){
						case 0://red 
							p1PieceFile = new File(path + "p1Piece.png");
							break;
						case 1://black
							p1PieceFile = new File(path + "p2Piece.png");
							break;
						case 2: //purple
							p1PieceFile = new File(path + "PurplePiece.png");
							break;
						case 3://halloween
							p1PieceFile = new File(path + "HalloweenPiece.png");
							break;
						case 4://easter
							p1PieceFile = new File(path + "EasterPiece.png");
							break;
						default:
							p1PieceFile = new File(path + "p1Piece.png");
					}
					try {
						inputStream = new FileInputStream(p1PieceFile);
						p1Piece = new Image(inputStream);
					} catch (FileNotFoundException e) {
						
					}
					redraw(primaryStage);
				}
			});
			
			ChoiceBox<String> p2sprite = new ChoiceBox<String>();
			p2sprite.getItems().addAll("Red", "Black", "Purple", "Halloween", "Easter");
			p2sprite.getSelectionModel().select(1);
			p2sprite.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					String separator = File.separator;
					String path = "src" + separator + "connectfour" + separator + "util" + separator + "images" + separator;
					File p2PieceFile;
					
					InputStream inputStream = null;
					switch(newValue.intValue()){
						case 0://red 
							p2PieceFile = new File(path + "p1Piece.png");
							break;
						case 1://black
							p2PieceFile = new File(path + "p2Piece.png");
							break;
						case 2://purple
							p2PieceFile = new File(path + "PurplePiece.png");
							break;
						case 3://halloween
							p2PieceFile = new File(path + "HalloweenPiece.png");
							break;
						case 4://easter
							p2PieceFile = new File(path + "EasterPiece.png");
							break;
						default:
							p2PieceFile = new File(path + "p2Piece.png");
					}
					try {
						inputStream = new FileInputStream(p2PieceFile);
						p2Piece = new Image(inputStream);
					} catch (FileNotFoundException e) {
						
					}
					redraw(primaryStage);
				}
			});
			selections.getChildren().addAll(p1sprite, p2sprite);
			selections.setAlignment(Pos.CENTER);
			

			HBox root = new HBox(10);
			root.setPadding(new Insets(20));
			root.setAlignment(Pos.CENTER);
			root.getChildren().addAll(labels, selections);

			Stage dialog = new Stage();
			dialog.initOwner(primaryStage);
			dialog.getIcons().add(raiseFlag());
            dialog.setTitle("Piece Settings");
            dialog.setScene(new Scene(root, 280, 135));
            dialog.show();
		});
		
		edit.getItems().addAll(changeDifficulty, advanced);
		
		
		Menu help = new Menu("Help");
		
		MenuItem suggestMove = new MenuItem("Show suggested move");
		suggestMove.setOnAction((event) -> {
            
            int tempMove = 0;
            try{
            	ComputerPlayer tempPlayer = new ComputerPlayer(gameBoard.isRedTurn());
            	tempMove = 1 + tempPlayer.getEvenBetterMove(gameBoard, (int)(ply/1.5));
            } catch(NullPointerException e){
            	
            }

            VBox suggestionBox = new VBox(5);
            suggestionBox.getChildren().add(new Label("" + tempMove));
            suggestionBox.setAlignment(Pos.CENTER);

			Stage dialog = new Stage();
            dialog.initOwner(primaryStage);
            dialog.getIcons().add(raiseFlag());
            dialog.setTitle("Suggestion");
            dialog.setScene(new Scene(suggestionBox, 220, 40));
            dialog.show();
		});
		
		MenuItem rules = new MenuItem("How to Play");
		rules.setOnAction((event) -> {
            
            VBox dialogBox = new VBox(10);
            Label rulesLabel = new Label("Connect Four is a two player game in which the two players take \n"
            		+ "turns dropping pieces into a grid.  The first player to get four pieces of their own\n"
            		+ "color connected in a straight line, horizontally, vertically, or diagonally, wins the \n"
            		+ "game.  If the board fills up before either player wins, it is considered a draw.\n");
            Label IOInfo = new Label("To play, simply click on the column that you wish to place your piece into.");
            dialogBox.getChildren().addAll(rulesLabel, IOInfo);
            dialogBox.setPadding(new Insets(10));
            
			Stage dialog = new Stage();
            dialog.initOwner(primaryStage);
            dialog.getIcons().add(raiseFlag());
            dialog.setTitle("Help");
            dialog.setScene(new Scene(dialogBox, 450, 150));
            dialog.show();
		});
		
		help.getItems().addAll(suggestMove, rules);
		
		
		mainMenu.getMenus().addAll(file, edit, help);
		
		return mainMenu;
	}

	/**
	 * Creates a GUI element that controls the difficulty setting of the computer player(s)
	 * 
	 * @return GUI element controlling computer difficulty
	 */
	private VBox makeDifficultyBox() {
		Label difficultyLabel = new Label("Computer Difficulty:");
		Slider difficulty     = new Slider();
		difficulty.setMin(1);
		difficulty.setMax(10);
		difficulty.setValue(ply);
		difficulty.setOnDragDetected((event) -> {
			try {
				((Node)event.getSource()).startFullDrag();
			} catch (ClassCastException e) {
				//dragging wrong element
			}
		});
		difficulty.setOnMouseDragged((subEvent) -> {
			ply = (int)difficulty.getValue();
		});
		Label disclaimer = new Label("Higher difficulties will result in increasingly \n"
									+ "large amount of time used per turn");

		HBox difficultySliderBox = new HBox(5);
		VBox difficultyBox       = new VBox(10);
		difficultySliderBox.getChildren().addAll(difficultyLabel, difficulty);
		difficultySliderBox.setAlignment(Pos.CENTER);
		difficultyBox.setPadding(new Insets(20));
		difficultyBox.getChildren().addAll(difficultySliderBox, disclaimer);
		difficultyBox.setAlignment(Pos.CENTER);

		return difficultyBox;
	}

	/**
	 * Completely refreshes the view by deleting all images then re-adding them where they should be.
	 * 
	 * @param primaryStage The primary Stage of the entire application
	 */
	private void redraw(Stage primaryStage) {
		board.getChildren().clear();
		setUpBoard(primaryStage);
		
		ImageView tempImg;
		for(int col = 0; col < 7; ++col){
			for(int row = 0; row < 6; ++row){
				if(gameBoard.getBoard()[row][col] == -1){
					StackPane temp = (StackPane)getNodeByCoordinate(col, 5-row);
					tempImg = new ImageView(p2Piece);
					temp.getChildren().add(tempImg);
				} else if(gameBoard.getBoard()[row][col] == 1){
					StackPane temp = (StackPane)getNodeByCoordinate(col, 5-row);
					tempImg = new ImageView(p1Piece);
					temp.getChildren().add(tempImg);
				}
			}
		}
	}

	/**
	 * Creates a GUI element that labels the column coordinates of the game board
	 * 
	 * @return GUI element giving the coordinates
	 */
	private HBox makeCoordinates() {
		HBox coordinates = new HBox(93);

		Label c1 = new Label("1");
		c1.setScaleX(2);
		c1.setScaleY(2);
		Label c2 = new Label("2");
		c2.setScaleX(2);
		c2.setScaleY(2);
		Label c3 = new Label("3");
		c3.setScaleX(2);
		c3.setScaleY(2);
		Label c4 = new Label("4");
		c4.setScaleX(2);
		c4.setScaleY(2);
		Label c5 = new Label("5");
		c5.setScaleX(2);
		c5.setScaleY(2);
		Label c6 = new Label("6");
		c6.setScaleX(2);
		c6.setScaleY(2);
		Label c7 = new Label("7");
		c7.setScaleX(2);
		c7.setScaleY(2);
		
		coordinates.setPadding(new Insets(5, 30, 5, 50));
		coordinates.getChildren().addAll(c1, c2, c3, c4, c5, c6, c7);
		return coordinates;
	}

	/**
	 * Creates the image of the game board and creates the infrastructure for running the game
	 * 
	 * @param primaryStage The primary Stage of the entire application
	 */
	private void setUpBoard(Stage primaryStage) {
		StackPane temp;
		ImageView tempImg;
		
		for(int col = 0; col < 7; ++col){
			for(int row = 0; row < 6; ++row){
				tempImg = new ImageView(square);
				tempImg.setMouseTransparent(true);
				temp = new StackPane(tempImg);
				temp.setOnMouseClicked((event) -> {
					setDisable(true);
					try{
						move = GridPane.getColumnIndex((Node)event.getSource());
						playTurn(primaryStage);
						
						if(p1isHuman ^ p2isHuman){
							Thread taskThread = new Thread( () -> {
								try{
									ComputerPlayer computer = new ComputerPlayer(gameBoard.isRedTurn());
									move = computer.getEvenBetterMove(gameBoard, ply);
									
									Platform.runLater(new Runnable() {
										@Override
										public void run() {
											playTurn(primaryStage);
											setDisable(false);
										}
									});
								}catch(IndexOutOfBoundsException e){
									//game ended
								}
							});
							taskThread.start();
						} else{
							setDisable(false);
						}
					}catch(ArrayIndexOutOfBoundsException e){
						//tried to place piece above the board, ignore and move on
						setDisable(false);
					}
				});
				board.add(temp, col, row);
			}
		}
	}
	
	/**
	 * Enables and disables interactivity with the board
	 * 
	 * @param disabled Value to set interactivity to
	 */
	private void setDisable(boolean disabled){
		for(Node n : board.getChildren()){
			n.setDisable(disabled);
		}
	}

	/**
	 * Loads all images into global cache to be used elsewhere.
	 */
	private void loadImages() {
		String separator = File.separator;
		File square      = new File("src" + separator + "connectfour" + separator + "util" + separator + "images" + separator + "square.png");
		File p1Piece     = new File("src" + separator + "connectfour" + separator + "util" + separator + "images" + separator + "p1Piece.png");
		File p2Piece     = new File("src" + separator + "connectfour" + separator + "util" + separator + "images" + separator + "p2Piece.png");
		
		InputStream inputStream = null;
		try{
			inputStream  = new FileInputStream(square);
			this.square  = new Image(inputStream);
			
			inputStream  = new FileInputStream(p2Piece);
			this.p2Piece = new Image(inputStream);
			
			inputStream  = new FileInputStream(p1Piece);
			this.p1Piece = new Image(inputStream);
			
		}catch(IOException e){
			
		}
	}
	
	/**
	 * Utility function to locate a node in a GridPane by a particular pair of coordinates
	 * 
	 * @param x X coordinate of desired Node
	 * @param y Y coordinate of desired Node
	 * @return Returns the desired Node
	 */
	private Node getNodeByCoordinate(int x, int y){ //there has to be a better way to do this
		Node temp = null;
		for(Node node : board.getChildren()){ 
			int nodeX = GridPane.getColumnIndex(node);
			int nodeY = GridPane.getRowIndex(node);
			if(x == nodeX && y == nodeY){
				temp = node;
				break;
			}
		}
		return temp;
	}

	/**
	 * Creates a GUI element to be placed in the sidebar, for purpose of promoting the correct aesthetic atmosphere of the game.
	 * 
	 * @return GUI sidebar element
	 */
	private Node makeSideView(){
		String separator  = File.separator;
		File portraitFile = new File("src" + separator + "connectfour" + separator + "util" + separator + "images" + separator + "PlayerPortrait.png");
		Image portraitImg = null;
		try{
			InputStream input = new FileInputStream(portraitFile);
			portraitImg       = new Image(input);
		}catch(IOException e){
			
		}
		ImageView portraitView = new ImageView(portraitImg);
		VBox portrait          = new VBox(portraitView);
		portrait.setAlignment(Pos.CENTER);
		
		String[] fischerQuotes = {
				"I don't believe in \npsychology. I believe in \ngood moves.",
				"I give 98 percent of my \nmental energy to chess. \nOthers give only 2 percent.",
				"It’s just you and your \nopponent at the board \nand you're trying to \nprove something.",
				"The object is to crush \nthe opponent's mind.",
				"I play honestly and I \nplay to win. If I lose, I \ntake my medicine.",
				"One day you give your \nopponent a lesson, the next \nday he gives you one. ",
				"I like the moment when I \nbreak a man's ego.",
				"There are tough players \nand nice guys, and I'm a \ntough player.",
				"I am the best player in \nthe world and I am here \nto prove it",
				"You know, I can beat all \nthose guys.",
				"There's no one alive I \ncan't beat.",
				"I know people who have \nall the will in the world, \nbut still can't play good \nchess.",
				"Tactics flow from a \nsuperior position.",
				"My opponents make good \nmoves too. Sometimes I \ndon't take these things \ninto consideration. ",
				"I don't listen to weakies.",
				"I consider myself to be\nan all around genius.",
				"The world is coming to\nan end anyway!",
				"Our mind is all we've \ngot. Not that it won't lead \nus astray sometimes, but we \nstill have to analyze things \nout within ourselves."};
		Label quoteField = new Label();
		quoteField.setText( fischerQuotes[(int)(Math.random()*fischerQuotes.length)]);
		
		final int[] moves = {0};
		Thread quoteManager = new Thread( () -> { //change the quote that is displayed shortly after someone makes a move
			while(! (Math.abs(gameBoard.getValue()) > 1_000 || gameBoard.getNumOfMoves() >= 42) ){
				Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
				try {
					Thread.sleep(1_000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				if(moves[0] != gameBoard.getNumOfMoves()){
					moves[0] = gameBoard.getNumOfMoves();
					
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							quoteField.setText(fischerQuotes[(int)(Math.random()*fischerQuotes.length)]);
						}
					});
				}
			}
		});
		quoteManager.start();

		VBox playerView = new VBox(10);
		playerView.getChildren().addAll(portrait, quoteField);
		playerView.setPrefWidth(175);
		playerView.setPadding(new Insets(35, 10, 35, 10));
		return playerView;
		
	}

	/**
	 * Raises the flag of the Empire.
	 * 
	 * @return Returns the Empire's flag
	 */
	private Image raiseFlag() {
		String separator  = File.separator;
		File flagFile     = new File("src" + separator + "connectfour" + separator + "util" + separator + "images" + separator + "Flag.png");
		InputStream input = null;
		Image flag        = null;
		try{
			input = new FileInputStream(flagFile);
			flag  = new Image(input);
			input.close();
		}catch(FileNotFoundException e){
			System.out.println("no file");
		}catch(IOException e){
			System.out.println("Other err");
		}
		return flag;
	}
	
	public static void main(String[] args) {
	    launch(args);
	}
	
}
