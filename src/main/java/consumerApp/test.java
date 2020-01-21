package consumerApp;


import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import consumerApp.utils.Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The controller for our application, where the application logic is
 * implemented. It handles the button for starting/stopping the camera and the
 * acquired video stream.
 *
 * @author <a href="mailto:luigi.derussis@polito.it">Luigi De Russis</a>
 * @author <a href="http://max-z.de">Maximilian Zuleger</a> (minor fixes)
 * @version 2.0 (2016-09-17)
 * @since 1.0 (2013-10-20)
 */
class Test {
    // the FXML button
    @FXML
    private Button button;
    // the FXML image view
    @FXML
    private ImageView currentFrame;

    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;
    // the OpenCV object that realizes the video capture
    // a flag to change the button behavior
    private boolean cameraActive = false;
    // the id of the camera to be used
    private static int cameraId = 0;
    int i = 0;
    public Test() throws IOException {
    }

    /**
     * The action triggered by pushing the button on the GUI
     *
     * @param event the push button event
     */
    @FXML
    protected void startCamera(ActionEvent event) {
        if (!this.cameraActive) {
            // start the video capture

            // is the video stream available?
                this.cameraActive = true;

                // grab a frame every 33 ms (30 frames/sec)
                Runnable frameGrabber = new Runnable() {

                    @Override
                    public void run() {
                        Properties props = new Properties();
                        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
                        props.put(ConsumerConfig.GROUP_ID_CONFIG, "savingFrames6");
                        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
                        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArrayDeserializer");
                        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
                        KafkaConsumer consumer = new KafkaConsumer(props);
                        consumer.subscribe(Arrays.asList("savingFrames"));
                        BufferedImage image = null;
                        try {
                            int attemptCount = 0;
                            int i = 0;
                            while (attemptCount <= 3) {
                                ConsumerRecords<String, byte[]> records = consumer.poll(10000);
                                if (records.count() > 0) {
                                    for (ConsumerRecord<String, byte[]> record : records) {
                                        ByteArrayInputStream bis = new ByteArrayInputStream(record.value());
                                        BufferedImage inputImage = ImageIO.read(bis);
                                        System.out.println("test images : "+inputImage);
                                        Image imageToShow = SwingFXUtils.toFXImage(inputImage, null);
                                        updateImageView(currentFrame, imageToShow);
                                    }

                                } else {
                                    attemptCount++;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            consumer.close();
                        }
                    }
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                //    System.out.println("times for timestamp : "+timer);
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                // update the button content
                this.button.setText("Stop Camera");

        } else {
            // the camera is not active at this point
            this.cameraActive = false;
            // update again the button content
            this.button.setText("Start Camera");

            // stop the timer
            this.stopAcquisition();
        }
    }

        /**
     * Stop the acquisition from the camera and release all the resources
     */
    private void stopAcquisition() {
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                // stop the timer
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // log any exception
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }


    }

    /**
     * Update the {@link ImageView} in the JavaFX main thread
     *
     * @param view  the {@link ImageView} to update
     * @param image the {@link Image} to show
     */
    private void updateImageView(ImageView view, Image image) {
        Utils.onFXThread(view.imageProperty(), image);
    }

    /**
     * On application close, stop the acquisition from the camera
     */
    public void setClosed() {
        this.stopAcquisition();
    }

}