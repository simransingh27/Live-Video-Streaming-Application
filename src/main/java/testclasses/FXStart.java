package testclasses;

import org.opencv.core.Core;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;



/**
 * The main class for a JavaFX application. It creates and handle the main
 * window with its resources (style, graphics, etc.).
 *
 * @author <a href="mailto:luigi.derussis@polito.it">Luigi De Russis</a>
 * @version 1.5 (2016-09-17)
 * @since 1.0 (2013-10-20)
 *
 */
public class FXStart extends Application
{
    @Override
    public void start(Stage primaryStage)
    {
        try
        {
            // load the FXML resource
            FXMLLoader loader =  new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("FXView.fxml"));
            // FXMLLoader loader = new FXMLLoader(getClass().getResource("../../resources/FXViewCV.fxml"));
            // store the root element so that the controllers can use it
            BorderPane rootElement = (BorderPane) loader.load();
            // create and style a scene
            Scene scene = new Scene(rootElement, 700, 550);
            //URL url = this.getClass().getResource("application.css");
//            if (url == null) {
//                System.out.println("Resource not found. Aborting.");
//                System.exit(-1);
//            }
//            String css = url.toExternalForm();
           // scene.getStylesheets().add(css);
            scene.getStylesheets().add(getClass().getResource("/application.css").toExternalForm());
            // create the stage with the given title and the previously created
            // scene
            primaryStage.setTitle("Publisher Application");
            primaryStage.setScene(scene);
            // show the GUI
            primaryStage.show();

            // set the proper behavior on closing the application
            final FXCameraController controller = loader.getController();
            primaryStage.setOnCloseRequest((new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we)
                {
                    controller.setClosed();
                }
            }));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * For launching the application...
     *
     * @param args
     *            optional params
     */
    public static void main(String[] args)
    {
        // load the native OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }
}