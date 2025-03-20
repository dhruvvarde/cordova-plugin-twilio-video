package org.apache.cordova.twiliovideo;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.twilio.conversations.CallbackListener;
import com.twilio.conversations.Conversation;
import com.twilio.conversations.ConversationsClient;
import com.twilio.conversations.StatusListener;
import com.twilio.util.ErrorInfo;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ajay Makwana on 10/01/22.
 */
public class TwilioChatUnreadMessages implements CallbackListener<ConversationsClient> {
    private static Context mContext;
    private static int count = 0;
    private ConversationsClient mConversationsClient;
    private String mConversationSid;
    private Conversation mConversation;
    public MessageCountListener mListener;

    public TwilioChatUnreadMessages(Context context, String conversationSid, MessageCountListener listener) {
        this.mContext = context;
        this.mConversationSid = conversationSid;
        this.mListener = listener;
    }

    public void getUnreadMessagesCount(final Conversation currentConversation) {
        mListener.onMessageCount(count, currentConversation);
    }

    public void build(String accessToken) {
        // Create the ConversationsClient

        ConversationsClient.Properties props = ConversationsClient.Properties.newBuilder().setRegion("us1").createProperties();

        ConversationsClient.create(mContext.getApplicationContext(), accessToken, props, this);
    }

    @Override
    public void onSuccess(ConversationsClient conversationsClient) {
        mConversationsClient = conversationsClient;
        getOrCreateConversationFromSid(mConversationSid);
    }

    private void getOrCreateConversationFromSid(final String conversationSid) {
        if (mConversationsClient != null) {
            mConversationsClient.getConversation(conversationSid, new CallbackListener<Conversation>() {
                @Override
                public void onSuccess(Conversation conversation) {
                    mConversation = conversation;
                    mConversation.join(new StatusListener() {
                        @Override
                        public void onSuccess() {
                            Log.d("TAG", "onSuccess: Conversation Joined");
                            // In Conversations SDK, we use markAllMessagesAsRead instead of setAllMessagesConsumed

                            mConversation.setAllMessagesRead(new CallbackListener<Long>() {
                                @Override
                                public void onSuccess(Long result) {
                                    Log.e("TAG", "onSuccess: all messages marked as read");
                                    getUnreadMessagesCount(mConversation);
                                }

                                @Override
                                public void onError(ErrorInfo errorInfo) {
                                    Log.e("TAG", "onError: " + errorInfo.getMessage());
                                    getUnreadMessagesCount(mConversation);
                                }
                            });
                        }

                        @Override
                        public void onError(ErrorInfo errorInfo) {
                            if (errorInfo.getCode() == 50404) {
                                Log.e("TAG", "onError: " + errorInfo.getMessage());
                                getUnreadMessagesCount(mConversation);
                            }
                        }
                    });
                }

                @Override
                public void onError(ErrorInfo errorInfo) {
                    mConversationsClient.createConversation(conversationSid, new CallbackListener<Conversation>() {
                        @Override
                        public void onSuccess(Conversation result) {
                            mConversation = result;
                            Log.d("ConversationCreate", TwilioVideoActivity.userId);
                            fetch(TwilioVideoActivity.userId);
                        }

                        @Override
                        public void onError(ErrorInfo errorInfo) {
                            Log.e("TAG", "Error creating conversation: " + errorInfo.getMessage());
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onError(ErrorInfo errorInfo) {
        Log.e("TAG", "onError: " + errorInfo.getMessage());
    }

    public void fetch(String patientId) {
        JSONObject obj = new JSONObject(getTokenRequestParams(patientId));
        String requestUrl = "https://medicoparseserver.stg.iron.fit/api/doctor/private_chat/get_access_token?identity=" + patientId;
        Log.d("TAG", "Requesting access token from: " + requestUrl);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, requestUrl, obj, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String token = response.optJSONObject("data").optString("token");
                build(token);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("TAG", "onErrorResponse: " + error.getLocalizedMessage());
            }
        });
        jsonObjReq.setShouldCache(false);
        Volley.newRequestQueue(mContext).add(jsonObjReq);
    }

    private Map<String, String> getTokenRequestParams(String patientId) {
        Map<String, String> params = new HashMap<>();
        params.put("identity", patientId);
        return params;
    }
}


//public class TwilioChatUnreadMessages extends CallbackListener<ChatClient> {
//    private static Context mContext;
//    private static int count = 0;
//    private ChatClient mChatClient;
//    private String mChannelId;
//    private Channel mChannel;
//    public MessageCountListener mListener;
//
//    public TwilioChatUnreadMessages(Context context, String channelId, MessageCountListener listener) {
//        this.mContext = context;
//        this.mChannelId = channelId;
//        this.mListener = listener;
//    }
//
//    public void getUnreadMessagesCount(final Channel currentChannel) {
//                mListener.onMessageCount(count,currentChannel);
//    }
//
//    public void build(String accessToken) {
//        ChatClient.Properties props =
//                new ChatClient.Properties.Builder()
//                        .setRegion("us1")
//                        .createProperties();
//        ChatClient.create(mContext.getApplicationContext(),
//                accessToken,
//                props,
//                this);
//    }
//
//    @Override
//    public void onSuccess(ChatClient chatClient) {
//        mChatClient = chatClient;
//        getOrCreateChannelFromChannelId(mChannelId);
//    }
//
//    private void getOrCreateChannelFromChannelId(final String mChannelId) {
//        if (mChatClient != null) {
//            mChatClient.getChannels().getChannel(mChannelId
//                    , new CallbackListener<Channel>() {
//                        @Override
//                        public void onSuccess(Channel channel) {
//                            mChannel = channel;
//                            mChannel.join(new StatusListener() {
//                                @Override
//                                public void onSuccess() {
//                                    Log.d("TAG", "onSuccess: Channel Joined");
//                                    mChannel.getMessages().setAllMessagesConsumedWithResult(new CallbackListener<Long>() {
//                                        @Override
//                                        public void onSuccess(Long aLong) {
//                                            Log.e("TAG", "onSuccess: all messages consumed");
//                                            getUnreadMessagesCount(mChannel);
//                                        }
//
//                                        @Override
//                                        public void onError(ErrorInfo errorInfo) {
//                                            Log.e("TAG", "onError: "+errorInfo.getMessage() );
//                                            getUnreadMessagesCount(mChannel);
//                                        }
//                                    });
//                                }
//
//                                @Override
//                                public void onError(ErrorInfo errorInfo) {
//                                    if (errorInfo.getCode() == 50404) {
//                                        Log.e("TAG", "onError: " + errorInfo.getMessage());
//                                        getUnreadMessagesCount(mChannel);
//                                    }
//                                }
//                            });
//                        }
//
//                        @Override
//                        public void onError(ErrorInfo errorInfo) {
//                            mChatClient.getChannels().createChannel(mChannelId, Channel.ChannelType.PRIVATE, new CallbackListener<Channel>() {
//                                @Override
//                                public void onSuccess(Channel channel) {
//                                    mChannel = channel;
//                                    Log.d("Chanelcreate",TwilioVideoActivity.userId);
//                                    fetch(TwilioVideoActivity.userId);
//
//                                }
//                            });
//                        }
//                    });
//        }
//    }
//
//    @Override
//    public void onError(ErrorInfo errorInfo) {
//        Log.e("TAG", "onError: " + errorInfo.getMessage());
//    }
//
//
//    public void fetch(String patientId) {
//        JSONObject obj = new JSONObject(getTokenRequestParams(patientId));
//        String requestUrl = "https://medicoparseserver.stg.iron.fit/api/doctor/private_chat/get_access_token?identity="+patientId;
//        Log.d("TAG", "Requesting access token from: " + requestUrl);
//
//        JsonObjectRequest jsonObjReq =
//                new JsonObjectRequest(Request.Method.POST, requestUrl, obj, new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        String token = response.optJSONObject("data").optString("token");
//                        build(token);
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.d("TAG", "onErrorResponse: "+error.getLocalizedMessage());
//                    }
//                });
//        jsonObjReq.setShouldCache(false);
//        Volley.newRequestQueue(mContext).add(jsonObjReq);
//    }
//
//    private Map<String, String> getTokenRequestParams(String patientId) {
//        Map<String, String> params = new HashMap<>();
//        params.put("identity", patientId);
//        return params;
//    }
//}



