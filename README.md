# Live-Video-Streaming-Application
End-user application :supports live streaming and re-streaming(with forward and rewind functionality).

Overview :
The end-user application allows the user to play the broadcasted video from a resource with minimum latency. 
This application embeds a Kafka consumer application which fetches the messages from a Kafka Topic, combines the video frames
and renders the video on the user screen.

We have two different live channels - Live-1 and Live-2 to fetch the video from publisher app1(Web-cam)  and publisher app2(USB camera). 
 Also, we have re-stream features which allows user to play the video from begining.
 
 Please refer to the below screenshots. 
 
This project combines stream processing layer with presentation layer as you can see in the overview of project. 


![Overview](https://user-images.githubusercontent.com/40739676/72839227-ce723a00-3c89-11ea-82ad-c91bb188b283.PNG)


Stream processing contains Kafka consumer instance that fetches a continuous strem of Byte Arrays. 
Then, tranform them to images. Presentation layer is build in JavaFX where video is rendered. Please refer to the below screenshot.


![KafkaConsumer](https://user-images.githubusercontent.com/40739676/72839441-39bc0c00-3c8a-11ea-85ae-3d9b5a2090b8.PNG)
