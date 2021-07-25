package publicTransit;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class TransitSystem  extends Application {
	
    private static int placesMarkedCounter; // This is to keep track of marked Places, to not allow a third one to be marked. 0 by default. 
	private Stage stage;
	private Pane center;
	private BorderPane root;
	private ListGraph<Place> map; 
    private Place from; // To store the from and to places selected. 
    private Place to;
	private final double MAX_FONT_SIZE = 30.0; 
	private String imagePath = "file:Stockholm.png"; // This png file is stored locally. 
	private Button searchButton;
	private ChoiceBox<String> fromBox;
	private ChoiceBox<String> toBox;
	private Button swapButton;
	private RadioButton bfs;
    private RadioButton dijkstra;
    private boolean changed;

	@Override
	public void start(Stage stage) throws Exception {

		map = new ListGraph<Place>(); // We initialize the ListGraph. 
		this.stage = stage;
		root = new BorderPane();
		VBox top = new VBox();
		top.setStyle("-fx-background-color: #89CFF0;"); // Sets the color to baby blue. 
	    root.setTop(top); // The VBox is the top of the root. 
        MenuBar menuBar = new MenuBar();
        top.getChildren().add(menuBar);
	    FlowPane flow = new FlowPane();
	    top.getChildren().add(flow);
	    flow.setAlignment(Pos.CENTER);
	    flow.setHgap(10); // Gives space between buttons on the flow pane. 
	    flow.setPadding(new Insets(10)); // This gives vertical space between the buttons and the MenuBar place. 
		
	    fromBox = new ChoiceBox<>();
	    toBox = new ChoiceBox<>();
	    fromBox.setPrefWidth(200);
	    fromBox.setPrefHeight(50);
	    toBox.setPrefWidth(200);
	    toBox.setPrefHeight(50);

		Label fromLabel = new Label("From");
		Label toLabel = new Label("To");
		fromLabel.setFont(new Font(MAX_FONT_SIZE)); 
		toLabel.setFont(new Font(MAX_FONT_SIZE)); 
		
	    swapButton = new Button("Swap");
	    swapButton.setPrefWidth(100);
	    swapButton.setPrefHeight(50);
	    swapButton.setFont(new Font(20));
	    swapButton.setDisable(true); // Should only be active if "from" and "to" are chosen. We activate it later. 
	    swapButton.setOnMouseClicked(new ClickSwapHandler());
		
		flow.getChildren().addAll(fromLabel, fromBox);
	    flow.getChildren().add(swapButton);
		flow.getChildren().addAll(toLabel, toBox);
		
		fromBox.setOnHidden(new ClickFromBoxHandler()); // This will activate only when the user presses an item and the list closes. 
		toBox.setDisable(true); // Should only be enabled once the user has chosen a "from" first. 
		toBox.setOnHidden(new ClickToBoxHandler());
		
		searchButton = new Button("Search"); 
		flow.getChildren().add(searchButton);
		searchButton.setPrefWidth(100);
		searchButton.setPrefHeight(50);
		searchButton.setFont(new Font(20));
		searchButton.setDisable(true); // Should only be active if "from" and "to" are chosen. We activate it later. 
		searchButton.setOnMouseClicked(new SearchHandler());
		
        Image image = new Image(imagePath); 
        ImageView imageView = new ImageView(image); 
        imageView.setFitHeight(700);
        imageView.setFitWidth(1200);
        
        center = new Pane(); 
        root.setCenter(center);
        center.getChildren().add(imageView); 
        
        VBox littleFlow = new VBox(50);
        center.getChildren().add(littleFlow);
        littleFlow.relocate(940, 10);
        bfs = new RadioButton("Least amounts of stops");
        dijkstra = new RadioButton("Quickest trip");
        ToggleGroup group = new ToggleGroup();
        bfs.setToggleGroup(group);
        dijkstra.setToggleGroup(group);
        littleFlow.getChildren().addAll(bfs, dijkstra);
        bfs.setFont(Font.font("Bold", 20));
        dijkstra.setFont(Font.font("Bold", 20));
        bfs.setTextFill(Color.RED);
        dijkstra.setTextFill(Color.RED);
        dijkstra.setSelected(true);
        
        loadPlaces(); // This loads the places in the map and the drop down lists (ChoiceBoxes). 
        loadEdges();
        
	    Menu fileMenu = new Menu("Options"); // The MenuBar is for when pressing in the left upper corner. 
        menuBar.getMenus().add(fileMenu);
        MenuItem newEdgeItem = new MenuItem("Create New Connection");  
        fileMenu.getItems().add(newEdgeItem); 
        newEdgeItem.setOnAction(new NewConnectionHandler());
        MenuItem removeEdgeItem = new MenuItem("Remove Connection"); 
        fileMenu.getItems().add(removeEdgeItem);  
        removeEdgeItem.setOnAction(new RemoveConnectionHandler()); 
        MenuItem changeEdgeItem = new MenuItem("Change Connection"); 
        fileMenu.getItems().add(changeEdgeItem); 
        changeEdgeItem.setOnAction(new ChangeConnectionHandler()); 
        MenuItem openItem = new MenuItem("Open"); 
        fileMenu.getItems().add(openItem); 
        openItem.setOnAction(new OpenHandler()); 
        MenuItem saveItem = new MenuItem("Save");
        fileMenu.getItems().add(saveItem);
        saveItem.setOnAction(new SaveHandler()); 
        MenuItem exitItem = new MenuItem("Exit");
        fileMenu.getItems().add(exitItem);
        exitItem.setOnAction(new ExitItemHandler()); 
        
	    Scene scene = new Scene(root, 1200, 750); 
        stage.setScene(scene);
        stage.setTitle("Public Transit Simulator");
        stage.setOnCloseRequest(new ExitHandler()); 
        stage.show();
	}
	
	class ClickFromBoxHandler implements EventHandler<Event> {

		@Override
		public void handle(Event arg0) {
			if (!(fromBox.getValue() == null)) { // If the user has chosen a "from" value in the fromBox. 
				toBox.setDisable(false); // We activate the toBox again. 
			}
			for (Place place: map.getNodes()) {
				if (place.getName().equals(fromBox.getValue())) {
					if (place.getName().equals(toBox.getValue())) { // To avoid the ability to choose the same from as the to. 
						fromBox.setValue(from.getName()); // We set the value to the current "from" value. Basically it doesn't change. 
						continue; // So basically, whether the user chooses the from value that we already have,
						// or chooses the toBox's value, it will remain the current from. No change. 
						// And this all covers the user pressing something that is marked on the map. Because that will be either from or to. 
					}
					if (!(from == null)) { // If there is a current from variable, meaning if there is a current place marked. 
						from.setMarked(false); // We set the current from Place to not marked. 
						from.setFill(Color.BLUE); 
					} else {
						placesMarkedCounter++; // This should only go from 0 to 1 but then stay 1. 
					}
					from = place; // And we update from to be the new Place. 
					place.setMarked(true);
					place.setFill(Color.RED);
				}	
			}
		}
	}
	
	class ClickToBoxHandler implements EventHandler<Event> {

		@Override
		public void handle(Event arg0) {
			for (Place place: map.getNodes()) {
				if (place.getName().equals(toBox.getValue())) {
					if (place.getName().equals(fromBox.getValue())) { 
						if (to == null) { // In case we have no "to" value so far. When the toBox is empty, and the user chooses the same as from. 
							toBox.setValue(null);
						} else {
							toBox.setValue(to.getName()); 
							continue; 
						}
					}
					if (!(to == null)) { 
						to.setMarked(false); 
						to.setFill(Color.BLUE);  
					} else {
						placesMarkedCounter++; 
					}
					to = place; 
					place.setMarked(true);
					place.setFill(Color.RED);
				}	
			}
			if (placesMarkedCounter == 2) {
				searchButton.setDisable(false);
				swapButton.setDisable(false);
			} else {
				searchButton.setDisable(true);
				swapButton.setDisable(true);
			}
		}
	}
	
	class ClickSwapHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent arg0) {
			Place tempFrom = from;
			String tempFromString = fromBox.getValue();
			fromBox.setValue(toBox.getValue());
			toBox.setValue(tempFromString);
			from = to;
			to = tempFrom;
		}
	}
	
	class SearchHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent arg0) {
			Set<Line> lines = new HashSet<>();
			List<Edge<Place>> edges = null;
			if (!map.pathExists(from, to)) {
				Alert alert = new Alert(Alert.AlertType.ERROR, "There is no trip available between these places.");
				alert.showAndWait();
				return;
			}
			
			if (bfs.isSelected()) {
				edges = map.getPath(from, to); // A list of edges. 
			} else if (dijkstra.isSelected()) {
				edges = map.getDijkstraPath(from, to);
			}
			String text = "";
			int totalTime = 0;
			for (Edge<Place> edge: edges) { // Note that getDestination returns a Place, and the toString() there returns name. 
				totalTime += edge.getWeight();
			}
			text = "Total " + totalTime + " minutes" + "\n";
			for (Edge<Place> edge: edges) { 
				text += "to " + edge.getDestination() + " by " + edge.getName() + " takes " + edge.getWeight() + " minutes" + "\n";	
        		Line edgeLine = 
        				new Line(edge.getSource().getX()-4, edge.getSource().getY()-3, edge.getDestination().getX()-4, edge.getDestination().getY()-3); 
            	edgeLine.setStrokeWidth(2);
            	if (edge.getName().contains("Green")) {
            		edgeLine.setStroke(Color.GREEN);
            	} else if (edge.getName().contains("Blue")) {
            		edgeLine.setStroke(Color.BLUE);
            	} else if (edge.getName().contains("Red")) {
            		edgeLine.setStroke(Color.RED);
            	}
            	center.getChildren().add(edgeLine);
            	edgeLine.setDisable(true);  // This way the line doesn't "block" clicks on the Place itself. 
            	lines.add(edgeLine);
			}
			FindPathAlert alert = new FindPathAlert();
			alert.setHeaderText("The path from " + from + " to " + to + ":"); // Call the from and to fields.
			alert.setText(text);
			alert.show(); // Note, show(), not showAndWait() on account of them lambda expression below. 
			
			alert.setOnCloseRequest((svent) -> {   
				for (Line line: lines) {
					center.getChildren().remove(line);
				}
		      });
		}
	}
	
	class NewConnectionHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent arg0) {
			try {
				if (from == null || to == null) {
					Alert alert = new Alert(Alert.AlertType.ERROR, "Two places have not been chosen!");
					alert.showAndWait();
					return;
				}
				NewConnectionAlert alert = new NewConnectionAlert();
            	alert.setHeaderText("Connection from " + from + " to " + to); // Call the from and to fields. 
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.CANCEL) {
                	return;
                } else if (result.isPresent() && result.get() == ButtonType.OK) {
                	String name = alert.getName();
                	int time = alert.getTime();
                	if (name.isBlank()) { // isBlank checks for spaces too. So we make sure the user enters an actual name. 
    					Alert al = new Alert(Alert.AlertType.ERROR, "No name has been entered!");
    					alert.setHeaderText(null); 
    					al.showAndWait();
                	} else if (!(map.getEdgeBetween(from, to) == null)) {
    					Alert al = new Alert(Alert.AlertType.ERROR, "There is already a connection between these places!");
    					alert.setHeaderText(null); 
    					al.showAndWait();
                	} else {
	                	map.connect(from, to, name, time); // Call connect from ListGraph to create the connection.
	                	changed = true; // Because only if pressing "Ok" do we actually make changes. 
                	}
                }
			} catch(NumberFormatException e) { 
				Alert alert = new Alert(Alert.AlertType.ERROR, "Incorrect input!");
				alert.setHeaderText(null); 
				alert.showAndWait();
				return;
			}
		}
	}
	
	class RemoveConnectionHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent arg0) {
			try {
				if (from == null || to == null || map.getEdgeBetween(from, to) == null) {
					Alert alert = new Alert(Alert.AlertType.ERROR, "There is no direct connection between the two selected places!");
					alert.showAndWait();
				} else {
					Alert alert = new Alert(Alert.AlertType.CONFIRMATION, 
							"Disconnect " + from + " and " + to + "?");
					Optional<ButtonType> result = alert.showAndWait();
					if (result.get() == ButtonType.OK) {
						map.disconnect(from, to);
					}
					changed = true;
				}
			} catch(NumberFormatException e) { 
				Alert alert = new Alert(Alert.AlertType.ERROR, "Incorrect input!");
				alert.setHeaderText(null); 
				alert.showAndWait();
				return;
			}
		}
	}
	
	class ChangeConnectionHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent arg0) {
			if (!(placesMarkedCounter == 2)) {
				Alert alert = new Alert(Alert.AlertType.ERROR,
                        "Two places must be selected!"); 
				alert.setHeaderText(null); 
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK)
                    return; 
			} else if (map.getEdgeBetween(from, to) == null) { // If there is no edge/connection between them. 
				Alert alert = new Alert(Alert.AlertType.ERROR,
                        "The two places are not connected!"); 
				alert.setHeaderText(null); 
                alert.showAndWait();
			} else {
				try {
					ChangeConnectionAlert alert = new ChangeConnectionAlert();
	            	alert.setHeaderText("Connection from " + from + " to " + to); // Call the from and to fields. 
	            	alert.setName(map.getEdgeBetween(from, to).getName()); // We set the name of the Edge. 
	            	int oldTime = map.getEdgeBetween(from, to).getWeight(); // Get the time. 
	            	alert.setTime(String.valueOf(oldTime)); // We set the time of the Edge. 
	            	Optional<ButtonType> result = alert.showAndWait();
	            	if (result.isPresent() && result.get() == ButtonType.CANCEL) { 
	            		return;
	            	} else if (result.isPresent() && result.get() == ButtonType.OK) { // If the user presses OK.  
	            		int newTime = alert.getTimeField(); // The current (latest written) timeField is stored. 
	            		String newName = alert.getNameField();
	            		map.changeConnection(from, to, newTime, newName); // This updates the time/name both ways. 
	            		map.changeConnection(to, from, newTime, newName); 
	            		changed = true;
	            	}
				} catch(NumberFormatException e) { 
					Alert al = new Alert(Alert.AlertType.ERROR, "Incorrect input!");
					al.showAndWait();
					return;
				}
			}
		}
	}
	
	class SaveHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent arg0) {
			changed = false; // This indicates that we have saved, as we have no changes since the last save. 
			String fileName =  "edges.graph";
			try {
				FileWriter writer = new FileWriter(fileName);
	            PrintWriter out = new PrintWriter(writer);
	            
	            List<Place> places = new ArrayList<>(); 
	            for (Place place: map.getNodes()) {
	            	places.add(place);
	            }
	            for (Place place: places) {
	            	Collection<Edge<Place>> edgesFrom = map.getEdgesFrom(place);
	            	for (Edge<Place> edge: edgesFrom) {
	            		out.print("\n" + place + ";"); 
	            		out.print(edge.getDestination() + ";"); 
	            		out.print(edge.getName() + ";");
	            		out.print(edge.getWeight());
	            	}
	            } 
	            out.close();
	        } catch(IOException e){
	        	Alert alert = new Alert(Alert.AlertType.ERROR,"IO-fel " + e.getMessage());
	        	alert.showAndWait();
	        }
		}
	}
	
	class OpenHandler implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent arg0) {
            try{
            	String filename = "edges.graph";
            	placesMarkedCounter = 0; 
            	fromBox.setValue(null);
            	toBox.setValue(null);
            	toBox.setDisable(true);
            	from = null;
            	to = null;
            	swapButton.setDisable(true);
            	searchButton.setDisable(true);
            	// All of the above is essentially to reset everything as the user opens again. Unmark the places and all. 

            	if (changed) {
            		Alert alert = unsavedAlert(); // Call our own unsavedAlert() method.
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.CANCEL)
                        return; 
                    changed = false; // Because we are loading the last saved map. It is as of now, fully saved. 
            	} 

            	// Now we have to load all of the information. 
            	FileReader reader = new FileReader(filename);
                BufferedReader in = new BufferedReader(reader);  
                
                in.readLine(); // This is because the first line is empty. 
                
            	for (Place place: map.getNodes()) {
            		map.remove(place); // We remove all places in order to remove the edges really. The only way I could get it to work. 
            	}
            	loadPlaces(); // We load all the Places again, but not the edges, as those will be done below. 
                
                String line; 
                while ((line = in.readLine()) != null){
                	String[] edgeProperties = line.split(";");
                	String fromString = edgeProperties[0];
                	String toString = edgeProperties[1];
                	String edgeName = edgeProperties[2];
                	int edgeWeight = Integer.parseInt(edgeProperties[3]);
                	Place from = null; // Have to initialize these here. 
                	Place to = null;
                	for (Place place: map.getNodes()) { // We find the from and to places that match the strings. 
                		if (fromString.equals(place.getName())) {
                			from = place;
                		} 
                		if (toString.equals(place.getName())) { 
                			to = place;
                		}
                	}
                	
                	if (map.getEdgeBetween(from, to) == null) {
                		map.connect(from, to, edgeName, edgeWeight); // With this, we have created all the connections. 
                	}
                }
                in.close();
            }catch(IOException e){ 
                Alert alert = new Alert(Alert.AlertType.ERROR,"IO error " + e.getMessage());
                alert.showAndWait();
            }
		}
	 }
	
    class ExitHandler implements EventHandler<WindowEvent>{ // For when the user exits the program overall. 
        @Override public void handle(WindowEvent event){
            if (changed){
                Alert alert = unsavedAlert(); // Call our own unsavedAlert() method. 
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.CANCEL) 
                    event.consume(); 
            }
        }
    }
    
	private Alert unsavedAlert() { // To call later to confirm unsaved changes. 
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Unsaved changes, continue anyway?"); 
        alert.setTitle("Warning!"); 
        alert.setHeaderText(null);  
        return alert;
	}
	
    class ExitItemHandler implements EventHandler<ActionEvent>{ // For when the user exits the menu. 
        @Override public void handle(ActionEvent event){
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        } 
    }

	private void loadPlaces() { // Pre-loaded Places. 
		loadPlace("Fridhemsplan", 264.8, 330.4);
		loadPlace("Gamla Stan", 554.4, 418.4);
		loadPlace("Gröna Lund", 762.4, 360.8);
		loadPlace("Gärdet", 763.2, 65.6);
		loadPlace("Henriksdal", 865.6, 546.4);
		loadPlace("Hornstull", 332, 552);
		loadPlace("Hötorget", 477.6, 271.2);
		loadPlace("Kaknäs", 979.2, 240);
		loadPlace("Karlaplan", 696.0, 181.6);
		loadPlace("Karolinska Institutet", 199.2, 119.2);
		loadPlace("Kristineberg", 64.8, 343.2);
		loadPlace("Kungsträdgården", 544.8, 328.0);
		loadPlace("Manilla", 1027.2, 323.2);
		loadPlace("Mariatorget", 496.8, 520.0);
		loadPlace("Medborgarplatsen", 624, 537.6);
		loadPlace("Odenplan", 378.4, 167.2);
		loadPlace("Rådhuset", 411.2, 347.2);
		loadPlace("Rådmansgatan", 461.6, 194.4);
		loadPlace("Skanstull", 651.2, 620.8);
		loadPlace("Slussen", 591.2, 483.2);
		loadPlace("St Eriksplan", 300.8, 227.2);
		loadPlace("Stadion", 612.8, 137.6);
		loadPlace("Stadshagen", 140.8, 257.6);
		loadPlace("T-Centralen", 477.6, 320.0);
		loadPlace("Tekniska Högskolan", 489.6, 89.6);
		loadPlace("Thorildsplan", 156.8, 352.0);
		loadPlace("Västra Skogen", 34.4, 175.2);
		loadPlace("Zinkensdamn", 439.2, 515.2);
		loadPlace("Östermalmstorg", 576.8, 263.2);
	}
	
	private void loadPlace(String name, double x, double y) {
		Place place = new Place(name, x, y);
		map.add(place);
		center.getChildren().add(place);
    	Label label = new Label(name);
    	label.relocate(x-23, y-23);
    	label.setFont(Font.font(null, FontWeight.BOLD, 10));
    	center.getChildren().add(label);
    	label.setDisable(true); // Just to make sure it is not clickable. To not get in the way. 
    	label.setStyle("-fx-opacity: 1;"); // Fix the color to make it black, not grayed out. 
    	fromBox.getItems().add(name);
    	toBox.getItems().add(name); // These last two rows load all the places in the two drop down lists. 
	}
	
	private void loadEdges() { // Pre-loaded Edges. 
		loadEdge("Tekniska Högskolan", "Stadion", "Red-line subway", 4);
		loadEdge("Stadion", "Östermalmstorg", "Red-line subway", 2);
		loadEdge("Östermalmstorg", "Karlaplan", "Red-line subway", 2);
		loadEdge("Karlaplan", "Gärdet", "Red-line subway", 3);
		loadEdge("Östermalmstorg", "T-Centralen", "Red-line subway", 1);
		loadEdge("T-Centralen", "Gamla Stan", "Red-line subway", 3);
		loadEdge("Gamla Stan", "Slussen", "Red-line subway", 2);
		loadEdge("Slussen", "Mariatorget", "Red-line subway", 3);
		loadEdge("Mariatorget", "Zinkensdamn", "Red-line subway", 1);
		loadEdge("Zinkensdamn", "Hornstull", "Red-line subway", 1);
		loadEdge("Kristineberg", "Thorildsplan", "Green-line subway", 2);
		loadEdge("Thorildsplan", "Fridhemsplan", "Green-line subway", 1);
		loadEdge("Fridhemsplan", "St Eriksplan", "Green-line subway", 2);
		loadEdge("St Eriksplan", "Odenplan", "Green-line subway", 2);
		loadEdge("Odenplan", "Rådmansgatan", "Green-line subway", 3);
		loadEdge("Rådmansgatan", "Hötorget", "Green-line subway", 2);
		loadEdge("Hötorget", "T-Centralen", "Green-line subway", 2);
		loadEdge("Slussen", "Medborgarplatsen", "Green-line subway", 2);
		loadEdge("Medborgarplatsen", "Skanstull", "Green-line subway", 2);
		loadEdge("Kungsträdgården", "T-Centralen", "Blue-line subway", 1);
		loadEdge("T-Centralen", "Rådhuset", "Blue-line subway", 1);
		loadEdge("Rådhuset", "Fridhemsplan", "Blue-line subway", 2);
		loadEdge("Fridhemsplan", "Stadshagen", "Blue-line subway", 3);
		loadEdge("Stadshagen", "Västra Skogen", "Blue-line subway", 3);
		loadEdge("T-Centralen", "Kaknäs", "Bus 69", 14);
		loadEdge("Tekniska Högskolan", "Karlaplan", "Bus 67", 5);
		loadEdge("Karlaplan", "Kaknäs", "Bus 67", 7);
		loadEdge("Kaknäs", "Manilla", "Bus 67", 10);
		loadEdge("T-Centralen", "Manilla", "Commuter train", 35);
		loadEdge("Slussen", "Henriksdal", "Bus 471", 13);
		loadEdge("T-Centralen", "Gröna Lund", "Commuter train", 9);
		loadEdge("Karlaplan", "Gröna Lund", "Bus 67", 5);
		loadEdge("Gröna Lund", "Manilla", "Bus 67", 6);
		loadEdge("Tekniska Högskolan", "Odenplan", "Bus 4", 6);
		loadEdge("Odenplan", "T-Centralen", "Commuter Train", 3);
		loadEdge("St Eriksplan", "Karolinska Institutet", "Bus 54", 4);
		loadEdge("Skanstull", "Karolinska Institutet", "Bus 34", 34);
		loadEdge("Skanstull", "Henriksdal", "Bus 34", 10);
		loadEdge("Fridhemsplan", "Karlaplan", "Bus 7", 15);
		loadEdge("Thorildsplan", "Hornstull", "Bus 74", 21);
	}
	
	private void loadEdge(String from, String to, String modeOfTransportation, int time) {
		Place placeFrom = null;
		Place placeTo = null;
		for (Place place: map.getNodes()) {
			if (place.getName().equals(from)) {
				placeFrom = place;
			} else if (place.getName().equals(to)) {
				placeTo = place;
			}
		}
		map.connect(placeFrom, placeTo, modeOfTransportation, time);
	}
	
	protected class Place extends Circle { 
		
		private String name;
		private boolean marked; 
		private double x;
		private double y;
		private boolean wasClicked;
		
		protected Place(String name, double x, double y){
			this.name = name;
			setRadius(6); // The object of this class itself is a Circle, so we act directly upon it. 
			this.x = x;
			this.y = y; 
			relocate(x-10, y-10); 
			setFill(Color.BLUE);
			setOnMouseClicked(new ClickHandler());
		}
		
		protected boolean isMarked() { // This is to check outside, to be able to connect two Places. 
			return marked;
		}
		
		protected boolean wasClicked() {
			return wasClicked;
		}
		
		protected void setMarked(boolean marked) {
			this.marked = marked;
		}
		
		protected String getName() {
			return name;
		}

		protected double getX() { 
			return x;
		}

		protected double getY() {
			return y;
		}

		@Override
		public String toString(){
			return name;
		}
		
	    class ClickHandler implements EventHandler<MouseEvent> {    	
			@Override
			public void handle(MouseEvent arg0) {
				
				wasClicked = true;
				
				if (placesMarkedCounter == 0) { 
					from = Place.this; 
					fromBox.setValue(Place.this.getName()); // To update the ChoiceBoxes as the user marks Places on the map. 
					toBox.setDisable(false); // To activate the toBox now that a "from" value is chosen. 
				} else if (placesMarkedCounter == 1) { 
					to = Place.this; // This only serves use if we press a blue Place. It gets undone further below if we unmark a red Place. 
					toBox.setValue(Place.this.getName()); // To update the ChoiceBox. 
				} 
				
				if (marked) {
					setFill(Color.BLUE); 
					placesMarkedCounter--; 
					marked = false;
					// The below if and else is to update the ChoiceBoxes appropriately. 
					if (Place.this.equals(from)) { // If it is a from, the to becomes the from. Changed from if (isFrom). 
						fromBox.setValue(toBox.getValue()); // So we swap the values. 
						toBox.setValue(null); // And make the toBox empty. 
					} else {
						toBox.setValue(null);
					}
					if (placesMarkedCounter == 0) { // This is is we ONLY have a from marked, and now unmark it. 
						from = null; // from is now null, and to becomes null below so both are null as both are unmarked. 
						fromBox.setValue(null); // Then the fromBox should naturally become empty. 
						toBox.setDisable(true); // Because the fromBox is now empty, and we don't want the user to be able to choose "to". 
					} // It needs to be here because isFrom is made false if we have one places marked and unmarked it. In the else if further above. 
					// So it needs to be outside of the if (isFrom) block. 
					
					to = null; // Added this after realizing that "to" was made Place.this further above if the counter was 1. 
					// But that should only happen if it was unmarked. If marked, that means we are pressing the "from". 
					// So because we turned "to" into this "from" that we pressed, we need to turn it back to null. 
					// There is no scenario where we have two marked places, and we unmark one, that null shouldn't be null. 
					
					for (Place pl: map.getNodes()) { 
						if (pl.getFill().equals(Color.RED)) { 
							from = pl; // So "from" becomes the only red Place, and "to" as seen above is already null. 
						} 
					}
				} else {
					if (placesMarkedCounter < 2) {
						setFill(Color.RED); 
						placesMarkedCounter++; 
						marked = true;
					}
				}
				
				if (placesMarkedCounter == 2) {
					searchButton.setDisable(false);
					swapButton.setDisable(false);
				} else {
					searchButton.setDisable(true);
					swapButton.setDisable(true);
				}
			}
	    }
	}
}
