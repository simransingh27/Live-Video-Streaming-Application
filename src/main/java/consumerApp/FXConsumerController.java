package consumerApp;

import consumerApp.utils.Utils;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.kafka.clients.consumer.ConsumerConfig;
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
import java.util.Properties;
import java.util.UUID;
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
    @FXML
    private Button stream3;

    // the FXML image view
    @FXML
    private ImageView currentFrame;
    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;
    // a flag to change the button behavior
    private boolean cameraActive = false;
    private boolean replayActive = false;
    private String globalTopicName = null;


    public FXConsumerController() throws IOException {

    }

    /**
     * Forward functionality
     */
    public void forwardVideo(ActionEvent actionEvent) {
        currentValueOfButton = "forward";
        currentOffset += 340;
        System.out.println("value of current offset in forward : " + currentOffset);

    }

    /**
     * Rewind functionality.
     */
    public void rewindVideo(ActionEvent actionEvent) {
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
            String data = (String) node.getUserData();
            System.out.println("testing for event handler : " + data);
            Runnable frameGrabber = new Runnable() {
                @Override
                public void run() {
                    if (topicName[0] != null) {
                        Long actualPosition = null;
                        TopicPartition actualTopicPartition = new TopicPartition(topicName[0], 0);
                        Properties props = new Properties();
                        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
                        props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString()); // randomise this
                        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
                        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArrayDeserializer");
                        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
                        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
                        KafkaConsumer consumerReplay = new KafkaConsumer(props);
                        consumerReplay.subscribe(Arrays.asList(topicName[0]));
                        //System.out.println("replay starts here");
                        try {
                            int attemptCount = 0;
                            while (attemptCount <= 3) {
                                ConsumerRecords<String, byte[]> recordReplay = consumerReplay.poll(Duration.ofMillis(1000));
                                if (recordReplay.count() > 0) {
                                    if (replayActive) {
                                        for (ConsumerRecord<String, byte[]> record : recordReplay) {
                                            ByteArrayInputStream bis = new ByteArrayInputStream(record.value());
                                            BufferedImage inputImage = ImageIO.read(bis);
                                            actualPosition = consumerReplay.position(actualTopicPartition);
                                            if (currentValueOfButton == "forward") {
                                                consumerReplay.seek(actualTopicPartition, currentOffset);
                                                System.out.println("current offset after forward pressed : " + currentOffset);
                                            } else if (currentValueOfButton == "rewind") {
                                                consumerReplay.seek(actualTopicPartition, currentOffset);
                                                System.out.println("current offset after  rewind pressed : " + currentOffset);
                                            }
                                            currentOffset = consumerReplay.position(actualTopicPartition);
                                            System.out.println("replay video");
                                            currentValueOfButton = null;

                                            consumerReplay.commitAsync();
                                            Thread.sleep(33);
                                            Image imageToShow = SwingFXUtils.toFXImage(inputImage, null);
                                            updateImageView(currentFrame, imageToShow);
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

            System.out.println("value of forward button : " + button2Value);
            System.out.println("value of rewind button : " + button3Value);
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
            Runnable imageGrab = new Runnable() {
                @Override
                public void run() {
                    Long actualPosition = null;
                    TopicPartition actualTopicPartition = new TopicPartition(topicName[0], 0);
                    Properties props = new Properties();
                    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
                    props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString()); // randomise this
                    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
                    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArrayDeserializer");
                    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
                    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
                    KafkaConsumer consumerLiveVideo = new KafkaConsumer(props);
                    consumerLiveVideo.subscribe(Arrays.asList(topicName[0]));
                    try {
                        int attemptCount = 0;
                        int i = 0;
                        while (attemptCount <= 3) {
                            ConsumerRecords<String, byte[]> recordsLive = consumerLiveVideo.poll(Duration.ofMillis(100));
                            if (recordsLive.count() > 0) {
                                if (cameraActive) {
                                    for (ConsumerRecord<String, byte[]> record : recordsLive) {
                                        actualPosition = consumerLiveVideo.position(actualTopicPartition);
                                        System.out.println("Live video");
                                        ByteArrayInputStream bis = new ByteArrayInputStream(record.value());
                                        BufferedImage inputImage = ImageIO.read(bis);
                                        Image imageToShow = SwingFXUtils.toFXImage(inputImage, null);
                                        updateImageView(currentFrame, imageToShow);
                                        consumerLiveVideo.commitAsync();

                                    }
                                } else {
                                    attemptCount = 4;
                                    stopAcquisition();
                                    cameraActive = false;
                                    // update again the button content
                                    button.setText("Back Live ");
                                    System.out.println("in this loop(camera one)");

                                }
                            } else {
                                attemptCount++;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        System.out.println("consumer close node : ");
                        consumerLiveVideo.close();
                        stopAcquisition();
                    }
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