package com.example.jbdl.major_project;

public class CommonConstants {

    //Kafka topic constants
    public static final String USER_CREATE_KAFKA_TOPIC="user_create";
    public static final String TRANSACTION_CREATE_KAFKA_TOPIC="transaction_create";
    public static final String WALLET_UPDATE_KAFKA_TOPIC="wallet_update";
    public static final String TRANSACTION_COMPLETE_KAFKA_TOPIC="transaction_complete";

    //Kafka attributes constants
    public static final String EMAIL_ATTRIBUTE="email";
    public static final String PHONE_ATTRIBUTE="phone";
    public static final String TRANSACTION_ID_ATTRIBUTE="transactionId";
    public static final String SENDER_ATTRIBUTE="sender";
    public static final String RECEIVER_ATTRIBUTE="receiver";
    public static final String AMOUNT_ATTRIBUTE="amount";
    public static final String WALLET_UPDATE_STATUS_ATTRIBUTE="wallet_update_status";
    public static final String WALLET_UPDATE_SUCCESS_STATUS="SUCCESS";
    public static final String WALLET_UPDATE_FAILED_STATUS="FAILED";

    public static final String EMAIL_MESSAGE_ATTRIBUTE="email_msg";
    public static final String TRANSACTION_STATUS_ATTRIBUTE="transaction_status";

}

