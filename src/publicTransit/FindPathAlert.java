package publicTransit;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;

public class FindPathAlert extends Alert {
	
	private TextArea textArea = new TextArea();
	
	public FindPathAlert() {
		super(AlertType.INFORMATION);
		FlowPane flow = new FlowPane(); 
		flow.getChildren().add(textArea);
		getDialogPane().setContent(flow);
	}
	
	public void setText(String string) { 
		textArea.clear();
		textArea.setText(string);
	}
}
