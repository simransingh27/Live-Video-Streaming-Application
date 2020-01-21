package consumerApp.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import org.apache.commons.codec.binary.Base64;
import javafx.scene.image.Image;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;

/**
 * Provide general purpose methods for handling OpenCV-JavaFX data conversion.
 * Moreover, expose some "low level" methods for matching few JavaFX behavior.
 *
 * @author <a href="mailto:luigi.derussis@polito.it">Luigi De Russis</a>
 * @author <a href="http://max-z.de">Maximilian Zuleger</a>
 * @version 1.0 (2016-09-17)
 * @since 1.0
 */
public final class Utils {

    public static Image mat2Image1(Mat frame)
    {
        try
        {
            return SwingFXUtils.toFXImage(matToBufferedImage(frame), null);
        }
        catch (Exception e)
        {
            System.err.println("Cannot convert the Mat object: " + e);
            return null;
        }
    }

    public static Image mat2Image(BufferedImage consumeImage,int i) {
        try {
            ImageIO.write(consumeImage, "jpg",new File("C:\\Users\\Dell\\Desktop\\DataFilesForTest\\OpenCV_Consumer\\image"+i+".jpg"));
            return SwingFXUtils.toFXImage(consumeImage, null);
        } catch (Exception e) {
            System.err.println("Cannot convert the Mat obejct: " + e);
            return null;
        }
    }

    public static BufferedImage matToBufferedImage(Mat original) {
        // init
        BufferedImage image = null;
        int width = original.width(), height = original.height(), channels = original.channels();
        byte[] sourcePixels = new byte[width * height * channels];
        original.get(0, 0, sourcePixels);
        image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        System.out.println("sourcePixels : "+ sourcePixels);
        System.out.println("Image format TYPE_3BYTE_BGR : "+ width+" , "+height+" , "+channels );
        System.out.println("BufferedImage : "+image);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);
        System.out.println("target pixels : "+targetPixels);

        return image;
    }

    /**
     * Generic method for putting element running on a non-JavaFX thread on the
     * JavaFX thread, to properly update the UI
     *
     * @param property a {@link ObjectProperty}
     * @param value    the value to set for the given {@link ObjectProperty}
     */
    public static <T> void onFXThread(final ObjectProperty<T> property, final T value) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                property.set(value);
            }
        });
    }

    private static BufferedImage matToBufferedImage(byte[] consumerByteArrays) {
        // init
        BufferedImage image = null;

       // Mat ordinalMat = null;
      //  ordinalMat.put(640, 480,consumerByteArrays);
        image = new BufferedImage(640, 480, BufferedImage.TYPE_3BYTE_BGR);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();;
        System.arraycopy(consumerByteArrays, 0, targetPixels, 0, consumerByteArrays.length);
        return image;
    }
    public static Mat jsonToMat(String json){

        JsonParser parser = new JsonParser();
        JsonObject JsonObject = parser.parse(json).getAsJsonObject();
        int rows = JsonObject.get("rows").getAsInt();
        int cols = JsonObject.get("cols").getAsInt();
        int type = JsonObject.get("type").getAsInt();
        Mat mat = new Mat(rows, cols, type);

        String dataString = JsonObject.get("data").getAsString();
        if( type == 16 ) {
            byte[] data = Base64.decodeBase64(dataString.getBytes());
            mat.put(0, 0, data);
        }
        else {

            throw new UnsupportedOperationException("unknown type");
        }
        return mat;
    }
}