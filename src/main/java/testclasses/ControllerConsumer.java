package testclasses;

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
import org.apache.kafka.common.TopicPartition;
import consumerApp.utils.Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * The controller has been enhanced and changed to Kafka Consumer
 * Two consumer were implemented for live video and On-demand video
 * Forward and rewind functionality were added for on-demand video
 */
public class ControllerConsumer {
    // the id of the camera to be used
    private static int cameraId = 0;
    int i = 0;
    ConsumerRecords<String, byte[]> records = null;
    KafkaConsumer consumer = null;
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
    private boolean playVideo = false;

    public ControllerConsumer() throws IOException {
    }

    /**
     * The action triggered by pushing the button on the GUI
     *This method is basically a Kafka consumer
     * Many feature were implemented -Live video , lag issues were fixed when video stops
     * Latest offset were commited manually including Kafka async method.
     */
    @FXML
    protected void startCamera(ActionEvent event) throws IOException {
        final String topicName = "testt10";
        int i = 0;
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "testt10");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put("enable.auto.commit", false);
        consumer = new KafkaConsumer(props);
        consumer.subscribe(Arrays.asList(topicName));

        if (!this.playVideo) {
            this.playVideo = true;
            final int[] attemptCount = {0};
            TopicPartition actualTopicPartition = new TopicPartition(topicName, 0);
            final TopicPartition finalActualTopicPartition = actualTopicPartition;

            Runnable frameGrabber = new Runnable() {
                @Override
                public void run() {
                    while (attemptCount[0] <= 2) {
                        records = consumer.poll(10000);
                        if (records.count() > 0) {  //frame available == true
                            Long frames = null;
                            ArrayList<Long> addFrameNumber = new ArrayList<Long>();
                            if (playVideo) {
                                for (ConsumerRecord<String, byte[]> record : records) {
                                    //consumer.seekToEnd(Arrays.asList(actualTopicPartition));
                                    long actualPosition = consumer.position(finalActualTopicPartition);
                                    frames = actualPosition;
                                    System.out.println("current frame number : " + actualPosition);
                                    ByteArrayInputStream bis = new ByteArrayInputStream(record.value());
                                    BufferedImage inputImage = null;
                                    try {
                                        inputImage = ImageIO.read(bis);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    System.out.println("test images : " + inputImage);
                                    Image imageToShow = SwingFXUtils.toFXImage(inputImage, null);
                                    updateImageView(currentFrame, imageToShow);
                                    consumer.commitAsync();
                                }
                            } else {
                                playVideo = false;

                                // update again the button content
//                                button.setText("Live video test");
//                                for (ConsumerRecord<String, byte[]> record : records) {
//                                    long actualPosition = consumer.position(finalActualTopicPartition);
//                                    addFrameNumber.add(actualPosition);
//                                    System.out.println("latest frame number : " + actualPosition+" frames when stoped : "+frames);
//                                    consumer.commitAsync();
//                                    if(playVideo)
//                                      break;
//
//                                }


                            }
                        } else {
                            System.err.println("No frames generated ");
                            attemptCount[0] = attemptCount[0] + 1;
                        }
                    }
                }
            };
            this.timer = Executors.newSingleThreadScheduledExecutor();
            this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
            this.button.setText("Stop video");
            consumer.commitAsync();


        } else {
            playVideo = false;

            // update again the button content
            button.setText("Live video");
//            actualTopicPartition = new TopicPartition(topicName, 0);
//
//                long actualPosition = consumer.position(actualTopicPartition);
//                System.out.println("lets check latest offset before going live again "+ actualPosition);

            consumer.commitAsync();
            consumer.close();
            stopAcquisition();
        }


        // update the button content
//        } else {
//            this.cameraActive = false;
//            // update again the button content
//            this.button.setText("Test");
//            consumer.commitAsync();
//            consumer.close();
//            stopAcquisition();
//        }
    }

//            Runnable frameGrabber = new Runnable() {
//                @Override
//                public void run() {
//
//                    try {
//                        int attemptCount = 0;
//                        int i = 0;
//                        while (attemptCount <= 3) {
//
//                            if (records.count() > 0 ){
//                                if(cameraActive == true) {
//                                    button.setText("Stop video");
//                                    for (ConsumerRecord<String, byte[]> record : records) {
//                                        ByteArrayInputStream bis = new ByteArrayInputStream(record.value());
//                                        BufferedImage inputImage = ImageIO.read(bis);
//                                        System.out.println("test images : " + inputImage);
//                                        Image imageToShow = SwingFXUtils.toFXImage(inputImage, null);
//                                        updateImageView(currentFrame, imageToShow);
//                                        System.out.println("testing in loop  !");
//                                    }
//                                }else {
//                                    button.setText("Back To Live");
//                                    consumer.close();
//                                }
//
//                            } else {
//                                attemptCount++;
//                            }
//                        }
//                } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            };
//
//            this.timer = Executors.newSingleThreadScheduledExecutor();
//            System.out.println("test after thread ");
//            this.timer.scheduleAtFixedRate(frameGrabber, 10, 10, TimeUnit.SECONDS);

//            // update the button content
//            this.button.setText("Back To Live");
//            System.out.println("test after camera stop ");
//        } else {
//            // the camera is not active at this point
//            this.cameraActive = false;
//            // update again the button content
//            this.button.setText("Live");
//            System.out.println("final test? ");
//            // stop the timer
//            this.stopAcquisition();
//        }


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