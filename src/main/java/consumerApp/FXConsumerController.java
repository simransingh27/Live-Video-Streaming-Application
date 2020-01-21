package consumerApp;

import consumerApp.utils.Utils;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The controller has been enhanced and changed to Kafka Consumer
 * Two consumer were implemented for live video and On-demand video
 * Forward and rewind functionality were added for on-demand video
 */

public class FXConsumerController {

    private static long currentOffset;
    private static String currentValueOfButton;
    private static String newVideo = "";
    final String[] topicName = {null};
    // the FXML button
    @FXML
    private Button button;
    @FXML
    private Button button1;
    @FXML
    private Button button2;
    @FXML
    private Button button3;
    @FXML
    private Button stream1;
    @FXML
    private Button stream2;
    // the FXML image view
    @FXML
    private ImageView currentFrame;
    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;
    // a flag to change the button behavior
    private boolean cameraActive = false;
    private boolean replayActive = false;
    private String globalTopicName = null;


    public FXConsumerController() {

    }

    /**
     * Forward functionality
     */
    public void forwardVideo() {
        currentValueOfButton = "forward";
        currentOffset += 340;
        System.out.println("value of current offset in forward : " + currentOffset);

    }

    /**
     * Rewind functionality.
     */
    public void rewindVideo() {
        if (currentOffset >= 0) {
            currentValueOfButton = "rewind";
            currentOffset -= 310;
            System.out.println("value of Current offset in back method : " + currentOffset);
        } else {
            currentOffset = 0;
        }
    }

    /**
     * The action triggered by pushing the button on the GUI
     * This method is basically a Kafka consumer
     * Many feature were implemented - on-demand video .
     * Forward and Rewind Functionality.
     */
    @FXML
    protected void replayCamera(ActionEvent event) {
        if (!replayActive) {
            replayActive = true;
            button2.setDisable(false);
            button3.setDisable(false);
            stream1.setDisable(true);
            stream2.setDisable(true);
            newVideo = "replayCamera";
            button1.setText("Stop");
            final String[] topicName = {globalTopicName};
            Node node = (Node) event.getSource();
            Runnable frameGrabber = new Runnable() {
                @Override
                public void run() {
                    if (topicName[0] != null) {
                        TopicPartition actualTopicPartition = new TopicPartition(topicName[0], 0);
                        KafkaConsumer consumerReplay = new KafkaConsumer(Property.loadPropertiesReplay());
                        consumerReplay.subscribe(Arrays.asList(topicName[0]));
                        try {
                            int attemptCount = 0;
                            while (attemptCount <= 3) {
                                ConsumerRecords<String, byte[]> recordReplay = consumerReplay.poll(Duration.ofMillis(1000));
                                if (recordReplay.count() > 0) {
                                    if (replayActive) {
                                        for (ConsumerRecord<String, byte[]> record : recordReplay) {
                                            if (currentValueOfButton == "forward") {
                                                consumerReplay.seek(actualTopicPartition, currentOffset);
                                                currentValueOfButton = null;
                                            } else if (currentValueOfButton == "rewind") {
                                                consumerReplay.seek(actualTopicPartition, currentOffset);
                                                currentValueOfButton = null;
                                            }
                                            currentOffset = consumerReplay.position(actualTopicPartition);
                                            Thread.sleep(33);//control frame rate.
                                            updateImageView(currentFrame, FXConsumerController.renderVideo(record.value()));
                                            consumerReplay.commitAsync();
                                        }
                                    } else {
                                        attemptCount = 4;
                                    }
                                } else {
                                    attemptCount++;
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            System.out.println("consumerReplay close node : ");
                            consumerReplay.close();
                        }
                    }
                }
            };
            this.timer = Executors.newSingleThreadScheduledExecutor();
            this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
        } else {
            replayActive = false;
            button1.setText("Replay");
            this.stopAcquisition();

        }
    }


    /**
     * The action triggered by pushing the button on the GUI
     * This method is basically a Kafka consumer
     * Many feature were implemented -Live video , lag issues were fixed when video stops
     * Latest offset were commited manually including Kafka async method.
     */
    @FXML
    protected void startCamera(ActionEvent event) {
        if (!cameraActive) {
            cameraActive = true;
            button.setDisable(false);
            button.setText("Stop");
            button1.setDisable(false);
            boolean button2Value = button2.isDisable();
            boolean button3Value = button3.isDisable();
            boolean stream1Value = stream1.isDisable();
            boolean stream2Value = stream2.isDisable();

            if (!button2Value) {
                button2.setDisable(true);
            }
            if (!button3Value) {
                button3.setDisable(true);
            }
            if (stream1Value) {
                stream1.setDisable(false);
            }
            if (stream2Value) {
                stream2.setDisable(false);
            }
            Node node = (Node) event.getSource();
            String data = (String) node.getUserData();
            if (data != null) {
                topicName[0] = data;
                globalTopicName = topicName[0];
            }
            Runnable imageGrab = () -> {
                KafkaConsumer consumerLiveVideo = new KafkaConsumer(Property.loadPropertiesLive());
                consumerLiveVideo.subscribe(Collections.singletonList(topicName[0]));
                try {
                    int attemptCount = 0;
                    while (attemptCount <= 3) {
                        ConsumerRecords<String, byte[]> recordsLive = consumerLiveVideo.poll(Duration.ofMillis(100));
                        if (recordsLive.count() > 0) {
                            if (cameraActive) {
                                for (ConsumerRecord<String, byte[]> record : recordsLive) {
                                    updateImageView(currentFrame, FXConsumerController.renderVideo(record.value()));
                                    consumerLiveVideo.commitAsync();
                                }
                            } else {
                                attemptCount = 4;
                                stopAcquisition();
                                cameraActive = false;
                                // update again the button content
                                button.setText("Back Live ");
                            }
                        } else {
                            attemptCount++;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    consumerLiveVideo.close();
                    stopAcquisition();
                }
            };
            this.timer = Executors.newSingleThreadScheduledExecutor();
            this.timer.scheduleAtFixedRate(imageGrab, 0, 33, TimeUnit.MILLISECONDS);
            // this.stopAcquisition();
        } else {
            // the camera is not active at this point
            cameraActive = false;
            // update again the button content
            button.setText("Back Live ");
            // stop the timer
            this.stopAcquisition();
        }

    }

    public static Image renderVideo(byte[] record) {
        ByteArrayInputStream bis = new ByteArrayInputStream(record);
        BufferedImage inputImage = null;
        try {
            inputImage = ImageIO.read(bis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Image imageToShow = SwingFXUtils.toFXImage(inputImage, null);

        return imageToShow;
    }

    /**
     * Stop the acquisition from the frames and release all the resources
     */

    private void stopAcquisition() {
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                // stop the timer
                this.timer.shutdown();
                this.timer.awaitTermination(3, TimeUnit.MILLISECONDS);
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