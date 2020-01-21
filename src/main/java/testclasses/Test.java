package testclasses;

import org.apache.kafka.clients.admin.NewTopic;
import org.opencv.core.CvType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

public class Test {
    public static void testOpenCV() throws IOException {
        File input = new File("C:\\Users\\Dell\\Desktop\\DataFilesForTest\\InputImage\\image3.jpg");
        BufferedImage image = ImageIO.read(input);
        BufferedImage imageCopy = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        imageCopy.getGraphics().drawImage(image, 0, 0, null);
        byte[] data = ((DataBufferByte) imageCopy.getRaster().getDataBuffer()).getData();
        System.out.println("Buffered Image dimension : "+image.getHeight()+" , " + image.getWidth()+" , "+ CvType.CV_8UC3);
        System.out.println("Image byte[] : "+data);
        String topicName ="videoFXTopic";
        NewTopic topicCreated = ProducerVideoMessages.createTopics(topicName);
        ProducerVideoMessages.sendImages(data,topicCreated.name());


    }

    public static void main(String[] args) throws IOException {
        Test.testOpenCV();
    }
}
