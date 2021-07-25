package publicTransit;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class ChangeConnectionAlert extends Alert { 
	
	private TextField nameField = new TextField();
	private TextField timeField = new TextField();

	public ChangeConnectionAlert() {
		super(AlertType.CONFIRMATION);
		GridPane grid = new GridPane(); 
		grid.addRow(0, new Label("Name:  "), nameField); 
		grid.addRow(1, new Label("Time:  "), timeField); 
		grid.setVgap(10); 
		grid.setAlignment(Pos.CENTER);
		getDialogPane().setContent(grid); 
		setTitle("Connection"); 
		setHeaderText(null);
		getDialogPane().setPrefSize(350, 100);
	}

	public void setName(String name) {
		nameField.setText(name); 
	}

	public void setTime(String time) {
		timeField.setText(time);
	}

	public int getTimeField() {
		return Integer.parseInt(timeField.getText()); 
	}
	
	public String getNameField() {
		return nameField.getText(); 
	}
}
