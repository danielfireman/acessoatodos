package com.acessoatodos;

import com.acessoatodos.AWSResource;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

public class AWSTeste {
	private AmazonSQS sqs;
	private List<Message> messages;
	
	public AWSTeste() {	
		AWSCredentials credentials = null;
		credentials = new ProfileCredentialsProvider("Diego").getCredentials();
		sqs = new AmazonSQSClient(credentials);
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        sqs.setRegion(usWest2);
	}
	
	
	@Test
    public void createQueue() throws Exception {
		CreateQueueRequest createQueueRequest = new CreateQueueRequest("MyQueueTest");
		AWSResource.myQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();
        assertTrue(AWSResource.myQueueUrl.matches("https://sqs.us-west-2.amazonaws.com/\\d+/MyQueueTest"));
	}
	
	@Test
	public void listQueues() throws Exception {
		assertTrue(sqs.listQueues().getQueueUrls().size()  > 0 );   
	}
	
	@Test
	public void sendMessage() throws Exception {
		SendMessageResult awsResponse = sqs.sendMessage(new SendMessageRequest(AWSResource.myQueueUrl, "This is my message text."));
		assertTrue(awsResponse.getMD5OfMessageBody().matches("\\w+"));
	}
	
	@Test
	public void receiveMessages() throws Exception {
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(AWSResource.myQueueUrl);
		messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
        assertTrue(messages.get(0).getMD5OfBody().matches("\\w+"));
	}
}
