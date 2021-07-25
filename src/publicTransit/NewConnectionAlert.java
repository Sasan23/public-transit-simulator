package publicTransit;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class NewConnectionAlert extends Alert { 
	
	private TextField name = new TextField();
	private TextField time = new TextField();

	public NewConnectionAlert() {
		super(AlertType.CONFIRMATION);
		GridPane grid = new GridPane(); 
		grid.addRow(0, new Label("Name:  "), name); 
		grid.addRow(1, new Label("Time:  "), time); 
		grid.setVgap(10); 
		grid.setAlignment(Pos.CENTER);
		getDialogPane().setContent(grid); 
		setTitle("Connection"); 
		setHeaderText(null);
		getDialogPane().setPrefSize(350, 100);
	}

	public String getName() {
		return name.getText();
	}

	public int getTime() {
		return Integer.parseInt(time.getText()); 
	}
}
