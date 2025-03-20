package org.apache.cordova.twiliovideo;

//import com.twilio.chat.Channel;
//
//public interface MessageCountListener {
//    void onMessageCount(int count, Channel channel);
//}


import com.twilio.conversations.Conversation;  // Import the Conversations SDK
import com.twilio.conversations.Message;

public interface MessageCountListener {
    void onMessageCount(int count, Conversation conversation);  // Replace Channel with Conversation
}