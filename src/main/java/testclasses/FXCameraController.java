package testclasses;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import testclasses.utils.Utils;
import org.apache.kafka.clients.admin.NewTopic;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.awt.image.BufferedImage;
import java.io.IOException;
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
public class FXCameraController {
    // the FXML button
    @FXML
    private Button button;
    // the FXML image view
    @FXML
    private ImageView currentFrame;

    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;
    // the OpenCV object that realizes the video capture
    //private VideoCapture capture0 = new VideoCapture();
    private VideoCapture publisherApp = new VideoCapture();
    // a flag to change the button behavior
    private boolean cameraActive = false;
    // the id of the camera to be used
    private static int cameraId = 0;
    String topicName = "stream9";
    NewTopic topicCreated = ProducerVideoMessages.createTopics(topicName);
    int i = 0;
    public FXCameraController() throws IOException {
    }

    /**
     * The action triggered by pushing the button on the GUI
     *
     * @param event the push button event
     */
    @FXML
    protected void startCamera(ActionEvent event) {
        if (!this.cameraActive) {
            System.out.println("camera active? : "+this.cameraActive);
            // start the video capture
//            this.publisherApp.set(CV_CAP_PROP_FRAME_WIDTH,320);
//            this.publisherApp.set(CV_CAP_PROP_FRAME_HEIGHT,240);
            this.publisherApp.open(cameraId);
            // is the video stream available?
            if (this.publisherApp.isOpened()) {
                this.cameraActive = true;
                // grab a frame every 33 ms (30 frames/sec)
                Runnable frameGrabber = new Runnable() {

                    @Override
                    public void run() {
                        Mat frame = grabFrame();
                        Image imageToShow = Utils.mat2Image(frame,i++, topicCreated.name());
                        updateImageView(currentFrame, imageToShow);

                    }
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                 this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                // update the button content
                this.button.setText("Stop Camera");
            } else {
                // log the error
                System.err.println("Impossible to open the camera connection...");
            }
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
     * Get a frame from the opened video stream (if any)
     *
     * @return the {@link Mat} to show
     */
    private Mat grabFrame() {
        // init everything
        Mat frame = new Mat();
        BufferedImage bi = null;
        // check if the capture is open
        if (this.publisherApp.isOpened()) {
            try {
                // read the current frame
                this.publisherApp.read(frame);

                // if the frame is not empty, process it
                if (!frame.empty()) {
                     System.out.println("frame : "+ frame);
                    Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2RGB);

                    // bi = new BufferedImage(frame.width(),frame.height(),BufferedImage.TYPE_3BYTE_BGR);
                    //byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
                   /* byte[] return_buff = new byte[(int) (frame.total() *
                            frame.channels())];
                    frame.get(0,0,return_buff);*/
                    //System.out.println("byteArrays from toFrame() : "+data);
                    System.out.println("Frame size and type : " + frame.size() + ", " + frame.type());
                    /**
                     * This is the third way of sending mat :Testing if it works
                     */
                   /* // for writing
                    MatOfByte frameBytes = new MatOfByte(frame.reshape(1, frame.rows() * frame.cols() * frame.channels()));
                    byte[] bytes_ = frameBytes.toArray();

                    String s;
//                    s = Base64.getEncoder.getencodeToString(bytes_, Base64.DEFAULT);
                    byte[] encodedString = Base64.encodeBase64(bytes_);
                  Base64.encodeBase64(encodedString);
                  s = new String(encodedString);
                    System.out.println("frames : "+ frame);
                    System.out.println("Encode it into a string : "+s );

                    byte[] bytestt_ = Base64.decodeBase64(s);
                    MatOfByte bytes =  new MatOfByte(bytestt_);
                    Mat desc = new Mat(frame.rows() * frame.cols() * frame.channels(),1,frame.type());
                    bytes.convertTo(frame,frame.type());
                    desc = desc.reshape(frame.channels(),frame.rows());
                    System.out.println("Again convert back to mat type : "+desc);
                */
//                   byte[] data =  new byte[(int) (frame.total() *  frame.channels())];
//                    System.out.println("Frames before conversion : "+frame);
//                    System.out.println("data of byte array : " +data);
//                    String s = new String(Base64.encodeBase64(data));
//                    System.out.println("test for string for producer : "+s);
//                    byte[] b = s.getBytes();
//                    System.out.println("data of byte array ??? : "+b);
//                    Mat mat = Imgcodecs.imdecode(new MatOfByte(b),
//                            Imgcodecs.IMREAD_UNCHANGED);
//                    System.out.println("Frames after conversion : "+mat);
//                    System.out.println("Frames before conversion : "+frame);
//                    byte[] data = new byte[(int) (frame.cols() * frame.rows() * frame.elemSize())];
//                    frame.get(0, 0, data);
//                    System.out.println("byte array : "+data);
//                    System.out.println("Frames after byte[] : "+frame);
//                    String dataString = new String(Base64.encodeBase64(data));
//                    System.out.println("String conversion: "+dataString);
//
//                    byte[] data1 = Base64.decodeBase64(dataString.getBytes());
//                    System.out.println("Consumer byte array from string : "+data1);
//                    Mat mat1 = null;
//                    mat1.put(0,0,data1);
//                    System.out.println("Frames on consumer side :"+ mat1);

                    //  ProducerVideoMessages.sendImages(return_buff,topicCreated.name());
                }

            } catch (Exception e) {
                // log the error
                System.err.println("Exception during the image elaboration: " + e);
            }
        }

        return frame;
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

        if (this.publisherApp.isOpened()) {
            // release the camera
            this.publisherApp.release();
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